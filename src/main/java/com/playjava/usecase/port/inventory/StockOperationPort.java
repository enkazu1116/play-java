package com.playjava.usecase.port.inventory;

/**
 * 在庫の照会・減算・戻しを行う Port（他文脈への窓口）。
 * 注文サービスは MStockMapper に直接依存せず、この Port 経由でのみ在庫チェック・在庫操作を行う。
 */
public interface StockOperationPort {

    /**
     * 指定した商品IDの在庫数量を返す。
     * 在庫レコードが存在しない、または論理削除済みの場合は RuntimeException をスローする。
     *
     * @param productId 商品ID
     * @return 在庫数量（0以上）
     * @throws RuntimeException 在庫レコードが存在しない場合
     */
    int getStockQuantity(String productId);

    /**
     * 指定した商品の在庫を減算する。
     * 在庫レコードが存在しない、数量が不足している場合は RuntimeException をスローする。
     * 減算後に在庫が0になった場合はステータスを「在庫なし(1)」に更新する。
     *
     * @param productId 商品ID
     * @param quantity  減算する数量
     * @throws RuntimeException 在庫レコードが存在しない、または数量不足の場合
     */
    void deductStock(String productId, int quantity);

    /**
     * 指定した商品の在庫を戻す（加算する）。
     * 在庫レコードが存在しない場合は RuntimeException をスローする。
     * 戻し後に在庫が0より大きくなり、ステータスが「在庫なし(1)」の場合は「在庫あり(0)」に更新する。
     *
     * @param productId 商品ID
     * @param quantity  戻す数量
     * @throws RuntimeException 在庫レコードが存在しない場合
     */
    void returnStock(String productId, int quantity);
}
