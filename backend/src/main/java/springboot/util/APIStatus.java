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

    ///////////////////
    //   LDA SIDE   //
    //////////////////
    ERR_MODEL_MISSING_TOPIC(600, "Missing number of topics"),
    ERR_MODEL_MISSING_ALPHA(600, "Missing alpha number"),
    ERR_MODEL_MISSING_BETA(602, "Missing beta number"),

    ////////////////////
    //PREPROCESS SIDE//
    ///////////////////
    ERR_PREPROCESS_MISSING_FILENAME(700, "Missing file name"),
    ERR_PREPROCESS_MISSING_COLUMN(701, "Missing column name");

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
