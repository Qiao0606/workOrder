package com.example.springdemo.dto;

import lombok.Data;

@Data
public class ClassificationResult {
    private Long orderId;
    private Integer categoryId;
    private String categoryName;
    private String categoryCode;
    private Float confidence;
    private String matchType;
    private String matchEvidence;
    private String aiReasoning;
}
