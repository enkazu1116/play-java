package com.playjava.application.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.playjava.frameworks.context.UserContext;
import com.playjava.enterprise.entity.MCustomer;
import com.playjava.usecase.service.impl.MCustomerServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@SpringBootTest
@Transactional
@DisplayName("MCustomerController テスト")
class MCustomerControllerTest {

    @Autowired
    private MCustomerController mCustomerController;

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
    @DisplayName("POST /api/v1/customers - 顧客作成が成功すること")
    void testCreateCustomer_Success() {
        MCustomer customer = new MCustomer();
        customer.setCustomerName("api_customer");
        customer.setMobileNumber("09012341234");

        assertDoesNotThrow(() -> mCustomerController.createCustomer(customer));
        assertNotNull(customer.getCustomerId(), "customerIdが設定されていること");
    }

    @Test
    @DisplayName("PUT /api/v1/customers - 顧客更新が成功すること")
    void testUpdateCustomer_Success() {
        MCustomer customer = new MCustomer();
        customer.setCustomerName("before_update");
        customer.setMobileNumber("09011112222");
        mCustomerController.createCustomer(customer);

        UUID customerId = customer.getCustomerId();
        customer.setCustomerName("after_update");

        assertDoesNotThrow(() -> mCustomerController.updateCustomer(customer));

        MCustomer updated = mCustomerService.getById(customerId);
        assertEquals("after_update", updated.getCustomerName(), "顧客名が更新されていること");
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{customerId} - 顧客論理削除が成功すること")
    void testDeleteCustomer_Success() {
        MCustomer customer = new MCustomer();
        customer.setCustomerName("delete_api");
        customer.setMobileNumber("09033334444");
        mCustomerController.createCustomer(customer);

        UUID customerId = customer.getCustomerId();

        assertDoesNotThrow(() -> mCustomerController.deleteCustomer(customerId));
        assertNull(mCustomerService.getById(customerId), "論理削除後は取得できないこと");
    }

    @Test
    @DisplayName("GET /api/v1/customers/search - ページング・ソート付きで検索できること")
    void testSearchCustomer_PagingAndSorting() {
        for (int i = 1; i <= 15; i++) {
            MCustomer c = new MCustomer();
            c.setCustomerName("paging_customer_" + i);
            c.setMobileNumber("0908888" + String.format("%04d", i));
            mCustomerController.createCustomer(c);
        }

        IPage<MCustomer> page1 = mCustomerController.searchCustomer(
                null, "paging_customer_", null, null, null, null,
                1, 10, "updateDate", "desc");

        assertNotNull(page1, "ページング結果が取得できること");
        assertEquals(1, page1.getCurrent(), "現在のページ番号が1であること");
        assertEquals(10, page1.getRecords().size(), "1ページ目の件数が10件であること");
    }
}

