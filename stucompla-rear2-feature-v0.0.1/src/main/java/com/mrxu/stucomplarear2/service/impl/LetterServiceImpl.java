package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.LetterAddDto;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.PunishmentService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LetterServiceImpl extends ServiceImpl<LetterMapper, Letter> implements LetterService {

    @Autowired
    private PunishmentService punishmentService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result add(LetterAddDto letterAddDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String senderId = JWTUtil.getUserId(token);
        // 禁言检查
        if (punishmentService.isUserMuted(senderId)) {
            return Result.fail("您已被禁言，无法发送私信。" + punishmentService.getMuteReason(senderId));
        }
        Letter letter = new Letter();
        letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
        letter.setReceiverId(letterAddDto.getReceiverId());
        letter.setSenderId(senderId);
        letter.setLetterDetail(letterAddDto.getLetterDetail());
        letter.setLetterStatus(0);
        letter.setMessageType("letter");
        String minId = senderId.compareTo(letterAddDto.getReceiverId()) < 0 ? senderId : letterAddDto.getReceiverId();
        String maxId = senderId.compareTo(letterAddDto.getReceiverId()) >= 0 ? senderId : letterAddDto.getReceiverId();
        letter.setSessionId(minId + "_" + maxId);
        letter.setCreateTime(new Date());
        letter.setUpdateTime(new Date());
        this.save(letter);
        return Result.succ("success");
    }

    @Override
    public Result getMySessionList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);

        // 单次查询获取用户所有私信类型的消息
        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq("sender_id", userId)
                .or()
                .eq("receiver_id", userId));
        queryWrapper.eq("message_type", "letter");
        queryWrapper.ne("sender_id", "0");
        queryWrapper.orderByDesc("create_time");
        List<Letter> letters = this.list(queryWrapper);

        // 按 sessionId 分组，计算每个会话的元数据
        Map<String, List<Letter>> sessionMap = new LinkedHashMap<>();
        for (Letter letter : letters) {
            sessionMap.computeIfAbsent(letter.getSessionId(), k -> new ArrayList<>()).add(letter);
        }

        // 构建会话信息列表
        List<Map<String, Object>> sessionList = new ArrayList<>();
        Set<String> otherIds = new HashSet<>();
        for (Map.Entry<String, List<Letter>> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            List<Letter> sessionLetters = entry.getValue();

            String[] parts = sessionId.split("_");
            if (parts.length != 2) continue;
            String otherId = parts[0].equals(userId) ? parts[1] : parts[0];
            otherIds.add(otherId);

            Letter lastLetter = sessionLetters.get(0); // 已按 create_time desc 排序
            long unread = sessionLetters.stream()
                    .filter(l -> l.getReceiverId().equals(userId) && l.getLetterStatus() != null && l.getLetterStatus() == 0)
                    .count();

            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("sessionId", sessionId);
            sessionInfo.put("otherId", otherId);
            sessionInfo.put("lastContent", lastLetter.getLetterDetail());
            sessionInfo.put("lastTime", lastLetter.getCreateTime());
            sessionInfo.put("unread", unread);
            sessionList.add(sessionInfo);
        }

        // 批量查询对方用户信息
        if (!otherIds.isEmpty()) {
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            userQuery.in("user_id", otherIds);
            userQuery.select("user_id", "nickname", "username", "avatar");
            List<User> users = userMapper.selectList(userQuery);
            Map<String, User> userMap = new HashMap<>();
            for (User user : users) {
                userMap.put(user.getUserId(), user);
            }
            // 填充昵称和头像
            for (Map<String, Object> si : sessionList) {
                String oid = (String) si.get("otherId");
                User otherUser = userMap.get(oid);
                if (otherUser != null) {
                    si.put("otherNickname", otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername());
                    si.put("otherAvatar", otherUser.getAvatar());
                } else {
                    si.put("otherNickname", "用户" + oid);
                    si.put("otherAvatar", null);
                }
            }
        }

        return Result.succ(sessionList);
    }

    @Override
    public Result getLetterListBySessionId(String sessionId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);

        // 验证用户是否属于该会话
        String[] parts = sessionId.split("_");
        if (parts.length != 2 || (!parts[0].equals(userId) && !parts[1].equals(userId))) {
            return Result.fail("无权访问该会话");
        }

        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        queryWrapper.orderByAsc("create_time");
        List<Letter> letters = this.list(queryWrapper);

        // 批量标记已读 - 单条 UPDATE 语句替代 N 次循环更新
        UpdateWrapper<Letter> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("session_id", sessionId);
        updateWrapper.eq("receiver_id", userId);
        updateWrapper.eq("letter_status", 0);
        updateWrapper.set("letter_status", 1);
        updateWrapper.set("update_time", new Date());
        this.update(updateWrapper);

        return Result.succ(letters);
    }

    @Override
    public Result getMyNoticeList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", "0");
        queryWrapper.eq("receiver_id", userId);
        queryWrapper.eq("message_type", "system");
        queryWrapper.orderByDesc("create_time");
        List<Letter> letters = this.list(queryWrapper);
        return Result.succ(letters);
    }

    @Override
    public Result getMyUnReadTotal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId);
        queryWrapper.eq("letter_status", 0);
        long total = this.count(queryWrapper);
        return Result.succ(total);
    }

    @Override
    public Result getMyUnReadLetterTotal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId);
        queryWrapper.ne("sender_id", "0");
        queryWrapper.eq("letter_status", 0);
        long total = this.count(queryWrapper);
        return Result.succ(total);
    }

    @Override
    public Result getMyUnReadNoticeTotal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", "0");
        queryWrapper.eq("receiver_id", userId);
        queryWrapper.eq("letter_status", 0);
        queryWrapper.eq("message_type", "system");
        long total = this.count(queryWrapper);
        return Result.succ(total);
    }

    @Override
    public Result myMessageList(String type, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Letter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId);
        if (type != null && !type.isEmpty()) {
            if ("interaction".equals(type)) {
                queryWrapper.in("message_type", "comment", "like", "follow", "mention", "collect");
            } else if ("like".equals(type)) {
                queryWrapper.in("message_type", "like", "collect");
            } else if ("comment_received".equals(type)) {
                queryWrapper.eq("message_type", "comment");
            } else if ("follow".equals(type)) {
                queryWrapper.eq("message_type", "follow");
            } else {
                queryWrapper.eq("message_type", type);
            }
        }
        queryWrapper.orderByDesc("create_time");
        int pn = pageNum != null ? pageNum : 1;
        int ps = pageSize != null ? pageSize : 10;
        Page<Letter> page = new Page<>(pn, ps);
        Page<Letter> resultPage = this.page(page, queryWrapper);
        return Result.succ(resultPage);
    }

    @Override
    public Result markRead(String letterId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Letter letter = this.getById(letterId);
        if (letter == null) {
            return Result.fail("消息不存在");
        }
        if (!letter.getReceiverId().equals(userId)) {
            return Result.fail("无权操作");
        }
        letter.setLetterStatus(1);
        letter.setUpdateTime(new Date());
        this.updateById(letter);
        return Result.succ("success");
    }

    @Override
    public Result markAllRead(String type, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        UpdateWrapper<Letter> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("receiver_id", userId);
        updateWrapper.eq("letter_status", 0);
        if (type != null && !type.isEmpty()) {
            if ("interaction".equals(type)) {
                // 互动类型：包括 comment、like、follow、mention、collect
                updateWrapper.in("message_type", "comment", "like", "follow", "mention", "collect");
            } else if ("letter".equals(type)) {
                // 私信类型：标记所有私信为已读
                updateWrapper.eq("message_type", "letter");
            } else if ("like".equals(type)) {
                // 点赞与收藏：包括 like 和 collect
                updateWrapper.in("message_type", "like", "collect");
            } else {
                updateWrapper.eq("message_type", type);
            }
        }
        updateWrapper.set("letter_status", 1);
        updateWrapper.set("update_time", new Date());
        this.update(updateWrapper);
        return Result.succ("success");
    }

    @Override
    public Result unreadCountByType(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);

        // 单次 GROUP BY 查询替代 7 次独立查询
        List<Map<String, Object>> counts = baseMapper.countUnreadByType(userId);

        long commentCount = 0, letterCount = 0, systemCount = 0, likeCount = 0, followCount = 0, orderCount = 0, logisticsCount = 0, mentionCount = 0, collectCount = 0;

        for (Map<String, Object> row : counts) {
            String messageType = (String) row.get("message_type");
            if (messageType == null) continue;
            long cnt = ((Number) row.get("cnt")).longValue();
            switch (messageType) {
                case "comment": commentCount = cnt; break;
                case "letter": letterCount = cnt; break;
                case "system": systemCount = cnt; break;
                case "like": likeCount = cnt; break;
                case "follow": followCount = cnt; break;
                case "order": orderCount = cnt; break;
                case "logistics": logisticsCount = cnt; break;
                case "mention": mentionCount = cnt; break;
                case "collect": collectCount = cnt; break;
            }
        }

        Map<String, Object> countMap = new HashMap<>();
        countMap.put("comment", commentCount);
        countMap.put("letter", letterCount);
        countMap.put("system", systemCount);
        countMap.put("like", likeCount);
        countMap.put("follow", followCount);
        countMap.put("mention", mentionCount);
        countMap.put("collect", collectCount);
        countMap.put("interaction", commentCount + likeCount + followCount + mentionCount + collectCount);
        countMap.put("order", orderCount);
        countMap.put("logistics", logisticsCount);
        countMap.put("total", commentCount + letterCount + systemCount + likeCount + followCount + orderCount + logisticsCount + mentionCount + collectCount);
        return Result.succ(countMap);
    }

    @Override
    public void sendSystemNotification(String receiverId, String content, String messageType) {
        sendSystemNotification(receiverId, content, messageType, null, null);
    }

    @Override
    public void sendSystemNotification(String receiverId, String content, String messageType, String targetType, String targetId) {
        Letter letter = new Letter();
        letter.setLetterId(IdGenerator.generateId(IdGenerator.LETTER));
        letter.setSenderId("0");
        letter.setReceiverId(receiverId);
        letter.setLetterDetail(content);
        letter.setLetterStatus(0);
        letter.setMessageType(messageType);
        String sessionId = "system_" + receiverId;
        if (targetType != null && !targetType.isEmpty() && targetId != null && !targetId.isEmpty()) {
            sessionId = "system_" + receiverId + "_" + targetType + "_" + targetId;
        }
        letter.setSessionId(sessionId);
        letter.setCreateTime(new Date());
        letter.setUpdateTime(new Date());
        this.save(letter);
    }
}
