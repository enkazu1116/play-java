package com.playjava.context;

import com.playjava.valueobject.SystemUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserContext テスト")
class UserContextTest {

    @BeforeEach
    void setUp() {
        // 各テストの前にクリア
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        // 各テストの後にクリア
        UserContext.clear();
    }

    @Test
    @DisplayName("ユーザーIDを設定して取得できること")
    void testSetAndGetCurrentUserId() {
        // Given: テストユーザーID
        String testUserId = "test-user-id-123";

        // When: ユーザーIDを設定
        UserContext.setCurrentUserId(testUserId);

        // Then: 設定したユーザーIDが取得できること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(testUserId, currentUserId, "設定したユーザーIDが取得できること");
    }

    @Test
    @DisplayName("未設定の場合はANONYMOUSが返ること")
    void testGetCurrentUserId_Default() {
        // Given: ユーザーIDを設定しない（クリア済み）

        // When: ユーザーIDを取得
        String currentUserId = UserContext.getCurrentUserId();

        // Then: ANONYMOUSが返ること
        assertEquals(SystemUser.ANONYMOUS, currentUserId, 
            "未設定の場合はANONYMOUSが返ること");
    }

    @Test
    @DisplayName("ユーザーIDをクリアできること")
    void testClearCurrentUserId() {
        // Given: ユーザーIDを設定
        UserContext.setCurrentUserId("test-user-id");

        // When: クリア
        UserContext.clear();

        // Then: ANONYMOUSが返ること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId, 
            "クリア後はANONYMOUSが返ること");
    }

    @Test
    @DisplayName("システムユーザーとして処理を実行できること")
    void testRunAsSystemUser() {
        // Given: 処理の前にユーザーIDを設定
        UserContext.setCurrentUserId("original-user-id");

        // When: SYSTEMユーザーとして処理を実行
        final String[] capturedUserId = new String[1];
        UserContext.runAsSystemUser(SystemUser.SYSTEM, () -> {
            capturedUserId[0] = UserContext.getCurrentUserId();
        });

        // Then: 処理中はSYSTEMユーザーIDが使われること
        assertEquals(SystemUser.SYSTEM, capturedUserId[0], 
            "処理中はSYSTEMユーザーIDが使われること");

        // Then: 処理後は元のユーザーIDに戻ること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals("original-user-id", currentUserId, 
            "処理後は元のユーザーIDに戻ること");
    }

    @Test
    @DisplayName("runAsSystemUserで元のユーザーIDがnullの場合、処理後はクリアされること")
    void testRunAsSystemUser_NullOriginal() {
        // Given: ユーザーIDを設定しない（クリア済み）

        // When: BOOTSTRAPユーザーとして処理を実行
        UserContext.runAsSystemUser(SystemUser.BOOTSTRAP, () -> {
            // 処理内容
        });

        // Then: 処理後はANONYMOUSが返ること（クリアされている）
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId, 
            "処理後はクリアされてANONYMOUSが返ること");
    }

    @Test
    @DisplayName("複数回設定できること")
    void testSetCurrentUserId_Multiple() {
        // Given & When: 複数回設定
        UserContext.setCurrentUserId("user1");
        assertEquals("user1", UserContext.getCurrentUserId());

        UserContext.setCurrentUserId("user2");
        assertEquals("user2", UserContext.getCurrentUserId());

        UserContext.setCurrentUserId("user3");
        assertEquals("user3", UserContext.getCurrentUserId());

        // Then: 最後に設定した値が取得できること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals("user3", currentUserId, "最後に設定した値が取得できること");
    }

    @Test
    @DisplayName("ThreadLocalなので別スレッドでは独立していること")
    void testThreadLocal_Independence() throws InterruptedException {
        // Given: メインスレッドでユーザーIDを設定
        UserContext.setCurrentUserId("main-thread-user");

        // When: 別スレッドでユーザーIDを取得
        final String[] otherThreadUserId = new String[1];
        Thread otherThread = new Thread(() -> {
            otherThreadUserId[0] = UserContext.getCurrentUserId();
        });
        otherThread.start();
        otherThread.join();

        // Then: 別スレッドではANONYMOUSが返ること（独立している）
        assertEquals(SystemUser.ANONYMOUS, otherThreadUserId[0], 
            "別スレッドでは独立してANONYMOUSが返ること");

        // Then: メインスレッドでは設定した値が保持されていること
        assertEquals("main-thread-user", UserContext.getCurrentUserId(), 
            "メインスレッドでは設定した値が保持されていること");
    }
}
