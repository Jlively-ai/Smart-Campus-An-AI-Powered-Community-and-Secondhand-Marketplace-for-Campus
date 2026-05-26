package com.mrxu.stucomplarear2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("cart")
public class Cart {
    @TableId(type = IdType.ASSIGN_ID)
    private String cartId;
    private String userId;
    private String goodsId;
    private Integer quantity;
    private Date createTime;
    private Date updateTime;
}
