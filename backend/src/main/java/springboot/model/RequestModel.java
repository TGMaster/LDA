/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package springboot.model;

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
public class RequestModel {
    private String training;
    private String k;
    private String iteration;
    private String optimizer;
}
