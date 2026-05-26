package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.utils.response.Result;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> getAllCategories();

    Result listCategories();

    Result addCategory(Category category);

    Result updateCategory(Category category);

    Result deleteCategory(Integer categoryId);
}
