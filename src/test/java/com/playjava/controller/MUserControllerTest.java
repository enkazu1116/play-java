package com.playjava.controller;

import com.playjava.context.UserContext;
import com.playjava.entity.MUser;
import com.playjava.service.impl.MUserServiceImpl;
import com.playjava.valueobject.SystemUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("MUserController テスト")
class MUserControllerTest {

    @Autowired
    private MUserController mUserController;

    @Autowired
    private MUserServiceImpl mUserService;

    @BeforeEach
    void setUp() {
        // 各テストの前にUserContextをクリア
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        // 各テストの後にUserContextをクリア
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /api/v1/users/createUser - ユーザー作成が成功すること")
    void testCreateUser_Success() {
        // Given: リクエストボディを準備
        MUser user = new MUser();
        user.setUserName("apitest");
        user.setPassword("apipassword");
        user.setRole(1);

        // When: コントローラーを直接呼び出し
        assertDoesNotThrow(() -> mUserController.createUser(user), 
            "ユーザー作成が例外をスローしないこと");
        
        // Then: ユーザーIDが設定されていること
        assertNotNull(user.getUserId(), "userIdが設定されていること");
    }

    @Test
    @DisplayName("リクエスト完了後にUserContextがクリアされること")
    void testCreateUser_ClearUserContext() {
        // Given: リクエストボディを準備
        MUser user = new MUser();
        user.setUserName("contexttest");
        user.setPassword("contextpass");
        user.setRole(2);

        // When: コントローラーを呼び出す
        mUserController.createUser(user);

        // Then: リクエスト完了後、UserContextがクリアされていること
        // （未設定の場合はANONYMOUSが返る）
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId,
            "リクエスト完了後、UserContextがクリアされてANONYMOUSが返ること");
    }

    @Test
    @DisplayName("作成したユーザーがデータベースに保存されること")
    void testCreateUser_SaveToDatabase() {
        // Given: リクエストボディを準備
        MUser user = new MUser();
        user.setUserName("dbtest");
        user.setPassword("dbpassword");
        user.setRole(1);

        // When: コントローラーを呼び出す
        mUserController.createUser(user);

        // Then: データベースから取得できること
        assertNotNull(user.getUserId(), "userIdが設定されていること");
        
        MUser savedUser = mUserService.getById(user.getUserId());
        assertNotNull(savedUser, "データベースからユーザーが取得できること");
        assertEquals("dbtest", savedUser.getUserName(), "ユーザー名が一致すること");
    }

    @Test
    @DisplayName("必須フィールドが設定されていない場合は例外が発生すること")
    void testCreateUser_MissingFields() {
        // Given: 空のリクエストボディ（必須フィールドが未設定）
        MUser user = new MUser();
        // userName, password, role を設定しない

        // When & Then: コントローラーを呼び出すと例外が発生すること
        // データベースのNOT NULL制約により、DataIntegrityViolationExceptionが発生する
        assertThrows(Exception.class, () -> mUserController.createUser(user), 
            "必須フィールドが未設定の場合、例外が発生すること");
    }

    @Test
    @DisplayName("複数のリクエストを連続で処理できること")
    void testCreateUser_MultipleRequests() {
        // Given: 複数のリクエストを準備
        MUser user1 = new MUser();
        user1.setUserName("multi1");
        user1.setPassword("pass1");
        user1.setRole(1);

        MUser user2 = new MUser();
        user2.setUserName("multi2");
        user2.setPassword("pass2");
        user2.setRole(2);

        // When: 複数のリクエストを連続で処理
        mUserController.createUser(user1);
        mUserController.createUser(user2);

        // Then: 両方のユーザーが作成されていること
        assertNotNull(user1.getUserId(), "user1のuserIdが設定されていること");
        assertNotNull(user2.getUserId(), "user2のuserIdが設定されていること");
        assertNotEquals(user1.getUserId(), user2.getUserId(), 
            "異なるuserIdが生成されていること");
    }
}
