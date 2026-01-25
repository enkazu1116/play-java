package com.playjava.frameworks.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.playjava.enterprise.entity.MProduct;

@Mapper
public interface MProductMapper extends BaseMapper<MProduct>{
  
}
