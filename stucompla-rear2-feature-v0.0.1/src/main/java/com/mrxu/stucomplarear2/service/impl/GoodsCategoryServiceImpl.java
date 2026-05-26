package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.dto.GoodsCategoryDto;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.mapper.GoodsCategoryMapper;
import com.mrxu.stucomplarear2.service.GoodsCategoryService;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    @Override
    public List<GoodsCategory> getAllGoodsCategories() {
        return this.list();
    }

    @Override
    public Result getGoodsCategoryList(GoodsCategoryDto goodsCategoryDto) {
        if (goodsCategoryDto.getPageNum() == null || goodsCategoryDto.getPageSize() == null) {
            QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
            if (goodsCategoryDto.getCategoryName() != null && !goodsCategoryDto.getCategoryName().isEmpty()) {
                queryWrapper.like("goods_category_name", goodsCategoryDto.getCategoryName());
            }
            queryWrapper.orderByAsc("goods_category_id");
            return Result.succ(this.list(queryWrapper));
        }
        Page<GoodsCategory> page = new Page<>(goodsCategoryDto.getPageNum(), goodsCategoryDto.getPageSize());
        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
        if (goodsCategoryDto.getCategoryName() != null && !goodsCategoryDto.getCategoryName().isEmpty()) {
            queryWrapper.like("goods_category_name", goodsCategoryDto.getCategoryName());
        }
        queryWrapper.orderByAsc("goods_category_id");
        Page<GoodsCategory> categoryPage = this.page(page, queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("total", categoryPage.getTotal());
        map.put("records", categoryPage.getRecords());
        return Result.succ(map);
    }

    @Override
    public Result addGoodsCategory(GoodsCategory goodsCategory) {
        this.save(goodsCategory);
        return Result.succ("success");
    }

    @Override
    public Result updateGoodsCategory(GoodsCategory goodsCategory) {
        this.updateById(goodsCategory);
        return Result.succ("success");
    }

    @Override
    public Result deleteGoodsCategory(Integer goodsCategoryId) {
        this.removeById(goodsCategoryId);
        return Result.succ("success");
    }
}
