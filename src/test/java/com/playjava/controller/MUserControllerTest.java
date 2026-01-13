package com.playjava.controller;

import com.playjava.context.UserContext;
import com.playjava.entity.MUser;
import com.playjava.service.impl.MUserServiceImpl;
import com.playjava.valueobject.SystemUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

        MUser user2 = new MUser();
        user2.setUserName("multi2");
        user2.setPassword("pass2");

        // When: 複数のリクエストを連続で処理
        mUserController.createUser(user1);
        mUserController.createUser(user2);

        // Then: 両方のユーザーが作成されていること
        assertNotNull(user1.getUserId(), "user1のuserIdが設定されていること");
        assertNotNull(user2.getUserId(), "user2のuserIdが設定されていること");
        assertNotEquals(user1.getUserId(), user2.getUserId(), 
            "異なるuserIdが生成されていること");
    }

    @Test
    @DisplayName("PUT /api/v1/users/updateUser - ユーザー更新が成功すること")
    void testUpdateUser_Success() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("updateapi");
        user.setPassword("updatepass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();
        
        // 更新内容を設定
        user.setUserName("updated");
        user.setPassword("newpass");

        // When: コントローラーを呼び出し
        assertDoesNotThrow(() -> mUserController.updateUser(user), 
            "ユーザー更新が例外をスローしないこと");
        
        // Then: データベースの値が更新されていること
        MUser updatedUser = mUserService.getById(userId);
        assertEquals("updated", updatedUser.getUserName(), "ユーザー名が更新されていること");
    }

    @Test
    @DisplayName("更新処理でUserContextが正しく設定・クリアされること")
    void testUpdateUser_UserContextHandling() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("contextupdate");
        user.setPassword("contextpass");
        mUserController.createUser(user);
        
        // 更新内容を設定
        user.setUserName("contextmodified");

        // When: 更新コントローラーを呼び出す
        mUserController.updateUser(user);

        // Then: リクエスト完了後、UserContextがクリアされていること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId,
            "リクエスト完了後、UserContextがクリアされてANONYMOUSが返ること");
    }

    @Test
    @DisplayName("更新したユーザーがデータベースに保存されること")
    void testUpdateUser_SaveToDatabase() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("dbupdate");
        user.setPassword("dbpass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();
        String originalUserName = user.getUserName();
        
        // 更新内容を設定
        user.setUserName("dbupdated");
        user.setRole(3);

        // When: コントローラーを呼び出す
        mUserController.updateUser(user);

        // Then: データベースから更新された値を取得できること
        MUser savedUser = mUserService.getById(userId);
        assertNotNull(savedUser, "データベースからユーザーが取得できること");
        assertEquals("dbupdated", savedUser.getUserName(), "更新後のユーザー名が保存されていること");
        assertEquals(3, savedUser.getRole(), "更新後のロールが保存されていること");
        assertNotEquals(originalUserName, savedUser.getUserName(), 
            "ユーザー名が更新前と異なること");
    }

    @Test
    @DisplayName("更新時にupdateUserとupdateDateが設定されること")
    void testUpdateUser_SetUpdateInfo() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("updateinfo");
        user.setPassword("updatepass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();
        
        // 更新内容を設定
        user.setUserName("infoupdated");

        // When: コントローラーを呼び出す
        mUserController.updateUser(user);

        // Then: updateUserとupdateDateが設定されていること
        MUser updatedUser = mUserService.getById(userId);
        assertNotNull(updatedUser.getUpdateUser(), "updateUserが設定されていること");
        assertNotNull(updatedUser.getUpdateDate(), "updateDateが設定されていること");
        assertEquals(userId, updatedUser.getUpdateUser(), 
            "updateUserが設定されていること（自分自身で更新）");
    }

    @Test
    @DisplayName("複数のユーザーを連続で更新できること")
    void testUpdateUser_MultipleRequests() {
        // Given: 複数のユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("multiupdate1");
        user1.setPassword("pass1");
        mUserController.createUser(user1);
        String userId1 = user1.getUserId();

        MUser user2 = new MUser();
        user2.setUserName("multiupdate2");
        user2.setPassword("pass2");
        mUserController.createUser(user2);
        String userId2 = user2.getUserId();
        
        // 更新内容を設定
        user1.setUserName("updated1");
        user2.setUserName("updated2");

        // When: 複数のリクエストを連続で処理
        mUserController.updateUser(user1);
        mUserController.updateUser(user2);

        // Then: 両方のユーザーが更新されていること
        MUser updated1 = mUserService.getById(userId1);
        MUser updated2 = mUserService.getById(userId2);
        assertEquals("updated1", updated1.getUserName(), "user1が更新されていること");
        assertEquals("updated2", updated2.getUserName(), "user2が更新されていること");
    }

    @Test
    @DisplayName("存在しないユーザーの更新は失敗すること")
    void testUpdateUser_NonExistentUser() {
        // Given: 存在しないユーザーID
        MUser user = new MUser();
        user.setUserId("non-existent-id");
        user.setUserName("nonexistent");
        user.setPassword("pass");

        // When: コントローラーを呼び出す
        mUserController.updateUser(user);
        
        // Then: データベースには存在しないこと
        MUser result = mUserService.getById("non-existent-id");
        assertNull(result, "存在しないユーザーは更新されないこと");
    }

    @Test
    @DisplayName("DELETE /api/v1/users/deleteUser/{userId} - ユーザー論理削除が成功すること")
    void testDeleteUser_Success() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("deleteapi");
        user.setPassword("deletepass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();

        // When: コントローラーを呼び出し
        assertDoesNotThrow(() -> mUserController.deleteUser(userId), 
            "ユーザー削除が例外をスローしないこと");
        
        // Then: データベースから取得できないこと（論理削除済み）
        MUser deletedUser = mUserService.getById(userId);
        assertNull(deletedUser, "論理削除されたユーザーは取得できないこと");
    }

    @Test
    @DisplayName("削除処理でUserContextが正しく設定・クリアされること")
    void testDeleteUser_UserContextHandling() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("contextdelete");
        user.setPassword("contextpass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();

        // When: 削除コントローラーを呼び出す
        mUserController.deleteUser(userId);

        // Then: リクエスト完了後、UserContextがクリアされていること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId,
            "リクエスト完了後、UserContextがクリアされてANONYMOUSが返ること");
    }

    @Test
    @DisplayName("論理削除されたユーザーがデータベースから取得できないこと")
    void testDeleteUser_NotRetrievable() {
        // Given: まずユーザーを作成
        MUser user = new MUser();
        user.setUserName("notretrievable");
        user.setPassword("deletepass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();
        
        // 削除前は取得できることを確認
        MUser beforeDelete = mUserService.getById(userId);
        assertNotNull(beforeDelete, "削除前はユーザーが存在すること");

        // When: コントローラーを呼び出す
        mUserController.deleteUser(userId);

        // Then: データベースから取得できないこと
        MUser afterDelete = mUserService.getById(userId);
        assertNull(afterDelete, "論理削除後はユーザーが取得できないこと");
    }

    @Test
    @DisplayName("複数のユーザーを連続で論理削除できること")
    void testDeleteUser_MultipleRequests() {
        // Given: 複数のユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("multidelete1");
        user1.setPassword("pass1");
        mUserController.createUser(user1);
        String userId1 = user1.getUserId();

        MUser user2 = new MUser();
        user2.setUserName("multidelete2");
        user2.setPassword("pass2");
        mUserController.createUser(user2);
        String userId2 = user2.getUserId();

        // When: 複数のリクエストを連続で処理
        mUserController.deleteUser(userId1);
        mUserController.deleteUser(userId2);

        // Then: 両方のユーザーが論理削除されていること
        MUser deleted1 = mUserService.getById(userId1);
        MUser deleted2 = mUserService.getById(userId2);
        assertNull(deleted1, "user1が論理削除されていること");
        assertNull(deleted2, "user2が論理削除されていること");
    }

    @Test
    @DisplayName("作成→更新→削除のフローが正しく動作すること")
    void testDeleteUser_FullWorkflow() {
        // Given: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("workflow");
        user.setPassword("workflowpass");
        mUserController.createUser(user);
        
        String userId = user.getUserId();
        
        // 作成されたことを確認
        MUser createdUser = mUserService.getById(userId);
        assertNotNull(createdUser, "ユーザーが作成されていること");
        assertEquals("workflow", createdUser.getUserName());
        
        // ユーザーを更新
        user.setUserName("workflowupdated");
        mUserController.updateUser(user);
        
        // 更新されたことを確認
        MUser updatedUser = mUserService.getById(userId);
        assertNotNull(updatedUser, "ユーザーが更新されていること");
        assertEquals("workflowupdated", updatedUser.getUserName());
        
        // When: ユーザーを削除
        mUserController.deleteUser(userId);
        
        // Then: 論理削除されて取得できないこと
        MUser deletedUser = mUserService.getById(userId);
        assertNull(deletedUser, "ユーザーが論理削除されて取得できないこと");
    }

    @Test
    @DisplayName("存在しないユーザーIDでの削除は例外をスローしないこと")
    void testDeleteUser_NonExistentUser() {
        // Given: 存在しないユーザーID
        String nonExistentId = "non-existent-id-123";

        // When & Then: コントローラーを呼び出しても例外が発生しないこと
        assertDoesNotThrow(() -> mUserController.deleteUser(nonExistentId), 
            "存在しないユーザーIDでも例外をスローしないこと");
    }

    @Test
    @DisplayName("POST /api/v1/users/searchUser - ユーザー名で検索できること（従来版）")
    void testSearchUserLegacy_ByUserName() {
        // Given: 複数のユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("search_api_alice");
        user1.setPassword("pass1");
        mUserController.createUser(user1);

        MUser user2 = new MUser();
        user2.setUserName("search_api_bob");
        user2.setPassword("pass2");
        mUserController.createUser(user2);

        MUser user3 = new MUser();
        user3.setUserName("alice_another");
        user3.setPassword("pass3");
        mUserController.createUser(user3);

        // When: "alice"で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("alice");
        List<MUser> results = mUserController.searchUserSimple(searchCondition);

        // Then: aliceを含むユーザーが取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertTrue(results.size() >= 2, "aliceを含むユーザーが複数取得できること");
        assertTrue(results.stream().anyMatch(u -> u.getUserName().contains("alice")), 
            "検索結果にaliceを含むユーザーが含まれていること");
    }

    @Test
    @DisplayName("検索条件なしで全ユーザーが取得できること（従来版）")
    void testSearchUserLegacy_NoCondition() {
        // Given: ユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("all_api_1");
        user1.setPassword("pass1");
        mUserController.createUser(user1);

        MUser user2 = new MUser();
        user2.setUserName("all_api_2");
        user2.setPassword("pass2");
        mUserController.createUser(user2);

        // When: 検索条件なしで検索
        MUser searchCondition = new MUser();
        List<MUser> results = mUserController.searchUserSimple(searchCondition);

        // Then: 全ユーザーが取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertTrue(results.size() >= 2, "複数のユーザーが取得できること");
    }

    @Test
    @DisplayName("論理削除されたユーザーは検索結果に含まれないこと（従来版）")
    void testSearchUserLegacy_ExcludeDeletedUsers() {
        // Given: ユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("exclude_api_1");
        user1.setPassword("pass1");
        mUserController.createUser(user1);
        String userId1 = user1.getUserId();

        MUser user2 = new MUser();
        user2.setUserName("exclude_api_2");
        user2.setPassword("pass2");
        mUserController.createUser(user2);

        // user1を論理削除
        mUserController.deleteUser(userId1);

        // When: "exclude_api"で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("exclude_api");
        List<MUser> results = mUserController.searchUserSimple(searchCondition);

        // Then: 削除されていないuser2のみが取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertEquals(1, results.size(), "削除されていないユーザーのみが取得できること");
        assertEquals("exclude_api_2", results.get(0).getUserName(), 
            "削除されていないユーザーが取得できること");
        assertFalse(results.stream().anyMatch(u -> u.getUserId().equals(userId1)), 
            "論理削除されたユーザーは含まれないこと");
    }

    @Test
    @DisplayName("部分一致検索が正しく動作すること（従来版）")
    void testSearchUserLegacy_PartialMatch() {
        // Given: ユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("partial_search_test");
        user1.setPassword("pass1");
        mUserController.createUser(user1);

        MUser user2 = new MUser();
        user2.setUserName("test_partial_middle");
        user2.setPassword("pass2");
        mUserController.createUser(user2);

        // When: "partial"で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("partial");
        List<MUser> results = mUserController.searchUserSimple(searchCondition);

        // Then: partialを含むユーザーが取得できること
        assertNotNull(results, "検索結果が取得できること");
        assertTrue(results.size() >= 2, "partialを含むユーザーが複数取得できること");
        assertTrue(results.stream().allMatch(u -> u.getUserName().contains("partial")), 
            "全ての結果にpartialが含まれていること");
    }

    @Test
    @DisplayName("該当するユーザーがいない場合は空のリストが返ること（従来版）")
    void testSearchUserLegacy_NoResults() {
        // Given: 存在しないユーザー名

        // When: 存在しないユーザー名で検索
        MUser searchCondition = new MUser();
        searchCondition.setUserName("nonexistent_api_xyz_99999");
        List<MUser> results = mUserController.searchUserSimple(searchCondition);

        // Then: 空のリストが返ること
        assertNotNull(results, "検索結果がnullではないこと");
        assertTrue(results.isEmpty(), "該当するユーザーがいない場合は空のリストが返ること");
    }

    @Test
    @DisplayName("検索後にUserContextが影響を受けないこと（従来版）")
    void testSearchUserLegacy_NoSideEffects() {
        // Given: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("sideeffect_test");
        user.setPassword("pass");
        mUserController.createUser(user);

        // UserContextをクリア
        UserContext.clear();

        // When: 検索を実行
        MUser searchCondition = new MUser();
        searchCondition.setUserName("sideeffect");
        mUserController.searchUserSimple(searchCondition);

        // Then: UserContextが変更されていないこと
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId, 
            "検索処理でUserContextが変更されないこと");
    }

    @Test
    @DisplayName("作成→検索→更新→検索→削除→検索のフローが正しく動作すること（従来版）")
    void testSearchUserLegacy_FullWorkflow() {
        // Given & When: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("workflow_search");
        user.setPassword("pass");
        mUserController.createUser(user);
        String userId = user.getUserId();

        // 検索して存在を確認
        MUser searchCondition1 = new MUser();
        searchCondition1.setUserName("workflow_search");
        List<MUser> results1 = mUserController.searchUserSimple(searchCondition1);
        assertEquals(1, results1.size(), "作成後に検索で取得できること");

        // ユーザー名を更新
        user.setUserName("workflow_updated");
        mUserController.updateUser(user);

        // 更新後の名前で検索
        MUser searchCondition2 = new MUser();
        searchCondition2.setUserName("workflow_updated");
        List<MUser> results2 = mUserController.searchUserSimple(searchCondition2);
        assertEquals(1, results2.size(), "更新後の名前で検索できること");

        // 元の名前では検索できないことを確認
        List<MUser> results3 = mUserController.searchUserSimple(searchCondition1);
        assertTrue(results3.isEmpty(), "元の名前では検索できないこと");

        // ユーザーを削除
        mUserController.deleteUser(userId);

        // 削除後は検索できないことを確認
        List<MUser> results4 = mUserController.searchUserSimple(searchCondition2);
        assertTrue(results4.isEmpty(), "削除後は検索できないこと");
    }

    @Test
    @DisplayName("GET /api/v1/users/searchUser - ページング機能が正しく動作すること")
    void testSearchUser_Paging() {
        // Given: 15件のユーザーを作成
        for (int i = 1; i <= 15; i++) {
            MUser user = new MUser();
            user.setUserName("paging_api_" + i);
            user.setPassword("pass" + i);
            mUserController.createUser(user);
        }

        // When: 1ページ目を取得（10件）
        IPage<MUser> page1 = mUserController.searchUser("paging_api", 1, 10, "createDate", "asc");

        // Then: ページング情報が正しいこと
        assertNotNull(page1, "ページング結果が取得できること");
        assertEquals(1, page1.getCurrent(), "現在のページ番号が1であること");
        assertEquals(10, page1.getSize(), "ページサイズが10であること");
        assertEquals(15, page1.getTotal(), "全件数が15件であること");
        assertEquals(2, page1.getPages(), "総ページ数が2であること");
        assertEquals(10, page1.getRecords().size(), "1ページ目のレコード数が10件であること");

        // When: 2ページ目を取得
        IPage<MUser> page2 = mUserController.searchUser("paging_api", 2, 10, "createDate", "asc");

        // Then: 2ページ目の情報が正しいこと
        assertEquals(2, page2.getCurrent(), "現在のページ番号が2であること");
        assertEquals(5, page2.getRecords().size(), "2ページ目のレコード数が5件であること");
    }

    @Test
    @DisplayName("GET /api/v1/users/searchUser - ソート機能（昇順）が正しく動作すること")
    void testSearchUser_SortAscending() {
        // Given: 複数のユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("sort_api_c");
        user1.setPassword("pass1");
        mUserController.createUser(user1);

        MUser user2 = new MUser();
        user2.setUserName("sort_api_a");
        user2.setPassword("pass2");
        mUserController.createUser(user2);

        MUser user3 = new MUser();
        user3.setUserName("sort_api_b");
        user3.setPassword("pass3");
        mUserController.createUser(user3);

        // When: ユーザー名で昇順ソート
        IPage<MUser> result = mUserController.searchUser("sort_api", 1, 10, "userName", "asc");

        // Then: ユーザー名が昇順で並んでいること
        List<MUser> records = result.getRecords();
        assertEquals(3, records.size(), "検索結果が3件であること");
        assertEquals("sort_api_a", records.get(0).getUserName(), "1番目がsort_api_aであること");
        assertEquals("sort_api_b", records.get(1).getUserName(), "2番目がsort_api_bであること");
        assertEquals("sort_api_c", records.get(2).getUserName(), "3番目がsort_api_cであること");
    }

    @Test
    @DisplayName("GET /api/v1/users/searchUser - ソート機能（降順）が正しく動作すること")
    void testSearchUser_SortDescending() {
        // Given: 複数のユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("desc_api_1");
        user1.setPassword("pass1");
        mUserController.createUser(user1);

        MUser user2 = new MUser();
        user2.setUserName("desc_api_2");
        user2.setPassword("pass2");
        mUserController.createUser(user2);

        // When: ユーザー名で降順ソート
        IPage<MUser> result = mUserController.searchUser("desc_api", 1, 10, "userName", "desc");

        // Then: ユーザー名が降順で並んでいること
        List<MUser> records = result.getRecords();
        assertEquals(2, records.size(), "検索結果が2件であること");
        assertEquals("desc_api_2", records.get(0).getUserName(), "1番目がdesc_api_2であること");
        assertEquals("desc_api_1", records.get(1).getUserName(), "2番目がdesc_api_1であること");
    }

    @Test
    @DisplayName("GET /api/v1/users/searchUser - デフォルト値が正しく適用されること")
    void testSearchUser_DefaultValues() {
        // Given: ユーザーを作成
        for (int i = 1; i <= 3; i++) {
            MUser user = new MUser();
            user.setUserName("default_api_" + i);
            user.setPassword("pass" + i);
            mUserController.createUser(user);
        }

        // When: パラメータなしで検索（デフォルト値使用）
        IPage<MUser> result = mUserController.searchUser(null, 1, 10, "createDate", "desc");

        // Then: デフォルト値が適用されていること
        assertNotNull(result, "検索結果が取得できること");
        assertEquals(1, result.getCurrent(), "デフォルトページ番号が1であること");
        assertEquals(10, result.getSize(), "デフォルトページサイズが10であること");
    }

    @Test
    @DisplayName("GET /api/v1/users/searchUser - 異なるページサイズで取得できること")
    void testSearchUser_DifferentPageSize() {
        // Given: 25件のユーザーを作成
        for (int i = 1; i <= 25; i++) {
            MUser user = new MUser();
            user.setUserName("pagesize_api_" + i);
            user.setPassword("pass" + i);
            mUserController.createUser(user);
        }

        // When: ページサイズ20で取得
        IPage<MUser> result = mUserController.searchUser("pagesize_api", 1, 20, "createDate", "asc");

        // Then: ページング情報が正しいこと
        assertEquals(20, result.getSize(), "ページサイズが20であること");
        assertEquals(25, result.getTotal(), "全件数が25件であること");
        assertEquals(2, result.getPages(), "総ページ数が2であること");
        assertEquals(20, result.getRecords().size(), "取得レコード数が20件であること");
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/{userName} - ユーザー名が存在する場合、trueが返ること")
    void testExistsByUserName_Exists() {
        // Given: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("exists_api_test");
        user.setPassword("password123");
        mUserController.createUser(user);

        // When: ユーザー名の存在チェック
        boolean exists = mUserController.existsByUserName("exists_api_test", null);

        // Then: 存在することが確認できること
        assertTrue(exists, "ユーザー名が存在する場合、trueが返ること");
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/{userName} - ユーザー名が存在しない場合、falseが返ること")
    void testExistsByUserName_NotExists() {
        // Given: 存在しないユーザー名

        // When: ユーザー名の存在チェック
        boolean exists = mUserController.existsByUserName("nonexistent_api_xyz", null);

        // Then: 存在しないことが確認できること
        assertFalse(exists, "ユーザー名が存在しない場合、falseが返ること");
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/{userName} - 除外IDを指定した場合、自分自身は除外されること")
    void testExistsByUserName_WithExcludeId() {
        // Given: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("exclude_api_test");
        user.setPassword("password123");
        mUserController.createUser(user);
        String userId = user.getUserId();

        // When: 自分自身のIDを除外してチェック
        boolean exists = mUserController.existsByUserName("exclude_api_test", userId);

        // Then: 自分自身は除外されるのでfalseが返ること
        assertFalse(exists, "自分自身を除外した場合、falseが返ること");
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/{userName} - ユーザー作成前のチェックで重複を検出できること")
    void testExistsByUserName_BeforeCreate() {
        // Given: ユーザーを作成
        MUser user = new MUser();
        user.setUserName("duplicate_check");
        user.setPassword("password123");
        mUserController.createUser(user);

        // When: 同じユーザー名で作成前チェック
        boolean exists = mUserController.existsByUserName("duplicate_check", null);

        // Then: 重複が検出されること
        assertTrue(exists, "作成前のチェックで重複が検出できること");
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/{userName} - ユーザー更新前のチェックで自分以外の重複を検出できること")
    void testExistsByUserName_BeforeUpdate() {
        // Given: 2人のユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("update_check_1");
        user1.setPassword("password123");
        mUserController.createUser(user1);

        MUser user2 = new MUser();
        user2.setUserName("update_check_2");
        user2.setPassword("password456");
        mUserController.createUser(user2);
        String userId2 = user2.getUserId();

        // When: user2がuser1の名前に変更しようとしてチェック
        boolean exists = mUserController.existsByUserName("update_check_1", userId2);

        // Then: user1が存在するため重複が検出されること
        assertTrue(exists, "更新前のチェックで他のユーザーの重複が検出できること");

        // When: user2が自分の名前のままでチェック
        boolean notExists = mUserController.existsByUserName("update_check_2", userId2);

        // Then: 自分自身は除外されるので重複が検出されないこと
        assertFalse(notExists, "更新前のチェックで自分自身は除外されること");
    }
}
