package com.playjava.enterprise.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MUser バリデーションテスト
 */
@DisplayName("MUser バリデーション テスト")
class MUserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("全ての項目が正しい場合、バリデーションエラーがないこと")
    void testValidUser_Success() {
        // Given: 正しいユーザー情報
        MUser user = new MUser();
        user.setUserName("testuser");
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーがないこと
        assertTrue(violations.isEmpty(), "バリデーションエラーがないこと");
    }

    @Test
    @DisplayName("ユーザー名がnullの場合、バリデーションエラーになること")
    void testValidation_UserNameNull() {
        // Given: ユーザー名がnull
        MUser user = new MUser();
        user.setUserName(null);
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーが発生すること
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
        assertEquals(1, violations.size(), "エラーが1件であること");
        assertTrue(violations.iterator().next().getMessage().contains("必須"),
            "ユーザー名が必須であるメッセージが含まれること");
    }

    @Test
    @DisplayName("ユーザー名が空文字の場合、バリデーションエラーになること")
    void testValidation_UserNameEmpty() {
        // Given: ユーザー名が空文字
        MUser user = new MUser();
        user.setUserName("");
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーが発生すること
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
    }

    @Test
    @DisplayName("ユーザー名が3文字未満の場合、バリデーションエラーになること")
    void testValidation_UserNameTooShort() {
        // Given: ユーザー名が2文字
        MUser user = new MUser();
        user.setUserName("ab");
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーが発生すること
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
        assertTrue(violations.iterator().next().getMessage().contains("3文字以上"),
            "3文字以上であるメッセージが含まれること");
    }

    @Test
    @DisplayName("ユーザー名が50文字を超える場合、バリデーションエラーになること")
    void testValidation_UserNameTooLong() {
        // Given: ユーザー名が51文字
        MUser user = new MUser();
        user.setUserName("a".repeat(51));
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーが発生すること
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
        assertTrue(violations.iterator().next().getMessage().contains("50文字以内"),
            "50文字以内であるメッセージが含まれること");
    }

    @Test
    @DisplayName("パスワードがnullの場合、バリデーションエラーになること")
    void testValidation_PasswordNull() {
        // Given: パスワードがnull
        MUser user = new MUser();
        user.setUserName("testuser");
        user.setPassword(null);
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーが発生すること
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
    }

    @Test
    @DisplayName("パスワードが8文字未満の場合、バリデーションエラーになること")
    void testValidation_PasswordTooShort() {
        // Given: パスワードが7文字
        MUser user = new MUser();
        user.setUserName("testuser");
        user.setPassword("pass123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーが発生すること
        assertFalse(violations.isEmpty(), "バリデーションエラーが発生すること");
        assertTrue(violations.iterator().next().getMessage().contains("8文字以上"),
            "8文字以上であるメッセージが含まれること");
    }


    @Test
    @DisplayName("複数のフィールドでバリデーションエラーが発生すること")
    void testValidation_MultipleErrors() {
        // Given: 複数のフィールドが不正
        MUser user = new MUser();
        user.setUserName("ab");  // 3文字未満
        user.setPassword("pass");  // 8文字未満

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: 複数のエラーが発生すること
        assertTrue(violations.size() >= 2, "複数のバリデーションエラーが発生すること");
    }

    @Test
    @DisplayName("境界値：ユーザー名が3文字の場合、バリデーションエラーがないこと")
    void testValidation_UserNameMinLength() {
        // Given: ユーザー名が3文字（最小値）
        MUser user = new MUser();
        user.setUserName("abc");
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーがないこと
        assertTrue(violations.isEmpty(), "バリデーションエラーがないこと");
    }

    @Test
    @DisplayName("境界値：ユーザー名が50文字の場合、バリデーションエラーがないこと")
    void testValidation_UserNameMaxLength() {
        // Given: ユーザー名が50文字（最大値）
        MUser user = new MUser();
        user.setUserName("a".repeat(50));
        user.setPassword("password123");
        user.setRole(1);

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーがないこと
        assertTrue(violations.isEmpty(), "バリデーションエラーがないこと");
    }

    @Test
    @DisplayName("境界値：パスワードが8文字の場合、バリデーションエラーがないこと")
    void testValidation_PasswordMinLength() {
        // Given: パスワードが8文字（最小値）
        MUser user = new MUser();
        user.setUserName("testuser");
        user.setPassword("pass1234");

        // When: バリデーションを実行
        Set<ConstraintViolation<MUser>> violations = validator.validate(user);

        // Then: エラーがないこと
        assertTrue(violations.isEmpty(), "バリデーションエラーがないこと");
    }
}
