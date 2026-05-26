package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.service.CartService;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/add")
    public Result addToCart(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        String goodsId = (String) body.get("goodsId");
        Integer quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : 1;
        return cartService.addToCart(goodsId, quantity, request);
    }

    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @GetMapping("/list")
    public Result getMyCart(HttpServletRequest request) {
        return cartService.getMyCart(request);
    }

    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @PostMapping("/update")
    public Result updateQuantity(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        String cartId = (String) body.get("cartId");
        Integer quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : 1;
        return cartService.updateQuantity(cartId, quantity, request);
    }

    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/remove/{cartIds}")
    public Result removeFromCart(@PathVariable String cartIds, HttpServletRequest request) {
        return cartService.removeFromCart(cartIds, request);
    }

    @RequiresRoles(value = {"user", "admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/clear")
    public Result clearCart(HttpServletRequest request) {
        return cartService.clearCart(request);
    }
}
