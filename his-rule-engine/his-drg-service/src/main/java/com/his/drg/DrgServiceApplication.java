package com.his.drg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.his.drg", "com.his.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan({"com.his.drg.mapper", "com.his.common.mapper"})
public class DrgServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrgServiceApplication.class, args);
    }
}
