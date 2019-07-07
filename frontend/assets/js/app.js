'use strict'
var app = angular.module('springboot', [
	'ngAnimate',
	'toastr',
	'ngRoute',
	'ui.router',
	'oc.lazyLoad',
	'springboot.utils',
	'springboot.constants',
	'springboot.session',
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
    .config(['$ocLazyLoadProvider', 'AppConfig', function($ocLazyLoadProvider, AppConfig) {
    	var modules = [
    	{
    		name: 'listNewsModule',
    		files: [
    		'assets/js/components/news/list/list_news.js'
    		]
    	},
    	{
    		name: 'createNewsModule',
    		files: [
    		'assets/js/components/news/create/create_news.js',
    		'assets/css/newfont.css'
    		]
    	},
    	{
    		name: 'updateNewsModule',
    		files: [
    		'assets/js/components/news/update/update_news.js',
    		'assets/css/newfont.css'
    		]
    	},
        {
            name: 'detailNewsModule',
            files: [
            'assets/js/components/news/detail/detail_news.js'
            ]
        },
        {
            name: 'contactModule',
            files: [
            'assets/js/components/contact/contact.js'
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
    	function($stateProvider, $urlRouterProvider) {
    		$urlRouterProvider.otherwise('index');
    		$stateProvider
    			// Control login
    			.state({
    				name: 'main',
    				abstract: true,
    				templateUrl: 'assets/js/components/template/main.html',
    				controller: ['$scope', 'Util', 'API', '$state', 'Session', function ($scope, Util, API, $state, Session){
    					$scope.year = new Date().getFullYear();
                        $scope.isLogin = false;
                        $scope.loginUser = function() {
                            Session.login ({
                                userName: $scope.user.username,
                                passwordHash: $scope.user.password,
                                keepLogin: false
                            }, function(response) {
                                var status = response.status;
                                if (status === 200) {
                                    $scope.isLogin = true;
                                    $('#login').modal('toggle');
                                    Util.showSuccessToast("Login Successfully!!!");
                                } else {
                                    var err = _.find(APIStatus, {status: status});
                                    if (err) {
                                        Util.showErrorAPI(err.msgKey);
                                    }
                                }
                            }).finally(function() {
                                $scope.submitting = false;
                            });
                        };

                        $scope.logout = function() {
                            $scope.isLogin = false;
                            Session.logout("");
                        };

                        // Session
                        Session.init(false).then(function() {
                            $scope.isLogin = true;
                            $scope.user = Session.getUser();
                        }, function() {
                            // error handle
                        });
                    }]
                })

                //News
                .state({
                	name: 'news',
                	parent: 'main',
                	abstract: true,
                	url: '/news',
                	template: '<div ui-view></div>'
                })
                .state({
                	name: 'news.list',
                	url: '^/index',
                	templateUrl: 'assets/js/components/news/list/list_news.html',
                	controller: 'ListNewsController',
                	resolve: {
                		loadModule: ['$ocLazyLoad', function($ocLazyLoad) {
                			return $ocLazyLoad.load('listNewsModule');
                		}]
                	}
                })
                .state({
                	name: 'news.create',
                	url: '/create',
                	templateUrl: 'assets/js/components/news/create/create_news.html',
                	controller: 'AddNewsController',
                	resolve: {
                		loadModule: ['$ocLazyLoad', function($ocLazyLoad) {
                			return $ocLazyLoad.load('createNewsModule');
                		}]
                	}
                })
                .state({
                    name: 'news.details',
                    url: '/{id}',
                    templateUrl: 'assets/js/components/news/detail/detail_news.html',
                    controller: 'DetailNewsController',
                    resolve: {
                        loadModule: ['$ocLazyLoad', function($ocLazyLoad) {
                            return $ocLazyLoad.load('detailNewsModule');
                        }]
                    }
                })
                .state({
                	name: 'news.update',
                	url: '/update/{id}',
                	templateUrl: 'assets/js/components/news/update/update_news.html',
                	controller: 'UpdateNewsController',
                	resolve: {
                		loadModule: ['$ocLazyLoad', function($ocLazyLoad) {
                			return $ocLazyLoad.load('updateNewsModule');
                		}]
                	}
                })
                .state({
                	name: 'contact',
                	parent: 'main',
                	url: '/contact',
                	templateUrl: 'assets/js/components/contact/contact.html',
                    controller: 'ContactController',
                    resolve: {
                        loadModule: ['$ocLazyLoad', function($ocLazyLoad) {
                            return $ocLazyLoad.load('contactModule');
                        }]
                    }
                })
                ;
            }])

            // Config request
            // Set up interceptor
            .config(['$httpProvider', function ($httpProvider) {

                $httpProvider.defaults.headers.post['Content-Type'] = 'application/json;charset=utf-8';

                $httpProvider.interceptors.push(function ($q, $cookies) {
                    return {
                        'request': function (config) {
                            config.headers['X-Access-Token'] = $cookies.get('AccessToken');
                            return config;
                        }
                    };
                });
            }])

            .config(['$qProvider', function ($qProvider) {
                $qProvider.errorOnUnhandledRejections(false);
            }]);