'use strict';
var constant = angular.module('springboot.constants', [])

.constant('AppConfig', {
        // Cache module using ocLazyLoad
        // Should use "true" when deploy to server
        OCLAZY_CACHE_MODULE: true,
        API_PATH: 'http://171.244.50.210:8080/api',
        SESSION_COOKIES: 'AccessToken'
    })

    // patterns
    .constant('Patterns', {
        EMAIL_PATTERN: /^[_A-Za-z0-9-\+]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-\]+)*(\.[A-Za-z]{2,})$/,
        USERNAME_PATTERN: /^[a-z0-9_-]{3,45}$/,
        NAME_PATTERN: /^[a-zA-Z ]{3,45}$/,
        PHONE_PATTERN: /^\+?[0-9]\d{1,16}$/,
        PASSWORD_PATTERN: /^.{6,}$/,
        DEFAULT_DATE_FORMAT: "MM/DD/YYYY HH:mm:ss",
        COMPANY_PATTERN: /^[a-zA-Z ]{3,45}$/
    })

    // APIs
    .constant('API', {
        LOGOUT: {path: '/logout', method: 'POST'},
        LOGIN: {path: '/login', method: 'POST'},

        // News
        GET_LIST_NEWS: {path: '/news/list', method: 'POST'},
        CREATE_NEWS: {path: '/news', method: 'POST'},
        GET_NEWS_DETAIL: {path: '/news/', method: 'GET'},
        UPDATE_NEWS: {path: '/news/', method: 'PUT'},
        DELETE_NEWS: {path: '/news', method: 'DELETE'},

        // User
        GET_USER: {path: '/user', method: 'POST'},

        // Mail
        SEND_EMAIL: {path: '/email', method: 'POST'}
    })

    // API status
    .constant('APIStatus', [
        {status: 200, msgKey: 'api.status.200', message: "OK"},

        //////////////////
        // CLIENT SIDE  //
        //////////////////
        {status: 400, msgKey: 'api.status.400', message: "Bad request"},
        {status: 401, msgKey: 'api.status.401', message: "Unauthorized or Access Token is expired"},
        {status: 403, msgKey: 'api.status.403', message: "Forbidden! Access denied"},
        {status: 406, msgKey: 'api.status.406', message: "Bad parameters"},
        {status: 407, msgKey: 'api.status.407', message: "Already existed"},
        //////////////////
        // SERVER SIDE  //
        //////////////////
        {status: 500, msgKey: 'api.status.500', message: "Internal Server Error"},
        {status: 501, msgKey: 'api.status.501', message: "Create model error"},
        {status: 502, msgKey: 'api.status.502', message: "Cannot send email"},
        //////////////////
        // SESSION SIDE //
        //////////////////
        {status: 600, msgKey: 'api.status.600', message: "Access token not found"},
        {status: 601, msgKey: 'api.status.601', message: "Access token is invalid"},
        {status: 602, msgKey: 'api.status.602', message: "Access token is expired"},
        {status: 603, msgKey: 'api.status.603', message: "Invalid session data"},
        {status: 604, msgKey: 'api.status.604', message: "Session not found"},
        {status: 605, msgKey: 'api.status.605', message: "Invalid account"},
        {status: 606, msgKey: 'api.status.606', message: "Create User Session fail"},
        //////////////////
        //   NEWS SIDE  //
        //////////////////
        {status: 800, msgKey: 'api.status.800', message: "Missing news title"},
        {status: 801, msgKey: 'api.status.801', message: "Missing news information"},
        {status: 802, msgKey: 'api.status.802', message: "Missing news author"},
        {status: 803, msgKey: 'api.status.803', message: "News is not found"},
    ])