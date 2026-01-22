# 境界づけられたコンテキスト設計

## 概要

システムを複数の境界づけられたコンテキスト（Bounded Context）に分割し、各コンテキストが独立したドメインモデルと責務を持つように設計する。

## 現状の分析

### 現在の依存関係

```
OrderServiceImpl
  ├─ MCustomerMapper (顧客マスタ)
  ├─ MProductMapper (商品マスタ)
  └─ MStockMapper (在庫マスタ)

MStockServiceImpl
  └─ MProductMapper (商品マスタ)
```

### 問題点

1. **直接的な依存関係**: 注文コンテキストが他のコンテキスト（顧客、商品、在庫）に直接依存している
2. **責務の混在**: 注文サービスが在庫管理のロジックを含んでいる
3. **結合度の高さ**: 他のコンテキストの変更が注文コンテキストに影響を与える可能性

## 提案する境界づけられたコンテキスト

### 1. 顧客管理コンテキスト (Customer Management Context)

**責務**
- 顧客情報の管理
- 顧客のライフサイクル管理（登録、更新、削除）
- 顧客検索・一覧取得

**エンティティ**
- `MCustomer` (顧客マスタ)

**サービス**
- `MCustomerService` / `MCustomerServiceImpl`

**コントローラー**
- `MCustomerController`

**公開API（他コンテキスト向け）**
- `getCustomer(String customerId)`: 顧客情報の取得（存在確認用）
- `isCustomerActive(String customerId)`: 顧客の有効性チェック

**依存関係**
- なし（独立したコンテキスト）

---

### 2. 商品管理コンテキスト (Product Management Context)

**責務**
- 商品情報の管理
- 商品のライフサイクル管理（登録、更新、削除）
- 商品検索・一覧取得
- 商品価格管理

**エンティティ**
- `MProduct` (商品マスタ)

**サービス**
- `MProductService` / `MProductServiceImpl` (未実装)

**コントローラー**
- `MProductController` (未実装)

**公開API（他コンテキスト向け）**
- `getProduct(String productId)`: 商品情報の取得（存在確認用）
- `getProductPrice(String productId)`: 商品価格の取得
- `isProductActive(String productId)`: 商品の有効性チェック

**依存関係**
- なし（独立したコンテキスト）

---

### 3. 在庫管理コンテキスト (Inventory Management Context)

**責務**
- 在庫情報の管理
- 在庫の増減管理
- 在庫照会
- 在庫不足チェック

**エンティティ**
- `MStock` (在庫マスタ)

**サービス**
- `MStockService` / `MStockServiceImpl`

**コントローラー**
- `MStockController`

**公開API（他コンテキスト向け）**
- `checkStockAvailability(String productId, Integer quantity)`: 在庫可用性チェック
- `deductStock(String productId, Integer quantity)`: 在庫減算
- `returnStock(String productId, Integer quantity)`: 在庫戻し
- `getStockQuantity(String productId)`: 在庫数量取得

**依存関係**
- `Product Management Context` (商品の存在確認)

**統合パターン**
- **Published Language**: 在庫操作の結果を明確なDTOで返す
- **Anti-Corruption Layer**: 商品コンテキストへの依存を抽象化

---

### 4. 注文管理コンテキスト (Order Management Context)

**責務**
- 注文の作成・管理
- 注文のライフサイクル管理（作成、確定、キャンセル）
- 注文検索・一覧取得
- 注文詳細取得

**エンティティ**
- `TOrder` (注文トランザクション)
- `TOrderItem` (注文明細トランザクション)

**サービス**
- `OrderService` / `OrderServiceImpl`

**コントローラー**
- `OrderController`

**公開API（他コンテキスト向け）**
- なし（注文は他のコンテキストから参照されるのみ）

**依存関係**
- `Customer Management Context` (顧客の存在確認)
- `Product Management Context` (商品情報・価格取得)
- `Inventory Management Context` (在庫チェック・在庫操作)

**統合パターン**
- **Open Host Service**: 在庫管理コンテキストの公開APIを使用
- **Anti-Corruption Layer**: 他のコンテキストへの依存を抽象化

---

### 5. 認証・認可コンテキスト (Authentication & Authorization Context)

**責務**
- ユーザー管理
- 認証・認可
- セッション管理

**エンティティ**
- `MUser` (ユーザーマスタ)

**サービス**
- `MUserService` / `MUserServiceImpl`

**コントローラー**
- `MUserController`

