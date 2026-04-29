package com.his.common.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置
 *
 * <p>配置 Swagger/OpenAPI 文档信息</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HIS 规则引擎 API")
                        .description("医院信息系统规则引擎 API 文档，包含医保结算、用药审核、质控等规则管理接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HIS Team")
                                .email("his@example.com")));
    }
}
