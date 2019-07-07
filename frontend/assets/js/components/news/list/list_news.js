'use strict';

angular.module('springboot.news', [])

.controller("ListNewsController", [
	'$scope',
	'Util',
	'API',
	'$http',
    '$anchorScroll',
    '$state',
    function($scope, Util, API, $http,$anchorScroll,$state) {
            // Scroll To Content
            $scope.scrollTo = function(id) {
                $anchorScroll(id);
            }


			/**
             *
             * News List
             */
             $scope.isCallingAPIList = false;
             var paramList = {
             	searchKey: "",
             	name: "",
             	sortCase: 4,
             	ascSort: 0,
             	pageNumber: 1,
             	pageSize: 6
             };
             $scope.pagination = {};
             $scope.isSort = true;
             $scope.pageSize = 6;
             $scope.maxSize = 5;
             $scope.currentPage = 1;
             $scope.searchString = "";

             $scope.loadListNews = function (sortCase) {
             	if ($scope.isCallingAPIList) {
             		return;
             	}
             	$scope.isCallingAPIList = true;
             	paramList.pageNumber = $scope.currentPage;
             	paramList.pageSize = $scope.pageSize;
             	if (sortCase) {
             		$scope.isSort = !$scope.isSort;
             		paramList.ascSort = $scope.isSort;
             		paramList.sortCase = sortCase;
             	}
                // reset state
                $scope.pagination = {};
                paramList.searchKey = $scope.searchString;

                Util.createRequest(API.GET_LIST_NEWS, paramList, function (response) {

                	var status = response.status;
                	if (status === 200) {
                        $scope.newsList = response.data.content;
                        $scope.totalItems = response.data.totalElements;
                        $scope.totalPages = response.data.totalPages;
                    } else {
                      Util.showErrorAPI(status);

                  }
                  $scope.isCallingAPIList = false;
              });
            };

            $scope.loadListNews();

            $scope.loadNewsDetail = function (newsId) {
                $state.go('news.details', {id: newsId});
            };
        }
        ]);