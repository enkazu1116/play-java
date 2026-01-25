# アーキテクチャ設計
## MVC + 拡張設計をクリーンアーキテクチャに
### 課題
現状、MVCにDTOやHandlerなど役割ごとにフォルダを分けているが、
役割が増えてきており、わかりづらくなっている。

### 解決方法
Clean Architectureを採用し、システム内の構成を4つの階層ごとに分割し、
責務をより明確化する。

---

## 分割方針と各階層の役割

### Enterprise Business Rules（企業／ドメインのビジネスルール）
最も内側の核。データ構造と区切られた文脈（Bounded Context）の「あるべき形」を定義する。
技術やフレームワークに依存しない。

| 役割 | 説明 |
|------|------|
| データ構造の定義 | エンティティ・値オブジェクトにより、ドメインの概念を表現する |
| 文脈の境界 | どのエンティティがどの文脈に属するかをコード上の配置で示す |

**フォルダ構成**
- `entity` … 識別子を持ちライフサイクルがあるドメインオブジェクト
- `valueobject` … 識別子を持たず属性で同一性が決まるオブジェクト

---

### Application Business Rules（アプリケーションのユースケース）
「何を実現するか」を定める層。エンティティの組み合わせ方や、一連の操作の流れを定義する。
入力装置（HTTP など）や永続化の詳細には依存しない。

| 役割 | 説明 |
|------|------|
| ユースケースの表明 | 利用者ができる操作の種類と、その結果を定義する |
| 文脈のオーケストレーション | 複数文脈にまたがる場合は Port を通して他文脈の情報を集め、一つのユースケースを形にする |

**フォルダ構成**
- `controller` … HTTP リクエストを受け取り、ユースケースを起動し、結果を返す入り口（Application の「何をやるか」を外部に公開する窓口）

---

### Interface Adapters（インターフェース適合）
外部世界（HTTP、DB、他文脈）と Enterprise / Application の間の変換を行う。
技術的な入力・出力・永続化の「形」をここで決める。

| 役割 | 説明 |
|------|------|
| 入力の適合 | DTO やリクエスト形式に合わせて、ユースケースが使える形に変換する |
| 出力の適合 | ユースケースの結果をレスポンスやドキュメントの形に変換する |
| 他文脈への窓口 | **Port（インターフェース）** を定義し、他文脈からの「問い合わせ」を型で表現する |
| ビジネスロジックの実装 | Service の impl がユースケースの手順（オーケストレーション）を実装する |

**フォルダ構成**
- `dto` … リクエスト／レスポンスや、文脈をまたぐデータの形
- `handler` … 例外処理・リクエスト前後の共通処理・ユーティリティ的な処理
- `service.contract` … 他層から呼ばれる**サービスのインターフェース（契約）**。Frameworks の `interface`（Port 実装）と区別するため、こちらは「契約」を表す `contract` を用いる
- `service.impl` … 上記の実装。ユースケースの手順と、Port 経由の問い合わせを記述する
- `port` … **他文脈や外部システムへの「問い合わせ」のインターフェース**（後述）

---

### Frameworks & Drivers（フレームワークと駆動装置）
DB・HTTP・設定など、具体的な技術に強く依存する部分。
ビジネスルールからは参照されず、Port の実装（`interface`）や Mapper がここに属する。

| 役割 | 説明 |
|------|------|
| 永続化 | Mapper が DB アクセスを担う。Port の実装が Mapper を呼ぶこともある |
| 設定・横断関心 | フレームワークの設定や、スレッドローカルなコンテキストなど |
| 他文脈との接続 | **interface** に、Port の実装を置く。他文脈の Mapper や API を呼び、Adapters 層の Port を満たす。「Adapter」と区別するため、この層のフォルダ名は `interface` とする |

**フォルダ構成**
- `mapper` … MyBatis 等による永続化
- `config` … Spring 等の設定
- `context` … リクエストスコープの情報（ユーザー情報など）
- `interface` … **Port の実装**。他文脈の Mapper や API を呼び、Adapters の Port インターフェースを満たす。Frameworks 側の「外部 I/O の実装」であり、Interface Adapters の「Adapter」と混同しないよう、フォルダ名を `interface` にしている

---

## 区切られた文脈の扱い方

### 現在
各ドメインごとに必要な情報があれば、直接参照している。
各ドメインの Mapper や Service を呼び出し、DI をすることで利用を行っている。

### 解決方法（Port でラップする）
複数にまたがった文脈を扱うときは **Port を利用する**。

