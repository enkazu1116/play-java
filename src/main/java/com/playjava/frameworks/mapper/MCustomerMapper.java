package com.playjava.frameworks.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.playjava.enterprise.entity.MCustomer;
import java.util.UUID;

@Mapper
public interface MCustomerMapper extends BaseMapper<MCustomer>{
  
    /**
     * 顧客IDで論理削除を含めて取得する
     * @param customerId 顧客ID
     * @return 顧客情報（論理削除済みも含む）
     */
    @Select("SELECT * FROM m_customer WHERE customer_id = #{customerId}")
    MCustomer selectByIdIncludeDeleted(UUID customerId);
}
