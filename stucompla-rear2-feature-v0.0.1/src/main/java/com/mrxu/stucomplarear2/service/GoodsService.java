package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.dto.GoodsAddDto;
import com.mrxu.stucomplarear2.dto.GoodsEditDto;
import com.mrxu.stucomplarear2.dto.GoodsFindDto;
import com.mrxu.stucomplarear2.dto.GoodsAuditDto;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.utils.response.Result;

import javax.servlet.http.HttpServletRequest;

public interface GoodsService extends IService<Goods> {

    Result add(GoodsAddDto goodsDto, HttpServletRequest request);

    Result editGoods(GoodsEditDto goodsEditDto, HttpServletRequest request);

    Result deleteMyGoods(String goodsId, HttpServletRequest request);

    Result putMyGoods(String goodsId, HttpServletRequest request);

    Result unShelveMyGoods(String goodsId, HttpServletRequest request);

    Result unShelveGoods(String goodsId);

    Result deleteGoods(String goodsId, String cause);

    Result findGoods(GoodsFindDto goodsFindDto, HttpServletRequest request);

    Result getGoodsTotal();

    Result getGoodsByCategory();

    Goods updateViewNum(Goods goods);

    Result auditGoods(GoodsAuditDto auditDto, HttpServletRequest request);
}
