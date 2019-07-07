/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.controller;

import com.spring.boot.entity.News;
import com.spring.boot.model.entity.AuthUser;
import com.spring.boot.model.request.NewsRequestModel;
import com.spring.boot.model.request.PagingRequestModel;
import com.spring.boot.util.APIStatus;
import com.spring.boot.service.NewsService;
import com.spring.boot.util.ApplicationException;
import com.spring.boot.util.CommonUtil;
import com.spring.boot.util.Constant;
import com.spring.boot.util.ResponseUtil;
import com.spring.boot.util.RestAPIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author TGMaster
 */
@RestController
@RequestMapping(Constant.NEWS_API)
public class NewsController extends BasedAPI {

    @Autowired
    NewsService newsService;

    // Get News Detail
    @RequestMapping(value = Constant.WITHIN_ID, method = RequestMethod.GET)
    public ResponseEntity<RestAPIResponse> detailNews(
            @PathVariable String id
    ) {
        News news = newsService.findById(id);
        if (news != null) {
            return responseUtil.successResponse(news);
        } else {
            throw new ApplicationException(APIStatus.ERR_NEWS_NOT_FOUND);
        }
    }

    // Get List News
    @RequestMapping(value = Constant.NEWS_LIST, method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> listNews(
            @RequestBody PagingRequestModel pagingRequestModel
    ) {
        Page<News> listNews = newsService.search(pagingRequestModel);
        return responseUtil.successResponse(listNews);
    }

    // Create News
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<RestAPIResponse> createNews(
            @RequestBody NewsRequestModel requestModel,
            HttpServletRequest request
    ) {
        AuthUser user = getAuthUserFromSession(request);
        if (user != null) {
            isValidNews(requestModel);
            try {
                News news = new News();
                news.setNewsId(CommonUtil.generateUUID());
                news.setNewsTitle(requestModel.getTitle());
                news.setNewsInfo(requestModel.getInfo());
                news.setNewsAuthor(requestModel.getAuthor());
                news.setNewsImage(requestModel.getImage());
                newsService.save(news);
                return responseUtil.successResponse(news);
            } catch (Exception ex) {
                throw new RuntimeException("Add news error", ex);
            }
        } else {
            throw new ApplicationException(APIStatus.ERR_FORBIDDEN);
        }
    }

    // Update News
    @RequestMapping(value = Constant.WITHIN_ID, method = RequestMethod.PUT)
    public ResponseEntity<RestAPIResponse> updateNews(
            @RequestBody NewsRequestModel requestModel,
            HttpServletRequest request
    ) {
        AuthUser user = getAuthUserFromSession(request);
        if (user != null) {
            isValidNews(requestModel);
            News news = newsService.findById(requestModel.getId());
            if (news != null) {
                if (!news.getNewsTitle().equals(requestModel.getTitle())) {
                    news.setNewsTitle(requestModel.getTitle());
                }
                if (!news.getNewsInfo().equals(requestModel.getInfo())) {
                    news.setNewsInfo(requestModel.getInfo());
                }
                if (!news.getNewsImage().equals(requestModel.getImage())) {
                    news.setNewsImage(requestModel.getImage());
                }
                newsService.save(news);
                return responseUtil.successResponse(news);
            } else {
                throw new ApplicationException(APIStatus.ERR_NEWS_NOT_FOUND);
            }
        } else {
            throw new ApplicationException(APIStatus.ERR_FORBIDDEN);
        }
    }

    // Delete News
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<RestAPIResponse> updateNews(
            @RequestParam(name = "news_ids") String newsIds,
            HttpServletRequest request
    ) {
        AuthUser user = getAuthUserFromSession(request);
        if (user != null) {
            if (!newsIds.equals("")) {
                String[] newsIdArray = newsIds.split(",");
                for (String id : newsIdArray) {
                    News news = newsService.findById(id);
                    if (news != null) {
                        newsService.deleteOne(news);
                    } else {
                        throw new ApplicationException(APIStatus.ERR_NEWS_NOT_FOUND);
                    }
                }
                return responseUtil.successResponse("OK");
            } else {
                throw new ApplicationException(APIStatus.ERR_BAD_PARAMS);
            }
        } else {
            throw new ApplicationException(APIStatus.ERR_FORBIDDEN);
        }
    }

    private void isValidNews(NewsRequestModel requestModel) {
        if (requestModel.getTitle() == null || requestModel.getTitle().equals("")) {
            throw new ApplicationException(APIStatus.ERR_NEWS_MISSING_TITLE);
        }
        if (requestModel.getInfo() == null || requestModel.getInfo().equals("")) {
            throw new ApplicationException(APIStatus.ERR_NEWS_MISSING_INFO);
        }
        if (requestModel.getAuthor() == null || requestModel.getAuthor().equals("")) {
            throw new ApplicationException(APIStatus.ERR_NEWS_MISSING_AUTHOR);
        }
        if (requestModel.getImage() == null || requestModel.getImage().equals("")) {
            throw new ApplicationException(APIStatus.ERR_NEWS_MISSING_IMAGE);
        }
    }
}
