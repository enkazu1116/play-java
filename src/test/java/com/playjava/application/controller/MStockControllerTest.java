package com.playjava.application.controller;

import com.playjava.frameworks.context.UserContext;
import com.playjava.enterprise.entity.MProduct;
import com.playjava.enterprise.entity.MStock;
import com.playjava.frameworks.mapper.MProductMapper;
import com.playjava.usecase.service.impl.MStockServiceImpl;
import com.playjava.enterprise.valueobject.SystemUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
@DisplayName("MStockController テスト")
class MStockControllerTest {

    @Autowired
    private MStockController mStockController;

    @Autowired
    private MStockServiceImpl mStockService;

    @Autowired
    private MProductMapper mProductMapper;

    @BeforeEach
    void setUp() {
        // 各テストの前にUserContextをクリア
        UserContext.clear();
        UserContext.setCurrentUserId(SystemUser.BOOTSTRAP);
    }

    @AfterEach
    void tearDown() {
        // 各テストの後にUserContextをクリア
        UserContext.clear();
    }

    /**
     * テスト用の商品を作成するヘルパーメソッド
     */
    private MProduct createTestProduct() {
        MProduct product = new MProduct();
        product.setProductNumber("CTRL001");
        product.setProductName("コントローラーテスト商品");
        product.setDescription("コントローラーテスト用の商品です");
        product.setPrice(2000);
        product.setCategory(1);
        product.setDeleteFlag(false);
        mProductMapper.insert(product);
        return product;
    }

    @Test
    @DisplayName("POST /api/v1/stocks - 在庫登録が成功すること")
    void testCreateStock_Success() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(150);
        stock.setStatus(0);

        // When: コントローラーを直接呼び出し
        assertDoesNotThrow(() -> mStockController.createStock(stock), 
            "在庫登録が例外をスローしないこと");
        
