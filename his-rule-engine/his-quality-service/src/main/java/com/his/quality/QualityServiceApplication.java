package com.his.quality;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.his.quality", "com.his.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan({"com.his.quality.mapper", "com.his.common.mapper"})
public class QualityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QualityServiceApplication.class, args);
    }
}
