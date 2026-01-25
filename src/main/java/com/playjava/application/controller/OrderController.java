package com.playjava.application.controller;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.playjava.adapters.service.impl.OrderServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import com.playjava.adapters.dto.CreateOrderRequest;
import com.playjava.adapters.dto.OrderDetailResponse;
import com.playjava.frameworks.context.UserContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.playjava.enterprise.entity.TOrder;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderServiceImpl orderService;

    // 店舗購入の注文作成
    @PostMapping("/store-purchase")
    public String createStorePurchaseOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("createStorePurchaseOrder: customerId={}, items={}", request.getCustomerId(), request.getItems());
        
        try {
            // 注文作成処理
            String orderId = orderService.createStorePurchaseOrderImpl(request);
            log.info("注文作成成功: orderId={}", orderId);
            return orderId;
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 注文キャンセル（在庫不足時にキャンセルを選択した場合）
    @PostMapping("/cancel")
    public String createCancelledOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("createCancelledOrder: customerId={}, items={}", request.getCustomerId(), request.getItems());
        
        try {
            // 注文キャンセル処理（注文履歴は残す）
            String orderId = orderService.createCancelledOrderImpl(request);
            log.info("注文キャンセル成功: orderId={}", orderId);
            return orderId;
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 取り寄せ注文の作成
    @PostMapping("/special-order")
    public String createSpecialOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("createSpecialOrder: customerId={}, items={}", request.getCustomerId(), request.getItems());
        
        try {
            // 取り寄せ注文作成処理
            String orderId = orderService.createSpecialOrderImpl(request);
            log.info("取り寄せ注文作成成功: orderId={}", orderId);
            return orderId;
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // カスタマイズオーダーの作成
    @PostMapping("/custom-order")
    public String createCustomOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("createCustomOrder: customerId={}, items={}", request.getCustomerId(), request.getItems());
        
        try {
            // カスタマイズオーダー作成処理
            String orderId = orderService.createCustomOrderImpl(request);
            log.info("カスタマイズオーダー作成成功: orderId={}", orderId);
            return orderId;
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 注文確定
    @PutMapping("/{orderId}/confirm")
    public void confirmOrder(@PathVariable String orderId) {
        log.info("confirmOrder: orderId={}", orderId);
        
        try {
            // 注文確定処理
            orderService.confirmOrderImpl(orderId);
            log.info("注文確定成功: orderId={}", orderId);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }

    // 注文一覧（検索、ページング、ソート対応）
    @GetMapping("/search")
    public IPage<TOrder> searchOrder(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) OffsetDateTime orderDateFrom,
            @RequestParam(required = false) OffsetDateTime orderDateTo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean deleteFlag,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        log.info("searchOrder: orderId={}, customerId={}, orderDateFrom={}, orderDateTo={}, status={}, deleteFlag={}, pageNum={}, pageSize={}, sortBy={}, sortOrder={}",
                orderId, customerId, orderDateFrom, orderDateTo, status, deleteFlag, pageNum, pageSize, sortBy, sortOrder);

        return orderService.searchOrderImpl(
                orderId, customerId, orderDateFrom, orderDateTo, status, deleteFlag,
                pageNum, pageSize, sortBy, sortOrder);
    }

    // 注文詳細
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderDetail(@PathVariable String orderId) {
        log.info("getOrderDetail: orderId={}", orderId);
        
        return orderService.getOrderDetailImpl(orderId);
    }

    // 注文キャンセル（既存注文のキャンセル）
    @PutMapping("/{orderId}/cancel")
    public void cancelOrder(@PathVariable String orderId) {
        log.info("cancelOrder: orderId={}", orderId);
        
        try {
            // 注文キャンセル処理
            orderService.cancelOrderImpl(orderId);
            log.info("注文キャンセル成功: orderId={}", orderId);
        } finally {
            // リクエスト完了後、コンテキストをクリア
            UserContext.clear();
        }
    }
}
