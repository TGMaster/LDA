package com.spring.boot.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestModel {

    private String name;
    private String email;
    private String phone;
    private String message;
}
