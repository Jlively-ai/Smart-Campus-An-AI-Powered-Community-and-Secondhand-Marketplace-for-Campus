package com.mrxu.stucomplarear2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.dto.GoodsAddDto;
import com.mrxu.stucomplarear2.dto.GoodsEditDto;
import com.mrxu.stucomplarear2.dto.GoodsFindDto;
import com.mrxu.stucomplarear2.dto.GoodsAuditDto;
import com.mrxu.stucomplarear2.dto.GoodsVo;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.entity.ContentShare;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.ViolationDelete;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.mapper.GoodsCategoryMapper;
import com.mrxu.stucomplarear2.mapper.ContentShareMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.GoodsService;
import com.mrxu.stucomplarear2.service.LetterService;
import com.mrxu.stucomplarear2.service.RecycleBinService;
import com.mrxu.stucomplarear2.service.ViolationDeleteService;
import com.mrxu.stucomplarear2.service.impl.DailyStatsService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mr.Xu
 * @since 2022-04-15
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private RecycleBinService recycleBinService;
    @Autowired
    private ViolationDeleteService violationDeleteService;
    @Autowired
    private DailyStatsService dailyStatsService;
    @Autowired
    private ContentShareMapper contentShareMapper;

    @ApiOperation("发布二手商品")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/add")
    public Result add(@RequestBody GoodsAddDto goodsDto, HttpServletRequest request) {
        // 这个@RequestBody有两个包，别导错了！！
        Result result = goodsService.add(goodsDto, request);
        return result;
    }

    @ApiOperation("修改二手商品")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/edit")
    public Result edit(@RequestBody GoodsEditDto goodsEditDto, HttpServletRequest request) {
        Result result = goodsService.editGoods(goodsEditDto, request);
        return result;
    }

    @ApiOperation("删除自己的二手商品")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/{goodsId}")
    public Result deleteMyGoods(@PathVariable("goodsId") String goodsId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        return recycleBinService.moveToRecycleBin(userId, "goods", goodsId);
    }

    @ApiOperation("重新上架自己的二手商品")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/putMyGoods/{goodsId}")
    public Result putMyGoods(@PathVariable("goodsId") String goodsId, HttpServletRequest request) {
        Result result = goodsService.putMyGoods(goodsId, request);
        return result;
    }

    @ApiOperation("下架自己的商品")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/unShelveMyGoods/{goodsId}")
    public Result unShelveMyGoods(@PathVariable("goodsId") String goodsId, HttpServletRequest request) {
        Result result = goodsService.unShelveMyGoods(goodsId, request);
        return result;
    }

    @ApiOperation("管理员下架商品")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unShelve/{goodsId}")
    public Result unShelveGoods(@PathVariable("goodsId") String goodsId) {
        Result result = goodsService.unShelveGoods(goodsId);
        return result;
    }

    @ApiOperation("管理员删除商品")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/deleteGoods/{goodsId}")
    public Result deleteGoods(@PathVariable("goodsId") String goodsId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) return Result.fail("商品不存在");
        return violationDeleteService.moveViolation(goods.getUserId(), "goods", goodsId, cause, handlerId, "delete");
    }

    @ApiOperation("管理员锁定商品")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/lockGoods/{goodsId}")
    public Result lockGoods(@PathVariable("goodsId") String goodsId, String cause, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String handlerId = JWTUtil.getUserId(token);
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) return Result.fail("商品不存在");
        if (goods.getLocked() != null && goods.getLocked() == 1) return Result.fail("商品已锁定");
        goods.setLocked(1);
        goods.setLockReason(cause);
        goods.setUpdateTime(new Date());
        goodsService.updateById(goods);
        violationDeleteService.moveViolation(goods.getUserId(), "goods_lock", goodsId, cause, handlerId, "lock");
        return Result.succ("锁定成功");
    }

    @ApiOperation("管理员解锁商品")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/unlockGoods/{goodsId}")
    public Result unlockGoods(@PathVariable("goodsId") String goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) return Result.fail("商品不存在");
        goods.setLocked(0);
        goods.setLockReason(null);
        goods.setUpdateTime(new Date());
        goodsService.updateById(goods);
        QueryWrapper<ViolationDelete> qw = new QueryWrapper<>();
        qw.eq("item_id", goodsId).eq("operation_type", "lock");
        violationDeleteService.remove(qw);
        return Result.succ("解锁成功");
    }

    @ApiOperation("审核商品")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/audit")
    public Result auditGoods(@RequestBody GoodsAuditDto auditDto, HttpServletRequest request) {
        return goodsService.auditGoods(auditDto, request);
    }

    @ApiOperation("获取二手商品列表")
    @GetMapping("/getList")
    public Result getList(GoodsFindDto goodsFindDto, HttpServletRequest request) {
        System.out.println(goodsFindDto);
        Result result = goodsService.findGoods(goodsFindDto, request);
        return result;
    }

    @ApiOperation("获取商品总数")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getGoodsTotal")
    public Result getGoodsTotal() {
        Result result = goodsService.getGoodsTotal();
        return result;
    }

    @ApiOperation("二手上架分类统计")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @GetMapping("/getGoodsByCategory")
    public Result getGoodsByCategory() {
        Result result = goodsService.getGoodsByCategory();
        return result;
    }

    @ApiOperation("分享商品（分享数+1）")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/share/{goodsId}")
    public Result shareGoods(@PathVariable("goodsId") String goodsId, HttpServletRequest request) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) return Result.fail("商品不存在");
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        // 检查是否已分享，防止重复
        QueryWrapper<ContentShare> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("item_type", "goods").eq("item_id", goodsId);
        if (contentShareMapper.selectCount(qw) > 0) {
            return Result.fail("已分享过该商品");
        }
        // 记录分享
        ContentShare share = new ContentShare();
        share.setShareId(IdGenerator.generateId(IdGenerator.CONTENT_SHARE));
        share.setUserId(userId);
        share.setItemType("goods");
        share.setItemId(goodsId);
        share.setCreateTime(new Date());
        contentShareMapper.insert(share);
        goods.setShareNum(goods.getShareNum() != null ? goods.getShareNum() + 1 : 1);
        goodsService.updateById(goods);
        dailyStatsService.incrementStat("goods", goods.getGoodsId(), goods.getUserId(), "shareNum");
        return Result.succ(goods.getShareNum());
    }

    @ApiOperation("商品详情")
    @GetMapping("/{goodsId}")
    public Result getGoodsDetail(@PathVariable("goodsId") String goodsId, HttpServletRequest request) {
        try {
            // 获取当前用户ID和角色
            String viewerId = null;
            boolean viewerIsAdmin = false;
            try {
                String token = request.getHeader("Authorization");
                if (token != null && !token.isEmpty()) {
                    viewerId = JWTUtil.getUserId(token);
                    String role = JWTUtil.getRole(token);
                    viewerIsAdmin = "admin".equals(role) || "super".equals(role);
                }
            } catch (Exception ignored) {}

            QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("goods_id", goodsId);
            Goods goods = goodsService.getOne(queryWrapper);
            if (goods == null) {
                return Result.fail("商品不存在");
            }
            // 锁定过滤：非作者不能看到锁定的商品（管理员除外）
            if (!viewerIsAdmin && goods.getLocked() != null && goods.getLocked() == 1 && !goods.getUserId().equals(viewerId)) {
                return Result.fail("商品已被锁定");
            }
            // 审核状态检查：非管理员只能查看审核通过的商品
            if (!viewerIsAdmin
                    && goods.getAuditState() != null && goods.getAuditState() != 1) {
                return Result.fail("商品不存在或未通过审核");
            }

            goods = goodsService.updateViewNum(goods);
            dailyStatsService.incrementStat("goods", goods.getGoodsId(), goods.getUserId(), "viewNum");
            GoodsVo goodsVo = new GoodsVo();
            BeanUtils.copyProperties(goods, goodsVo);
            //查对应的发布人信息
            User user = userMapper.selectById(goods.getUserId());
            goodsVo.setUser(user);
            //查对应的帖子类型信息
            GoodsCategory goodsCategory = goodsCategoryMapper.selectById(goods.getGoodsCategoryId());
            goodsVo.setGoodsCategory(goodsCategory);

            // 构建增强的返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("goodsId", goodsVo.getGoodsId());
            result.put("goodsName", goodsVo.getGoodsName());
            result.put("goodsDetail", goodsVo.getGoodsDetail());
            result.put("goodsImages", goodsVo.getGoodsImages());
            result.put("goodsPrice", goodsVo.getGoodsPrice());
            result.put("goodsCount", goodsVo.getGoodsCount());
            result.put("goodsStatus", goodsVo.isGoodsStatus());
            result.put("viewNum", goodsVo.getViewNum());
            result.put("shareNum", goodsVo.getShareNum());
            result.put("createTime", goodsVo.getCreateTime());
            result.put("updateTime", goodsVo.getUpdateTime());
            // 发布者公开信息
            result.put("userId", goods.getUserId());
            // 已注销用户显示用户已注销
            if (user != null && user.getStatus() != null && user.getStatus() == 2) {
                result.put("nickname", "用户已注销");
                result.put("avatar", null);
                result.put("sex", null);
            } else {
                result.put("nickname", user != null ? user.getNickname() : "未知");
                result.put("avatar", user != null ? user.getAvatar() : null);
                result.put("sex", user != null ? user.getSex() : null);
            }
            // 分类名称
            result.put("goodsCategoryName", goodsCategory != null ? goodsCategory.getGoodsCategoryName() : null);
            // 发布者粉丝数
            Integer followerCount = followMapper.selectCount(new QueryWrapper<Follow>().eq("following_id", goods.getUserId()));
            result.put("followerCount", followerCount);

            // 检查当前用户是否已分享
            boolean hasShared = false;
            if (viewerId != null) {
                try {
                    QueryWrapper<ContentShare> shareQw = new QueryWrapper<>();
                    shareQw.eq("user_id", viewerId).eq("item_type", "goods").eq("item_id", goodsId);
                    hasShared = contentShareMapper.selectCount(shareQw) > 0;
                } catch (Exception e) {
                    hasShared = false;
                }
            }
            result.put("hasShared", hasShared);

            return Result.succ(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
    }
}
