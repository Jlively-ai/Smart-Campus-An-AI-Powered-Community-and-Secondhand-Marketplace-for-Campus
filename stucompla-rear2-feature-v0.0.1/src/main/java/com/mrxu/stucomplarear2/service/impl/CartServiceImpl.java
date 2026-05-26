package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Cart;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.mapper.CartMapper;
import com.mrxu.stucomplarear2.mapper.GoodsMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.CartService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result addToCart(String goodsId, Integer quantity, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) return Result.fail("商品不存在");
        if (goods.getUserId().equals(userId)) return Result.fail("不能购买自己的商品");
        // Check if already in cart
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("goods_id", goodsId);
        Cart existing = this.getOne(wrapper);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdateTime(new Date());
            this.updateById(existing);
            return Result.succ("已更新数量");
        }
        Cart cart = new Cart();
        cart.setCartId(IdGenerator.generateId(IdGenerator.CART));
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setQuantity(quantity);
        cart.setCreateTime(new Date());
        cart.setUpdateTime(new Date());
        this.save(cart);
        return Result.succ("已加入购物车");
    }

    @Override
    public Result getMyCart(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Cart> cartList = this.list(wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Cart cart : cartList) {
            Map<String, Object> item = new HashMap<>();
            item.put("cartId", cart.getCartId());
            item.put("goodsId", cart.getGoodsId());
            item.put("quantity", cart.getQuantity());
            item.put("createTime", cart.getCreateTime());
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods != null) {
                item.put("goodsName", goods.getGoodsName());
                item.put("goodsPrice", goods.getGoodsPrice());
                item.put("goodsImages", goods.getGoodsImages());
                item.put("goodsCount", goods.getGoodsCount());
                item.put("goodsStatus", goods.getGoodsStatus());
                item.put("locked", goods.getLocked());
                item.put("sellerId", goods.getUserId());
                User seller = userMapper.selectById(goods.getUserId());
                item.put("sellerNickname", seller != null ? seller.getNickname() : "未知");
            } else {
                item.put("goodsName", "商品已删除");
                item.put("goodsDeleted", true);
            }
            records.add(item);
        }
        return Result.succ(records);
    }

    @Override
    public Result updateQuantity(String cartId, Integer quantity, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) return Result.fail("购物车记录不存在");
        cart.setQuantity(quantity);
        cart.setUpdateTime(new Date());
        this.updateById(cart);
        return Result.succ("更新成功");
    }

    @Override
    public Result removeFromCart(String cartIds, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        String[] ids = cartIds.split(",");
        for (String id : ids) {
            Cart cart = this.getById(id.trim());
            if (cart != null && cart.getUserId().equals(userId)) {
                this.removeById(id.trim());
            }
        }
        return Result.succ("删除成功");
    }

    @Override
    public Result clearCart(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        this.remove(wrapper);
        return Result.succ("购物车已清空");
    }
}
