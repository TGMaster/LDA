package com.spring.boot.service;

import com.spring.boot.entity.Admin;
import com.spring.boot.model.entity.Session;

public interface AuthService {
    public Admin getUserByUserId(String userId);
    public Admin getUserByUserName(String userName);
    public Session createUserToken(Admin user);
    public Session getUserTokenById(String id);
    public void deleteUserToken(Session userToken);
}
