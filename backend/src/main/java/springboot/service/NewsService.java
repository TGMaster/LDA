/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.service;

import com.spring.boot.entity.News;
import com.spring.boot.model.request.PagingRequestModel;
import org.springframework.data.domain.Page;

/**
 *
 * @author S410U
 */
public interface NewsService {
    News findByTitle(String newsTitle);

    News findById(String newsId);
    
    News save(News news);
    
    void deleteOne(News news);
    
    Page<News> search(PagingRequestModel pagingRequestModel);
}
