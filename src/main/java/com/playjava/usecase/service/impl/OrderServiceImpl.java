package com.playjava.usecase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.playjava.frameworks.mapper.TOrderMapper;
import com.playjava.frameworks.mapper.TOrderItemMapper;
import com.playjava.frameworks.mapper.MProductMapper;
import com.playjava.enterprise.entity.TOrder;
import com.playjava.enterprise.entity.TOrderItem;
import com.playjava.enterprise.entity.MProduct;
import com.playjava.usecase.port.customer.CustomerExistencePort;
import com.playjava.usecase.port.inventory.StockOperationPort;
import com.playjava.usecase.service.contract.OrderService;
import com.playjava.usecase.dto.CreateOrderRequest;
import com.playjava.usecase.dto.OrderItemRequest;
import com.playjava.usecase.dto.OrderDetailResponse;
import com.playjava.usecase.handler.UuidFactory;
import com.playjava.usecase.handler.GlobalExceptionHandler.StockInsufficientException;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.vavr.control.Option;

@Service
public class OrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder> implements OrderService {

    @Autowired
    private TOrderItemMapper tOrderItemMapper;
    
    @Autowired
    private CustomerExistencePort customerExistencePort;
    
    @Autowired
    private StockOperationPort stockOperationPort;
    
    @Autowired
    private MProductMapper mProductMapper;

