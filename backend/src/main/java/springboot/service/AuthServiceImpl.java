package com.spring.boot.service;

import com.google.gson.Gson;
import com.spring.boot.entity.Admin;
import com.spring.boot.model.entity.AuthUser;
import com.spring.boot.model.entity.Session;
import com.spring.boot.repository.AdminRepository;
import com.spring.boot.repository.SessionRepository;
import com.spring.boot.util.APIStatus;
import com.spring.boot.util.ApplicationException;
import com.spring.boot.util.Constant;
import com.spring.boot.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AuthServiceImpl implements AuthService {

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Override
    public Admin getUserByUserId(String userId) {
        return adminRepository.findAdminByUserId(userId);
    }

    @Override
    public Admin getUserByUserName(String userName) {
        return adminRepository.findAdminByUserName(userName);
    }

    @Override
    public Session createUserToken(Admin user) {
        Gson gson = new Gson();
        try {
            Session session = new Session();
            session.setAccountLoginId(user.getUserId());
            Date currentDate = new Date();
            session.setLoginDate(DateUtil.convertToUTC(currentDate));
            Date expiration = new Date(currentDate.getTime() + Constant.DEFAULT_SESSION_TIME_OUT);
            session.setExpirationDate(DateUtil.convertToUTC(expiration));
            AuthUser authUser = AuthUser.createAuthUser(user);
            session.setSessionData(gson.toJson(authUser));
            sessionRepository.save(session);
            return session;
        } catch (Exception e) {
            throw new ApplicationException(APIStatus.ERR_CREATE_MODEL, e);
        }
    }

    @Override
    public Session getUserTokenById(String id) {
        return sessionRepository.findByTokenId(id);
    }

    @Override
    public void deleteUserToken(Session userToken) {
        sessionRepository.delete(userToken);
    }
}
