package com.playjava.service;

import com.playjava.context.UserContext;
import com.playjava.entity.MProduct;
import com.playjava.entity.MStock;
import com.playjava.mapper.MProductMapper;
import com.playjava.service.impl.MStockServiceImpl;
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
@DisplayName("MStockServiceImpl テスト")
class MStockServiceImplTest {

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
        product.setProductNumber("TEST001");
        product.setProductName("テスト商品");
        product.setDescription("テスト用の商品です");
        product.setPrice(1000);
        product.setCategory(1);
        product.setDeleteFlag(false);
        mProductMapper.insert(product);
        return product;
    }

    @Test
    @DisplayName("在庫登録が成功すること")
    void testCreateStock_Success() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);

        // When: 在庫を登録
        boolean result = mStockService.createStockImpl(stock);

        // Then: 登録が成功すること
        assertTrue(result, "在庫登録が成功すること");
        assertNotNull(stock.getStockId(), "stockIdが設定されていること");
        assertEquals(product.getProductId(), stock.getProductId(), "productIdが正しく設定されていること");
        assertEquals(100, stock.getQuantity(), "quantityが正しく設定されていること");
        assertEquals(0, stock.getStatus(), "statusが正しく設定されていること");
        assertFalse(stock.getDeleteFlag(), "削除フラグがfalseであること");
    }

    @Test
    @DisplayName("statusがnullの場合、quantityに基づいて自動設定されること")
    void testCreateStock_AutoSetStatus() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備（statusをnullに設定）
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(50);
        stock.setStatus(null); // statusをnullに設定

        // When: 在庫を登録
        mStockService.createStockImpl(stock);

        // Then: statusが自動設定されていること（quantity > 0 なので 0 = 在庫あり）
        assertNotNull(stock.getStatus(), "statusが設定されていること");
        assertEquals(0, stock.getStatus(), "quantity > 0 の場合、statusが0（在庫あり）になること");
    }

    @Test
    @DisplayName("quantityが0の場合、statusが1（在庫なし）に自動設定されること")
    void testCreateStock_AutoSetStatusZero() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備（quantity=0, status=null）
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(0);
        stock.setStatus(null);

        // When: 在庫を登録
        mStockService.createStockImpl(stock);

        // Then: statusが1（在庫なし）に設定されていること
        assertEquals(1, stock.getStatus(), "quantity = 0 の場合、statusが1（在庫なし）になること");
    }

    @Test
    @DisplayName("商品が存在しない場合、例外がスローされること")
    void testCreateStock_ProductNotFound() {
        // Given: 存在しない商品IDを指定
        MStock stock = new MStock();
        stock.setProductId("non-existent-product-id");
        stock.setQuantity(100);
        stock.setStatus(0);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.createStockImpl(stock);
        }, "商品が存在しない場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("商品が存在しません"), 
            "エラーメッセージに「商品が存在しません」が含まれること");
    }

    @Test
    @DisplayName("既に在庫レコードが存在する場合、例外がスローされること")
    void testCreateStock_DuplicateStock() {
        // Given: テスト商品を作成し、在庫を登録
        MProduct product = createTestProduct();
        MStock firstStock = new MStock();
        firstStock.setProductId(product.getProductId());
        firstStock.setQuantity(100);
        firstStock.setStatus(0);
        mStockService.createStockImpl(firstStock);

        // 同じ商品IDで在庫を登録しようとする
        MStock secondStock = new MStock();
        secondStock.setProductId(product.getProductId());
        secondStock.setQuantity(200);
        secondStock.setStatus(0);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.createStockImpl(secondStock);
        }, "既に在庫レコードが存在する場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("既に存在します"), 
            "エラーメッセージに「既に存在します」が含まれること");
    }

    @Test
    @DisplayName("quantityが負の値の場合、例外がスローされること")
    void testCreateStock_NegativeQuantity() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備（quantityが負の値）
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(-1);
        stock.setStatus(0);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.createStockImpl(stock);
        }, "quantityが負の値の場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫数量は0以上の整数である必要があります"), 
            "エラーメッセージに「在庫数量は0以上の整数である必要があります」が含まれること");
    }

    @Test
    @DisplayName("quantityがnullの場合、例外がスローされること")
    void testCreateStock_NullQuantity() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備（quantityがnull）
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(null);
        stock.setStatus(0);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.createStockImpl(stock);
        }, "quantityがnullの場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫数量は0以上の整数である必要があります"), 
            "エラーメッセージに「在庫数量は0以上の整数である必要があります」が含まれること");
    }

    @Test
    @DisplayName("statusが範囲外（負の値）の場合、例外がスローされること")
    void testCreateStock_InvalidStatusNegative() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備（statusが負の値）
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(-1);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.createStockImpl(stock);
        }, "statusが範囲外の場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫ステータスは0（在庫あり）、1（在庫なし）、2（発注済み）のいずれかである必要があります"), 
            "エラーメッセージに「在庫ステータスは0（在庫あり）、1（在庫なし）、2（発注済み）のいずれかである必要があります」が含まれること");
    }

    @Test
    @DisplayName("statusが範囲外（3以上）の場合、例外がスローされること")
    void testCreateStock_InvalidStatusTooLarge() {
        // Given: テスト商品を作成
        MProduct product = createTestProduct();
        
        // 在庫情報を準備（statusが3）
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(3);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.createStockImpl(stock);
        }, "statusが範囲外の場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫ステータスは0（在庫あり）、1（在庫なし）、2（発注済み）のいずれかである必要があります"), 
            "エラーメッセージに「在庫ステータスは0（在庫あり）、1（在庫なし）、2（発注済み）のいずれかである必要があります」が含まれること");
    }

    @Test
    @DisplayName("在庫更新が成功すること")
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

        // When: 在庫を更新
        boolean result = mStockService.updateStockImpl(stock);

        // Then: 更新が成功すること
        assertTrue(result, "在庫更新が成功すること");
        
        // 更新後の値を確認
        MStock updatedStock = mStockService.getById(stock.getStockId());
        assertEquals(200, updatedStock.getQuantity(), "quantityが更新されていること");
    }

    @Test
    @DisplayName("在庫IDがnullの場合、例外がスローされること")
    void testUpdateStock_NullStockId() {
        // Given: stockIdがnullの在庫情報
        MStock stock = new MStock();
        stock.setStockId(null);
        stock.setQuantity(100);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.updateStockImpl(stock);
        }, "在庫IDがnullの場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫IDは必須です"), 
            "エラーメッセージに「在庫IDは必須です」が含まれること");
    }

    @Test
    @DisplayName("在庫レコードが存在しない場合、例外がスローされること")
    void testUpdateStock_StockNotFound() {
        // Given: 存在しない在庫IDを指定
        MStock stock = new MStock();
        stock.setStockId("non-existent-stock-id");
        stock.setQuantity(100);

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.updateStockImpl(stock);
        }, "在庫レコードが存在しない場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫レコードが存在しません"), 
            "エラーメッセージに「在庫レコードが存在しません」が含まれること");
    }

    @Test
    @DisplayName("在庫削除が成功すること")
    void testDeleteStock_Success() {
        // Given: テスト商品と在庫を作成
        MProduct product = createTestProduct();
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);
        mStockService.createStockImpl(stock);
        String stockId = stock.getStockId();

        // When: 在庫を削除
        boolean result = mStockService.deleteStockImpl(stockId);

        // Then: 削除が成功すること
        assertTrue(result, "在庫削除が成功すること");
        
        // 削除後の在庫が取得できないこと（論理削除）
        MStock deletedStock = mStockService.getById(stockId);
        assertNull(deletedStock, "削除後の在庫が取得できないこと");
    }

    @Test
    @DisplayName("在庫照会が成功すること")
    void testGetStockByProductId_Success() {
        // Given: テスト商品と在庫を作成
        MProduct product = createTestProduct();
        MStock stock = new MStock();
        stock.setProductId(product.getProductId());
        stock.setQuantity(100);
        stock.setStatus(0);
        mStockService.createStockImpl(stock);

        // When: 在庫を照会
        MStock foundStock = mStockService.getStockByProductIdImpl(product.getProductId());

        // Then: 在庫情報が正しく取得できること
        assertNotNull(foundStock, "在庫情報が取得できること");
        assertEquals(product.getProductId(), foundStock.getProductId(), "productIdが正しいこと");
        assertEquals(100, foundStock.getQuantity(), "quantityが正しいこと");
        assertEquals(0, foundStock.getStatus(), "statusが正しいこと");
    }

    @Test
    @DisplayName("商品が存在しない場合、在庫照会で例外がスローされること")
    void testGetStockByProductId_ProductNotFound() {
        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.getStockByProductIdImpl("non-existent-product-id");
        }, "商品が存在しない場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("商品が存在しません"), 
            "エラーメッセージに「商品が存在しません」が含まれること");
    }

    @Test
    @DisplayName("在庫レコードが存在しない場合、在庫照会で例外がスローされること")
    void testGetStockByProductId_StockNotFound() {
        // Given: テスト商品を作成（在庫は登録しない）
        MProduct product = createTestProduct();

        // When/Then: 例外がスローされること
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mStockService.getStockByProductIdImpl(product.getProductId());
        }, "在庫レコードが存在しない場合、例外がスローされること");
        
        assertTrue(exception.getMessage().contains("在庫レコードが存在しません"), 
            "エラーメッセージに「在庫レコードが存在しません」が含まれること");
    }
}
