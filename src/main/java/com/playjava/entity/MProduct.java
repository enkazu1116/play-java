package com.playjava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("m_product")
public class MProduct {
    @TableId(type = IdType.ASSIGN_UUID)
    private String productId;
    private String productNumber;
    private String productName;
    private String description;
    private Integer price;
    private Integer category;

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
