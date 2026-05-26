package com.mrxu.stucomplarear2.controller;

import com.mrxu.stucomplarear2.dto.GoodsCategoryDto;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.service.GoodsCategoryService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods-category")
public class GoodsCategoryController {
    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @ApiOperation("获取二手商品分类列表")
    @GetMapping("/list")
    public Result list(GoodsCategoryDto goodsCategoryDto) {
        return goodsCategoryService.getGoodsCategoryList(goodsCategoryDto);
    }

    @ApiOperation("添加商品分类")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/add")
    public Result add(String goodsCategoryName) {
        GoodsCategory category = new GoodsCategory();
        category.setGoodsCategoryName(goodsCategoryName);
        goodsCategoryService.save(category);
        return Result.succ("success");
    }

    @ApiOperation("删除商品分类")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/delete")
    public Result delete(Integer goodsCategoryId) {
        goodsCategoryService.removeById(goodsCategoryId);
        return Result.succ("success");
    }
}
