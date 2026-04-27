package com.example.springdemo.mapper;

import com.example.springdemo.dto.ClassificationResult;
import com.example.springdemo.model.BizWorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface BizWorkOrderMapper {
    BizWorkOrder selectById(@Param("orderId") Long orderId);

    int updateCategory(ClassificationResult result);

    List<BizWorkOrder> selectUnclassified();

    int insert(BizWorkOrder workOrder);

    int update(BizWorkOrder workOrder);

    int updateById(BizWorkOrder workOrder);

    // 统计相关方法
    int countTotal();

    int countClassified();
}
