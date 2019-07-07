'use strict';

angular.module("springboot.news", [])
.controller("DetailNewsController", [
	'$scope',
	'Util',
	'API',
    '$stateParams',
    '$anchorScroll',
    '$state',
    '$uibModal',
    function($scope, Util, API, $stateParams, $anchorScroll, $state, $uibModal) {

        // Scroll To Content
        $scope.scrollTo = function(id) {
            $anchorScroll(id);
        }

        $scope.getInfoNews = function() {

            Util.createRequest(API.GET_NEWS_DETAIL.path + $stateParams.id, function (response) {
                var status = response.status;
                if (status === 200) {
                    $scope.news = response.data;
                } else {
                    Util.showErrorAPI(status);
                }
            });
        };

        $scope.getInfoNews();

        $scope.loadUpdateNews = function(newsId) {
            $state.go('news.update', {id: newsId});
        };

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
                            Util.showSuccessToast("Xóa thành công");
                            $state.go('news.list');
                        } else {
                            Util.showErrorToast("Xóa không thành công");
                        }
                    });
                });
            };
        }
        ]);