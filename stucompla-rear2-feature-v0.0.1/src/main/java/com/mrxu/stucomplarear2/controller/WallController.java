package com.mrxu.stucomplarear2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.dto.WallApplyDto;
import com.mrxu.stucomplarear2.dto.WallAuditDto;
import com.mrxu.stucomplarear2.dto.WallEditDto;
import com.mrxu.stucomplarear2.dto.WallFindDto;
import com.mrxu.stucomplarear2.entity.Wall;
import com.mrxu.stucomplarear2.entity.WallLike;
import com.mrxu.stucomplarear2.entity.ContentShare;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.ViolationDelete;
import com.mrxu.stucomplarear2.mapper.WallLikeMapper;
import com.mrxu.stucomplarear2.mapper.ContentShareMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.service.WallService;
import com.mrxu.stucomplarear2.service.RecycleBinService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.impl.DailyStatsService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mr.Xu
 * @since 2021-12-15
 */
@RestController
@RequestMapping("/wall")
public class WallController {

    @Resource
    private WallService wallService;
    @Autowired
    private RecycleBinService recycleBinService;
    @Autowired
    private ViolationDeleteService violationDeleteService;
    @Autowired
    private DailyStatsService dailyStatsService;
    @Autowired
    private WallLikeMapper wallLikeMapper;
    @Autowired
    private ContentShareMapper contentShareMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LetterService letterService;

    @ApiOperation("申请上墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/apply")
    public Result apply(@RequestBody WallApplyDto wallApplyDto, HttpServletRequest request) {
        Result applyResult = wallService.apply(wallApplyDto, request);
        return applyResult;
    }

    @ApiOperation("上墙审核")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/audit")
    public Result audit(@RequestBody WallAuditDto wallAuditDto, HttpServletRequest request) {
        String auditResult = wallService.audit(wallAuditDto, request);
        if (auditResult.equals("审核成功")) {
            return Result.succ(200, auditResult, null);
        }
        return Result.fail(auditResult);
    }

    @ApiOperation("修改表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/edit")
    public Result edit(@RequestBody WallEditDto wallEditDto, HttpServletRequest request) {
        Result result = wallService.editWall(wallEditDto, request);
        return result;
    }

    @ApiOperation("墙列表（用户页面）")
//    @RequiresRoles("user")
    @GetMapping("/wallList")
    public Result wallList(Integer pageNum, Integer pageSize, String wallContent, String userId, String nickname, HttpServletRequest request) {
        WallFindDto wallFindDto = new WallFindDto();
        wallFindDto.setAuditState(1);//已审核的内容
        wallFindDto.setPageNum(pageNum);
        wallFindDto.setPageSize(pageSize);
        wallFindDto.setWallContent(wallContent);
        wallFindDto.setUserId(userId);
        wallFindDto.setNickname(nickname);
        Map<String, Object> map = wallService.findWall(wallFindDto, true, request);
        return Result.succ(map);
    }


    @ApiOperation("我的墙列表")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/myWallList")
    public Result getmyWallList(Integer pageNum, Integer pageSize, Integer auditState, String wallContent, HttpServletRequest request) {
        WallFindDto wallFindDto = new WallFindDto();
        wallFindDto.setPageNum(pageNum);
        wallFindDto.setPageSize(pageSize);
        wallFindDto.setAuditState(auditState);
        wallFindDto.setWallContent(wallContent);
        Result result = wallService.findMyWall(wallFindDto, request);
        return result;
    }

    @ApiOperation("删除表白墙")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/delete/{wallId}")
    public Result deleteWall(@PathVariable("wallId") String wallId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        return violationDeleteService.moveViolation(wall.getUserId(), "wall", wallId, cause, handlerId, "delete");
    }

    @ApiOperation("管理员锁定表白墙")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lockWall/{wallId}")
    public Result lockWall(@PathVariable("wallId") String wallId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("表白墙不存在");
        if (wall.getLocked() != null && wall.getLocked() == 1) return Result.fail("表白墙已锁定");
        wall.setLocked(1);
        wall.setLockReason(cause);
        wall.setUpdateTime(new Date());
        wallService.updateById(wall);
        violationDeleteService.moveViolation(wall.getUserId(), "wall_lock", wallId, cause, handlerId, "lock");
        return Result.succ("锁定成功");
    }

