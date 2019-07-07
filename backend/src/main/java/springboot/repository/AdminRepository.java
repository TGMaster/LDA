/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.repository;

import com.spring.boot.entity.Admin;
import javax.transaction.Transactional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author TGMaster
 */
@Repository
@Transactional
public interface AdminRepository extends PagingAndSortingRepository<Admin, Long> {
    Admin findAdminByUserName(String userName);
    Admin findAdminByUserId(String userId);
}
