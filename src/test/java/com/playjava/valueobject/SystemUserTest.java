package com.playjava.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemUser テスト")
class SystemUserTest {

    @Test
    @DisplayName("SYSTEM定数が正しいUUID形式であること")
    void testSystemConstant() {
        // Given & When: SYSTEM定数を取得

        // Then: 正しいUUID形式であること
        assertEquals("00000000-0000-0000-0000-000000000001", SystemUser.SYSTEM, 
            "SYSTEMが正しい値であること");
        assertTrue(SystemUser.SYSTEM.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"), 
            "SYSTEMがUUID形式であること");
    }

    @Test
    @DisplayName("ANONYMOUS定数が正しいUUID形式であること")
    void testAnonymousConstant() {
        // Given & When: ANONYMOUS定数を取得

        // Then: 正しいUUID形式であること
        assertEquals("00000000-0000-0000-0000-000000000002", SystemUser.ANONYMOUS, 
            "ANONYMOUSが正しい値であること");
        assertTrue(SystemUser.ANONYMOUS.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"), 
            "ANONYMOUSがUUID形式であること");
    }

    @Test
    @DisplayName("BOOTSTRAP定数が正しいUUID形式であること")
    void testBootstrapConstant() {
        // Given & When: BOOTSTRAP定数を取得

        // Then: 正しいUUID形式であること
        assertEquals("00000000-0000-0000-0000-000000000003", SystemUser.BOOTSTRAP, 
            "BOOTSTRAPが正しい値であること");
        assertTrue(SystemUser.BOOTSTRAP.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"), 
            "BOOTSTRAPがUUID形式であること");
    }

    @Test
    @DisplayName("3つの定数が全て異なる値であること")
    void testConstantsAreUnique() {
        // Given & When: 3つの定数を取得

        // Then: 全て異なる値であること
        assertNotEquals(SystemUser.SYSTEM, SystemUser.ANONYMOUS, 
            "SYSTEMとANONYMOUSが異なること");
        assertNotEquals(SystemUser.SYSTEM, SystemUser.BOOTSTRAP, 
            "SYSTEMとBOOTSTRAPが異なること");
        assertNotEquals(SystemUser.ANONYMOUS, SystemUser.BOOTSTRAP, 
            "ANONYMOUSとBOOTSTRAPが異なること");
    }

    @Test
    @DisplayName("isSystemUser - SYSTEMの場合trueを返すこと")
    void testIsSystemUser_System() {
        // Given: SYSTEMユーザーID

        // When & Then: trueが返ること
        assertTrue(SystemUser.isSystemUser(SystemUser.SYSTEM), 
            "SYSTEMの場合trueを返すこと");
    }

    @Test
    @DisplayName("isSystemUser - ANONYMOUSの場合trueを返すこと")
    void testIsSystemUser_Anonymous() {
        // Given: ANONYMOUSユーザーID

        // When & Then: trueが返ること
        assertTrue(SystemUser.isSystemUser(SystemUser.ANONYMOUS), 
            "ANONYMOUSの場合trueを返すこと");
    }

    @Test
    @DisplayName("isSystemUser - BOOTSTRAPの場合trueを返すこと")
    void testIsSystemUser_Bootstrap() {
        // Given: BOOTSTRAPユーザーID

        // When & Then: trueが返ること
        assertTrue(SystemUser.isSystemUser(SystemUser.BOOTSTRAP), 
            "BOOTSTRAPの場合trueを返すこと");
    }

    @Test
    @DisplayName("isSystemUser - 一般ユーザーIDの場合falseを返すこと")
    void testIsSystemUser_RegularUser() {
        // Given: 一般ユーザーID
        String regularUserId = "12345678-1234-1234-1234-123456789abc";

        // When & Then: falseが返ること
        assertFalse(SystemUser.isSystemUser(regularUserId), 
            "一般ユーザーIDの場合falseを返すこと");
    }

    @Test
    @DisplayName("isSystemUser - nullの場合falseを返すこと")
    void testIsSystemUser_Null() {
        // Given: null

        // When & Then: falseが返ること
        assertFalse(SystemUser.isSystemUser(null), 
            "nullの場合falseを返すこと");
    }

    @Test
    @DisplayName("isSystemUser - 空文字の場合falseを返すこと")
    void testIsSystemUser_Empty() {
        // Given: 空文字

        // When & Then: falseが返ること
        assertFalse(SystemUser.isSystemUser(""), 
            "空文字の場合falseを返すこと");
    }
}
