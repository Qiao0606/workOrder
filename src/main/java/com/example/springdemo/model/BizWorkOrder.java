package com.example.springdemo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BizWorkOrder {
    private Long orderId;
    private String orderCode;
    private Integer categoryId;
    private LocalDateTime acceptTime;
    private String acceptUser;
    private String userPhone;
    private String userRegion;
    private String userAddress;
    private String orderContent;
    private String aiCandidateCategory;
    private String aiMatchEvidence;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
