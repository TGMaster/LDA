package com.spring.boot.service;

import com.spring.boot.model.entity.AuthUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthUserDetailService extends UserDetailsService {
    AuthUser loadUserByAccessToken(String token);
}