        // Then: 在庫IDが設定されていること
        assertNotNull(stock.getStockId(), "stockIdが設定されていること");
        assertEquals(product.getProductId(), stock.getProductId(), "productIdが正しく設定されていること");
    }

    @Test
    @DisplayName("リクエスト完了後にUserContextがクリアされること")
    void testCreateStock_ClearUserContext() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);

        // When: コントローラーを呼び出す
        mStockController.createStock(stock);

        // Then: リクエスト完了後、UserContextがクリアされていること
        String currentUserId = UserContext.getCurrentUserId();
        assertEquals(SystemUser.ANONYMOUS, currentUserId,
            "リクエスト完了後、UserContextがクリアされてANONYMOUSが返ること");
    }

    @Test
    @DisplayName("PUT /api/v1/stocks/updateStock - 在庫更新が成功すること")
    void testUpdateStock_Success() {
        // Given: テスト商品と在庫を作成
        MProduct product = createTestProduct();
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);
        mStockService.createStockImpl(stock);

        // 在庫情報を更新
        stock.setQuantity(200);
        stock.setStatus(0);

        // When: コントローラーを呼び出す
        assertDoesNotThrow(() -> mStockController.updateStock(stock), 
            "在庫更新が例外をスローしないこと");

        // Then: 更新後の値を確認
        MStock updatedStock = mStockService.getById(stock.getStockId());
        assertEquals(200, updatedStock.getQuantity(), "quantityが更新されていること");
    }

    @Test
    @DisplayName("DELETE /api/v1/stocks/deleteStock/{stockId} - 在庫削除が成功すること")
    void testDeleteStock_Success() {
        // Given: テスト商品と在庫を作成
        MProduct product = createTestProduct();
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);
        mStockService.createStockImpl(stock);
        String stockId = stock.getStockId();

        // When: コントローラーを呼び出す
        assertDoesNotThrow(() -> mStockController.deleteStock(stockId), 
            "在庫削除が例外をスローしないこと");

        // Then: 削除後の在庫が取得できないこと（論理削除）
        MStock deletedStock = mStockService.getById(stockId);
        assertNull(deletedStock, "削除後の在庫が取得できないこと");
    }

    @Test
    @DisplayName("GET /api/v1/stocks/search - 在庫検索が成功すること")
    void testSearchStock_Success() {
        // Given: テスト商品と在庫を作成
        MProduct product = createTestProduct();
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);
        mStockService.createStockImpl(stock);

        // When: コントローラーを呼び出す
        IPage<MStock> result = mStockController.searchStock(
            product.getProductId(), null, null, null, null, null, 1, 10, "updateDate", "desc");

        // Then: 検索結果が取得できること
        assertNotNull(result, "検索結果が取得できること");
        assertTrue(result.getTotal() > 0, "検索結果が1件以上あること");
    }

    @Test
    @DisplayName("GET /api/v1/stocks/search - ページングが正しく動作すること")
    void testSearchStock_Pagination() {
        // Given: 複数のテスト商品と在庫を作成
        for (int i = 0; i < 15; i++) {
            MProduct product = new MProduct();
            product.setProductNumber("PAGE" + String.format("%03d", i));
            product.setProductName("ページングテスト商品" + i);
            product.setDescription("ページングテスト用");
            product.setPrice(1000 + i);
            product.setCategory(1);
            product.setDeleteFlag(false);
            mProductMapper.insert(product);

            MStock stock = new MStock();
            stock.setProductId(product.getProductId());
            stock.setQuantity(100 + i);
            stock.setStatus(0);
            mStockService.createStockImpl(stock);
        }

        // When: 1ページ目を取得（ページサイズ10）
        IPage<MStock> page1 = mStockController.searchStock(
            null, null, null, null, null, null, 1, 10, "updateDate", "desc");

        // Then: ページングが正しく動作すること
        assertNotNull(page1, "検索結果が取得できること");
        assertEquals(10, page1.getRecords().size(), "1ページ目の件数が10件であること");
        assertTrue(page1.getTotal() >= 15, "総件数が15件以上であること");
    }

    @Test
    @DisplayName("GET /api/v1/stocks/products/{productId} - 在庫照会が成功すること")
    void testGetStockByProductId_Success() {
        // Given: テスト商品と在庫を作成
        MProduct product = createTestProduct();
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);
        mStockService.createStockImpl(stock);

        // When: コントローラーを呼び出す
        MStock foundStock = mStockController.getStockByProductId(product.getProductId());

        // Then: 在庫情報が正しく取得できること
        assertNotNull(foundStock, "在庫情報が取得できること");
        assertEquals(product.getProductId(), foundStock.getProductId(), "productIdが正しいこと");
        assertEquals(100, foundStock.getQuantity(), "quantityが正しいこと");
    }

    @Test
    @DisplayName("GET /api/v1/stocks/search - ソートが正しく動作すること")
    void testSearchStock_Sort() {
        // Given: 複数のテスト商品と在庫を作成
        for (int i = 0; i < 5; i++) {
            MProduct product = new MProduct();
            product.setProductNumber("SORT" + String.format("%03d", i));
            product.setProductName("ソートテスト商品" + i);
            product.setDescription("ソートテスト用");
            product.setPrice(1000);
            product.setCategory(1);
            product.setDeleteFlag(false);
            mProductMapper.insert(product);

            MStock stock = new MStock();
            stock.setProductId(product.getProductId());
            stock.setQuantity(100 + i * 10); // quantityを異なる値に設定
            stock.setStatus(0);
            mStockService.createStockImpl(stock);
        }

        // When: quantityで昇順ソート
        IPage<MStock> ascResult = mStockController.searchStock(
            null, null, null, null, null, null, 1, 10, "quantity", "asc");

        // Then: ソートが正しく動作すること
        assertNotNull(ascResult, "検索結果が取得できること");
        assertTrue(ascResult.getRecords().size() > 0, "検索結果が1件以上あること");
        
        // quantityが昇順になっていることを確認
        if (ascResult.getRecords().size() > 1) {
            for (int i = 0; i < ascResult.getRecords().size() - 1; i++) {
                Integer current = ascResult.getRecords().get(i).getQuantity();
                Integer next = ascResult.getRecords().get(i + 1).getQuantity();
                assertTrue(current <= next, 
                    String.format("quantityが昇順になっていること: %d <= %d", current, next));
            }
        }
    }
}
