package com.playjava.frameworks.adapter.product;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.playjava.usecase.port.product.ProductManagementPort;
import com.playjava.frameworks.mapper.MProductMapper;
import com.playjava.enterprise.entity.MProduct;

/**
 * ProductManagementPort の実装。
 * 商品マスタ（MProductMapper）に問い合わせ、在庫サービスからは Port 経由でのみ利用される。
 */
@Component
public class ProductManagementAdapter implements ProductManagementPort {
    
    @Autowired
    private MProductMapper mProductMapper;

    @Override
    public MProduct selectById(String productId) {
        return mProductMapper.selectById(productId);
    }

}
