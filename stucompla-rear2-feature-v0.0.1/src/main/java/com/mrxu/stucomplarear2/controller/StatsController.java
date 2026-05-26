package com.mrxu.stucomplarear2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.entity.Collect;
import com.mrxu.stucomplarear2.entity.Follow;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.entity.MarketOrder;
import com.mrxu.stucomplarear2.entity.Post;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.Wall;
import com.mrxu.stucomplarear2.entity.Comment;
import com.mrxu.stucomplarear2.entity.Report;
import com.mrxu.stucomplarear2.entity.Letter;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.entity.DailyStats;
import com.mrxu.stucomplarear2.mapper.CategoryMapper;
import com.mrxu.stucomplarear2.mapper.CollectMapper;
import com.mrxu.stucomplarear2.mapper.DailyStatsMapper;
import com.mrxu.stucomplarear2.mapper.FollowMapper;
import com.mrxu.stucomplarear2.mapper.GoodsCategoryMapper;
import com.mrxu.stucomplarear2.mapper.GoodsMapper;
import com.mrxu.stucomplarear2.mapper.MarketOrderMapper;
import com.mrxu.stucomplarear2.mapper.PostMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.mapper.WallMapper;
import com.mrxu.stucomplarear2.mapper.CommentMapper;
import com.mrxu.stucomplarear2.mapper.ReportMapper;
import com.mrxu.stucomplarear2.mapper.LetterMapper;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private MarketOrderMapper marketOrderMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private CollectMapper collectMapper;
    @Autowired
    private WallMapper wallMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private LetterMapper letterMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DailyStatsMapper dailyStatsMapper;

    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/myStats")
    public Result myStats(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);

        // myPostCount - 只统计审核通过的帖子
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.eq("user_id", userId);
        postWrapper.eq("post_status", 0);  // 排除已删除
        postWrapper.eq("audit_state", 1);  // 只统计审核通过的
        long myPostCount = postMapper.selectCount(postWrapper);

        // myCommentCount
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("user_id", userId);
        long myCommentCount = commentMapper.selectCount(commentWrapper);

        // myGoodsCount - 只统计审核通过的商品
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("user_id", userId);
        goodsWrapper.eq("locked", 0);  // 排除锁定
        goodsWrapper.eq("audit_state", 1);  // 只统计审核通过的
        long myGoodsCount = goodsMapper.selectCount(goodsWrapper);

        // myCollectCount
        QueryWrapper<Collect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", userId);
        long myCollectCount = collectMapper.selectCount(collectWrapper);

        // myFollowerCount - count of follows where following_id = current user
        QueryWrapper<Follow> followerWrapper = new QueryWrapper<>();
        followerWrapper.eq("following_id", userId);
        long myFollowerCount = followMapper.selectCount(followerWrapper);

        // myFollowingCount - count of follows where follower_id = current user
        QueryWrapper<Follow> followingWrapper = new QueryWrapper<>();
        followingWrapper.eq("follower_id", userId);
        long myFollowingCount = followMapper.selectCount(followingWrapper);

        // myOrderCount - count of valid orders where buyer_id = current user (排除未支付/已退款/已退货)
        QueryWrapper<MarketOrder> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("buyer_id", userId);
        orderWrapper.in("order_status", 1, 2, 3, 8, 9);
        long myOrderCount = marketOrderMapper.selectCount(orderWrapper);

        // mySaleCount - count of valid orders where seller_id = current user (排除未支付/已退款/已退货)
        QueryWrapper<MarketOrder> saleWrapper = new QueryWrapper<>();
        saleWrapper.eq("seller_id", userId);
        saleWrapper.in("order_status", 1, 2, 3, 8, 9);
        long mySaleCount = marketOrderMapper.selectCount(saleWrapper);

        // myRevenue - sum of total_price from orders where seller_id = current user and order_status in (1, 2, 3, 8, 9)
        QueryWrapper<MarketOrder> revenueWrapper = new QueryWrapper<>();
        revenueWrapper.eq("seller_id", userId);
        revenueWrapper.in("order_status", 1, 2, 3, 8, 9);
        List<MarketOrder> revenueOrders = marketOrderMapper.selectList(revenueWrapper);
        double myRevenue = revenueOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

        // myMonthOrderCount & myMonthRevenue - seller orders this month
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(year, month, 1, 0, 0, 0);
        monthStart.set(Calendar.MILLISECOND, 0);

        QueryWrapper<MarketOrder> monthOrderWrapper = new QueryWrapper<>();
        monthOrderWrapper.eq("seller_id", userId);
        monthOrderWrapper.ge("create_time", monthStart.getTime());
        monthOrderWrapper.in("order_status", 1, 2, 3, 8, 9);
        List<MarketOrder> monthOrders = marketOrderMapper.selectList(monthOrderWrapper);
        long myMonthOrderCount = monthOrders.size();
        double myMonthRevenue = monthOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

        // myWeekTrend - last 7 days trend of seller orders
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> myWeekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date dayEnd = cal.getTime();

            QueryWrapper<MarketOrder> weekWrapper = new QueryWrapper<>();
            weekWrapper.eq("seller_id", userId);
            weekWrapper.ge("create_time", dayStart);
            weekWrapper.le("create_time", dayEnd);
            weekWrapper.in("order_status", 1, 2, 3, 8, 9);
            List<MarketOrder> dayOrders = marketOrderMapper.selectList(weekWrapper);
            long dayCount = dayOrders.size();
            double dayRevenue = dayOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("date", sdf.format(dayStart));
            item.put("orderCount", dayCount);
            item.put("revenue", Math.round(dayRevenue * 100) / 100.0);
            myWeekTrend.add(item);
        }

        // myYearTrend - last 12 months trend of seller orders
        SimpleDateFormat monthSdf = new SimpleDateFormat("yyyy-MM");
        List<Map<String, Object>> myYearTrend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            Calendar monthCal = Calendar.getInstance();
            monthCal.add(Calendar.MONTH, -i);
            int y = monthCal.get(Calendar.YEAR);
            int m = monthCal.get(Calendar.MONTH);
            Calendar mStart = Calendar.getInstance();
            mStart.set(y, m, 1, 0, 0, 0);
            mStart.set(Calendar.MILLISECOND, 0);
            Calendar mEnd = Calendar.getInstance();
            mEnd.set(y, m, 1, 0, 0, 0);
            mEnd.set(Calendar.MILLISECOND, 0);
            mEnd.add(Calendar.MONTH, 1);

            QueryWrapper<MarketOrder> monthWrapper = new QueryWrapper<>();
            monthWrapper.eq("seller_id", userId);
            monthWrapper.ge("create_time", mStart.getTime());
            monthWrapper.lt("create_time", mEnd.getTime());
            monthWrapper.in("order_status", 1, 2, 3, 8, 9);
            List<MarketOrder> monthOrdersList = marketOrderMapper.selectList(monthWrapper);
            long monthCount = monthOrdersList.size();
            double monthRev = monthOrdersList.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

            Map<String, Object> monthItem = new HashMap<>();
            monthItem.put("date", monthSdf.format(mStart.getTime()));
            monthItem.put("orderCount", monthCount);
            monthItem.put("revenue", Math.round(monthRev * 100) / 100.0);
            myYearTrend.add(monthItem);
        }

        // myGoodsCategoryStats - 只统计审核通过的商品分类
        List<GoodsCategory> categories = goodsCategoryMapper.selectList(null);
        QueryWrapper<Goods> myGoodsWrapper = new QueryWrapper<>();
        myGoodsWrapper.eq("user_id", userId);
        myGoodsWrapper.eq("locked", 0);
        myGoodsWrapper.eq("audit_state", 1);  // 只统计审核通过的
        List<Goods> myGoodsList = goodsMapper.selectList(myGoodsWrapper);
        Map<Integer, Long> myCountMap = myGoodsList.stream()
                .filter(g -> g.getGoodsCategoryId() != null)
                .collect(Collectors.groupingBy(Goods::getGoodsCategoryId, Collectors.counting()));
        List<Map<String, Object>> myGoodsCategoryStats = new ArrayList<>();
        for (GoodsCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getGoodsCategoryName());
            item.put("value", myCountMap.getOrDefault(category.getGoodsCategoryId(), 0L));
            myGoodsCategoryStats.add(item);
        }

        // 卖出的商品分类统计
        QueryWrapper<MarketOrder> soldOrderWrapper = new QueryWrapper<>();
        soldOrderWrapper.eq("seller_id", userId);
        soldOrderWrapper.in("order_status", 1, 2, 3, 8, 9);  // 已支付/已发货/已签收/已完成/已评价的订单
        List<MarketOrder> soldOrders = marketOrderMapper.selectList(soldOrderWrapper);
        // 从订单中获取商品ID，查询商品分类
        Map<Integer, Long> soldCountMap = new HashMap<>();
        for (MarketOrder order : soldOrders) {
            Goods soldGoods = goodsMapper.selectById(order.getGoodsId());
            if (soldGoods != null && soldGoods.getGoodsCategoryId() != null) {
                soldCountMap.merge(soldGoods.getGoodsCategoryId(), 1L, Long::sum);
            }
        }
        List<Map<String, Object>> soldGoodsCategoryStats = new ArrayList<>();
        for (GoodsCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getGoodsCategoryName());
            item.put("value", soldCountMap.getOrDefault(category.getGoodsCategoryId(), 0L));
            soldGoodsCategoryStats.add(item);
        }

        // 订单状态分布 - 作为卖家的订单状态统计（10种状态）
        String[] statusNames = {"未支付", "已支付", "已发货", "已签收", "退款中", "退货中", "已退款", "已退货", "已完成", "已评价"};
        QueryWrapper<MarketOrder> allOrderWrapper = new QueryWrapper<>();
        allOrderWrapper.eq("seller_id", userId);
        List<MarketOrder> allMyOrders = marketOrderMapper.selectList(allOrderWrapper);
        Map<Integer, Long> statusCountMap = allMyOrders.stream()
                .filter(o -> o.getOrderStatus() != null)
                .collect(Collectors.groupingBy(MarketOrder::getOrderStatus, Collectors.counting()));
        List<Map<String, Object>> orderStatusStats = new ArrayList<>();
        for (int i = 0; i < statusNames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", String.valueOf(i));
            item.put("value", statusCountMap.getOrDefault(i, 0L));
            orderStatusStats.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("myPostCount", myPostCount);
        data.put("myCommentCount", myCommentCount);
        data.put("myGoodsCount", myGoodsCount);
        data.put("myCollectCount", myCollectCount);
        data.put("myFollowerCount", myFollowerCount);
        data.put("myFollowingCount", myFollowingCount);
        data.put("myOrderCount", myOrderCount);
        data.put("mySaleCount", mySaleCount);
        data.put("myRevenue", Math.round(myRevenue * 100) / 100.0);
        data.put("myMonthOrderCount", myMonthOrderCount);
        data.put("myMonthRevenue", Math.round(myMonthRevenue * 100) / 100.0);
        data.put("myWeekTrend", myWeekTrend);
        data.put("myYearTrend", myYearTrend);
        data.put("myGoodsCategoryStats", myGoodsCategoryStats);
        data.put("soldGoodsCategoryStats", soldGoodsCategoryStats);
        data.put("orderStatusStats", orderStatusStats);

        // myWallCount
        QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
        wallWrapper.eq("user_id", userId);
        wallWrapper.eq("locked", 0);
        wallWrapper.eq("audit_state", 1);
        long myWallCount = wallMapper.selectCount(wallWrapper);
        data.put("myWallCount", myWallCount);

        // myReceivedLikeCount - 统计审核通过的帖子+表白墙的获赞
        QueryWrapper<Post> myPostWrapper = new QueryWrapper<>();
        myPostWrapper.eq("user_id", userId);
        myPostWrapper.eq("post_status", 0);  // 排除已删除
        myPostWrapper.eq("audit_state", 1);  // 只统计审核通过的
        List<Post> myPostList = postMapper.selectList(myPostWrapper);
        long myReceivedLikeCount = myPostList.stream().mapToLong(p -> p.getLikeNum() != null ? p.getLikeNum() : 0).sum();
        // Add wall likes
        QueryWrapper<Wall> myWallLikeWrapper = new QueryWrapper<>();
        myWallLikeWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("like_num");
        for (Wall w : wallMapper.selectList(myWallLikeWrapper)) { myReceivedLikeCount += (w.getLikeNum() != null ? w.getLikeNum() : 0); }
        data.put("myReceivedLikeCount", myReceivedLikeCount);

        // myReceivedCollectCount - 统计审核通过的帖子+表白墙的收藏
        long myReceivedCollectCount = myPostList.stream().mapToLong(p -> p.getCollectNum() != null ? p.getCollectNum() : 0).sum();
        QueryWrapper<Wall> myWallCollectWrapper = new QueryWrapper<>();
        myWallCollectWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("collect_num");
        for (Wall w : wallMapper.selectList(myWallCollectWrapper)) { myReceivedCollectCount += (w.getCollectNum() != null ? w.getCollectNum() : 0); }
        data.put("myReceivedCollectCount", myReceivedCollectCount);

        // myReceivedCommentCount - 只统计审核通过帖子的评论
        long myReceivedCommentCount = myPostList.stream().mapToLong(p -> p.getCommentNum() != null ? p.getCommentNum() : 0).sum();
        data.put("myReceivedCommentCount", myReceivedCommentCount);

        // myReceivedViewCount - Total view count of all my approved posts + goods + walls
        QueryWrapper<Post> postViewWrapper = new QueryWrapper<>();
        postViewWrapper.eq("user_id", userId).eq("post_status", 0).eq("audit_state", 1).select("view_num");
        long myReceivedViewCount = 0;
        for (Post p : postMapper.selectList(postViewWrapper)) { myReceivedViewCount += (p.getViewNum() != null ? p.getViewNum() : 0); }
        // Add goods views
        QueryWrapper<Goods> goodsViewWrapper = new QueryWrapper<>();
        goodsViewWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("view_num");
        for (Goods g : goodsMapper.selectList(goodsViewWrapper)) { myReceivedViewCount += (g.getViewNum() != null ? g.getViewNum() : 0); }
        // Add wall views
        QueryWrapper<Wall> wallViewWrapper = new QueryWrapper<>();
        wallViewWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("view_num");
        for (Wall w : wallMapper.selectList(wallViewWrapper)) { myReceivedViewCount += (w.getViewNum() != null ? w.getViewNum() : 0); }
        data.put("myReceivedViewCount", myReceivedViewCount);

        // myReceivedMentionCount - Count of mention-type unread letters where receiver_id = me
        QueryWrapper<Letter> mentionWrapper = new QueryWrapper<>();
        mentionWrapper.eq("receiver_id", userId).eq("message_type", "mention");
        data.put("myReceivedMentionCount", letterMapper.selectCount(mentionWrapper));

        // totalViewCount - same as myReceivedViewCount for personal stats
        data.put("totalViewCount", myReceivedViewCount);

        // totalLikeCount - Total likes received on my content (Post + Wall, Goods has no likeNum)
        long totalLikeCount = 0;
        QueryWrapper<Post> postLikeWrapper = new QueryWrapper<>();
        postLikeWrapper.eq("user_id", userId).eq("post_status", 0).eq("audit_state", 1).select("like_num");
        for (Post p : postMapper.selectList(postLikeWrapper)) { totalLikeCount += (p.getLikeNum() != null ? p.getLikeNum() : 0); }
        QueryWrapper<Wall> wallLikeWrapper = new QueryWrapper<>();
        wallLikeWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("like_num");
        for (Wall w : wallMapper.selectList(wallLikeWrapper)) { totalLikeCount += (w.getLikeNum() != null ? w.getLikeNum() : 0); }
        data.put("totalLikeCount", totalLikeCount);

        // totalCommentCount - Total comments received on my content
        long totalCommentCount = 0;
        QueryWrapper<Post> postCommentWrapper = new QueryWrapper<>();
        postCommentWrapper.eq("user_id", userId).eq("post_status", 0).eq("audit_state", 1).select("comment_num");
        for (Post p : postMapper.selectList(postCommentWrapper)) { totalCommentCount += (p.getCommentNum() != null ? p.getCommentNum() : 0); }
        data.put("totalCommentCount", totalCommentCount);

        // totalCollectCount - Total collects received on my content (Post + Wall)
        long totalCollectCount = 0;
        QueryWrapper<Post> postCollectWrapper = new QueryWrapper<>();
        postCollectWrapper.eq("user_id", userId).eq("post_status", 0).eq("audit_state", 1).select("collect_num");
        for (Post p : postMapper.selectList(postCollectWrapper)) { totalCollectCount += (p.getCollectNum() != null ? p.getCollectNum() : 0); }
        QueryWrapper<Wall> wallCollectWrapper = new QueryWrapper<>();
        wallCollectWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("collect_num");
        for (Wall w : wallMapper.selectList(wallCollectWrapper)) { totalCollectCount += (w.getCollectNum() != null ? w.getCollectNum() : 0); }
        data.put("totalCollectCount", totalCollectCount);

        // totalShareCount - Total shares received on my content (Wall has shareNum)
        long totalShareCount = 0;
        QueryWrapper<Wall> wallShareWrapper = new QueryWrapper<>();
        wallShareWrapper.eq("user_id", userId).eq("locked", 0).eq("audit_state", 1).select("share_num");
        for (Wall w : wallMapper.selectList(wallShareWrapper)) { totalShareCount += (w.getShareNum() != null ? w.getShareNum() : 0); }
        data.put("totalShareCount", totalShareCount);

        // totalOrderCount - 累计订单 (buy + sell)
        data.put("totalOrderCount", myOrderCount + mySaleCount);

        return Result.succ(data);
    }

    @ApiOperation("获取用户获赞数")
    @GetMapping("/likeCount")
    public Result likeCount(@RequestParam(required = false) String userId, HttpServletRequest request) {
        if (userId == null || userId.isEmpty()) {
            String token = request.getHeader("Authorization");
            userId = JWTUtil.getUserId(token);
        }
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.eq("user_id", userId);
        postWrapper.eq("post_status", 0);
        postWrapper.eq("audit_state", 1);
        List<Post> userPosts = postMapper.selectList(postWrapper);
        long totalLikes = 0;
        for (Post post : userPosts) {
            totalLikes += post.getLikeNum() != null ? post.getLikeNum() : 0;
        }
        return Result.succ(totalLikes);
    }

    @GetMapping("/overview")
    public Result overview() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);

        Calendar monthStart = Calendar.getInstance();
        monthStart.set(year, month, 1, 0, 0, 0);
        monthStart.set(Calendar.MILLISECOND, 0);

        Calendar yearStart = Calendar.getInstance();
        yearStart.set(year, 0, 1, 0, 0, 0);
        yearStart.set(Calendar.MILLISECOND, 0);

        QueryWrapper<MarketOrder> monthWrapper = new QueryWrapper<>();
        monthWrapper.ge("create_time", monthStart.getTime());
        monthWrapper.in("order_status", 1, 2, 3, 8, 9);
        List<MarketOrder> monthOrders = marketOrderMapper.selectList(monthWrapper);
        long monthOrderCount = monthOrders.size();
        double monthRevenue = monthOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

        QueryWrapper<MarketOrder> yearWrapper = new QueryWrapper<>();
        yearWrapper.ge("create_time", yearStart.getTime());
        yearWrapper.in("order_status", 1, 2, 3, 8, 9);
        List<MarketOrder> yearOrders = marketOrderMapper.selectList(yearWrapper);
        long yearOrderCount = yearOrders.size();
        double yearRevenue = yearOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("monthOrderCount", monthOrderCount);
        data.put("monthRevenue", Math.round(monthRevenue * 100) / 100.0);
        data.put("yearOrderCount", yearOrderCount);
        data.put("yearRevenue", Math.round(yearRevenue * 100) / 100.0);
        data.put("userTotal", userMapper.selectCount(null));
        QueryWrapper<Post> postTotalWrapper = new QueryWrapper<>();
        postTotalWrapper.eq("post_status", 0);
        postTotalWrapper.eq("audit_state", 1);
        data.put("postTotal", postMapper.selectCount(postTotalWrapper));
        QueryWrapper<Goods> goodsTotalWrapper = new QueryWrapper<>();
        goodsTotalWrapper.eq("audit_state", 1);
        data.put("goodsTotal", goodsMapper.selectCount(goodsTotalWrapper));
        data.put("commentTotal", commentMapper.selectCount(null));
        return Result.succ(data);
    }

    @GetMapping("/weeklyTrend")
    public Result weeklyTrend() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date dayEnd = cal.getTime();

            QueryWrapper<MarketOrder> wrapper = new QueryWrapper<>();
            wrapper.ge("create_time", dayStart);
            wrapper.le("create_time", dayEnd);
            wrapper.in("order_status", 1, 2, 3, 8, 9);
            List<MarketOrder> orders = marketOrderMapper.selectList(wrapper);
            long count = orders.size();
            double revenue = orders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("date", sdf.format(dayStart));
            item.put("orderCount", count);
            item.put("revenue", Math.round(revenue * 100) / 100.0);
            result.add(item);
        }
        return Result.succ(result);
    }

    @GetMapping("/goodsCategoryStats")
    public Result goodsCategoryStats() {
        List<GoodsCategory> categories = goodsCategoryMapper.selectList(null);
        QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
        goodsQueryWrapper.eq("audit_state", 1);  // 只统计审核通过的商品
        List<Goods> goodsList = goodsMapper.selectList(goodsQueryWrapper);
        Map<Integer, Long> countMap = goodsList.stream()
                .filter(g -> g.getGoodsCategoryId() != null)
                .collect(Collectors.groupingBy(Goods::getGoodsCategoryId, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (GoodsCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getGoodsCategoryName());
            item.put("value", countMap.getOrDefault(category.getGoodsCategoryId(), 0L));
            result.add(item);
        }
        return Result.succ(result);
    }

    @ApiOperation("未处理内容统计（红点提醒）")
    @GetMapping("/pendingCount")
    public Result pendingCount(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Map<String, Object> data = new HashMap<>();

        try {
            String userId = JWTUtil.getUserId(token);
            String role = JWTUtil.getRole(token);
            boolean isAdmin = "admin".equals(role) || "super".equals(role);

            if (isAdmin) {
                // 管理员：待审核帖子、待审核商品、待审核表白墙、待处理举报
                QueryWrapper<Post> postWrapper = new QueryWrapper<>();
                postWrapper.eq("audit_state", 0);
                data.put("pendingPostAudit", postMapper.selectCount(postWrapper));

                QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
                goodsWrapper.eq("audit_state", 0);
                data.put("pendingGoodsAudit", goodsMapper.selectCount(goodsWrapper));

                QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
                wallWrapper.eq("audit_state", 0);
                data.put("pendingWallAudit", wallMapper.selectCount(wallWrapper));

                QueryWrapper<Report> reportWrapper = new QueryWrapper<>();
                reportWrapper.eq("status", 0);
                data.put("pendingReport", reportMapper.selectCount(reportWrapper));

                long total = (long)data.get("pendingPostAudit") + (long)data.get("pendingGoodsAudit")
                    + (long)data.get("pendingWallAudit") + (long)data.get("pendingReport");
                data.put("total", total);
            } else {
                // 普通用户：待审核帖子、待审核商品、待审核表白墙、未读消息
                QueryWrapper<Post> postWrapper = new QueryWrapper<>();
                postWrapper.eq("user_id", userId);
                postWrapper.eq("audit_state", 0);
                data.put("pendingPostAudit", postMapper.selectCount(postWrapper));

                QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
                goodsWrapper.eq("user_id", userId);
                goodsWrapper.eq("audit_state", 0);
                data.put("pendingGoodsAudit", goodsMapper.selectCount(goodsWrapper));

                QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
                wallWrapper.eq("user_id", userId);
                wallWrapper.eq("audit_state", 0);
                data.put("pendingWallAudit", wallMapper.selectCount(wallWrapper));

                // 未读消息数
                QueryWrapper<Letter> letterWrapper = new QueryWrapper<>();
                letterWrapper.eq("receiver_id", userId);
                letterWrapper.eq("letter_status", 0);
                data.put("unreadMessage", letterMapper.selectCount(letterWrapper));

                // 未读处罚/违规/举报通知数
                data.put("unreadPunishment", letterMapper.countUnreadPunishment(userId));

                // 按板块统计未读消息数
                Map<String, Object> sectionCounts = letterMapper.countUnreadBySection(userId);
                data.put("unreadForum", sectionCounts != null && sectionCounts.get("forum") != null ? ((Number) sectionCounts.get("forum")).longValue() : 0L);
                data.put("unreadTrade", sectionCounts != null && sectionCounts.get("trade") != null ? ((Number) sectionCounts.get("trade")).longValue() : 0L);
                data.put("unreadWall", sectionCounts != null && sectionCounts.get("wall") != null ? ((Number) sectionCounts.get("wall")).longValue() : 0L);

                long total = (long)data.get("pendingPostAudit") + (long)data.get("pendingGoodsAudit")
                    + (long)data.get("pendingWallAudit") + (long)data.get("unreadMessage");
                data.put("total", total);
            }
        } catch (Exception e) {
            data.put("total", 0);
        }

        return Result.succ(data);
    }

    @ApiOperation("论坛数据统计")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/postStats")
    public Result postStats(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat monthSdf = new SimpleDateFormat("yyyy-MM");

        // 帖子分类统计
        List<Category> categories = categoryMapper.selectList(null);
        QueryWrapper<Post> myPostWrapper = new QueryWrapper<>();
        myPostWrapper.eq("user_id", userId).eq("post_status", 0).eq("audit_state", 1);
        List<Post> myPosts = postMapper.selectList(myPostWrapper);
        Map<Integer, Long> catCountMap = myPosts.stream()
                .filter(p -> p.getCategoryId() != null)
                .collect(Collectors.groupingBy(Post::getCategoryId, Collectors.counting()));
        List<Map<String, Object>> postCategoryStats = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", cat.getCategoryName());
            item.put("value", catCountMap.getOrDefault(cat.getCategoryId(), 0L));
            postCategoryStats.add(item);
        }

        // 近7天帖子趋势（每日增量：基于daily_stats实际活动数据）
        List<Map<String, Object>> postWeekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
            Date dayEnd = cal.getTime();
            String dateStr = sdf.format(dayStart);

            QueryWrapper<DailyStats> dayWrapper = new QueryWrapper<>();
            dayWrapper.eq("user_id", userId).eq("item_type", "post")
                    .ge("stat_date", dayStart).le("stat_date", dayEnd);
            List<DailyStats> dayStatsList = dailyStatsMapper.selectList(dayWrapper);
            long viewNum = dayStatsList.stream().mapToLong(s -> s.getViewNum() != null ? s.getViewNum() : 0).sum();
            long likeNum = dayStatsList.stream().mapToLong(s -> s.getLikeNum() != null ? s.getLikeNum() : 0).sum();
            long collectNum = dayStatsList.stream().mapToLong(s -> s.getCollectNum() != null ? s.getCollectNum() : 0).sum();
            long shareNum = dayStatsList.stream().mapToLong(s -> s.getShareNum() != null ? s.getShareNum() : 0).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("viewNum", viewNum);
            item.put("likeNum", likeNum);
            item.put("collectNum", collectNum);
            item.put("shareNum", shareNum);
            postWeekTrend.add(item);
        }

        // 近12个月帖子趋势（基于daily_stats实际活动数据）
        List<Map<String, Object>> postYearTrend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            Calendar monthCal = Calendar.getInstance();
            monthCal.add(Calendar.MONTH, -i);
            int y = monthCal.get(Calendar.YEAR);
            int m = monthCal.get(Calendar.MONTH);
            Calendar mStart = Calendar.getInstance();
            mStart.set(y, m, 1, 0, 0, 0); mStart.set(Calendar.MILLISECOND, 0);
            Calendar mEnd = Calendar.getInstance();
            mEnd.set(y, m, 1, 0, 0, 0); mEnd.set(Calendar.MILLISECOND, 0);
            mEnd.add(Calendar.MONTH, 1);

            QueryWrapper<DailyStats> monthWrapper = new QueryWrapper<>();
            monthWrapper.eq("user_id", userId).eq("item_type", "post")
                    .ge("stat_date", mStart.getTime()).lt("stat_date", mEnd.getTime());
            List<DailyStats> monthStatsList = dailyStatsMapper.selectList(monthWrapper);
            long viewNum = monthStatsList.stream().mapToLong(s -> s.getViewNum() != null ? s.getViewNum() : 0).sum();
            long likeNum = monthStatsList.stream().mapToLong(s -> s.getLikeNum() != null ? s.getLikeNum() : 0).sum();
            long collectNum = monthStatsList.stream().mapToLong(s -> s.getCollectNum() != null ? s.getCollectNum() : 0).sum();
            long shareNum = monthStatsList.stream().mapToLong(s -> s.getShareNum() != null ? s.getShareNum() : 0).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("date", monthSdf.format(mStart.getTime()));
            item.put("viewNum", viewNum);
            item.put("likeNum", likeNum);
            item.put("collectNum", collectNum);
            item.put("shareNum", shareNum);
            postYearTrend.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("postCategoryStats", postCategoryStats);
        data.put("postWeekTrend", postWeekTrend);
        data.put("postYearTrend", postYearTrend);
        return Result.succ(data);
    }

    @ApiOperation("表白墙数据统计")
    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/wallStats")
    public Result wallStats(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat monthSdf = new SimpleDateFormat("yyyy-MM");

        // 近7天表白墙趋势（每日增量：基于daily_stats实际活动数据）
        List<Map<String, Object>> wallWeekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
            Date dayEnd = cal.getTime();
            String dateStr = sdf.format(dayStart);

            QueryWrapper<DailyStats> dayWrapper = new QueryWrapper<>();
            dayWrapper.eq("user_id", userId).eq("item_type", "wall")
                    .ge("stat_date", dayStart).le("stat_date", dayEnd);
            List<DailyStats> dayStatsList = dailyStatsMapper.selectList(dayWrapper);
            long viewNum = dayStatsList.stream().mapToLong(s -> s.getViewNum() != null ? s.getViewNum() : 0).sum();
            long likeNum = dayStatsList.stream().mapToLong(s -> s.getLikeNum() != null ? s.getLikeNum() : 0).sum();
            long collectNum = dayStatsList.stream().mapToLong(s -> s.getCollectNum() != null ? s.getCollectNum() : 0).sum();
            long shareNum = dayStatsList.stream().mapToLong(s -> s.getShareNum() != null ? s.getShareNum() : 0).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("viewNum", viewNum);
            item.put("likeNum", likeNum);
            item.put("collectNum", collectNum);
            item.put("shareNum", shareNum);
            wallWeekTrend.add(item);
        }

        // 近12个月表白墙趋势（基于daily_stats实际活动数据）
        List<Map<String, Object>> wallYearTrend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            Calendar monthCal = Calendar.getInstance();
            monthCal.add(Calendar.MONTH, -i);
            int y = monthCal.get(Calendar.YEAR);
            int m = monthCal.get(Calendar.MONTH);
            Calendar mStart = Calendar.getInstance();
            mStart.set(y, m, 1, 0, 0, 0); mStart.set(Calendar.MILLISECOND, 0);
            Calendar mEnd = Calendar.getInstance();
            mEnd.set(y, m, 1, 0, 0, 0); mEnd.set(Calendar.MILLISECOND, 0);
            mEnd.add(Calendar.MONTH, 1);

            QueryWrapper<DailyStats> monthWrapper = new QueryWrapper<>();
            monthWrapper.eq("user_id", userId).eq("item_type", "wall")
                    .ge("stat_date", mStart.getTime()).lt("stat_date", mEnd.getTime());
            List<DailyStats> monthStatsList = dailyStatsMapper.selectList(monthWrapper);
            long viewNum = monthStatsList.stream().mapToLong(s -> s.getViewNum() != null ? s.getViewNum() : 0).sum();
            long likeNum = monthStatsList.stream().mapToLong(s -> s.getLikeNum() != null ? s.getLikeNum() : 0).sum();
            long collectNum = monthStatsList.stream().mapToLong(s -> s.getCollectNum() != null ? s.getCollectNum() : 0).sum();
            long shareNum = monthStatsList.stream().mapToLong(s -> s.getShareNum() != null ? s.getShareNum() : 0).sum();

            Map<String, Object> item = new HashMap<>();
            item.put("date", monthSdf.format(mStart.getTime()));
            item.put("viewNum", viewNum);
            item.put("likeNum", likeNum);
            item.put("collectNum", collectNum);
            item.put("shareNum", shareNum);
            wallYearTrend.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("wallWeekTrend", wallWeekTrend);
        data.put("wallYearTrend", wallYearTrend);
        return Result.succ(data);
    }
}
