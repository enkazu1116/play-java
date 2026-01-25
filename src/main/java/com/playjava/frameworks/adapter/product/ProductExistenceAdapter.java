package com.playjava.frameworks.adapter.product;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.playjava.usecase.port.product.ProductExistencePort;
import com.playjava.frameworks.mapper.MProductMapper;

/**
 * ProductExistencePort の実装。
 * 商品マスタ（MProductMapper）に問い合わせ、在庫サービスからは Port 経由でのみ利用される。
 */
@Component
public class ProductExistenceAdapter implements ProductExistencePort {

    @Autowired
    private MProductMapper mProductMapper;

    @Override
    public boolean existsByProductId(String productId) {
        if (productId == null || productId.isEmpty()) {
            return false;
        }
        return mProductMapper.selectById(productId) != null;
    }
}
