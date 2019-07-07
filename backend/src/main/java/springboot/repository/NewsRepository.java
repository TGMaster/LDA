/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.repository;

import com.spring.boot.entity.News;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author TGMaster
 */

@Repository
@Transactional
public interface NewsRepository extends PagingAndSortingRepository<News,String>, JpaSpecificationExecutor<News>{
    News findNewsByNewsTitle(String newsTitle);
}