    /**
     * 店舗購入の注文作成処理
     * @param request 注文作成リクエスト
     * @return 作成された注文ID
     */
    @Transactional
    public String createStorePurchaseOrderImpl(CreateOrderRequest request) {
        // 1. 顧客の存在確認（Port 経由）
        if (!customerExistencePort.existsActiveCustomer(request.getCustomerId())) {
            throw new RuntimeException("顧客が存在しません: customerId=" + request.getCustomerId());
        }

        // 2. 在庫チェック（在庫不足の商品を収集、Port 経由）
        List<StockInsufficientException.StockInsufficientItem> insufficientItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 商品の存在確認
            MProduct product = mProductMapper.selectById(itemRequest.getProductId());
            Option.of(product)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleteFlag()))
                .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + itemRequest.getProductId()));
            
            int available = stockOperationPort.getStockQuantity(itemRequest.getProductId());
            if (available < itemRequest.getQuantity()) {
                insufficientItems.add(new StockInsufficientException.StockInsufficientItem(
                    itemRequest.getProductId(),
                    available,
                    itemRequest.getQuantity()
                ));
            }
        }
        
        // 3. 在庫不足がある場合は例外を投げる（フロントエンドで取り寄せ/キャンセルを選択させる）
        if (!insufficientItems.isEmpty()) {
            throw new StockInsufficientException("在庫不足の商品があります", insufficientItems);
        }
        
        // 4. 注文IDを生成
        UUID orderId = UuidFactory.newUuid();
        
        // 5. 注文トランザクションを作成
        TOrder order = new TOrder();
        order.setOrderId(orderId.toString());
        order.setCustomerId(request.getCustomerId());
        order.setOrderDate(OffsetDateTime.now());
        order.setStatus(1); // 1: 注文確定（店舗購入は即時確定）
        order.setDeleteFlag(false);
        
        // 6. 注文を先に保存（外部キー制約のため）
        this.save(order);
        
        // 7. 注文明細の作成と在庫減算
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 7-1. 商品の存在確認と単価取得
            MProduct product = mProductMapper.selectById(itemRequest.getProductId());
            Option.of(product)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleteFlag()))
                .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + itemRequest.getProductId()));
            
            // 7-2. 在庫減算（Port 経由）
            stockOperationPort.deductStock(itemRequest.getProductId(), itemRequest.getQuantity());
            
            // 7-3. 注文明細の作成
            TOrderItem orderItem = new TOrderItem();
            orderItem.setOrderItemId(UuidFactory.newUuid().toString());
            orderItem.setOrderId(orderId.toString());
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            
            tOrderItemMapper.insert(orderItem);
        }
        
        return orderId.toString();
    }

    /**
     * 注文キャンセル処理
     * 在庫不足時にキャンセルを選択した場合に使用
     * @param request 注文作成リクエスト
     * @return 作成された注文ID（キャンセル済み）
     */
    @Transactional
    public String createCancelledOrderImpl(CreateOrderRequest request) {
        // 1. 顧客の存在確認（Port 経由）
        if (!customerExistencePort.existsActiveCustomer(request.getCustomerId())) {
            throw new RuntimeException("顧客が存在しません: customerId=" + request.getCustomerId());
        }

        // 2. 注文IDを生成
        UUID orderId = UuidFactory.newUuid();
        
        // 3. 注文トランザクションを作成（キャンセル済み）
        TOrder order = new TOrder();
        order.setOrderId(orderId.toString());
        order.setCustomerId(request.getCustomerId());
        order.setOrderDate(OffsetDateTime.now());
        order.setStatus(6); // 6: キャンセル済み
        order.setDeleteFlag(false);
        
        // 4. 注文を先に保存（外部キー制約のため）
        this.save(order);
        
        // 5. 注文明細の作成（在庫は減算しない）
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 5-1. 商品の存在確認と単価取得
            MProduct product = mProductMapper.selectById(itemRequest.getProductId());
            Option.of(product)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleteFlag()))
                .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + itemRequest.getProductId()));
            
            // 5-2. 注文明細の作成
            TOrderItem orderItem = new TOrderItem();
            orderItem.setOrderItemId(UuidFactory.newUuid().toString());
            orderItem.setOrderId(orderId.toString());
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            
            tOrderItemMapper.insert(orderItem);
        }
        
        return orderId.toString();
    }

    /**
     * 取り寄せ注文の作成処理
     * @param request 注文作成リクエスト
     * @return 作成された注文ID
     */
    @Transactional
    public String createSpecialOrderImpl(CreateOrderRequest request) {
        // 1. 顧客の存在確認（Port 経由）
        if (!customerExistencePort.existsActiveCustomer(request.getCustomerId())) {
            throw new RuntimeException("顧客が存在しません: customerId=" + request.getCustomerId());
        }

        // 2. 注文IDを生成
        UUID orderId = UuidFactory.newUuid();
        
        // 3. 注文トランザクションを作成（取り寄せ中）
        TOrder order = new TOrder();
        order.setOrderId(orderId.toString());
        order.setCustomerId(request.getCustomerId());
        order.setOrderDate(OffsetDateTime.now());
        order.setStatus(2); // 2: 取り寄せ中
        order.setDeleteFlag(false);
        
        // 4. 注文を先に保存（外部キー制約のため）
        this.save(order);
        
        // 5. 注文明細の作成（在庫は減算しない）
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 5-1. 商品の存在確認と単価取得
            MProduct product = mProductMapper.selectById(itemRequest.getProductId());
            Option.of(product)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleteFlag()))
                .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + itemRequest.getProductId()));
            
            // 5-2. 注文明細の作成
            TOrderItem orderItem = new TOrderItem();
            orderItem.setOrderItemId(UuidFactory.newUuid().toString());
            orderItem.setOrderId(orderId.toString());
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            
            tOrderItemMapper.insert(orderItem);
        }
        
        return orderId.toString();
    }

    /**
     * カスタマイズオーダーの作成処理
     * @param request 注文作成リクエスト
     * @return 作成された注文ID
     */
    @Transactional
    public String createCustomOrderImpl(CreateOrderRequest request) {
        // 1. 顧客の存在確認（Port 経由）
        if (!customerExistencePort.existsActiveCustomer(request.getCustomerId())) {
            throw new RuntimeException("顧客が存在しません: customerId=" + request.getCustomerId());
        }

        // 2. 注文IDを生成
        UUID orderId = UuidFactory.newUuid();
        
        // 3. 注文トランザクションを作成（カスタマイズ中）
        TOrder order = new TOrder();
        order.setOrderId(orderId.toString());
        order.setCustomerId(request.getCustomerId());
        order.setOrderDate(OffsetDateTime.now());
        order.setStatus(3); // 3: カスタマイズ中
        order.setDeleteFlag(false);
        
        // 4. 注文を先に保存（外部キー制約のため）
        this.save(order);
        
        // 5. 注文明細の作成（在庫は減算しない）
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 5-1. 商品の存在確認と単価取得
            MProduct product = mProductMapper.selectById(itemRequest.getProductId());
            Option.of(product)
                .filter(p -> !Boolean.TRUE.equals(p.getDeleteFlag()))
                .getOrElseThrow(() -> new RuntimeException("商品が存在しません: productId=" + itemRequest.getProductId()));
            
            // 5-2. 注文明細の作成
            TOrderItem orderItem = new TOrderItem();
            orderItem.setOrderItemId(UuidFactory.newUuid().toString());
            orderItem.setOrderId(orderId.toString());
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            
            tOrderItemMapper.insert(orderItem);
        }
        
        return orderId.toString();
    }

    /**
     * 注文確定処理
     * 取り寄せ注文やカスタマイズオーダーが完了したときに実行
     * @param orderId 注文ID
     * @return 確定成功の場合true
     */
    @Transactional
    public boolean confirmOrderImpl(String orderId) {
        // 1. 注文の存在確認
        TOrder order = this.getById(orderId);
        Option.of(order)
            .filter(o -> !Boolean.TRUE.equals(o.getDeleteFlag()))
            .getOrElseThrow(() -> new RuntimeException("注文が存在しません: orderId=" + orderId));
        
        // 2. 確定可能性の確認
        Integer currentStatus = order.getStatus();
        if (currentStatus == null) {
            throw new RuntimeException("注文ステータスが不正です: orderId=" + orderId);
        }
        
        // 既に確定済みの場合はエラー（冪等性のため）
        if (currentStatus == 1) {
            throw new RuntimeException("注文は既に確定済みです: orderId=" + orderId);
        }
        
        // キャンセル済みの場合はエラー
        if (currentStatus == 6) {
            throw new RuntimeException("キャンセル済みの注文は確定できません: orderId=" + orderId);
        }
        
        // 確定可能なステータスかチェック（0: 仮注文、2: 取り寄せ中、3: カスタマイズ中）
        if (currentStatus != 0 && currentStatus != 2 && currentStatus != 3) {
            throw new RuntimeException("このステータスの注文は確定できません: orderId=" + orderId + ", status=" + currentStatus);
        }
        
        // 3. 注文明細を取得
        LambdaQueryWrapper<TOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(TOrderItem::getOrderId, orderId);
        List<TOrderItem> orderItems = tOrderItemMapper.selectList(itemWrapper);
        
        if (orderItems.isEmpty()) {
            throw new RuntimeException("注文明細が存在しません: orderId=" + orderId);
        }
        
        // 4. 在庫チェックと減算（Port 経由）
        for (TOrderItem orderItem : orderItems) {
            stockOperationPort.deductStock(orderItem.getProductId(), orderItem.getQuantity());
        }
        
        // 5. 注文ステータスを「注文確定（1）」に更新
        order.setStatus(1);
        this.updateById(order);
        
        return true;
    }

    /**
     * 注文検索処理（ページング・ソート対応）
     */
    public IPage<TOrder> searchOrderImpl(
            String orderId,
            String customerId,
            OffsetDateTime orderDateFrom,
            OffsetDateTime orderDateTo,
            Integer status,
            Boolean deleteFlag,
            int pageNum,
            int pageSize,
            String sortBy,
            String sortOrder) {

        Page<TOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TOrder> wrapper = buildSearchWrapper(
                orderId, customerId, orderDateFrom, orderDateTo, status, deleteFlag);

        // ソート設定
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (asc) {
            switch (sortBy) {
                case "orderId":
                    wrapper.orderByAsc(TOrder::getOrderId);
                    break;
                case "customerId":
                    wrapper.orderByAsc(TOrder::getCustomerId);
                    break;
                case "orderDate":
                    wrapper.orderByAsc(TOrder::getOrderDate);
                    break;
                case "status":
                    wrapper.orderByAsc(TOrder::getStatus);
                    break;
                case "createDate":
                    wrapper.orderByAsc(TOrder::getCreateDate);
                    break;
                case "updateDate":
                default:
                    wrapper.orderByAsc(TOrder::getUpdateDate);
                    break;
            }
        } else {
            switch (sortBy) {
                case "orderId":
                    wrapper.orderByDesc(TOrder::getOrderId);
                    break;
                case "customerId":
                    wrapper.orderByDesc(TOrder::getCustomerId);
                    break;
                case "orderDate":
                    wrapper.orderByDesc(TOrder::getOrderDate);
                    break;
                case "status":
                    wrapper.orderByDesc(TOrder::getStatus);
                    break;
                case "createDate":
                    wrapper.orderByDesc(TOrder::getCreateDate);
                    break;
                case "updateDate":
                default:
                    wrapper.orderByDesc(TOrder::getUpdateDate);
                    break;
            }
        }

        return this.page(page, wrapper);
    }

    /**
     * 注文検索条件の共通Wrapper生成
     */
    private LambdaQueryWrapper<TOrder> buildSearchWrapper(
            String orderId,
            String customerId,
            OffsetDateTime orderDateFrom,
            OffsetDateTime orderDateTo,
            Integer status,
            Boolean deleteFlag) {

        LambdaQueryWrapper<TOrder> wrapper = new LambdaQueryWrapper<>();

        // 注文ID（完全一致）
        if (orderId != null && !orderId.isEmpty()) {
            wrapper.eq(TOrder::getOrderId, orderId);
        }

        // 顧客ID（完全一致）
        if (customerId != null && !customerId.isEmpty()) {
            wrapper.eq(TOrder::getCustomerId, customerId);
        }

        // 注文日時（範囲指定）
        if (orderDateFrom != null) {
            wrapper.ge(TOrder::getOrderDate, orderDateFrom);
        }
        if (orderDateTo != null) {
            wrapper.le(TOrder::getOrderDate, orderDateTo);
        }

        // ステータス（完全一致）
        if (status != null) {
            wrapper.eq(TOrder::getStatus, status);
        }

        // 削除フラグ（未指定時はfalseのみ、明示的に指定した場合のみ削除済みも含める）
        if (deleteFlag != null) {
            wrapper.eq(TOrder::getDeleteFlag, deleteFlag);
        } else {
            wrapper.eq(TOrder::getDeleteFlag, false);
        }

        return wrapper;
    }

    /**
     * 注文詳細取得処理
     * @param orderId 注文ID
     * @return 注文詳細情報
     */
    public OrderDetailResponse getOrderDetailImpl(String orderId) {
        // 1. 注文の存在確認
        TOrder order = this.getById(orderId);
        Option.of(order)
            .filter(o -> !Boolean.TRUE.equals(o.getDeleteFlag()))
            .getOrElseThrow(() -> new RuntimeException("注文が存在しません: orderId=" + orderId));
        
        // 2. 注文明細を取得
        LambdaQueryWrapper<TOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(TOrderItem::getOrderId, orderId);
        List<TOrderItem> orderItems = tOrderItemMapper.selectList(itemWrapper);
        
        // 3. レスポンスを作成
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrder(order);
        response.setItems(orderItems);
        
        return response;
    }

    /**
     * 注文キャンセル処理（既存注文のキャンセル）
     * @param orderId 注文ID
     * @return キャンセル成功の場合true
     */
    @Transactional
    public boolean cancelOrderImpl(String orderId) {
        // 1. 注文の存在確認
        TOrder order = this.getById(orderId);
        Option.of(order)
            .filter(o -> !Boolean.TRUE.equals(o.getDeleteFlag()))
            .getOrElseThrow(() -> new RuntimeException("注文が存在しません: orderId=" + orderId));
        
        // 2. キャンセル可能性の確認
        Integer currentStatus = order.getStatus();
        if (currentStatus == null) {
            throw new RuntimeException("注文ステータスが不正です: orderId=" + orderId);
        }
        
        // 既にキャンセル済みの場合はエラー（冪等性のため）
        if (currentStatus == 6) {
            throw new RuntimeException("注文は既にキャンセル済みです: orderId=" + orderId);
        }
        
        // 配送完了済みの場合はキャンセル不可
        if (currentStatus == 5) {
            throw new RuntimeException("配送完了済みの注文はキャンセルできません: orderId=" + orderId);
        }
        
        // 3. 在庫の戻し（既に在庫を減算している場合のみ、Port 経由）
        // status=1（注文確定）の場合のみ在庫を戻す
        if (currentStatus == 1) {
            LambdaQueryWrapper<TOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(TOrderItem::getOrderId, orderId);
            List<TOrderItem> orderItems = tOrderItemMapper.selectList(itemWrapper);
            for (TOrderItem orderItem : orderItems) {
                stockOperationPort.returnStock(orderItem.getProductId(), orderItem.getQuantity());
            }
        }
        
        // 4. 注文ステータスを「キャンセル済み（6）」に更新
        order.setStatus(6);
        this.updateById(order);
        
        return true;
    }
}
