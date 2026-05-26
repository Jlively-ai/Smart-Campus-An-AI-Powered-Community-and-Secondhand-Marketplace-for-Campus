package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.PunishmentDto;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.Punishment;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.mapper.PunishmentMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PunishmentServiceImpl extends ServiceImpl<PunishmentMapper, Punishment> implements PunishmentService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LetterMapper letterMapper;

    @Override
    public Result createPunishment(PunishmentDto punishmentDto, HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(jwt);

        User user = userMapper.selectById(punishmentDto.getUserId());
        if (user == null) return Result.fail("用户不存在");

        Punishment punishment = new Punishment();
        punishment.setPunishmentId(IdGenerator.generateId(IdGenerator.PUNISHMENT));
        punishment.setUserId(punishmentDto.getUserId());
        punishment.setType(punishmentDto.getType());
        punishment.setReason(punishmentDto.getReason());
        punishment.setHandlerId(handlerId);
        punishment.setReportId(punishmentDto.getReportId());
        punishment.setStatus(0); // 生效中
        punishment.setCreateTime(new Date());
        punishment.setUpdateTime(new Date());

        // 设置生效时间
        punishment.setStartTime(new Date());
        if (punishmentDto.getEndTime() != null && !punishmentDto.getEndTime().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                punishment.setEndTime(sdf.parse(punishmentDto.getEndTime()));
            } catch (ParseException e) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    punishment.setEndTime(sdf.parse(punishmentDto.getEndTime()));
                } catch (ParseException e2) {
                    return Result.fail("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
                }
            }
        }

        this.save(punishment);

        // 如果是封禁(ban)，锁定用户；如果是禁言(mute)，设置禁言状态
        if ("ban".equals(punishmentDto.getType())) {
            user.setLocked(true);
            user.setStatus(1);
            userMapper.updateById(user);
        } else if ("mute".equals(punishmentDto.getType())) {
            user.setStatus(3); // 3=禁言
            userMapper.updateById(user);
        }

        // 发送系统通知给被处罚用户
        String typeText = "mute".equals(punishmentDto.getType()) ? "禁言" : "ban".equals(punishmentDto.getType()) ? "封号" : "警告";
        String endTimeStr = punishment.getEndTime() != null ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(punishment.getEndTime()) : "永久";
        String content = "您已被管理员" + typeText + "，原因：" + punishmentDto.getReason() +
                "，解除时间：" + endTimeStr;

        Letter letter = new Letter();
        letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
        letter.setSenderId("0");
        letter.setReceiverId(punishmentDto.getUserId());
        letter.setLetterDetail(content);
        letter.setMessageType("system");
        letter.setSessionId("system_" + punishmentDto.getUserId() + "_punishment_" + punishment.getPunishmentId());
        letter.setLetterStatus(0);
        letter.setCreateTime(new Date());
        letter.setUpdateTime(new Date());
        letterMapper.insert(letter);

        return Result.succ("处罚创建成功");
    }

    @Override
    public Map<String, Object> findPunishmentList(Integer pageNum, Integer pageSize, String userId, String type, Integer status) {
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<Punishment> page = new Page<>(pn, ps);
        QueryWrapper<Punishment> qw = new QueryWrapper<>();
        if (userId != null && !userId.isEmpty()) {
            qw.eq("user_id", userId);
        }
        if (type != null && !type.isEmpty()) {
            qw.eq("type", type);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        this.page(page, qw);

        // 自动将已过期的处罚标记为过期
        Date now = new Date();
        for (Punishment p : page.getRecords()) {
            if (p.getStatus() == 0 && p.getEndTime() != null && p.getEndTime().before(now)) {
                p.setStatus(2); // 已过期
                this.updateById(p);
                // 如果是封禁且已过期，解锁用户
                if ("ban".equals(p.getType())) {
                    User user = userMapper.selectById(p.getUserId());
                    if (user != null && user.getLocked()) {
                        // 检查是否还有其他生效中的封禁
                        QueryWrapper<Punishment> banQw = new QueryWrapper<>();
                        banQw.eq("user_id", p.getUserId()).eq("type", "ban").eq("status", 0);
                        long activeBanCount = this.count(banQw);
                        if (activeBanCount <= 0) {
                            user.setLocked(false);
                            userMapper.updateById(user);
                        }
                    }
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("records", page.getRecords());
        map.put("total", page.getTotal());
        map.put("pages", page.getPages());
        map.put("current", page.getCurrent());
        return map;
    }

    @Override
    public List<Punishment> getActivePunishments(String userId) {
        // 先更新过期的处罚
        Date now = new Date();
        QueryWrapper<Punishment> expireQw = new QueryWrapper<>();
        expireQw.eq("user_id", userId).eq("status", 0).isNotNull("end_time").lt("end_time", now);
        List<Punishment> expired = this.list(expireQw);
        for (Punishment p : expired) {
            p.setStatus(2);
            this.updateById(p);
        }

        QueryWrapper<Punishment> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("status", 0);
        return this.list(qw);
    }

    @Override
    public boolean isUserMuted(String userId) {
        List<Punishment> active = getActivePunishments(userId);
        return active.stream().anyMatch(p -> "mute".equals(p.getType()));
    }

    @Override
    public String getMuteReason(String userId) {
        List<Punishment> active = getActivePunishments(userId);
        return active.stream()
                .filter(p -> "mute".equals(p.getType()))
                .map(p -> {
                    String endTimeStr = p.getEndTime() != null ?
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(p.getEndTime()) : "永久";
                    return p.getReason() + "（解除时间：" + endTimeStr + "）";
                })
                .findFirst()
                .orElse("");
    }

    @Override
    public Result liftPunishment(String punishmentId) {
        Punishment punishment = this.getById(punishmentId);
        if (punishment == null) return Result.fail("处罚不存在");
        punishment.setStatus(1); // 已解除
        punishment.setUpdateTime(new Date());
        this.updateById(punishment);

        // 如果是封禁，检查是否还有其他生效中的封禁
        if ("ban".equals(punishment.getType())) {
            User user = userMapper.selectById(punishment.getUserId());
            if (user != null && user.getLocked()) {
                QueryWrapper<Punishment> banQw = new QueryWrapper<>();
                banQw.eq("user_id", punishment.getUserId()).eq("type", "ban").eq("status", 0);
                long activeBanCount = this.count(banQw);
                if (activeBanCount <= 0) {
                    user.setLocked(false);
                    user.setStatus(0);
                    userMapper.updateById(user);
                }
            }
        }

        // 如果是禁言，检查是否还有其他生效中的禁言
        if ("mute".equals(punishment.getType())) {
            User user = userMapper.selectById(punishment.getUserId());
            if (user != null && user.getStatus() != null && user.getStatus() == 3) {
                QueryWrapper<Punishment> muteQw = new QueryWrapper<>();
                muteQw.eq("user_id", punishment.getUserId()).eq("type", "mute").eq("status", 0);
                long activeMuteCount = this.count(muteQw);
                if (activeMuteCount <= 0) {
                    user.setStatus(0);
                    userMapper.updateById(user);
                }
            }
        }

        // 通知用户
        String typeText = "mute".equals(punishment.getType()) ? "禁言" : "ban".equals(punishment.getType()) ? "封号" : "警告";
        Letter letter = new Letter();
        letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
        letter.setSenderId("0");
        letter.setReceiverId(punishment.getUserId());
        letter.setLetterDetail("您的" + typeText + "处罚已被管理员解除");
        letter.setMessageType("system");
        letter.setSessionId("system_" + punishment.getUserId());
        letter.setLetterStatus(0);
        letter.setCreateTime(new Date());
        letter.setUpdateTime(new Date());
        letterMapper.insert(letter);

        return Result.succ("处罚已解除");
    }

    @Override
    public List<Punishment> getPublicPunishments(String userId) {
        // 返回用户最近的处罚记录（包括已解除和已过期的）
        QueryWrapper<Punishment> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        Page<Punishment> page = new Page<>(1, 10);
        this.page(page, qw);
        return page.getRecords();
    }

    @Override
    public Result appeal(String punishmentId, String appealReason, String userId) {
        Punishment punishment = this.getById(punishmentId);
        if (punishment == null) return Result.fail("处罚不存在");
        if (!punishment.getUserId().equals(userId)) {
            return Result.fail("无权申诉该处罚");
        }
        if (punishment.getStatus() != null && punishment.getStatus() != 0) {
            return Result.fail("该处罚已失效，无法申诉");
        }
        if (punishment.getAppealState() != null && punishment.getAppealState() == 0) {
            return Result.fail("该处罚已有待审核的申诉");
        }
        punishment.setAppealReason(appealReason);
        punishment.setAppealState(0); // 待审核
        punishment.setAppealTime(new Date());
        punishment.setUpdateTime(new Date());
        this.updateById(punishment);
        return Result.succ("申诉提交成功，等待管理员审核");
    }

    @Override
    public Result handleAppeal(String punishmentId, Integer appealState, String appealResult) {
        Punishment punishment = this.getById(punishmentId);
        if (punishment == null) return Result.fail("处罚不存在");
        if (punishment.getAppealState() == null || punishment.getAppealState() != 0) {
            return Result.fail("该处罚没有待审核的申诉");
        }
        punishment.setAppealState(appealState); // 1=通过, 2=驳回
        punishment.setAppealResult(appealResult);
        punishment.setUpdateTime(new Date());

        // 如果申诉通过，解除处罚
        if (appealState == 1) {
            punishment.setStatus(1); // 已解除
            // 如果是封禁，解锁用户
            if ("ban".equals(punishment.getType())) {
                User user = userMapper.selectById(punishment.getUserId());
                if (user != null) {
                    QueryWrapper<Punishment> banQw = new QueryWrapper<>();
                    banQw.eq("user_id", punishment.getUserId()).eq("type", "ban").eq("status", 0);
                    long activeBanCount = this.count(banQw);
                    if (activeBanCount <= 1) { // 当前这条还没更新，所以用<=1
                        user.setLocked(false);
                        user.setStatus(0);
                        userMapper.updateById(user);
                    }
                }
            }
            // 如果是禁言，恢复用户状态
            if ("mute".equals(punishment.getType())) {
                User user = userMapper.selectById(punishment.getUserId());
                if (user != null && user.getStatus() != null && user.getStatus() == 3) {
                    QueryWrapper<Punishment> muteQw = new QueryWrapper<>();
                    muteQw.eq("user_id", punishment.getUserId()).eq("type", "mute").eq("status", 0);
                    long activeMuteCount = this.count(muteQw);
                    if (activeMuteCount <= 1) {
                        user.setStatus(0);
                        userMapper.updateById(user);
                    }
                }
            }
            // 通知用户申诉通过
            String typeText = "mute".equals(punishment.getType()) ? "禁言" : "ban".equals(punishment.getType()) ? "封号" : "警告";
            Letter letter = new Letter();
            letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
            letter.setSenderId("0");
            letter.setReceiverId(punishment.getUserId());
            letter.setLetterDetail("您的" + typeText + "处罚申诉已通过，处罚已解除");
            letter.setMessageType("system");
            letter.setSessionId("system_" + punishment.getUserId() + "_punishment_" + punishment.getPunishmentId());
            letter.setLetterStatus(0);
            letter.setCreateTime(new Date());
            letter.setUpdateTime(new Date());
            letterMapper.insert(letter);
        } else {
            // 申诉驳回，通知用户
            String typeText = "mute".equals(punishment.getType()) ? "禁言" : "ban".equals(punishment.getType()) ? "封号" : "警告";
            Letter letter = new Letter();
            letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
            letter.setSenderId("0");
            letter.setReceiverId(punishment.getUserId());
            letter.setLetterDetail("您的" + typeText + "处罚申诉已被驳回，原因：" + (appealResult != null ? appealResult : "无"));
            letter.setMessageType("system");
            letter.setSessionId("system_" + punishment.getUserId() + "_punishment_" + punishment.getPunishmentId());
            letter.setLetterStatus(0);
            letter.setCreateTime(new Date());
            letter.setUpdateTime(new Date());
            letterMapper.insert(letter);
        }

        this.updateById(punishment);
        return Result.succ(appealState == 1 ? "申诉已通过，处罚已解除" : "申诉已驳回");
    }

    @Override
    public List<Punishment> getMyPunishments(String userId) {
        QueryWrapper<Punishment> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        return this.list(qw);
    }
}
