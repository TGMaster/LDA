angular.module("springboot.news", [])
.controller("AddNewsController", [
	'$scope',
	'Util',
	'API',
	'$http',
	'Session',
	'$state',
	function($scope, Util, API, $http, Session, $state) {

            // Check Session
            Session.init(true);

            $scope.submitting = false;

            $scope.createNews = function() {

                $scope.submitting = true;

                var params = {
                    "title": $scope.news.title,
                    "info": $scope.news.info,
                    "author": "Admin",
                    "image": ""
                };
                var form_data = new FormData();
                angular.forEach($scope.files, function(file){  
                    form_data.append('file', file);
                });

                var isUpload = false;

                $http.post('upload.php', form_data,
                {  
                    transformRequest: angular.identity,  
                    headers: { 'Content-Type': undefined,'Process-Data': false }
                }).then(function success(response) {
                    //console.log(response.data);
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

                	if (isUpload) {
                		Util.createRequest(API.CREATE_NEWS, params, function (response) {
                			var status = response.status;
                			if (status === 200) {

                        	// show message
                        	Util.showSuccessToast("Create news successfully!!!");

                        	// go to home page
                        	$state.go('news.list');
                        } else {
                        	Util.showErrorAPI(status);
                        }
                        $scope.submitting = false;
                    });
                	};
                });
                
            };

            $scope.goBack = function() {
                window.history.back();
            };
        }
        ]);