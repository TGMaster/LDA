package com.spring.boot.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.spring.boot.entity.Admin;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private String id;
    private String username;
    private String password;

    public static AuthUser createAuthUser(Admin user) {
        return new AuthUser(user.getUserId(),user.getUserName(),user.getPasswordHash());
    }
}
