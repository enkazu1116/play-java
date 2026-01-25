package com.playjava.frameworks.adapter.inventory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playjava.usecase.port.inventory.StockOperationPort;
import com.playjava.frameworks.mapper.MStockMapper;
import com.playjava.enterprise.entity.MStock;

/**
 * StockOperationPort の実装。
 * 在庫マスタ（MStockMapper）に問い合わせ・更新し、注文サービスからは Port 経由でのみ利用される。
 */
@Component
public class StockOperationAdapter implements StockOperationPort {

    @Autowired
    private MStockMapper mStockMapper;

    @Override
    public int getStockQuantity(String productId) {
        MStock stock = findStockByProductId(productId);
        return stock.getQuantity();
    }

    @Override
    public void deductStock(String productId, int quantity) {
        MStock stock = findStockByProductId(productId);
        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("在庫不足です: productId=" + productId +
                ", 在庫数量=" + stock.getQuantity() + ", 要求数量=" + quantity);
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        if (stock.getQuantity() == 0) {
            stock.setStatus(1); // 在庫なし
        }
        mStockMapper.updateById(stock);
    }

    @Override
    public void returnStock(String productId, int quantity) {
        MStock stock = findStockByProductId(productId);
        stock.setQuantity(stock.getQuantity() + quantity);
        if (stock.getQuantity() > 0 && Integer.valueOf(1).equals(stock.getStatus())) {
            stock.setStatus(0); // 在庫あり
        }
        mStockMapper.updateById(stock);
    }

    private MStock findStockByProductId(String productId) {
        LambdaQueryWrapper<MStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MStock::getProductId, productId);
        MStock stock = mStockMapper.selectOne(wrapper);
        if (stock == null || Boolean.TRUE.equals(stock.getDeleteFlag())) {
            throw new RuntimeException("在庫レコードが存在しません: productId=" + productId);
        }
        return stock;
    }
}
