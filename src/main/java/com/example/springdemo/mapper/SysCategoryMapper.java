package com.example.springdemo.mapper;

import com.example.springdemo.model.SysCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SysCategoryMapper {
    List<SysCategory> selectAll();

    SysCategory selectById(@Param("categoryId") Integer categoryId);

    int countAll();
}
