/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spring.boot.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author S410U
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagingRequestModel {
    private String searchKey;
    private String name;
    private int sortCase;
    private boolean ascSort;
    private int pageNumber;
    private int pageSize;
}
