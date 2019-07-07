/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.service;

import com.spring.boot.entity.Admin;
import com.spring.boot.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * @author TGMaster
 */
@Component
public class AdminServiceImpl implements AdminService{

    @Autowired
    AdminRepository adminRepository;

    @Override
    public Admin findAdminByUserName(String userName) {
        return adminRepository.findAdminByUserName(userName);
    }

    @Override
    public Admin findAdminByUserId(String userId) {
        return adminRepository.findAdminByUserId(userId);
    }

    @Override
    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }
}
