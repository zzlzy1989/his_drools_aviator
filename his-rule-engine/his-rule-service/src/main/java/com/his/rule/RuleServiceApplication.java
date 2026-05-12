package com.his.rule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.his.rule", "com.his.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan({"com.his.rule.mapper", "com.his.common.mapper"})
public class RuleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleServiceApplication.class, args);
    }
}
