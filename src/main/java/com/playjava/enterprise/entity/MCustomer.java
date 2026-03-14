package com.playjava.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@TableName("m_customer")
public class MCustomer {
    @TableId(type = IdType.ASSIGN_UUID)
    private UUID customerId;
    private String customerNumber;
    
    @NotBlank(message = "顧客名は必須です")
    @Size(max = 50, message = "顧客名は50文字以内で入力してください")
    private String customerName;
    
    @Size(max = 100, message = "住所は100文字以内で入力してください")
    private String address;
    
    @Pattern(regexp = "^$|^(\\+81[- ]?)?0[789]0[- ]?\\d{4}[- ]?\\d{4}$", message = "携帯番号の形式が正しくありません（090、080、070で始まる11桁の番号、ハイフン付き、ハイフンなし、+81などの国際形式が利用可能です）")
    @Size(max = 20, message = "携帯番号は20文字以内で入力してください")
    @TableField("phone_number")
    private String mobileNumber;
    
    @Email(message = "メールアドレスの形式が正しくありません")
    @Size(max = 50, message = "メールアドレスは50文字以内で入力してください")
    private String email;

    // 論理削除の設定
    @TableLogic(value = "false", delval = "true")
    private Boolean deleteFlag;

    // 自動設定
    @TableField(fill = FieldFill.INSERT)
    private UUID createUser;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createDate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private UUID updateUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateDate;
}
