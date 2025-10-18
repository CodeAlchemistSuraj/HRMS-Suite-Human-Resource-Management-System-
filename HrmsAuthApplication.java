package com.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class HrmsAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrmsAuthApplication.class, args);
    }
}