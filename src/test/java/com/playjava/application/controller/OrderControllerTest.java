package com.playjava.application.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.playjava.frameworks.context.UserContext;
import com.playjava.adapters.dto.CreateOrderRequest;
import com.playjava.adapters.dto.OrderDetailResponse;
import com.playjava.adapters.dto.OrderItemRequest;
import com.playjava.enterprise.entity.*;
import com.playjava.adapters.handler.GlobalExceptionHandler.StockInsufficientException;
import com.playjava.frameworks.mapper.*;
import com.playjava.adapters.service.impl.MCustomerServiceImpl;
import com.playjava.adapters.service.impl.MStockServiceImpl;
import com.playjava.frameworks.mapper.MProductMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("OrderController テスト")
class OrderControllerTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private MCustomerServiceImpl customerService;

    @Autowired
    private MProductMapper productMapper;

    @Autowired
    private MStockServiceImpl stockService;

    @Autowired
    private TOrderMapper orderMapper;

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
    @DisplayName("POST /api/v1/orders/store-purchase - 店舗購入の注文作成が成功すること")
    void testCreateStorePurchaseOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderController.createStorePurchaseOrder(request);

        assertNotNull(orderId, "注文IDが返されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(1, order.getStatus(), "ステータスが1（注文確定）であること");
    }

    @Test
    @DisplayName("POST /api/v1/orders/store-purchase - 在庫不足時にエラーが返されること")
    void testCreateStorePurchaseOrder_InsufficientStock() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(20); // 在庫10に対して20を注文
        items.add(item1);
        request.setItems(items);

        // 在庫不足例外が投げられることを確認
        // 実際のControllerではGlobalExceptionHandlerが処理するため、
        // ここではService層の例外が投げられることを確認
        assertThrows(
            StockInsufficientException.class,
            () -> orderController.createStorePurchaseOrder(request),
            "在庫不足時に例外が投げられること"
        );
    }

    @Test
    @DisplayName("POST /api/v1/orders/special-order - 取り寄せ注文の作成が成功すること")
    void testCreateSpecialOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderController.createSpecialOrder(request);

        assertNotNull(orderId, "注文IDが返されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(2, order.getStatus(), "ステータスが2（取り寄せ中）であること");
    }

    @Test
    @DisplayName("POST /api/v1/orders/custom-order - カスタマイズオーダーの作成が成功すること")
    void testCreateCustomOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderController.createCustomOrder(request);

        assertNotNull(orderId, "注文IDが返されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(3, order.getStatus(), "ステータスが3（カスタマイズ中）であること");
    }

    @Test
    @DisplayName("POST /api/v1/orders/cancel - キャンセル注文の作成が成功すること")
    void testCreateCancelledOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);

        String orderId = orderController.createCancelledOrder(request);

        assertNotNull(orderId, "注文IDが返されること");
        
        // 注文が正しく作成されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertNotNull(order, "注文が存在すること");
        assertEquals(6, order.getStatus(), "ステータスが6（キャンセル済み）であること");
    }

    @Test
    @DisplayName("PUT /api/v1/orders/{orderId}/confirm - 注文確定が成功すること")
    void testConfirmOrder_Success() {
        // 取り寄せ注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderController.createSpecialOrder(request);

        // 注文確定
        assertDoesNotThrow(() -> orderController.confirmOrder(orderId), "注文確定が成功すること");
        
        // 注文ステータスが更新されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertEquals(1, order.getStatus(), "ステータスが1（注文確定）に更新されること");
    }

    @Test
    @DisplayName("GET /api/v1/orders/search - 注文一覧検索が成功すること")
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
        orderController.createStorePurchaseOrder(request1);

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items2 = new ArrayList<>();
        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getProductId());
        item2.setQuantity(1);
        items2.add(item2);
        request2.setItems(items2);
        orderController.createSpecialOrder(request2);

        // 注文一覧を検索
        IPage<TOrder> result = orderController.searchOrder(
            null, testCustomer.getCustomerId(), null, null, null, false,
            1, 10, "orderDate", "desc"
        );

        assertNotNull(result, "検索結果が取得できること");
        assertTrue(result.getTotal() >= 2, "少なくとも2件の注文が取得できること");
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 注文詳細取得が成功すること")
    void testGetOrderDetail_Success() {
        // 注文を作成
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(2);
        items.add(item1);
        request.setItems(items);
        String orderId = orderController.createStorePurchaseOrder(request);

        // 注文詳細を取得
        OrderDetailResponse response = orderController.getOrderDetail(orderId);

        assertNotNull(response, "注文詳細が取得できること");
        assertNotNull(response.getOrder(), "注文情報が含まれること");
        assertEquals(orderId, response.getOrder().getOrderId(), "注文IDが正しいこと");
        assertNotNull(response.getItems(), "注文明細が含まれること");
        assertEquals(1, response.getItems().size(), "注文明細が1件であること");
    }

    @Test
    @DisplayName("PUT /api/v1/orders/{orderId}/cancel - 注文キャンセルが成功すること")
    void testCancelOrder_Success() {
        // 店舗購入注文を作成（自動的に確定される）
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getCustomerId());
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getProductId());
        item1.setQuantity(3);
        items.add(item1);
        request.setItems(items);
        String orderId = orderController.createStorePurchaseOrder(request);

        // 注文をキャンセル
        assertDoesNotThrow(() -> orderController.cancelOrder(orderId), "注文キャンセルが成功すること");
        
        // 注文ステータスが更新されているか確認
        TOrder order = orderMapper.selectById(orderId);
        assertEquals(6, order.getStatus(), "ステータスが6（キャンセル済み）に更新されること");
    }
}
