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
 * @author TGMaster
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsRequestModel {
    private String id;
    private String title;
    private String info;
    private String author;
    private String image;
}
