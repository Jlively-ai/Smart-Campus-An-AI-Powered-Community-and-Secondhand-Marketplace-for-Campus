package com.mrxu.stucomplarear2.controller;


import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.service.CategoryService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("种类列表")
    @GetMapping("/list")
    public Result listCategories() {
        return categoryService.listCategories();
    }

    @ApiOperation("添加帖子分类")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @PostMapping("/add")
    public Result add(String categoryName) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        categoryService.save(category);
        return Result.succ("success");
    }

    @ApiOperation("删除帖子分类")
    @RequiresRoles(value = {"admin", "super"}, logical = Logical.OR)
    @DeleteMapping("/delete")
    public Result delete(Integer categoryId) {
        categoryService.removeById(categoryId);
        return Result.succ("success");
    }
}
