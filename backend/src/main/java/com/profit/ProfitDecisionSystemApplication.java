package com.profit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.profit.infrastructure")
public class ProfitDecisionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfitDecisionSystemApplication.class, args);
    }
}