**公開API（他コンテキスト向け）**
- `getCurrentUser()`: 現在のユーザー情報取得
- `hasPermission(String userId, String permission)`: 権限チェック

**依存関係**
- なし（独立したコンテキスト）

---

## コンテキスト間の統合パターン

### 1. 共有カーネル (Shared Kernel)

**適用範囲**
- 共通の値オブジェクト（UUID、日時など）
- 共通の例外クラス
- 共通のユーティリティ

**実装**
- `com.playjava.handler.UuidFactory`
- `com.playjava.exception.GlobalExceptionHandler`
- `com.playjava.context.UserContext`

### 2. 公開ホストサービス (Open Host Service)

**適用範囲**
- 在庫管理コンテキストの在庫操作API
- 商品管理コンテキストの商品情報取得API
- 顧客管理コンテキストの顧客情報取得API

**実装方針**
- 各コンテキストのServiceインターフェースを公開APIとして定義
- 他コンテキストからはServiceインターフェース経由でアクセス

### 3. 腐敗防止層 (Anti-Corruption Layer)

**適用範囲**
- 注文管理コンテキストが他のコンテキストに依存する際の抽象化

**実装方針**
- 各コンテキストのServiceインターフェースをAdapterとして定義
- 注文コンテキスト内で、他のコンテキストへの依存を抽象化

### 4. 公開言語 (Published Language)

**適用範囲**
- コンテキスト間でやり取りするDTO
- エラーレスポンス

**実装方針**
- 各コンテキストの公開APIのリクエスト/レスポンスを明確に定義
- ドキュメント化

---

## コンテキスト間のDTO設計方針

### DDDの思想に基づく原則

**重要な原則: Entityはコンテキストの内部実装の詳細であり、外部に公開しない**

各境界づけられたコンテキスト（Bounded Context）は、独立したドメインモデルを持ちます。各コンテキストのEntityは、そのコンテキスト内部でのみ使用される実装の詳細であり、他のコンテキストから直接参照すべきではありません。

### なぜDTOが必要か

1. **ドメインモデルの独立性の保護**
   - 各コンテキストのEntityは、そのコンテキスト固有のドメインモデルを表現している
   - Entityを直接共有すると、コンテキスト間の結合度が高まり、一方の変更が他方に影響を与える
   - DTOを使用することで、各コンテキストの内部実装の変更が他のコンテキストに影響を与えない

2. **公開言語（Published Language）としての役割**
   - DTOは、コンテキスト間で合意された「公開言語」として機能する
   - 各コンテキストが公開する情報を明確に定義し、契約として機能する
   - コンテキスト間の通信の意味を明確にする

3. **腐敗防止層（Anti-Corruption Layer）の実装**
   - DTOを使用することで、他のコンテキストの内部実装（Entity）に依存しない
   - 他のコンテキストの変更から自コンテキストを保護する

### 設計方針

#### 1. 各コンテキストごとにDTOを定義

**原則**: コンテキスト間の通信には、必ずDTOを使用する

```
Customer Management Context
  └─ CustomerDTO (公開用)
      - customerId
      - customerName
      - isActive

Product Management Context
  └─ ProductDTO (公開用)
      - productId
      - productName
      - price
      - isActive

Inventory Management Context
  └─ StockAvailabilityDTO (公開用)
      - productId
      - availableQuantity
      - isAvailable
```

#### 2. EntityとDTOの役割分担

| 種類 | 役割 | 使用範囲 |
|------|------|----------|
| **Entity** | コンテキスト内部のドメインモデル | コンテキスト内部のみ |
| **DTO** | コンテキスト間の通信データ | コンテキスト間の通信 |

**Entityの特徴**
- コンテキスト内部でのみ使用される
- データベース構造と1:1の関係を持つ
- コンテキスト固有のビジネスロジックを含む可能性がある
- 他のコンテキストから直接参照されない

**DTOの特徴**
- コンテキスト間の通信に使用される
- 必要な情報のみを含む（公開すべき情報のみ）
- コンテキスト間で合意された契約として機能する
- バリデーションを含む（必要に応じて）

#### 3. 実装例

**例1: 顧客管理コンテキストの公開API**

