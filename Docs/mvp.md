# MVP
## 機能分類
- 商品マスタ管理
- 顧客マスタ管理
- 在庫管理
- 注文取引

### 商品マスタ管理
- 商品登録・編集・削除
- 商品一覧
- 価格設定

### 顧客マスタ管理
- 顧客登録・編集・削除
- 顧客一覧

### 在庫管理
- 在庫登録(SKU, 数量)・編集・削除
- 在庫一覧
- 在庫照会

### 注文取引
- 注文作成
- 注文確定
- 注文一覧
- 注文詳細
- 注文キャンセル

## システム設計
### Entity
DB構造と1:1
状態はenumかflagを用いる

### Mapper
InterfaceとしてのRepositryの役割を担う。
DB操作の抽象化を担当する部分である。
カスタムクエリの定義はMapperで行う。複雑なクエリの定義はMapperの責務とする。

### Service
** Adapter **
Mapperを除く、Interfaceを定義する。

** Impl **
Adapterで定義したInterfaceを実装する。
また、Mapperで定義したメソッドを実装する。

### Handler
処理の振る舞いを制御する。ここでのHandlerはAPIを制御するHandlerではない。
主にMybatis Plusの制御を行う。

### Controller
各APIエンドポイントの処理の流れの制御を行う。

## テーブル論理設計
### ユーザーマスタ
- user_id : UUID
- user_name : VARCHAR(50)
- password : VARCHAR(100)
- role : SMALLINT
- delete_flag : BOOLEAN
- create_user : VARCHAR(50)
- create_date : DATETIME
- update_user : VARCHAR(50)
- update_date : DATETIME

主キー user_id
ユニークキー user_name
インデックス user_name, role

### 顧客マスタ
- customer_id       : UUID
- customer_number   : VARCHAR(10)
- customer_name     : VARCHAR(50)
- address           : VARCHAR(100)
- phone_number      : VARCHAR(20)
- email             : VARCHAR(50)
- delete_flag       : BOOLEAN
- create_user       : UUID
- create_date       : DATETIME
- update_user       : UUID
- update_date       : DATETIME

主キー customer_id
ユニークキー customer_number
インデックス customer_name, address, phone_number, email
インデックスは、検索で主に使われることが想定されるデータを指定する。

### 商品マスタ
- product_id        : UUID
- product_number    : VARCHAR(10)
- product_name      : VARCHAR(50)
- description       : VARCHAR(100)
- price             : INT
- category          : SMALLINT
- delete_flag       : BOOLEAN
- create_user       : UUID
- create_date       : DATETIME
- update_user       : UUID
- update_date       : DATETIME

主キー product_id
ユニークキー product_number
インデックス product_name, price
categoryはSMALLINT、選択項目であるためカーディナリティが低いため、
インデックスには含めない

### 在庫マスタ
- stock_id          : UUID
- product_id        : UUID
- quantity          : INT
- status            : SMALLINT
- delete_flag       : BOOLEAN
- create_user       : UUID
- create_date       : DATETIME
- update_user       : UUID
- update_date       : DATETIME

主キー stock_id
ユニークキー product_id
外部キー product_id
インデックス product_id

### 注文トランザクション
- order_id          : UUID
- customer_id       : UUID
- order_date        : DATETIME
- status            : SMALLINT
- delete_flag       : BOOLEAN
- create_user       : UUID
- create_date       : DATETIME
- update_user       : UUID
- update_date       : DATETIME

主キー order_id
外部キー customer_id
インデックス customer_id, order_date

### 注文明細トランザクション
- order_item_id    : UUID
- order_id         : UUID
- product_id       : UUID
- quantity         : INT
- unit_price       : INT
- create_date      : DATETIME
- update_date      : DATETIME

主キー order_item_id
外部キー order_id, product_id
インデックス order_id
