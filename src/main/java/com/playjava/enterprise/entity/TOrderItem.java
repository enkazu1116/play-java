package com.playjava.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("t_order_item")
public class TOrderItem {
    @TableId(type = IdType.ASSIGN_UUID)
    private String orderItemId;
    private String orderId;
    private String productId;
    private Integer quantity;
    private Integer unitPrice;

    // 自動設定（注文明細には論理削除フラグとユーザー情報がない）
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createDate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateDate;
}
