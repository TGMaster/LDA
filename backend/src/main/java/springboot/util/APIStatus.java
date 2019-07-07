/*
 * Copyright (c) HOGO, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of HOGO,
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with HOGO.
 */
package springboot.util;

public enum APIStatus {

    OK(200, "OK"),
    NO_RESULT(201, "No more result."),
    //////////////////
    // CLIENT SIDE  //
    //////////////////
    ERR_BAD_REQUEST(400, "Bad request"),
    ERR_UNAUTHORIZED(401, "Unauthorized or Access Token is expired"),
    ERR_FORBIDDEN(403, "Forbidden! Access denied"),
    ERR_BAD_PARAMS(406, "Bad parameters"),
    ERR_ALREADY_EXISTED(407, "Already exsited."),
    //////////////////
    // SERVER SIDE  //
    //////////////////
    ERR_INTERNAL_SERVER(500, "Internal Server Error"),
    ERR_CREATE_MODEL(501, "Create model error"),
    //////////////////
    // DATABASE SIDE//
    //////////////////
    ERR_INCORRECT_MODEL_DATA(700, "Incorrect model data"),
    ERR_USER_NOT_FOUND(701, "User not found."),
    ERR_PASSWORD_NOT_MATCH(702, "Password doesn't match"),
    ERR_CREATE_USER(703, "Create User fail"),
    ERR_EMAIL_INVALID(704, "Email is invalid"),
    ERR_INVALID_PARAM(705, "Param is invalid"),
    ERR_OLD_PASS(706, "Old password incorrect"),

    //////////////////
    //   NEWS SIDE  //
    //////////////////
    ERR_NEWS_MISSING_TITLE(800, "Missing news title"),
    ERR_NEWS_MISSING_INFO(801, "Missing news information"),
    ERR_NEWS_MISSING_AUTHOR(802, "Missing news author"),
    ERR_NEWS_MISSING_IMAGE(803, "Missing news image"),
    ERR_NEWS_NOT_FOUND(804, "News is not found");
    private final int code;
    private final String description;

    private APIStatus(int s, String v) {
        code = s;
        description = v;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
