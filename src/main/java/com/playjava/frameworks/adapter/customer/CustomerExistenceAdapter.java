package com.playjava.frameworks.adapter.customer;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.playjava.usecase.port.customer.CustomerExistencePort;
import com.playjava.frameworks.mapper.MCustomerMapper;
import com.playjava.enterprise.entity.MCustomer;

/**
 * CustomerExistencePort の実装。
 * 顧客マスタ（MCustomerMapper）に問い合わせ、注文サービスからは Port 経由でのみ利用される。
 */
@Component
public class CustomerExistenceAdapter implements CustomerExistencePort {

    @Autowired
    private MCustomerMapper mCustomerMapper;

    @Override
    public boolean existsActiveCustomer(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return false;
        }
        MCustomer customer = mCustomerMapper.selectById(customerId);
        return customer != null && !Boolean.TRUE.equals(customer.getDeleteFlag());
    }
}
