package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class GoodsFindDto {

    /**
     * 二手商品id
     */
    private String goodsId;

    /**
     * 查询关键词
     */
    private String keyName;

    /**
     * 二手商品分类id
     */
    private Integer goodsCategoryId;

    /**
     * 二手商品上架状态：0-下架 1-上架
     */
    private Boolean goodsStatus;

    
    /**
     * 发布人id
     */
    private String userId;

    /**
     * 当前页
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;

    private String sort;

    private String sortBy;

    private String sortOrder;

    /**
     * 审核状态：0-待审 1-审核通过 2-未通过
     */
    private Integer auditState;

    /**
     * 卖家昵称（模糊搜索）
     */
    private String nickname;

}
