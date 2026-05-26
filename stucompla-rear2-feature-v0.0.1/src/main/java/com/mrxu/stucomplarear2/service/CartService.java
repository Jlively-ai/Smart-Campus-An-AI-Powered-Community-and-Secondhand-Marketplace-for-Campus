package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.Cart;
import com.mrxu.stucomplarear2.utils.response.Result;
import javax.servlet.http.HttpServletRequest;

public interface CartService extends IService<Cart> {
    Result addToCart(String goodsId, Integer quantity, HttpServletRequest request);
    Result getMyCart(HttpServletRequest request);
    Result updateQuantity(String cartId, Integer quantity, HttpServletRequest request);
    Result removeFromCart(String cartIds, HttpServletRequest request);
    Result clearCart(HttpServletRequest request);
}
