package com.playjava.handler;

import com.playjava.context.UserContext;
import com.playjava.entity.MUser;
import com.playjava.valueobject.SystemUser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FillMetaObjectHandler テスト")
class FillMetaObjectHandlerTest {

    private FillMetaObjectHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FillMetaObjectHandler();
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("insertFill - createUserとupdateUserが設定されること")
    void testInsertFill_SetUsers() {
        // Given: テストエンティティとUserContextを準備
        MUser user = new MUser();
        MetaObject metaObject = SystemMetaObject.forObject(user);
        UserContext.setCurrentUserId(SystemUser.BOOTSTRAP);

        // When: insertFillを実行
        handler.insertFill(metaObject);

        // Then: createUserとupdateUserが設定されること
        assertEquals(SystemUser.BOOTSTRAP, user.getCreateUser(), 
            "createUserがBOOTSTRAPで設定されること");
        assertEquals(SystemUser.BOOTSTRAP, user.getUpdateUser(), 
            "updateUserがBOOTSTRAPで設定されること");
    }

    @Test
    @DisplayName("insertFill - createDateとupdateDateが設定されること")
    void testInsertFill_SetDates() {
        // Given: テストエンティティを準備
        MUser user = new MUser();
        MetaObject metaObject = SystemMetaObject.forObject(user);
        UserContext.setCurrentUserId(SystemUser.SYSTEM);
        OffsetDateTime before = OffsetDateTime.now();

        // When: insertFillを実行
        handler.insertFill(metaObject);
        OffsetDateTime after = OffsetDateTime.now();

        // Then: createDateとupdateDateが設定されること
        assertNotNull(user.getCreateDate(), "createDateが設定されること");
        assertNotNull(user.getUpdateDate(), "updateDateが設定されること");
        
        // 時刻が妥当な範囲内であること
        assertTrue(user.getCreateDate().isAfter(before.minusSeconds(1)), 
            "createDateが妥当な時刻であること");
        assertTrue(user.getCreateDate().isBefore(after.plusSeconds(1)), 
            "createDateが妥当な時刻であること");
    }

    @Test
    @DisplayName("insertFill - UserContextが未設定の場合ANONYMOUSが使われること")
    void testInsertFill_DefaultAnonymous() {
        // Given: UserContextを設定しない（クリア済み）
        MUser user = new MUser();
        MetaObject metaObject = SystemMetaObject.forObject(user);

        // When: insertFillを実行
        handler.insertFill(metaObject);

        // Then: ANONYMOUSが使われること
        assertEquals(SystemUser.ANONYMOUS, user.getCreateUser(), 
            "createUserがANONYMOUSで設定されること");
        assertEquals(SystemUser.ANONYMOUS, user.getUpdateUser(), 
            "updateUserがANONYMOUSで設定されること");
    }

    @Test
    @DisplayName("insertFill - 一般ユーザーIDが使われること")
    void testInsertFill_RegularUserId() {
        // Given: 一般ユーザーIDを設定
        String regularUserId = "12345678-1234-1234-1234-123456789abc";
        MUser user = new MUser();
        MetaObject metaObject = SystemMetaObject.forObject(user);
        UserContext.setCurrentUserId(regularUserId);

        // When: insertFillを実行
        handler.insertFill(metaObject);

        // Then: 設定したユーザーIDが使われること
        assertEquals(regularUserId, user.getCreateUser(), 
            "createUserが設定したユーザーIDで設定されること");
        assertEquals(regularUserId, user.getUpdateUser(), 
            "updateUserが設定したユーザーIDで設定されること");
    }

    @Test
    @DisplayName("updateFill - updateUserとupdateDateが設定されること")
    void testUpdateFill_SetUserAndDate() {
        // Given: テストエンティティとUserContextを準備
        MUser user = new MUser();
        user.setUserId("test-user-id");
        user.setUserName("testuser");
        user.setCreateUser(SystemUser.BOOTSTRAP);
        user.setCreateDate(OffsetDateTime.now().minusDays(1));
        
        MetaObject metaObject = SystemMetaObject.forObject(user);
        String updateUserId = "updater-user-id";
        UserContext.setCurrentUserId(updateUserId);
        OffsetDateTime before = OffsetDateTime.now();

        // When: updateFillを実行
        handler.updateFill(metaObject);
        OffsetDateTime after = OffsetDateTime.now();

        // Then: updateUserとupdateDateが設定されること
        assertEquals(updateUserId, user.getUpdateUser(), 
            "updateUserが設定されること");
        assertNotNull(user.getUpdateDate(), "updateDateが設定されること");
        
        // createUserとcreateDateは変更されないこと
        assertEquals(SystemUser.BOOTSTRAP, user.getCreateUser(), 
            "createUserは変更されないこと");
        
        // updateDateが妥当な範囲内であること
        assertTrue(user.getUpdateDate().isAfter(before.minusSeconds(1)), 
            "updateDateが妥当な時刻であること");
        assertTrue(user.getUpdateDate().isBefore(after.plusSeconds(1)), 
            "updateDateが妥当な時刻であること");
    }

    @Test
    @DisplayName("updateFill - UserContextが未設定の場合ANONYMOUSが使われること")
    void testUpdateFill_DefaultAnonymous() {
        // Given: UserContextを設定しない（クリア済み）
        MUser user = new MUser();
        user.setUserId("test-user-id");
        user.setCreateUser(SystemUser.SYSTEM);
        user.setCreateDate(OffsetDateTime.now().minusDays(1));
        
        MetaObject metaObject = SystemMetaObject.forObject(user);

        // When: updateFillを実行
        handler.updateFill(metaObject);

        // Then: ANONYMOUSが使われること
        assertEquals(SystemUser.ANONYMOUS, user.getUpdateUser(), 
            "updateUserがANONYMOUSで設定されること");
    }

    @Test
    @DisplayName("複数回insertFillを呼び出しても正常に動作すること")
    void testInsertFill_Multiple() {
        // Given: 複数のエンティティを準備
        MUser user1 = new MUser();
        MUser user2 = new MUser();
        MetaObject metaObject1 = SystemMetaObject.forObject(user1);
        MetaObject metaObject2 = SystemMetaObject.forObject(user2);
        
        UserContext.setCurrentUserId(SystemUser.BOOTSTRAP);

        // When: 複数回insertFillを実行
        handler.insertFill(metaObject1);
        handler.insertFill(metaObject2);

        // Then: 両方とも正常に設定されること
        assertEquals(SystemUser.BOOTSTRAP, user1.getCreateUser(), 
            "user1のcreateUserが設定されていること");
        assertEquals(SystemUser.BOOTSTRAP, user2.getCreateUser(), 
            "user2のcreateUserが設定されていること");
        assertNotNull(user1.getCreateDate(), "user1のcreateDateが設定されていること");
        assertNotNull(user2.getCreateDate(), "user2のcreateDateが設定されていること");
    }
}
