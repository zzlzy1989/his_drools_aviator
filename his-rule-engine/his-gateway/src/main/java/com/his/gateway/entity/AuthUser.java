package com.his.gateway.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthUser {

    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String tenantId;
    private String status;
    private Integer loginFailCount;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime lockedUntil;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private Integer deleted;
}
