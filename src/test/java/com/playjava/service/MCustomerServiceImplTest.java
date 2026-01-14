package com.playjava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.playjava.context.UserContext;
import com.playjava.entity.MCustomer;
import com.playjava.service.impl.MCustomerServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("MCustomerServiceImpl テスト")
class MCustomerServiceImplTest {

    @Autowired
    private MCustomerServiceImpl mCustomerService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("顧客作成が成功すること")
    void testCreateCustomer_Success() {
        MCustomer customer = new MCustomer();
        customer.setCustomerName("test_customer");
        customer.setAddress("Tokyo");
        customer.setMobileNumber("09012345678");
        customer.setEmail("test@example.com");

        boolean result = mCustomerService.createCustomerImpl(customer);

        assertTrue(result, "顧客作成が成功すること");
        assertNotNull(customer.getCustomerId(), "customerIdが設定されていること");
        assertNotNull(customer.getCustomerNumber(), "customerNumberが設定されていること");
        assertFalse(customer.getDeleteFlag(), "deleteFlagがfalseであること");
    }

    @Test
    @DisplayName("顧客更新が成功し、顧客番号が変更されないこと")
    void testUpdateCustomer_Success() {
        MCustomer customer = new MCustomer();
        customer.setCustomerName("update_before");
        customer.setAddress("Osaka");
        customer.setMobileNumber("09011112222");
        customer.setEmail("before@example.com");
        mCustomerService.createCustomerImpl(customer);

        String customerId = customer.getCustomerId();
        String originalNumber = customer.getCustomerNumber();

        customer.setCustomerName("update_after");
        customer.setAddress("Kyoto");
        customer.setMobileNumber("09033334444");
        customer.setEmail("after@example.com");

        boolean result = mCustomerService.updateCustomerImpl(customer);

        assertTrue(result, "顧客更新が成功すること");
        MCustomer updated = mCustomerService.getById(customerId);
        assertNotNull(updated, "更新後も顧客が存在すること");
        assertEquals("update_after", updated.getCustomerName(), "顧客名が更新されていること");
        assertEquals(originalNumber, updated.getCustomerNumber(), "顧客番号は変更されていないこと");
    }

    @Test
    @DisplayName("顧客論理削除が成功し、getByIdで取得できないこと")
    void testDeleteCustomer_Success() {
        MCustomer customer = new MCustomer();
        customer.setCustomerName("delete_test");
        customer.setMobileNumber("09055556666");
        mCustomerService.createCustomerImpl(customer);

        String customerId = customer.getCustomerId();

        boolean result = mCustomerService.deleteCustomerImpl(customerId);

        assertTrue(result, "顧客論理削除が成功すること");
        MCustomer deleted = mCustomerService.getById(customerId);
        assertNull(deleted, "論理削除後はgetByIdで取得できないこと");
    }

    @Test
    @DisplayName("顧客検索（一覧）で条件に一致する顧客が取得できること")
    void testSearchCustomer_List() {
        MCustomer c1 = new MCustomer();
        c1.setCustomerName("search_target_1");
        c1.setAddress("Tokyo");
        c1.setMobileNumber("09000000001");
        mCustomerService.createCustomerImpl(c1);

        MCustomer c2 = new MCustomer();
        c2.setCustomerName("search_target_2");
        c2.setAddress("Osaka");
        c2.setMobileNumber("09000000002");
        mCustomerService.createCustomerImpl(c2);

        List<MCustomer> results = mCustomerService.searchCustomerImpl(
                null, "search_target", null, null, null, false);

        assertNotNull(results, "検索結果が取得できること");
        assertTrue(results.size() >= 2, "少なくとも2件以上取得できること");
        assertTrue(results.stream().anyMatch(c -> c.getCustomerName().equals("search_target_1")),
                "search_target_1 が含まれていること");
    }

    @Test
    @DisplayName("ページング付き顧客検索が正しく動作すること")
    void testSearchCustomer_Paging() {
        for (int i = 1; i <= 15; i++) {
            MCustomer c = new MCustomer();
            c.setCustomerName("page_customer_" + i);
            c.setMobileNumber("0909999" + String.format("%04d", i));
            mCustomerService.createCustomerImpl(c);
        }

        IPage<MCustomer> page1 = mCustomerService.searchCustomerImpl(
                null, "page_customer_", null, null, null, false,
                1, 10, "updateDate", "desc");

        assertNotNull(page1, "ページング結果が取得できること");
        assertEquals(1, page1.getCurrent(), "現在のページ番号が1であること");
        assertEquals(10, page1.getSize(), "ページサイズが10であること");
        assertEquals(15, page1.getTotal(), "全件数が15件であること");
        assertEquals(2, page1.getPages(), "総ページ数が2であること");
        assertEquals(10, page1.getRecords().size(), "1ページ目のレコード数が10件であること");
    }
}

