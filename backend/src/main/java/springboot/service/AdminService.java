/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.service;

import com.spring.boot.entity.Admin;

import java.util.List;

/**
 *
 * @author S410U
 */
public interface AdminService {
    Admin findAdminByUserName(String userName);
    Admin findAdminByUserId(String userId);
    Admin save(Admin admin);
}
