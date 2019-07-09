'use strict'
var app = angular.module('springboot', [
    'ngAnimate',
    'toastr',
    'ngRoute',
    'ui.router',
    'oc.lazyLoad',
    'springboot.utils',
    'springboot.constants',
    'ui.bootstrap'
])

    // Angular toastr config
    // see more https://github.com/Foxandxss/angular-toastr
    .config(['toastrConfig', function (toastrConfig) {
        angular.extend(toastrConfig, {
            autoDismiss: false,
            containerId: 'toast-container',
            maxOpened: 2,
            newestOnTop: true,
            positionClass: 'toast-top-right',
            preventDuplicates: false,
            preventOpenDuplicates: true,
            target: 'body'
        });
    }
    ])
    // Config ocLazy Loading
    .config(['$ocLazyLoadProvider', 'AppConfig', function ($ocLazyLoadProvider, AppConfig) {
        var modules = [
            {
                name: 'ldaModule',
                files: [
                    'assets/js/components/lda/lda.js'
                ]
            }
        ];

        // Read config value
        if (angular.isDefined(AppConfig.OCLAZY_CACHE_MODULE)) {
            angular.forEach(modules, function (v) {
                v.cache = AppConfig.OCLAZY_CACHE_MODULE;
            });
        }

        // We define some files for a specific module
        $ocLazyLoadProvider.config({
            modules: modules
        });
    }])
    // State Provider
    .config(['$stateProvider', '$urlRouterProvider',
        function ($stateProvider, $urlRouterProvider) {
            $urlRouterProvider.otherwise('index');
            $stateProvider
                // Main Page
                .state({
                    name: 'main',
                    abstract: true,
                    templateUrl: 'assets/js/components/template/main.html'
                })
                // Body
                .state({
                    name: 'lda',
                    parent: 'main',
                    url: '/lda',
                    templateUrl: 'assets/js/components/lda/lda.html',
                    controller: 'LDAController',
                    resolve: {
                        loadModule: ['$ocLazyLoad', function ($ocLazyLoad) {
                            return $ocLazyLoad.load('ldaModule');
                        }]
                    }
                })
                ;
        }])

    // Config request
    // Set up interceptor
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.defaults.headers.post['Content-Type'] = 'application/json;charset=utf-8';
    }])

    .config(['$qProvider', function ($qProvider) {
        $qProvider.errorOnUnhandledRejections(false);
    }]);