```java
// Customer Management Context
// 公開用DTO
public class CustomerDTO {
    private String customerId;
    private String customerName;
    private Boolean isActive;
    // 他のコンテキストに必要な情報のみを含む
}

// 公開API
public interface CustomerService {
    /**
     * 顧客情報を取得（他コンテキスト向け）
     * @param customerId 顧客ID
     * @return CustomerDTO（Entityではない）
     */
    CustomerDTO getCustomer(String customerId);
    
    /**
     * 顧客の有効性をチェック
     * @param customerId 顧客ID
     * @return 有効な場合true
     */
    boolean isCustomerActive(String customerId);
}

// 実装
@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private MCustomerMapper customerMapper;
    
    @Override
    public CustomerDTO getCustomer(String customerId) {
        // Entityを取得
        MCustomer entity = customerMapper.selectById(customerId);
        
        // DTOに変換（Entityを直接返さない）
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(entity.getCustomerId());
        dto.setCustomerName(entity.getCustomerName());
        dto.setIsActive(!Boolean.TRUE.equals(entity.getDeleteFlag()));
        
        return dto;
    }
}
```

**例2: 在庫管理コンテキストの公開API**

```java
// Inventory Management Context
// 公開用DTO
public class StockAvailabilityDTO {
    private String productId;
    private Integer availableQuantity;
    private Boolean isAvailable;
}

public class StockOperationResultDTO {
    private String productId;
    private Integer quantity;
    private Boolean success;
    private String message;
}

// 公開API
public interface InventoryService {
    /**
     * 在庫可用性チェック
     * @param productId 商品ID
     * @param quantity 必要数量
     * @return 在庫可用性情報（DTO）
     */
    StockAvailabilityDTO checkStockAvailability(String productId, Integer quantity);
    
    /**
     * 在庫減算
     * @param productId 商品ID
     * @param quantity 減算数量
     * @return 操作結果（DTO）
     * @throws StockInsufficientException 在庫不足の場合
     */
    StockOperationResultDTO deductStock(String productId, Integer quantity);
    
    /**
     * 在庫戻し
     * @param productId 商品ID
     * @param quantity 戻し数量
     * @return 操作結果（DTO）
     */
    StockOperationResultDTO returnStock(String productId, Integer quantity);
}

// 実装
@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private MStockMapper stockMapper;
    
    @Override
    public StockAvailabilityDTO checkStockAvailability(String productId, Integer quantity) {
        // Entityを取得
        MStock stock = stockMapper.selectOne(...);
        
        // DTOに変換（Entityを直接返さない）
        StockAvailabilityDTO dto = new StockAvailabilityDTO();
        dto.setProductId(productId);
        dto.setAvailableQuantity(stock.getQuantity());
        dto.setIsAvailable(stock.getQuantity() >= quantity);
        
        return dto;
    }
}
```

**例3: 注文管理コンテキストでの使用**

```java
// Order Management Context
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private CustomerService customerService; // 顧客管理コンテキストの公開API
    
    @Autowired
    private InventoryService inventoryService; // 在庫管理コンテキストの公開API
    
    @Autowired
    private ProductService productService; // 商品管理コンテキストの公開API
    
    public String createStorePurchaseOrderImpl(CreateOrderRequest request) {
        // 1. 顧客の存在確認（DTOを使用）
        CustomerDTO customer = customerService.getCustomer(request.getCustomerId());
        if (customer == null || !customer.getIsActive()) {
            throw new RuntimeException("顧客が存在しません");
        }
        
        // 2. 在庫チェック（DTOを使用）
        for (OrderItemRequest item : request.getItems()) {
            StockAvailabilityDTO stock = inventoryService.checkStockAvailability(
                item.getProductId(), item.getQuantity());
            
            if (!stock.getIsAvailable()) {
                throw new StockInsufficientException(...);
            }
        }
        
        // 3. 注文作成処理
        // ...
    }
}
```

### DDDの思想としての正当性

この方針は、以下のDDDの原則に基づいています：

1. **境界づけられたコンテキスト（Bounded Context）の独立性**
   - 各コンテキストは独立したドメインモデルを持つ
   - コンテキスト間の結合を最小限に抑える

2. **公開言語（Published Language）**
   - コンテキスト間の通信には、明確に定義された言語（DTO）を使用する
   - 各コンテキストが公開する情報を明確に定義する

3. **腐敗防止層（Anti-Corruption Layer）**
   - 他のコンテキストの内部実装（Entity）に依存しない
   - DTOを使用することで、他のコンテキストの変更から保護される

4. **関心の分離（Separation of Concerns）**
   - Entityはコンテキスト内部の実装の詳細
   - DTOはコンテキスト間の通信の契約

