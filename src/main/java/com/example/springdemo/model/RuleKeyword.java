package com.example.springdemo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleKeyword {
    private Long ruleId;
    private Integer categoryId;
    private String keyword;
    private String synonym;
    private Integer matchType;
    private Float weight;
    private Integer hitCount;
    private Integer status;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
