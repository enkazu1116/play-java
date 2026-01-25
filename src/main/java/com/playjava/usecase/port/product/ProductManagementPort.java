package com.playjava.usecase.port.product;

import com.playjava.enterprise.entity.MProduct;

/**
 * 商品情報・価格の問い合わせ窓口(port)
 * 在庫サービスは、MProductMapperに直接依存せず、このPort経由でのみ商品情報・価格を問い合わせる。
 */
public interface ProductManagementPort {
    
    /**
     * 指定した商品IDの商品情報を取得する。
     * @param productId 商品ID
     * @return 商品情報
     */
    MProduct selectById(String productId);
}
