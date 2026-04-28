package com.his.formula;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.his.formula", "com.his.common"})
@EnableDiscoveryClient
@MapperScan({"com.his.formula.mapper", "com.his.common.mapper"})
public class FormulaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormulaServiceApplication.class, args);
    }
}
