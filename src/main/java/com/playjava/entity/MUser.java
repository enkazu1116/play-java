package com.playjava.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("m_user")
public class MUser {
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;
    private String loginId;
    private String userName;
    private String email;
    private String passwordHash;
    private String role;
    private String status;
    private OffsetDateTime lastLoginAt;

    // 論理削除の設定
    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;

    // 自動設定
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
