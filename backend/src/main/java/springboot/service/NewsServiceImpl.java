/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.service;

import com.spring.boot.entity.News;
import com.spring.boot.model.request.PagingRequestModel;
import com.spring.boot.repository.NewsRepository;
import com.spring.boot.repository.specification.NewsSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 *
 * @author TGMaster
 */
@Component
public class NewsServiceImpl implements NewsService {

    @Autowired
    NewsRepository newsRepository;

    @Override
    public News findByTitle(String newsTitle) {
        return newsRepository.findNewsByNewsTitle(newsTitle);
    }

    @Override
    public News findById(String newsId) { return newsRepository.findById(newsId).get(); }

    @Override
    public News save(News news) {
        return newsRepository.save(news);
    }

    @Override
    public void deleteOne(News news) {
        newsRepository.delete(news);
    }

    @Override
    public Page<News> search(PagingRequestModel pagingRequestModel) {
        NewsSpecification newsSpecification = new NewsSpecification(pagingRequestModel.getSearchKey(), pagingRequestModel.getSortCase(), pagingRequestModel.isAscSort());
        PageRequest pageRequest = PageRequest.of((pagingRequestModel.getPageNumber() - 1), pagingRequestModel.getPageSize());
        return newsRepository.findAll(newsSpecification, pageRequest);
    }

}
