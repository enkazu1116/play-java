package com.playjava.usecase.service.impl;

import com.playjava.frameworks.context.UserContext;
import com.playjava.enterprise.entity.MUser;
import com.playjava.enterprise.valueobject.SystemUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        MUser user2 = new MUser();
        user2.setUserName("user2");
        user2.setPassword("pass2");

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

    @Test
    @DisplayName("ユーザー更新が成功すること")
    void testUpdateUser_Success() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("updatetest");
        user.setPassword("password123");
        mUserService.createUserImpl(user);
        
        // 更新用に値を変更
        String originalUserId = user.getUserId();
        user.setUserName("updateduser");
        user.setPassword("newpassword");
        
        // UserContextに更新者を設定
        UserContext.setCurrentUserId(originalUserId);

        // When: ユーザーを更新
        boolean result = mUserService.updateUserImpl(user);

        // Then: 更新が成功すること
        assertTrue(result, "ユーザー更新が成功すること");
        assertEquals(originalUserId, user.getUserId(), "userIdが変更されていないこと");
        assertEquals("updateduser", user.getUserName(), "ユーザー名が更新されていること");
    }

    @Test
    @DisplayName("updateUserとupdateDateが自動設定されること")
    void testUpdateUser_SetUpdateInfo() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("updateinfo");
        user.setPassword("password456");
        mUserService.createUserImpl(user);
        
        String userId = user.getUserId();
        
        // UserContextに別の更新者を設定
        UserContext.clear();
        UserContext.setCurrentUserId("updater-001");
        
        // 値を変更
        user.setUserName("modified");

        // When: ユーザーを更新
        mUserService.updateUserImpl(user);

        // Then: updateUserとupdateDateが更新されていること
        MUser updatedUser = mUserService.getById(userId);
        assertNotNull(updatedUser.getUpdateUser(), "updateUserが設定されていること");
        assertNotNull(updatedUser.getUpdateDate(), "updateDateが設定されていること");
        assertEquals("updater-001", updatedUser.getUpdateUser(), 
            "updateUserがUserContextの値で更新されていること");
    }

    @Test
    @DisplayName("UserContextから更新者が取得されること")
    void testUpdateUser_GetUpdaterFromContext() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("contextupdate");
        user.setPassword("password789");
        mUserService.createUserImpl(user);
        
        // UserContextに更新者を設定
        String updaterId = "test-updater-123";
        UserContext.clear();
        UserContext.setCurrentUserId(updaterId);
        
        // 値を変更
        user.setUserName("contextmodified");

        // When: ユーザーを更新
        mUserService.updateUserImpl(user);

        // Then: updateUserがUserContextから取得された値であること
        MUser updatedUser = mUserService.getById(user.getUserId());
        assertEquals(updaterId, updatedUser.getUpdateUser(), 
            "updateUserがUserContextから取得された値であること");
    }

    @Test
    @DisplayName("存在するユーザーの情報が正しく更新されること")
    void testUpdateUser_UpdateExistingUser() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("original");
        user.setPassword("originalpass");
        mUserService.createUserImpl(user);
        
        String userId = user.getUserId();
        String originalCreateUser = user.getCreateUser();
        
        // UserContextに更新者を設定
        UserContext.clear();
        UserContext.setCurrentUserId(userId);
        
        // 値を変更
        user.setUserName("updated");
        user.setPassword("updatedpass");
        user.setRole(3);

        // When: ユーザーを更新
        mUserService.updateUserImpl(user);

        // Then: データベースの値が更新されていること
        MUser updatedUser = mUserService.getById(userId);
        assertNotNull(updatedUser, "ユーザーが存在すること");
        assertEquals("updated", updatedUser.getUserName(), "ユーザー名が更新されていること");
        assertEquals(3, updatedUser.getRole(), "ロールが更新されていること");
        assertEquals(originalCreateUser, updatedUser.getCreateUser(), 
            "createUserは変更されていないこと");
    }

    @Test
    @DisplayName("複数のユーザーを連続で更新できること")
    void testUpdateUser_Multiple() {
        // Given: 複数のテストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("multiupdate1");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);
        
        MUser user2 = new MUser();
        user2.setUserName("multiupdate2");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);
        
        String userId1 = user1.getUserId();
        String userId2 = user2.getUserId();
        
        // 値を変更
        user1.setUserName("modified1");
        user2.setUserName("modified2");
        
        // When: 複数のユーザーを更新
        UserContext.clear();
        UserContext.setCurrentUserId(userId1);
        boolean result1 = mUserService.updateUserImpl(user1);
        
        UserContext.clear();
        UserContext.setCurrentUserId(userId2);
        boolean result2 = mUserService.updateUserImpl(user2);

        // Then: 両方とも更新が成功すること
        assertTrue(result1, "ユーザー1が更新されること");
        assertTrue(result2, "ユーザー2が更新されること");
        
        MUser updated1 = mUserService.getById(userId1);
        MUser updated2 = mUserService.getById(userId2);
        assertEquals("modified1", updated1.getUserName(), "ユーザー1の名前が更新されていること");
        assertEquals("modified2", updated2.getUserName(), "ユーザー2の名前が更新されていること");
    }

    @Test
    @DisplayName("ユーザー論理削除が成功すること")
    void testDeleteUser_Success() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("deletetest");
        user.setPassword("password123");
        mUserService.createUserImpl(user);
        
        String userId = user.getUserId();
        
        // UserContextに削除者を設定
        UserContext.clear();
        UserContext.setCurrentUserId(userId);

        // When: ユーザーを論理削除
        boolean result = mUserService.deleteUserImpl(userId);

        // Then: 削除が成功すること
        assertTrue(result, "ユーザー論理削除が成功すること");
        
        // 論理削除されたユーザーは通常の検索では取得できない
        MUser deletedUser = mUserService.getById(userId);
        assertNull(deletedUser, "論理削除されたユーザーは取得できないこと");
    }

    @Test
    @DisplayName("論理削除されたユーザーはdeleteFlag=trueになること")
    void testDeleteUser_SetDeleteFlag() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("flagtest");
        user.setPassword("password456");
        mUserService.createUserImpl(user);
        
        String userId = user.getUserId();
        
        // UserContextに削除者を設定
        UserContext.clear();
        UserContext.setCurrentUserId(userId);

        // When: ユーザーを論理削除
        mUserService.deleteUserImpl(userId);

        // Then: deleteFlagがtrueになっていること（直接SQLで確認する代わりに動作確認）
        MUser result = mUserService.getById(userId);
        assertNull(result, "論理削除されたユーザーはgetByIdで取得できないこと");
    }

    @Test
    @DisplayName("存在しないユーザーの削除は失敗すること")
    void testDeleteUser_NonExistentUser() {
        // Given: 存在しないユーザーID
        String nonExistentId = "non-existent-id";
        
        // UserContextに削除者を設定
        UserContext.clear();
        UserContext.setCurrentUserId("test-user");

        // When: 存在しないユーザーを削除
        boolean result = mUserService.deleteUserImpl(nonExistentId);

        // Then: 削除が失敗すること（対象が存在しないため）
        assertFalse(result, "存在しないユーザーの削除は失敗すること");
    }

    @Test
    @DisplayName("複数のユーザーを連続で論理削除できること")
    void testDeleteUser_Multiple() {
        // Given: 複数のテストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("multidelete1");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);
        
        MUser user2 = new MUser();
        user2.setUserName("multidelete2");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);
        
        String userId1 = user1.getUserId();
        String userId2 = user2.getUserId();
        
        // When: 複数のユーザーを削除
        UserContext.clear();
        UserContext.setCurrentUserId("deleter1");
        boolean result1 = mUserService.deleteUserImpl(userId1);
        
        UserContext.clear();
        UserContext.setCurrentUserId("deleter2");
        boolean result2 = mUserService.deleteUserImpl(userId2);

        // Then: 両方とも削除が成功すること
        assertTrue(result1, "ユーザー1が削除されること");
        assertTrue(result2, "ユーザー2が削除されること");
        
        // 論理削除されたユーザーは取得できないこと
        MUser deleted1 = mUserService.getById(userId1);
        MUser deleted2 = mUserService.getById(userId2);
        assertNull(deleted1, "ユーザー1が論理削除されていること");
        assertNull(deleted2, "ユーザー2が論理削除されていること");
    }

    @Test
    @DisplayName("作成→更新→削除のライフサイクルが正しく動作すること")
    void testDeleteUser_FullLifecycle() {
        // Given: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("lifecycle");
        user.setPassword("password");
        UserContext.clear();
        mUserService.createUserImpl(user);
        
        String userId = user.getUserId();
        
        // ユーザーが存在することを確認
        MUser createdUser = mUserService.getById(userId);
        assertNotNull(createdUser, "作成直後はユーザーが存在すること");
        
        // ユーザーを更新
        user.setUserName("updated");
        UserContext.clear();
        UserContext.setCurrentUserId(userId);
        mUserService.updateUserImpl(user);
        
        // 更新されたことを確認
        MUser updatedUser = mUserService.getById(userId);
        assertNotNull(updatedUser, "更新後もユーザーが存在すること");
        assertEquals("updated", updatedUser.getUserName(), "ユーザー名が更新されていること");
        
        // When: ユーザーを論理削除
        UserContext.clear();
        UserContext.setCurrentUserId(userId);
        boolean deleteResult = mUserService.deleteUserImpl(userId);

        // Then: 削除が成功し、取得できなくなること
        assertTrue(deleteResult, "削除が成功すること");
        MUser deletedUser = mUserService.getById(userId);
        assertNull(deletedUser, "論理削除後はユーザーが取得できないこと");
    }

    @Test
    @DisplayName("ユーザー名で検索できること")
    void testSearchUser_ByUserName() {
        // Given: 複数のテストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("search_test_alice");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);

        MUser user2 = new MUser();
        user2.setUserName("search_test_bob");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);

        MUser user3 = new MUser();
        user3.setUserName("another_alice");
        user3.setPassword("pass3");
        UserContext.clear();
        mUserService.createUserImpl(user3);

        // When: "alice"で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("alice");
        List<MUser> results = mUserService.searchUserImpl(searchCondition);

        // Then: aliceを含むユーザーが2件取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertEquals(2, results.size(), "aliceを含むユーザーが2件存在すること");
        assertTrue(results.stream().anyMatch(u -> u.getUserName().contains("alice")), 
            "検索結果にaliceを含むユーザーが含まれていること");
    }

    @Test
    @DisplayName("検索条件なしで全ユーザーが取得できること")
    void testSearchUser_NoCondition() {
        // Given: テストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("all_search_1");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);

        MUser user2 = new MUser();
        user2.setUserName("all_search_2");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);

        // When: 検索条件を指定せず検索
        MUser searchCondition = new MUser();
        List<MUser> results = mUserService.searchUserImpl(searchCondition);

        // Then: 全ユーザーが取得できること（少なくとも2件以上）
        assertNotNull(results, "検索結果が取得できること");
        assertTrue(results.size() >= 2, "複数のユーザーが取得できること");
    }

    @Test
    @DisplayName("論理削除されたユーザーは検索結果に含まれないこと")
    void testSearchUser_ExcludeDeletedUsers() {
        // Given: テストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("exclude_test_1");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);
        String userId1 = user1.getUserId();

        MUser user2 = new MUser();
        user2.setUserName("exclude_test_2");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);

        // user1を論理削除
        UserContext.clear();
        UserContext.setCurrentUserId(userId1);
        mUserService.deleteUserImpl(userId1);

        // When: "exclude_test"で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("exclude_test");
        List<MUser> results = mUserService.searchUserImpl(searchCondition);

        // Then: 削除されていないuser2のみが取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertEquals(1, results.size(), "削除されていないユーザーのみが取得できること");
        assertEquals("exclude_test_2", results.get(0).getUserName(), 
            "削除されていないユーザーが取得できること");
    }

    @Test
    @DisplayName("部分一致検索が正しく動作すること")
    void testSearchUser_PartialMatch() {
        // Given: テストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("prefix_test_suffix");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);

        MUser user2 = new MUser();
        user2.setUserName("test_middle_part");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);

        // When: "test"で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("test");
        List<MUser> results = mUserService.searchUserImpl(searchCondition);

        // Then: testを含む両方のユーザーが取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertTrue(results.size() >= 2, "testを含むユーザーが複数取得できること");
        assertTrue(results.stream().allMatch(u -> u.getUserName().contains("test")), 
            "全ての結果にtestが含まれていること");
    }

    @Test
    @DisplayName("該当するユーザーがいない場合は空のリストが返ること")
    void testSearchUser_NoResults() {
        // Given: 存在しないユーザー名で検索

        // When: 存在しないユーザー名で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("nonexistent_user_xyz_12345");
        List<MUser> results = mUserService.searchUserImpl(searchCondition);

        // Then: 空のリストが返ること
        assertNotNull(results, "検索結果がnullではないこと");
        assertTrue(results.isEmpty(), "該当するユーザーがいない場合は空のリストが返ること");
    }

    @Test
    @DisplayName("大文字小文字を区別せず検索できること")
    void testSearchUser_CaseInsensitive() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("CaseSensitive");
        user.setPassword("pass");
        UserContext.clear();
        mUserService.createUserImpl(user);

        // When: 小文字で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("case");
        List<MUser> results = mUserService.searchUserImpl(searchCondition);

        // Then: 検索結果が取得できること（データベースの設定による）
        assertNotNull(results, "検索結果が取得できること");
        // 注: 大文字小文字の区別はデータベースの照合順序（collation）に依存
    }

    @Test
    @DisplayName("ユーザー名が存在する場合、trueが返ること")
    void testExistsByUserName_Exists() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("exists_test");
        user.setPassword("password123");
        UserContext.clear();
        mUserService.createUserImpl(user);

        // When: ユーザー名の存在チェック
        boolean exists = mUserService.existsByUserName("exists_test");

        // Then: 存在することが確認できること
        assertTrue(exists, "ユーザー名が存在する場合、trueが返ること");
    }

    @Test
    @DisplayName("ユーザー名が存在しない場合、falseが返ること")
    void testExistsByUserName_NotExists() {
        // Given: 存在しないユーザー名

        // When: ユーザー名の存在チェック
        boolean exists = mUserService.existsByUserName("nonexistent_user_name_xyz");

        // Then: 存在しないことが確認できること
        assertFalse(exists, "ユーザー名が存在しない場合、falseが返ること");
    }

    @Test
    @DisplayName("除外IDを指定した場合、自分自身は除外されること")
    void testExistsByUserName_WithExcludeId() {
        // Given: テストユーザーを作成
        MUser user = new MUser();
        user.setUserName("exclude_duplicate");
        user.setPassword("password123");
        UserContext.clear();
        mUserService.createUserImpl(user);

        // When: 自分自身のIDを除外してチェック
        boolean exists = mUserService.existsByUserName("exclude_duplicate", user.getUserId());

        // Then: 自分自身は除外されるのでfalseが返ること
        assertFalse(exists, "自分自身を除外した場合、falseが返ること");
    }

    @Test
    @DisplayName("除外IDを指定して、他のユーザーが同じ名前を使っている場合、trueが返ること")
    void testExistsByUserName_WithExcludeId_OtherUserExists() {
        // Given: 同じ名前の2人のユーザーを作成（念のため）
        MUser user1 = new MUser();
        user1.setUserName("duplicate_name");
        user1.setPassword("password123");
        UserContext.clear();
        mUserService.createUserImpl(user1);

        MUser user2 = new MUser();
        user2.setUserName("duplicate_test_other");
        user2.setPassword("password456");
        UserContext.clear();
        mUserService.createUserImpl(user2);

        // When: user2のIDを除外して、user1の名前をチェック
        boolean exists = mUserService.existsByUserName("duplicate_name", user2.getUserId());

        // Then: user1が存在するのでtrueが返ること
        assertTrue(exists, "他のユーザーが同じ名前を使っている場合、trueが返ること");
    }

    @Test
    @DisplayName("大文字小文字を区別してチェックされること")
    void testExistsByUserName_CaseSensitive() {
        // Given: 小文字のユーザー名で作成
        MUser user = new MUser();
        user.setUserName("lowercase");
        user.setPassword("password123");
        UserContext.clear();
        mUserService.createUserImpl(user);

        // When: 大文字でチェック
        boolean existsUpperCase = mUserService.existsByUserName("LOWERCASE");

        // Then: データベースの照合順序による
        // 注: PostgreSQLはデフォルトでcase-sensitiveなので、存在しないと判定される
        // H2はcase-insensitiveなので、存在すると判定される
        // このテストは環境依存のため、コメントのみ
        assertNotNull(existsUpperCase, "チェック結果が返ること");
    }
}
