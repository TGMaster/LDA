'use strict';
var constant = angular.module('springboot.constants', [])

.constant('AppConfig', {
        // Cache module using ocLazyLoad
        // Should use "true" when deploy to server
        OCLAZY_CACHE_MODULE: true,
        API_PATH: 'http://localhost:8080/api'
    })

    // APIs
    .constant('API', {
        // Mail
        LDA_MODEL: {path: '/lda', method: 'POST'},
        PREPROCESS: {path: '/preprocess', method: 'POST'}
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
        //////////////////
        //   NEWS SIDE  //
        //////////////////
        {status: 600, msgKey: 'api.status.600', message: "Missing training percentage"},
        {status: 601, msgKey: 'api.status.601', message: "Missing number of topics"},
        {status: 602, msgKey: 'api.status.602', message: "Missing number of iterations"},
        {status: 603, msgKey: 'api.status.603', message: "Missing optimizer type"}
    ])