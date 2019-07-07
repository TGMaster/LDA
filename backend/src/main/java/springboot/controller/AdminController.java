/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.controller;

import com.spring.boot.Application;
import com.spring.boot.entity.Admin;
import com.spring.boot.model.entity.AuthUser;
import com.spring.boot.model.entity.Session;
import com.spring.boot.service.AdminService;
import com.spring.boot.service.AuthService;
import com.spring.boot.util.*;
import com.spring.boot.model.request.AuthRequestModel;

import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author TGMaster
 */
@RestController
public class AdminController extends BasedAPI {

    @Autowired
    AdminService adminService;

    @Autowired
    AuthService authService;

    @RequestMapping(path = Constant.LOGIN_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> login(
            @RequestBody AuthRequestModel requestModel
    ) {
        Admin user = adminService.findAdminByUserName(requestModel.getUserName());
        if (user == null) {
            throw new ApplicationException(APIStatus.ERR_USER_NOT_FOUND);
        }

        // check password
        String passwordHash = null;
        try {
            passwordHash = MD5Hash.MD5Encrypt(requestModel.passwordHash + user.getSalt());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("User login encrypt password error", ex);
        }

        if (!passwordHash.equals(user.getPasswordHash())) {
            throw new ApplicationException(APIStatus.ERR_PASSWORD_NOT_MATCH);
        }

        // Create Session
        Session session = authService.createUserToken(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserId(), user.getPasswordHash());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return responseUtil.successResponse(session.getTokenId());
    }

    @RequestMapping(path = Constant.LOGOUT_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> logout(
            HttpServletRequest request,
            @RequestHeader(value = Constant.HEADER_TOKEN) String tokenId
    ) {
        AuthUser user = getAuthUserFromSession(request);
        if (user != null) {
            if (tokenId != null && !tokenId.equals("")) {
                Session userToken = authService.getUserTokenById(tokenId);
                if (userToken != null) {
                    authService.deleteUserToken(userToken);
                    return responseUtil.successResponse("OK");
                } else {
                    throw new ApplicationException(APIStatus.ERR_SESSION_NOT_FOUND);
                }
            } else {
                throw new ApplicationException(APIStatus.ERR_BAD_PARAMS);
            }
        } else {
            throw new ApplicationException(APIStatus.ERR_SESSION_NOT_FOUND);
        }
    }

    @RequestMapping(path = Constant.ADMIN_API, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> addAdmin(
            @RequestBody Admin admin
    ) {
        try {
            Admin user = new Admin();
            user.setUserId(CommonUtil.generateUUID());
            user.setUserName(admin.getUserName());
            user.setSalt(CommonUtil.generateSalt());
            user.setPasswordHash(MD5Hash.MD5Encrypt(admin.getPasswordHash() + user.getSalt()));
            adminService.save(user);
            return responseUtil.successResponse(user);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Create new admin error", ex);
        }
    }

    @RequestMapping(path = Constant.GET_USER, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> getUser(
            HttpServletRequest request
    ) {
        AuthUser user = getAuthUserFromSession(request);
        if (user != null) {
            Admin admin = authService.getUserByUserId(user.getId());
            if (admin != null) {
                return responseUtil.successResponse(user);
            } else {
                throw new ApplicationException(APIStatus.ERR_USER_NOT_FOUND);
            }
        } else {
            throw new ApplicationException(APIStatus.ERR_SESSION_NOT_FOUND);
        }
    }
}
