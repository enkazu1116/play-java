package com.playjava.usecase.port.customer;

import java.util.UUID;

/**
 * 顧客の存在・有効性を問い合わせる Port（他文脈への窓口）。
 * 注文サービスは MCustomerMapper に直接依存せず、この Port 経由でのみ顧客の存在確認を行う。
 */
public interface CustomerExistencePort {

    /**
     * 指定した顧客IDの顧客が存在し、かつ有効（論理削除されていない）かどうかを返す。
     *
     * @param customerId 顧客ID
     * @return 存在し有効な場合 true、それ以外は false
     */
    boolean existsActiveCustomer(UUID customerId);
}
