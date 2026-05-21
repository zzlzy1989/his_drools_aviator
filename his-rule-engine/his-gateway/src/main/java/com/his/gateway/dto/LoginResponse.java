package com.his.gateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private String userId;
    private String username;
    private String realName;
    private String tenantId;
    private String role;
}