- 現在、Adapter（インターフェース）と impl で実装しているが、**他文脈に依存する部分**は Port のインターフェースでラップする。
- ある文脈の「ユースケース」は、**Port という窓口だけ**を使って他文脈に問い合わせる。Mapper や他文脈の Service に直接依存しない。
- Port の窓口から必要情報を集め、一つの文脈（一つのユースケース）を形成する。

**Port の置き場所**
- **Port のインターフェース** … Interface Adapters の `port` に置く（「他文脈への問い合わせ」の契約はアプリケーションに近い適合層の責務）。
- **Port の実装** … Frameworks & Drivers の `interface` に置く。他文脈の Mapper や API を呼ぶ「駆動側」の実装。階層内の「Adapter」との混同を避けるため、フォルダ名は `interface` とする。

この形にすると、他文脈の Mapper やテーブル構造が変わっても、影響は Port の実装だけに収まり、Enterprise / Application に近い層は安定する。

---

## フォルダ構成の具体案（パッケージ構成）

階層の役割をパッケージ名で明確にする場合の一例。

```
com.playjava/
├── PlayjavaApplication.java
│
├── enterprise/                    # Enterprise Business Rules
│   ├── entity/
│   │   ├── MCustomer.java
│   │   ├── MProduct.java
│   │   ├── MStock.java
│   │   ├── MUser.java
│   │   ├── TOrder.java
│   │   └── TOrderItem.java
│   └── valueobject/
│       ├── SystemUser.java
│       └── UserRole.java
│
├── application/                   # Application Business Rules
│   └── controller/
│       ├── MCustomerController.java
│       ├── MStockController.java
│       ├── MUserController.java
│       └── OrderController.java
│
├── adapters/                      # Interface Adapters
│   ├── dto/
│   │   ├── CreateOrderRequest.java
│   │   ├── OrderDetailResponse.java
│   │   └── OrderItemRequest.java
│   ├── handler/
│   │   ├── CustomerNumberGenerator.java
│   │   ├── FillMetaObjectHandler.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── UuidFactory.java
│   ├── port/                      # 他文脈・外部への問い合わせのインターフェース
│   │   └── product/
│   │       └── ProductExistencePort.java   # 例: 商品の存在・有効性の問い合わせ
│   ├── service/
│   │   ├── contract/              # サービスインターフェース（契約）。Frameworks の interface と区別
│   │   │   ├── MCustomerService.java
│   │   │   ├── MStockService.java
│   │   │   ├── MUserService.java
│   │   │   └── OrderService.java
│   │   └── impl/                  # ユースケースの実装
│   │       ├── MCustomerServiceImpl.java
│   │       ├── MStockServiceImpl.java
│   │       ├── MUserServiceImpl.java
│   │       └── OrderServiceImpl.java
│
└── frameworks/                    # Frameworks & Drivers
    ├── config/
    │   ├── MybatisPlusConfig.java
    │   └── OpenApiConfig.java
    ├── context/
    │   └── UserContext.java
    ├── mapper/
    │   ├── MCustomerMapper.java
    │   ├── MProductMapper.java
    │   ├── MStockMapper.java
    │   ├── MUserMapper.java
    │   ├── TOrderItemMapper.java
    │   └── TOrderMapper.java
    └── interface/                 # Port の実装（他文脈の Mapper/API を呼ぶ）。"Adapter" と区別するため interface
        └── product/
            └── ProductExistenceAdapter.java  # 例: ProductExistencePort の実装（クラス名は「○○Adapter」のまま可）
```

**用語の整理（"Adapter" の使い分け）**

| 使う場所 | 名前 | 役割 |
|----------|------|------|
| Interface Adapters 層 | 階層名のみ「Adapters」 | HTTP/DB/他文脈との「適合」全般を指す。フォルダ名には使わない |
| 同上・サービス契約 | `service.contract` | 他層から呼ばれるサービスのインターフェース。旧 service.adapter。別案: `service.api`（公開API）, `service.spi`（提供側インターフェース） |
| Frameworks 層・Port 実装 | `frameworks.interface` | Port を満たす実装が入るフォルダ。他文脈の Mapper/API を呼ぶ。「Adapter」と混同しないよう `interface` という名前にする |

### 移行の進め方
1. **Phase 1** … 上記の「役割」と「どのフォルダがどの層か」をドキュメントとチームで揃える。コードは現パッケージのままでもよい。
2. **Phase 2** … 新規・変更が多い箇所から、`port` と `frameworks.interface`（Port 実装）を導入し、他文脈への依存を Port 経由に切り替える。
3. **Phase 3** … 必要に応じて、既存クラスを `enterprise` / `application` / `adapters` / `frameworks` のパッケージへ移す。`service.adapter` → `service.contract` へのリネームもこの段階で行う。import の一括変更になり得るため、段階的に行う。
