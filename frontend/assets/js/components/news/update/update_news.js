angular.module("springboot.news", [])
.controller("UpdateNewsController", [
	'$scope',
	'Util',
	'API',
	'$http',
	'Session',
	'$state',
	'$stateParams',
	'$uibModal',
	function($scope, Util, API, $http, Session, $state, $stateParams, $uibModal) {

            // Check Session
            Session.init(true);

            /**
             *
             * Upadte News
             *
             **/

             var originNews;
             $scope.submitting = false;

             $scope.getInfoNews = function() {

             	Util.createRequest(API.GET_NEWS_DETAIL.path + $stateParams.id, function (response) {
             		var status = response.status;
             		if (status === 200) {
             			$scope.newsInfo = response.data;
             			originNews = angular.copy($scope.newsInfo);
             		} else {
             			Util.showErrorAPI(status);
             		}
             	});
             };

             $scope.getInfoNews();

             $scope.updateNews = function() {
             	var hasFile = false;

                // Set params
                var params = {
                	"id": $scope.newsInfo.newsId,
                	"title": $scope.newsInfo.newsTitle,
                	"info": $scope.newsInfo.newsInfo,
                	"author": "Admin",
                	"image": $scope.newsInfo.newsImage
                };

                $scope.submitting = true;
                if (Boolean($scope.files)) {
                	hasFile = true;
                }

                // upload file to upload folder
                if (hasFile) {
                	// ready to upload new file
                	var form_data = new FormData();
                	angular.forEach($scope.files, function(file){
                		form_data.append('file', file);
                	});

                	// check file is uploaded
                	var isUpload = false;

                	$http.post('upload.php', form_data,
                	{  
                		transformRequest: angular.identity,  
                		headers: { 'Content-Type': undefined,'Process-Data': false }
                	}).then(function success(response) {
                		if (response.data.status === 200) {
                			isUpload = true;
                			params.image = response.data.path + $scope.fileName;
                			Util.showSuccessToast(response.data.msg);
                		} else {
                			Util.showErrorToast(response.data.msg);
                		}

                	}, function error(response) {
                    	// Handle error
                    }).finally(function() {

                    	// If file is already upload
                    	if (isUpload) {
                    		doUpload(params);
                    	}
                    });
                } else {
                	doUpload(params);
                }
            };

            $scope.$watch('newsInfo', function (newValue) {
            	$scope.origin = angular.equals(newValue, originNews);
            }, true);

            $scope.confirmDeleteNews = function () {
                // show model confirm delete orders
                Util.showConfirmModal({
                	title: "Xóa tin tức",
                	message: "Bạn có muốn xóa tin tức này không?"
                }, function () {
                    // cal API cancel orders
                    var api = API.DELETE_NEWS;
                    api.path = API.DELETE_NEWS.path + "?news_ids=" + $stateParams.id;
                    Util.createRequest(api, function (response) {
                    	var status = response.status;
                    	if (status === 200) {
                            // show message delete successfully
                            Util.showSuccessToast("Delete news succesfully!!!");
                            $state.go('news.list');
                        } else {
                        	Util.showErrorToast("Delete news failed");
                        }
                    });
                });
            };

            $scope.readFileName = function() {
            	$scope.origin = false;
            }

            $scope.goBack = function() {
            	window.history.back();
            };

            function doUpload(params) {
            	console.log(params);
            	var apiCall = API.UPDATE_NEWS;
            	apiCall.path = API.UPDATE_NEWS.path + $stateParams.id;
            	Util.createRequest(apiCall, params, function(response) {
            		var status = response.status;
            		if (status === 200) {
            			Util.showSuccessToast("Update news successfully!!!");
            			$state.go('news.details', {id: $scope.newsInfo.newsId});
            		} else {
            			Util.showErrorToast("Update news failed");
            			$scope.submitting = false;
            		}
            	});
            };
        }

//
]);