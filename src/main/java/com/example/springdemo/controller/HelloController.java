package com.example.springdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    // 测试接口：http://localhost:8080/hello
    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Boot Maven!";
    }
}