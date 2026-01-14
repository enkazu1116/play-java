package com.playjava.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Data
@TableName("m_user")
@Schema(description = "ユーザーマスタ")
public class MUser {
    @Schema(description = "ユーザーID（UUID自動生成）", example = "550e8400-e29b-41d4-a716-446655440000", accessMode = Schema.AccessMode.READ_ONLY)
    @TableId(type = IdType.ASSIGN_UUID)
    private String userId;
    
    @Schema(description = "ユーザー名", example = "yamada_taro", minLength = 3, maxLength = 50)
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以内で入力してください")
    private String userName;
    
    @Schema(description = "パスワード", example = "password123", minLength = 8, maxLength = 100)
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;
    
    @Schema(description = "ロール（0:一般, 1:管理者）", example = "0", allowableValues = {"0", "1"})
    private Integer role;

    // 論理削除の設定
    @Schema(description = "削除フラグ", accessMode = Schema.AccessMode.READ_ONLY)
    @TableLogic(value = "false", delval = "true")
    private Boolean deleteFlag;

    // 自動設定
    @Schema(description = "作成者ID", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField(fill = FieldFill.INSERT)
    private String createUser;
    
    @Schema(description = "作成日時", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createDate;
    
    @Schema(description = "更新者ID", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;
    
    @Schema(description = "更新日時", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateDate;
}
