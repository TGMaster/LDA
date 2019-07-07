/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.repository.specification;

import com.spring.boot.entity.News;
import com.spring.boot.util.Constant;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author S410U
 */
public class NewsSpecification implements Specification<News> {
    
    private final String searchKey;
    private final int sortCase;
    private final boolean ascSort;
    
    public NewsSpecification(String searchKey, int sortCase, boolean ascSort) {
        this.searchKey = searchKey;
        this.sortCase = sortCase;
        this.ascSort = ascSort;
    }

    @Override
    public Predicate toPredicate(Root<News> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new LinkedList<>();
        // filter by search key [name]
        if (searchKey != null && !searchKey.trim().isEmpty()) {
            String wrapSearch = "%" + searchKey.trim() + "%";
            Predicate title = cb.like(root.get("newsTitle"), wrapSearch);
            Predicate info = cb.like(root.get("newsInfo"), wrapSearch);
            Predicate author = cb.like(root.get("newsAuthor"), wrapSearch);
            Predicate search = cb.or(title, info, author);
            predicates.add(search);
        }
        // sort
        Path orderClause;
        switch (sortCase) {
            case Constant.SORT_BY_TITLE:
                orderClause = root.get("newsTitle");
                break;
            case Constant.SORT_BY_INFO:
                orderClause = root.get("newsInfo");
                break;
            case Constant.SORT_BY_AUTHOR:
                orderClause = root.get("newsAuthor");
                break;
            case Constant.SORT_BY_DATE:
                orderClause = root.get("createDate");
                break;
            default:
                orderClause = root.get("createDate");
        }
        
        if (!ascSort) {
            query.orderBy(cb.desc(orderClause));
        } else {
            query.orderBy(cb.asc(orderClause));
        }

        return cb.and(predicates.toArray(new Predicate[]{}));
    }
    
    
}
