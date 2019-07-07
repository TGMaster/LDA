'use strict';
var session = angular.module('springboot.session', ['springboot.utils', 'springboot.constants', 'ngCookies'])

.factory('Session', [

	'API',
	'APIStatus',
	'toastr',
	'Util',
	'$state',
	'$rootScope',
	'$cookies',
	'$q',
	'AppConfig',

	function (API,APIStatus,toastr,Util,$state,$rootScope,$cookies,$q,AppConfig) {

		// Current User
		var sessionUser;

		var service = {

			// Get Session
			getSession: function() {
				return $cookies.get(AppConfig.SESSION_COOKIES);
			},

			// Set Session
			setSession: function(token) {
				if (token) {
					$cookies.put(AppConfig.SESSION_COOKIES, token);
				}
			},

			// Get User
			getUser: function() {
				return sessionUser;
			},

			// Set User
			setUser: function(user) {
				if (angular.isObject(user)) {
					$rootScope.currentUser = sessionUser = user;
				}
			},

			// Login
			login: function(data, callback) {
				var request = Util.createRequest(API.LOGIN, data, function(response) {
                    
                    callback && callback(response);
                    var status = response.status;
                    if (status === 200) {
                        // store user into session
                        var accessToken = response.data;
                        service.setSession(accessToken);
                    }
                });

                return request;
			},

			// Logout
			logout: function(callback) {
				var request = Util.createRequest(API.LOGOUT, function(response) {
                    
                    callback && callback(response);
                }).finally(function () {
                    // clear user into session
                    $cookies.remove(AppConfig.SESSION_COOKIES);
                    // redirect to main page
                    $state.go('news.list');
                });

                return request;
			},

			// Initialize
			init: function(goback) {
				var defer = $q.defer();
				// Validate
				if (service.getSession()) {
					// Get info
					Util.createRequest(API.GET_USER, {}, function(response) {

						var status = response.status;
						if (status == 200) {
							// Store session
							var userProfile = response.data;
							service.setUser(userProfile);
							defer.resolve(userProfile);
						} else {

							// Clear session
							$cookie.remove(AppConfig.SESSION_COOKIES);
							// Show error
							var err = _.find(APIStatus, {status: status});
							if (err) {
								toastr.error(err.msgKey);
							}
							defer.reject();
						}
					}, function(errResponse) {

						// Clear session
						$cookies.remove(AppConfig.SESSION_COOKIES);
						defer.reject(errResponse);

					}, true);
				} else {
					if (goback)
						$state.go('news.list');
					defer.reject();
				}

				return defer.promise;
			}
		};

		return service;
	}
]);