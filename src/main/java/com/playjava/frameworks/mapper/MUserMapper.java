package com.playjava.frameworks.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.playjava.enterprise.entity.MUser;

@Mapper
public interface MUserMapper extends BaseMapper<MUser>{
  
}
