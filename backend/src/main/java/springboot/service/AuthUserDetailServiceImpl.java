package com.spring.boot.service;

import com.google.gson.Gson;
import com.spring.boot.model.entity.AuthUser;
import com.spring.boot.model.entity.Session;
import com.spring.boot.repository.SessionRepository;
import com.spring.boot.util.APIStatus;
import com.spring.boot.util.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUserDetailServiceImpl implements AuthUserDetailService {

    Gson gson = new Gson();

    @Autowired
    SessionRepository sessionRepository;

    @Override
    public AuthUser loadUserByAccessToken(String token) {
        Session session = sessionRepository.findByTokenId(token);
        if (session != null) {
            if (session.getSessionData() != null && !session.getSessionData().equals("")) {
                AuthUser user = gson.fromJson(session.getSessionData(), AuthUser.class);
                return user;
            } else {
                throw new ApplicationException(APIStatus.ERR_SESSION_DATA_INVALID);
            }
        } else {
            throw new ApplicationException(APIStatus.ERR_SESSION_NOT_FOUND);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;
    }
}
