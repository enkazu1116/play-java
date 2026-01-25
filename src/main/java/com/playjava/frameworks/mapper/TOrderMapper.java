package com.playjava.frameworks.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.playjava.enterprise.entity.TOrder;

@Mapper
public interface TOrderMapper extends BaseMapper<TOrder>{
  
}
