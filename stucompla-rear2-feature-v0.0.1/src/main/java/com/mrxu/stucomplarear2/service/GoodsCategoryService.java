package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.GoodsCategoryDto;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.utils.response.Result;

import java.util.List;

public interface GoodsCategoryService extends IService<GoodsCategory> {

    List<GoodsCategory> getAllGoodsCategories();

    Result getGoodsCategoryList(GoodsCategoryDto goodsCategoryDto);

    Result addGoodsCategory(GoodsCategory goodsCategory);

    Result updateGoodsCategory(GoodsCategory goodsCategory);

    Result deleteGoodsCategory(Integer goodsCategoryId);
}
