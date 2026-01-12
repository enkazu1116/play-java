package com.playjava.service;

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
@Transactional // テスト後にロールバック
@DisplayName("MUserServiceImpl テスト")
class MUserServiceImplTest {

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
    @DisplayName("ユーザー作成が成功すること")
    void testCreateUser_Success() {
        // Given: テストユーザーを準備
        MUser user = new MUser();
        user.setUserName("testuser");
        user.setPassword("password123");
        user.setRole(1);

        // When: ユーザーを作成
        boolean result = mUserService.createUserImpl(user);

        // Then: 作成が成功すること
        assertTrue(result, "ユーザー作成が成功すること");
        assertNotNull(user.getUserId(), "userIdが設定されていること");
        assertEquals("testuser", user.getUserName(), "ユーザー名が正しく設定されていること");
        assertEquals(1, user.getRole(), "ロールが正しく設定されていること");
        assertFalse(user.getDeleteFlag(), "削除フラグがfalseであること");
    }

    @Test
    @DisplayName("createUserとupdateUserにBOOTSTRAPが設定されること")
    void testCreateUser_SetBootstrapUser() {
        // Given: テストユーザーを準備
        MUser user = new MUser();
        user.setUserName("testuser2");
        user.setPassword("password456");
        user.setRole(2);

        // When: ユーザーを作成
        mUserService.createUserImpl(user);

        // Then: createUserとupdateUserがBOOTSTRAPで設定されていること
        assertNotNull(user.getCreateUser(), "createUserが設定されていること");
        assertNotNull(user.getUpdateUser(), "updateUserが設定されていること");
        assertEquals(SystemUser.BOOTSTRAP, user.getCreateUser(), 
            "createUserがBOOTSTRAPで設定されていること");
        assertEquals(SystemUser.BOOTSTRAP, user.getUpdateUser(), 
            "updateUserがBOOTSTRAPで設定されていること");
    }

    @Test
    @DisplayName("createDateとupdateDateが自動設定されること")
    void testCreateUser_SetDates() {
        // Given: テストユーザーを準備
        MUser user = new MUser();
        user.setUserName("testuser3");
        user.setPassword("password789");
        user.setRole(1);

        // When: ユーザーを作成
        mUserService.createUserImpl(user);

        // Then: 日時が自動設定されていること
        assertNotNull(user.getCreateDate(), "createDateが設定されていること");
        assertNotNull(user.getUpdateDate(), "updateDateが設定されていること");
    }

    @Test
    @DisplayName("userIdがUUID形式で生成されること")
    void testCreateUser_GenerateUUID() {
        // Given: テストユーザーを準備
        MUser user = new MUser();
        user.setUserName("testuser4");
        user.setPassword("password000");
        user.setRole(1);

        // When: ユーザーを作成
        mUserService.createUserImpl(user);

        // Then: userIdがUUID形式であること
        assertNotNull(user.getUserId(), "userIdが設定されていること");
        // UUID形式（8-4-4-4-12）かどうかをチェック
        String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        assertTrue(user.getUserId().matches(uuidPattern), 
            "userIdがUUID形式であること");
    }

    @Test
    @DisplayName("作成後にUserContextにuserIdが設定されること")
    void testCreateUser_SetUserContext() {
        // Given: テストユーザーを準備
        MUser user = new MUser();
        user.setUserName("testuser5");
        user.setPassword("password111");
        user.setRole(1);

        // When: ユーザーを作成
        mUserService.createUserImpl(user);

        // Then: UserContextにuserIdが設定されていること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(user.getUserId(), currentUserId, 
            "UserContextに作成したuserIdが設定されていること");
    }

    @Test
    @DisplayName("複数のユーザーを連続で作成できること")
    void testCreateUser_Multiple() {
        // Given: 複数のテストユーザーを準備
        MUser user1 = new MUser();
        user1.setUserName("user1");
        user1.setPassword("pass1");
        user1.setRole(1);

        MUser user2 = new MUser();
        user2.setUserName("user2");
        user2.setPassword("pass2");
        user2.setRole(2);

        // When: 複数のユーザーを作成
        UserContext.clear(); // 最初にクリア
        boolean result1 = mUserService.createUserImpl(user1);
        
        UserContext.clear(); // 2人目の前にクリア
        boolean result2 = mUserService.createUserImpl(user2);

        // Then: 両方とも作成が成功し、異なるuserIdを持つこと
        assertTrue(result1, "ユーザー1が作成されること");
        assertTrue(result2, "ユーザー2が作成されること");
        assertNotEquals(user1.getUserId(), user2.getUserId(), 
            "異なるuserIdが生成されること");
    }
}
