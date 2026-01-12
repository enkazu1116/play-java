package com.playjava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("m_stock")
public class MStock {
    @TableId(type = IdType.ASSIGN_UUID)
    private String stockId;
    private String productId;
    private Integer quantity;
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