### 実装の優先順位

#### 優先度: 高

1. **各コンテキストの公開APIでDTOを使用**
   - 顧客管理コンテキスト: `CustomerDTO`を定義
   - 在庫管理コンテキスト: `StockAvailabilityDTO`, `StockOperationResultDTO`を定義
   - 商品管理コンテキスト: `ProductDTO`を定義

2. **EntityからDTOへの変換ロジックを実装**
   - 各コンテキストのService実装で、Entity→DTOの変換を行う
   - マッパーメソッドまたは専用のConverterクラスを使用

#### 優先度: 中

3. **DTOのバリデーション**
   - 各コンテキストの公開APIのDTOにバリデーションを追加
   - コンテキスト間の契約を明確にする

#### 優先度: 低

4. **DTOのバージョニング**
   - コンテキスト間の契約変更に対応するためのバージョニング戦略
   - 後方互換性の維持

### 注意事項

1. **過度なDTOの作成を避ける**
   - 単純な値（String, Integerなど）はDTO化不要
   - 複雑なオブジェクトや、複数の情報を含む場合にDTOを使用

2. **DTOの粒度**
   - コンテキスト間で必要な情報のみを含める
   - 過度に詳細な情報を含めない（情報隠蔽）

3. **変換コスト**
   - Entity→DTOの変換は軽量に保つ
   - 必要に応じてキャッシュを検討

4. **ドキュメント化**
   - 各コンテキストの公開APIとDTOを明確にドキュメント化
   - コンテキスト間の契約を明確にする

---

## 実装方針

### フェーズ1: コンテキストの明確化（現状維持）

**目標**
- 各コンテキストの責務を明確化
- 依存関係を可視化

**作業**
1. 各コンテキストのServiceインターフェースを公開APIとして明確化
2. コンテキスト間の依存関係をドキュメント化
3. 各コンテキストの責務を明確化

### フェーズ2: 依存関係の抽象化（推奨）

**目標**
- コンテキスト間の直接的な依存を削減
- インターフェース経由の依存に変更

**作業**
1. 各コンテキストの公開APIをServiceインターフェースとして定義
2. 注文コンテキストが他のコンテキストのMapperに直接依存しないように変更
3. 在庫管理コンテキストの公開API（deductStock, returnStock）を実装

**例: 在庫管理コンテキストの公開API（DTO使用）**

```java
// Inventory Management Context
// 公開用DTO
public class StockAvailabilityDTO {
    private String productId;
    private Integer availableQuantity;
    private Boolean isAvailable;
}

public class StockOperationResultDTO {
    private String productId;
    private Integer quantity;
    private Boolean success;
    private String message;
}

// 公開API（DTOを使用）
public interface InventoryService {
    /**
     * 在庫可用性チェック
     * @param productId 商品ID
     * @param quantity 必要数量
     * @return 在庫可用性情報（DTO、Entityではない）
     */
    StockAvailabilityDTO checkStockAvailability(String productId, Integer quantity);
    
    /**
     * 在庫減算
     * @param productId 商品ID
     * @param quantity 減算数量
     * @return 操作結果（DTO）
     * @throws StockInsufficientException 在庫不足の場合
     */
    StockOperationResultDTO deductStock(String productId, Integer quantity);
    
    /**
     * 在庫戻し
     * @param productId 商品ID
     * @param quantity 戻し数量
     * @return 操作結果（DTO）
     */
    StockOperationResultDTO returnStock(String productId, Integer quantity);
}
```

**例: 注文コンテキストでの使用（DTO経由）**

```java
// Order Management Context
@Service
public class OrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder> implements OrderService {
    
    @Autowired
    private InventoryService inventoryService; // 在庫管理コンテキストの公開API
    
    @Autowired
    private CustomerService customerService; // 顧客管理コンテキストの公開API
    
    @Autowired
    private ProductService productService; // 商品管理コンテキストの公開API
    
    public String createStorePurchaseOrderImpl(CreateOrderRequest request) {
        // 1. 顧客の存在確認（DTOを使用、Entityを直接取得しない）
        CustomerDTO customer = customerService.getCustomer(request.getCustomerId());
        if (customer == null || !customer.getIsActive()) {
            throw new RuntimeException("顧客が存在しません");
        }
        
        // 2. 在庫チェック（DTOを使用、Entityを直接取得しない）
        for (OrderItemRequest item : request.getItems()) {
            StockAvailabilityDTO stock = inventoryService.checkStockAvailability(
                item.getProductId(), item.getQuantity());
            
            if (!stock.getIsAvailable()) {
                throw new StockInsufficientException(...);
            }
        }
        
        // 3. 注文作成処理
        // ...
    }
    
    // Mapperへの直接依存を削除（Entityを直接取得しない）
    // @Autowired
    // private MStockMapper mStockMapper; // 削除
    // @Autowired
    // private MCustomerMapper mCustomerMapper; // 削除
}
```

