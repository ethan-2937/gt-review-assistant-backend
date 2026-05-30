package com.jzyz.gtreviewassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.jzyz.gtreviewassistant.mapper")
@SpringBootApplication
public class GtReviewAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(GtReviewAssistantApplication.class, args);
    }
}