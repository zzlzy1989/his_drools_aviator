package com.his.gateway.service;

import com.his.gateway.dto.LoginRequest;
import com.his.gateway.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, String clientIp);
}
