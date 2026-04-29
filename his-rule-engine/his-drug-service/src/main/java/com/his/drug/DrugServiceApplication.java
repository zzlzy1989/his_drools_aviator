package com.his.drug;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.his.drug", "com.his.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan({"com.his.drug.mapper", "com.his.common.mapper"})
public class DrugServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrugServiceApplication.class, args);
    }
}
