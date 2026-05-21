package com.his.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 64, message = "用户名长度2-64字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 128, message = "密码长度1-128字符")
    private String password;
}
