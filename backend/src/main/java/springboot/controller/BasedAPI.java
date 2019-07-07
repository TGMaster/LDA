package com.spring.boot.controller;

import com.spring.boot.model.entity.AuthUser;
import com.spring.boot.service.AuthUserDetailService;
import com.spring.boot.util.Constant;
import com.spring.boot.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public abstract class BasedAPI {

    @Autowired
    protected ResponseUtil responseUtil;

    @Autowired
    protected AuthUserDetailService authUserDetailService;

    public AuthUser getAuthUserFromSession(HttpServletRequest request) {
        String token = request.getHeader(Constant.HEADER_TOKEN);
        AuthUser user = authUserDetailService.loadUserByAccessToken(token);
        return user;
    }
}
