package com.playjava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.playjava.context.UserContext;
import com.playjava.entity.MUser;
import com.playjava.service.impl.MUserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis Plusページング機能のテスト
 */
@SpringBootTest
@Transactional
@DisplayName("MyBatis Plus ページング機能テスト")
class MUserPaginationTest {

    @Autowired
    private MUserServiceImpl mUserService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("ページング機能が正しく動作すること")
    void testPagination() {
        // Given: 15件のテストユーザーを作成
        for (int i = 1; i <= 15; i++) {
            MUser user = new MUser();
            user.setUserName("page_test_" + i);
            user.setPassword("password" + i);
            UserContext.clear();
            mUserService.createUserImpl(user);
        }

        // When: 1ページ目（10件）を取得
        IPage<MUser> page1 = mUserService.searchUserImpl("page_test", 1, 10, "createDate", "asc");

        // Then: ページング情報が正しいこと
        assertNotNull(page1, "ページング結果が取得できること");
        assertEquals(1, page1.getCurrent(), "現在のページ番号が1であること");
        assertEquals(10, page1.getSize(), "ページサイズが10であること");
        assertEquals(15, page1.getTotal(), "全件数が15件であること");
        assertEquals(2, page1.getPages(), "総ページ数が2であること");
        assertEquals(10, page1.getRecords().size(), "1ページ目のレコード数が10件であること");

        // When: 2ページ目（残り5件）を取得
        IPage<MUser> page2 = mUserService.searchUserImpl("page_test", 2, 10, "createDate", "asc");

        // Then: 2ページ目の情報が正しいこと
        assertEquals(2, page2.getCurrent(), "現在のページ番号が2であること");
        assertEquals(5, page2.getRecords().size(), "2ページ目のレコード数が5件であること");
        assertEquals(15, page2.getTotal(), "全件数が15件であること");
    }

    @Test
    @DisplayName("ソート機能が正しく動作すること")
    void testSorting() {
        // Given: 複数のテストユーザーを作成
        MUser user1 = new MUser();
        user1.setUserName("sort_c");
        user1.setPassword("pass1");
        UserContext.clear();
        mUserService.createUserImpl(user1);

        MUser user2 = new MUser();
        user2.setUserName("sort_a");
        user2.setPassword("pass2");
        UserContext.clear();
        mUserService.createUserImpl(user2);

        MUser user3 = new MUser();
        user3.setUserName("sort_b");
        user3.setPassword("pass3");
        UserContext.clear();
        mUserService.createUserImpl(user3);

        // When: ユーザー名で昇順ソート
        IPage<MUser> result = mUserService.searchUserImpl("sort_", 1, 10, "userName", "asc");

        // Then: ユーザー名が昇順で並んでいること
        assertNotNull(result, "検索結果が取得できること");
        assertEquals(3, result.getRecords().size(), "検索結果が3件であること");
        assertEquals("sort_a", result.getRecords().get(0).getUserName(), "1番目がsort_aであること");
        assertEquals("sort_b", result.getRecords().get(1).getUserName(), "2番目がsort_bであること");
        assertEquals("sort_c", result.getRecords().get(2).getUserName(), "3番目がsort_cであること");
    }
}