### フェーズ3: 商品管理コンテキストの実装（推奨）

**目標**
- 商品管理コンテキストを独立したコンテキストとして実装

**作業**
1. `MProductService` / `MProductServiceImpl` を実装
2. `MProductController` を実装
3. 在庫管理コンテキストと注文管理コンテキストが商品管理コンテキストの公開APIを使用するように変更

---

## コンテキストマップ

```
┌─────────────────────────────────────────────────────────────┐
│                    認証・認可コンテキスト                      │
│                    (Authentication Context)                   │
│                                                               │
│  - MUser                                                      │
│  - MUserService                                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ (UserContext経由)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    顧客管理コンテキスト                        │
│                  (Customer Management Context)               │
│                                                               │
│  - MCustomer                                                 │
│  - MCustomerService                                          │
│                                                               │
│  公開API:                                                    │
│  - getCustomer(customerId)                                   │
│  - isCustomerActive(customerId)                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ (依存)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    商品管理コンテキスト                        │
│                  (Product Management Context)                │
│                                                               │
│  - MProduct                                                  │
│  - MProductService (未実装)                                  │
│                                                               │
│  公開API:                                                    │
│  - getProduct(productId)                                     │
│  - getProductPrice(productId)                               │
│  - isProductActive(productId)                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ (依存)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    在庫管理コンテキスト                        │
│                  (Inventory Management Context)              │
│                                                               │
│  - MStock                                                    │
│  - MStockService                                             │
│                                                               │
│  公開API:                                                    │
│  - checkStockAvailability(productId, quantity)               │
│  - deductStock(productId, quantity)                          │
│  - returnStock(productId, quantity)                         │
│  - getStockQuantity(productId)                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ (依存)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    注文管理コンテキスト                        │
│                   (Order Management Context)                 │
│                                                               │
│  - TOrder, TOrderItem                                        │
│  - OrderService                                              │
│                                                               │
│  依存:                                                       │
│  - Customer Management Context                               │
│  - Product Management Context                                 │
│  - Inventory Management Context                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 実装の優先順位

### 優先度: 高

1. **在庫管理コンテキストの公開API実装（DTO使用）**
   - `StockAvailabilityDTO`, `StockOperationResultDTO` を定義
   - `deductStock`, `returnStock`, `checkStockAvailability` を公開APIとして実装（DTOを返す）
   - Entity→DTOの変換ロジックを実装
   - 注文コンテキストが在庫Mapperに直接依存しないように変更

2. **顧客管理コンテキストの公開API明確化（DTO使用）**
   - `CustomerDTO` を定義
   - `getCustomer`, `isCustomerActive` を公開APIとして明確化（DTOを返す）
   - Entity→DTOの変換ロジックを実装
   - 注文コンテキストが顧客Mapperに直接依存しないように変更

### 優先度: 中

3. **商品管理コンテキストの実装（DTO使用）**
   - `ProductDTO` を定義
   - `MProductService` / `MProductServiceImpl` を実装（DTOを返す）
   - `MProductController` を実装
   - Entity→DTOの変換ロジックを実装
   - 在庫管理コンテキストと注文管理コンテキストが商品管理コンテキストの公開APIを使用するように変更

### 優先度: 低

4. **腐敗防止層の実装**
   - 各コンテキストのAdapterレイヤーを実装
   - コンテキスト間の依存をより抽象化

---

## 注意事項

1. **段階的な移行**: 一度にすべてを変更せず、段階的に移行する
2. **後方互換性**: 既存のAPIを壊さないように注意する
3. **テスト**: 各フェーズで十分なテストを実施する
4. **ドキュメント**: コンテキスト間の依存関係と公開APIを明確にドキュメント化する

---

## 参考資料

- Domain-Driven Design (Eric Evans)
- Implementing Domain-Driven Design (Vaughn Vernon)
- 境界づけられたコンテキスト (Bounded Context)
- コンテキストマッピング (Context Mapping)
