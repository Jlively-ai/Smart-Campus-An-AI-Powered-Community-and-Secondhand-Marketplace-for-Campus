package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.mapper.CategoryMapper;
import com.mrxu.stucomplarear2.service.CategoryService;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getAllCategories() {
        return this.list();
    }

    @Override
    public Result listCategories() {
        List<Category> list = this.list();
        return Result.succ(list);
    }

    @Override
    public Result addCategory(Category category) {
        this.save(category);
        return Result.succ("添加成功");
    }

    @Override
    public Result updateCategory(Category category) {
        this.updateById(category);
        return Result.succ("修改成功");
    }

    @Override
    public Result deleteCategory(Integer categoryId) {
        this.removeById(categoryId);
        return Result.succ("删除成功");
    }
}