    @ApiOperation("管理员解锁表白墙")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlockWall/{wallId}")
    public Result unlockWall(@PathVariable("wallId") String wallId) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("表白墙不存在");
        wall.setLocked(0);
        wall.setLockReason(null);
        wall.setUpdateTime(new Date());
        wallService.updateById(wall);
        QueryWrapper<ViolationDelete> qw = new QueryWrapper<>();
        qw.eq("item_id", wallId).eq("operation_type", "lock");
        violationDeleteService.remove(qw);
        return Result.succ("解锁成功");
    }


    @ApiOperation("获取表白墙总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getWallTotal")
    public Result getWallTotal() {
        Result result = wallService.getWallTotal();
        return result;
    }


    @ApiOperation("表白墙分类统计")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getWallData")
    public Result getWallData() {
        Result result = wallService.getWallData();
        return result;
    }

//
//    @ApiOperation("个人墙列表")
//    @RequiresRoles("user")
//    @GetMapping("/myWallList")
//    public Result myWallList(ServletRequest request, Integer auditState, Integer pageNum, Integer pageSize) {
//        HttpServletRequest req = (HttpServletRequest) request;
//        //获取传递过来的accessToken
//        String accessToken = req.getHeader("Authorization");
//        //获取token里面的用户ID
//        String userId = JWTUtil.getUserId(accessToken);
//
//        WallFindDto wallFindDto = new WallFindDto();
//        wallFindDto.setUserId(Integer.valueOf(userId));
//        wallFindDto.setAuditState(auditState);
//        wallFindDto.setPageNum(pageNum);
//        wallFindDto.setPageSize(pageSize);
//        Map<String, Object> map = wallService.findWall(wallFindDto);
//        return Result.succ(map);
//    }

    @ApiOperation("墙列表（管理员页面）")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/auditWallList")
    public Result auditWallList(WallFindDto wallFindDto, HttpServletRequest request) {
        Map<String, Object> map = wallService.findWall(wallFindDto, request);
        return Result.succ(map);
    }

    @ApiOperation("浏览表白墙（浏览量+1）")
    @GetMapping("/view/{wallId}")
    public Result viewWall(@PathVariable("wallId") String wallId) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        wall.setViewNum(wall.getViewNum() != null ? wall.getViewNum() + 1 : 1);
        wallService.updateById(wall);
        dailyStatsService.incrementStat("wall", wall.getWallId(), wall.getUserId(), "viewNum");
        return Result.succ("ok");
    }

    @ApiOperation("表白墙详情")
    @GetMapping("/detail/{wallId}")
    public Result wallDetail(@PathVariable("wallId") String wallId, HttpServletRequest request) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");

        // 获取当前用户ID
        String viewerId = null;
        try {
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                viewerId = JWTUtil.getUserId(token);
            }
        } catch (Exception ignored) {}

        // 检查是否已点赞
        boolean hasLiked = false;
        if (viewerId != null) {
            try {
                QueryWrapper<WallLike> likeQw = new QueryWrapper<>();
                likeQw.eq("user_id", viewerId).eq("wall_id", wallId);
                hasLiked = wallLikeMapper.selectCount(likeQw) > 0;
            } catch (Exception e) {
                hasLiked = false;
            }
        }

        // 检查是否已分享
        boolean hasShared = false;
        if (viewerId != null) {
            try {
                QueryWrapper<ContentShare> shareQw = new QueryWrapper<>();
                shareQw.eq("user_id", viewerId).eq("item_type", "wall").eq("item_id", wallId);
                hasShared = contentShareMapper.selectCount(shareQw) > 0;
            } catch (Exception e) {
                hasShared = false;
            }
        }

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("wallId", wall.getWallId());
        result.put("wallContent", wall.getWallContent());
        result.put("wallImages", wall.getWallImages());
        result.put("userId", wall.getUserId());
        result.put("isAnonymous", wall.getIsAnonymous());
        result.put("viewNum", wall.getViewNum());
        result.put("likeNum", wall.getLikeNum());
        result.put("collectNum", wall.getCollectNum());
        result.put("shareNum", wall.getShareNum());
        result.put("locked", wall.getLocked());
        result.put("lockReason", wall.getLockReason());
        result.put("visibility", wall.getVisibility());
        result.put("mentionUsers", wall.getMentionUsers());
        result.put("createTime", wall.getCreateTime());
        result.put("updateTime", wall.getUpdateTime());
        result.put("hasLiked", hasLiked);
        result.put("hasShared", hasShared);

        // 获取用户信息
        if (!wall.getIsAnonymous() && wall.getUserId() != null) {
            User user = userMapper.selectById(wall.getUserId());
            if (user != null) {
                result.put("nickname", user.getNickname());
                result.put("sex", user.getSex());
                result.put("avatar", user.getAvatar());
            }
        }

        return Result.succ(result);
    }

    @ApiOperation("点赞表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/like/{wallId}")
    public Result likeWall(@PathVariable("wallId") String wallId, HttpServletRequest request) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 检查是否已点赞，防止重复
        QueryWrapper<WallLike> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("wall_id", wallId);
        if (wallLikeMapper.selectCount(qw) > 0) {
            return Result.fail("已点赞过该表白墙");
        }
        // 记录点赞
        WallLike wallLike = new WallLike();
        wallLike.setLikeId(IdGenerator.generateId(IdGenerator.WALL_LIKE));
        wallLike.setUserId(userId);
        wallLike.setWallId(wallId);
        wallLike.setCreateTime(new Date());
        wallLikeMapper.insert(wallLike);
        wall.setLikeNum(wall.getLikeNum() != null ? wall.getLikeNum() + 1 : 1);
        wallService.updateById(wall);
        dailyStatsService.incrementStat("wall", wall.getWallId(), wall.getUserId(), "likeNum");
        // 发送点赞通知
        if (!userId.equals(wall.getUserId())) {
            User liker = userMapper.selectById(userId);
            String nickname = liker != null ? (liker.getNickname() != null ? liker.getNickname() : liker.getUsername()) : "用户";
            letterService.sendSystemNotification(wall.getUserId(), nickname + " 赞了你的表白墙", "like", "wall", wallId);
        }
        return Result.succ(wall.getLikeNum());
    }

    @ApiOperation("取消点赞表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlike/{wallId}")
    public Result unlikeWall(@PathVariable("wallId") String wallId, HttpServletRequest request) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 删除点赞记录
        QueryWrapper<WallLike> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("wall_id", wallId);
        wallLikeMapper.delete(qw);
        wall.setLikeNum(wall.getLikeNum() != null && wall.getLikeNum() > 0 ? wall.getLikeNum() - 1 : 0);
        wallService.updateById(wall);
        return Result.succ(wall.getLikeNum());
    }

    @ApiOperation("分享表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/share/{wallId}")
    public Result shareWall(@PathVariable("wallId") String wallId, HttpServletRequest request) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 检查是否已分享，防止重复
        QueryWrapper<ContentShare> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("item_type", "wall").eq("item_id", wallId);
        if (contentShareMapper.selectCount(qw) > 0) {
            return Result.fail("已分享过该表白墙");
        }
        // 记录分享
        ContentShare share = new ContentShare();
        share.setShareId(IdGenerator.generateId(IdGenerator.CONTENT_SHARE));
        share.setUserId(userId);
        share.setItemType("wall");
        share.setItemId(wallId);
        share.setCreateTime(new Date());
        contentShareMapper.insert(share);
        wall.setShareNum(wall.getShareNum() != null ? wall.getShareNum() + 1 : 1);
        wallService.updateById(wall);
        dailyStatsService.incrementStat("wall", wall.getWallId(), wall.getUserId(), "shareNum");
        return Result.succ(wall.getShareNum());
    }

    @ApiOperation("收藏表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/collect/{wallId}")
    public Result collectWall(@PathVariable("wallId") String wallId) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        wall.setCollectNum(wall.getCollectNum() != null ? wall.getCollectNum() + 1 : 1);
        wallService.updateById(wall);
        dailyStatsService.incrementStat("wall", wall.getWallId(), wall.getUserId(), "collectNum");
        return Result.succ(wall.getCollectNum());
    }

    @ApiOperation("取消收藏表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/uncollect/{wallId}")
    public Result uncollectWall(@PathVariable("wallId") String wallId) {
        Wall wall = wallService.getById(wallId);
        if (wall == null) return Result.fail("不存在");
        wall.setCollectNum(wall.getCollectNum() != null && wall.getCollectNum() > 0 ? wall.getCollectNum() - 1 : 0);
        wallService.updateById(wall);
        return Result.succ(wall.getCollectNum());
    }

    @ApiOperation("用户删除自己的表白墙")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/deleteMyWall/{wallId}")
    public Result deleteMyWall(@PathVariable("wallId") String wallId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return recycleBinService.moveToRecycleBin(userId, "wall", wallId);
    }
}
