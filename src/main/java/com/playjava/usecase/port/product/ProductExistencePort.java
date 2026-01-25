package com.playjava.usecase.port.product;

/**
 * 商品の存在を問い合わせる Port（他文脈への窓口）。
 * 在庫サービスは MProductMapper に直接依存せず、この Port 経由でのみ商品存在を確認する。
 */
public interface ProductExistencePort {

    /**
     * 指定した商品IDの商品が存在するかどうかを返す。
     *
     * @param productId 商品ID
     * @return 存在する場合 true、それ以外は false
     */
    boolean existsByProductId(String productId);
}
