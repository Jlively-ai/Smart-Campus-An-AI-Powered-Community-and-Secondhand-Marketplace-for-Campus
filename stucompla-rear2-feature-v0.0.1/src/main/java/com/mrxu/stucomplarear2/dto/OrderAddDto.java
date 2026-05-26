package com.mrxu.stucomplarear2.dto;

import lombok.Data;

@Data
public class OrderAddDto {
    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 购买数量
     */
    private Integer buyCount;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;
}
