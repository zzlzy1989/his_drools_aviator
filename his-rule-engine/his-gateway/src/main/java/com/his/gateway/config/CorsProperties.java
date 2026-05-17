package com.his.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "his.cors")
public class CorsProperties {

    private List<String> allowedOrigins;

    private String allowedMethods;

    private String allowedHeaders;

    private Boolean allowCredentials;

    private Long maxAge;
}
