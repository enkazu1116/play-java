package com.playjava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.playjava.context.UserContext;
import com.playjava.dto.CreateOrderRequest;
import com.playjava.dto.OrderDetailResponse;
import com.playjava.dto.OrderItemRequest;
import com.playjava.entity.*;
import com.playjava.exception.GlobalExceptionHandler.StockInsufficientException;
import com.playjava.mapper.*;
import com.playjava.service.impl.MCustomerServiceImpl;
import com.playjava.service.impl.MStockServiceImpl;
import com.playjava.service.impl.OrderServiceImpl;
import com.playjava.mapper.MProductMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("OrderServiceImpl テスト")
class OrderServiceImplTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private MCustomerServiceImpl customerService;

    @Autowired
    private MProductMapper productMapper;

    @Autowired
    private MStockServiceImpl stockService;

    @Autowired
    private TOrderMapper orderMapper;

    @Autowired
    private MStockMapper stockMapper;

    private MCustomer testCustomer;
    private MProduct testProduct1;
    private MProduct testProduct2;
    private MStock testStock1;
    private MStock testStock2;

    @BeforeEach
    void setUp() {
        UserContext.clear();
        UserContext.setCurrentUserId("test-user-id");

        // テスト用顧客を作成
        testCustomer = new MCustomer();
        testCustomer.setCustomerName("テスト顧客");
        testCustomer.setAddress("東京都");
        testCustomer.setMobileNumber("09012345678");
        customerService.createCustomerImpl(testCustomer);

        // テスト用商品を作成
        long timestamp = System.currentTimeMillis() % 1000000; // 6桁に制限
        testProduct1 = new MProduct();
        testProduct1.setProductNumber(String.format("P%06d1", timestamp));
        testProduct1.setProductName("テスト商品1");
        testProduct1.setPrice(1000);
        testProduct1.setCategory(1);
        productMapper.insert(testProduct1);

        testProduct2 = new MProduct();
        testProduct2.setProductNumber(String.format("P%06d2", timestamp));
        testProduct2.setProductName("テスト商品2");
        testProduct2.setPrice(2000);
        testProduct2.setCategory(1);
        productMapper.insert(testProduct2);

        // テスト用在庫を作成
        testStock1 = new MStock();
        testStock1.setProductId(testProduct1.getProductId());
        testStock1.setQuantity(10);
        testStock1.setStatus(0);
        stockService.createStockImpl(testStock1);

        testStock2 = new MStock();
        testStock2.setProductId(testProduct2.getProductId());
        testStock2.setQuantity(5);
        testStock2.setStatus(0);
        stockService.createStockImpl(testStock2);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("店舗購入の注文作成が成功し、在庫が減算されること")
    void testCreateStorePurchaseOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderService.createStorePurchaseOrderImpl(request);

        assertNotNull(orderId, "注文IDが生成されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(testCustomer.getCustomerId(), order.getCustomerId(), "顧客IDが正しいこと");
        assertEquals(1, order.getStatus(), "ステータスが1（注文確定）であること");
        
        // 在庫が減算されているか確認
        MStock stock = stockMapper.selectById(testStock1.getStockId());
        assertEquals(7, stock.getQuantity(), "在庫が3減算されて7になっていること");
    }

    @Test
    @DisplayName("在庫不足時にStockInsufficientExceptionが投げられること")
    void testCreateStorePurchaseOrder_InsufficientStock() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(20); // 在庫10に対して20を注文
        items.add(item1);
        request.setItems(items);

        StockInsufficientException exception = assertThrows(
            StockInsufficientException.class,
            () -> orderService.createStorePurchaseOrderImpl(request),
            "在庫不足時に例外が投げられること"
        );

        assertNotNull(exception.getInsufficientItems(), "在庫不足商品情報が含まれること");
        assertEquals(1, exception.getInsufficientItems().size(), "在庫不足商品が1件であること");
        assertEquals(testProduct1.getProductId(), exception.getInsufficientItems().get(0).getProductId(), "商品IDが正しいこと");
        assertEquals(10, exception.getInsufficientItems().get(0).getAvailableQuantity(), "在庫数量が10であること");
        assertEquals(20, exception.getInsufficientItems().get(0).getRequestedQuantity(), "注文数量が20であること");
    }

    @Test
    @DisplayName("取り寄せ注文の作成が成功し、在庫が減算されないこと")
    void testCreateSpecialOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderService.createSpecialOrderImpl(request);

        assertNotNull(orderId, "注文IDが生成されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(2, order.getStatus(), "ステータスが2（取り寄せ中）であること");
        
        // 在庫が減算されていないか確認
        MStock stock = stockMapper.selectById(testStock1.getStockId());
        assertEquals(10, stock.getQuantity(), "在庫が減算されていないこと");
    }

    @Test
    @DisplayName("カスタマイズオーダーの作成が成功し、在庫が減算されないこと")
    void testCreateCustomOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderService.createCustomOrderImpl(request);

        assertNotNull(orderId, "注文IDが生成されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(3, order.getStatus(), "ステータスが3（カスタマイズ中）であること");
        
        // 在庫が減算されていないか確認
        MStock stock = stockMapper.selectById(testStock1.getStockId());
        assertEquals(10, stock.getQuantity(), "在庫が減算されていないこと");
    }

    @Test
    @DisplayName("取り寄せ注文の確定が成功し、在庫が減算されること")
    void testConfirmOrder_SpecialOrder() {
        // 取り寄せ注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createSpecialOrderImpl(request);

        // 在庫が減算されていないことを確認
        MStock stockBefore = stockMapper.selectById(testStock1.getStockId());
        assertEquals(10, stockBefore.getQuantity(), "確定前は在庫が減算されていないこと");

        // 注文確定
        boolean result = orderService.confirmOrderImpl(orderId);

        assertTrue(result, "注文確定が成功すること");
        
        // 注文ステータスが更新されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertEquals(1, order.getStatus(), "ステータスが1（注文確定）に更新されること");
        
        // 在庫が減算されているか確認
        MStock stockAfter = stockMapper.selectById(testStock1.getStockId());
        assertEquals(7, stockAfter.getQuantity(), "在庫が3減算されて7になっていること");
    }

    @Test
    @DisplayName("カスタマイズオーダーの確定が成功し、在庫が減算されること")
    void testConfirmOrder_CustomOrder() {
        // カスタマイズオーダーを作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(2);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createCustomOrderImpl(request);

        // 注文確定
        boolean result = orderService.confirmOrderImpl(orderId);

        assertTrue(result, "注文確定が成功すること");
        
        // 在庫が減算されているか確認
        MStock stock = stockMapper.selectById(testStock1.getStockId());
        assertEquals(8, stock.getQuantity(), "在庫が2減算されて8になっていること");
    }

    @Test
    @DisplayName("既に確定済みの注文を確定しようとするとエラーになること")
    void testConfirmOrder_AlreadyConfirmed() {
        // 店舗購入注文を作成（自動的に確定される）
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createStorePurchaseOrderImpl(request);

        // 既に確定済みの注文を確定しようとする
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> orderService.confirmOrderImpl(orderId),
            "既に確定済みの注文を確定しようとするとエラーになること"
        );

        assertTrue(exception.getMessage().contains("既に確定済み"), "エラーメッセージが適切であること");
    }

    @Test
    @DisplayName("注文一覧検索が正しく動作すること")
    void testSearchOrder_Success() {
        // 複数の注文を作成
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items1 = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(1);
        items1.add(item1);
        request1.setItems(items1);
        orderService.createStorePurchaseOrderImpl(request1);

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items2 = new ArrayList<>();
        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getProductId());
        item2.setQuantity(1);
        items2.add(item2);
        request2.setItems(items2);
        orderService.createSpecialOrderImpl(request2);

        // 顧客IDで検索
        IPage<TOrder> result = orderService.searchOrderImpl(
            null, testCustomer.getCustomerId(), null, null, null, false,
            1, 10, "orderDate", "desc"
        );

        assertNotNull(result, "検索結果が取得できること");
        assertTrue(result.getTotal() >= 2, "少なくとも2件の注文が取得できること");
    }

    @Test
    @DisplayName("注文詳細取得が正しく動作すること")
    void testGetOrderDetail_Success() {
        // 注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(2);
        items.add(item1);
        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getProductId());
        item2.setQuantity(1);
        items.add(item2);
        request.setItems(items);
        String orderId = orderService.createStorePurchaseOrderImpl(request);

        // 注文詳細を取得
        OrderDetailResponse response = orderService.getOrderDetailImpl(orderId);

        assertNotNull(response, "注文詳細が取得できること");
        assertNotNull(response.getOrder(), "注文情報が含まれること");
        assertEquals(orderId, response.getOrder().getOrderId(), "注文IDが正しいこと");
        assertNotNull(response.getItems(), "注文明細が含まれること");
        assertEquals(2, response.getItems().size(), "注文明細が2件であること");
    }

    @Test
    @DisplayName("確定済み注文のキャンセルが成功し、在庫が戻ること")
    void testCancelOrder_ConfirmedOrder() {
        // 店舗購入注文を作成（自動的に確定される）
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createStorePurchaseOrderImpl(request);

        // 在庫が減算されていることを確認
        MStock stockBefore = stockMapper.selectById(testStock1.getStockId());
        assertEquals(7, stockBefore.getQuantity(), "キャンセル前は在庫が7であること");

        // 注文をキャンセル
        boolean result = orderService.cancelOrderImpl(orderId);

        assertTrue(result, "注文キャンセルが成功すること");
        
        // 注文ステータスが更新されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertEquals(6, order.getStatus(), "ステータスが6（キャンセル済み）に更新されること");
        
        // 在庫が戻っているか確認
        MStock stockAfter = stockMapper.selectById(testStock1.getStockId());
        assertEquals(10, stockAfter.getQuantity(), "在庫が戻って10になっていること");
    }

    @Test
    @DisplayName("取り寄せ中の注文のキャンセルが成功し、在庫が戻らないこと")
    void testCancelOrder_SpecialOrder() {
        // 取り寄せ注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createSpecialOrderImpl(request);

        // 在庫が減算されていないことを確認
        MStock stockBefore = stockMapper.selectById(testStock1.getStockId());
        assertEquals(10, stockBefore.getQuantity(), "キャンセル前は在庫が10であること");

        // 注文をキャンセル
        boolean result = orderService.cancelOrderImpl(orderId);

        assertTrue(result, "注文キャンセルが成功すること");
        
        // 在庫が戻っていないことを確認（元々減算していないため）
        MStock stockAfter = stockMapper.selectById(testStock1.getStockId());
        assertEquals(10, stockAfter.getQuantity(), "在庫が10のままであること");
    }

    @Test
    @DisplayName("既にキャンセル済みの注文をキャンセルしようとするとエラーになること")
    void testCancelOrder_AlreadyCancelled() {
        // 取り寄せ注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createSpecialOrderImpl(request);

        // 一度キャンセル
        orderService.cancelOrderImpl(orderId);

        // 再度キャンセルしようとする
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> orderService.cancelOrderImpl(orderId),
            "既にキャンセル済みの注文をキャンセルしようとするとエラーになること"
        );

        assertTrue(exception.getMessage().contains("既にキャンセル済み"), "エラーメッセージが適切であること");
    }

    @Test
    @DisplayName("在庫不足時に注文確定しようとするとエラーになること")
    void testConfirmOrder_InsufficientStock() {
        // 取り寄せ注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderService.createSpecialOrderImpl(request);

        // 在庫を減らす（他の注文で在庫を使い切る）
        MStock stock = stockMapper.selectById(testStock1.getStockId());
        stock.setQuantity(2);
        stockMapper.updateById(stock);

        // 注文確定を試みる（在庫不足）
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> orderService.confirmOrderImpl(orderId),
            "在庫不足時に注文確定しようとするとエラーになること"
        );

        assertTrue(exception.getMessage().contains("在庫不足"), "エラーメッセージが適切であること");
    }
}
