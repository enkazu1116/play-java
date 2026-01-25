package com.playjava.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("t_order")
public class TOrder {
    @TableId(type = IdType.ASSIGN_UUID)
    private String orderId;
    private String customerId;
    private OffsetDateTime orderDate;
    private Integer status;

    // 論理削除の設定
    @TableLogic(value = "false", delval = "true")
    private Boolean deleteFlag;

    // 自動設定
    @TableField(fill = FieldFill.INSERT)
    private String createUser;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createDate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateDate;
}
