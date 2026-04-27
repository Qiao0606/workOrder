package com.example.springdemo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysCategory {
    private Integer categoryId;
    private Integer parentId;
    private String categoryName;
    private String categoryCode;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
