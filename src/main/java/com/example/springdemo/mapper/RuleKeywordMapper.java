package com.example.springdemo.mapper;

import com.example.springdemo.model.RuleKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface RuleKeywordMapper {
    List<RuleKeyword> selectAllActive();

    List<RuleKeyword> selectByCategoryId(@Param("categoryId") Integer categoryId);

    int countAllActive();

    int countTotal();
}
