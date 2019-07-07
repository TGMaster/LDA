'use strict';
var utils = angular.module('springboot.utils', [])

.factory('Util', ['$http', 'toastr', 'AppConfig', 'APIStatus', '$rootScope', '$uibModal', function ($http, toastr, AppConfig, APIStatus, $rootScope, $uibModal) {

    var _util = {
        createRequest: function (api, data, callback, errorCb, notLoading) {

            if (!notLoading) {
                $rootScope.loading = true;
            }

            if (!api) {
                console.error('Invalid API');
                return;
            }

            var link = api, method = 'GET', params = data, cb = callback, errCb = errorCb;
            if (angular.isObject(api)) {
                method = api.method || 'POST';
                link = api.path;
            }

                // No data posted
                if (angular.isFunction(data)) {

                    params = {};
                    cb = data;
                    errCb = callback;
                }

                var opts = {
                    url: AppConfig.API_PATH + link,
                    method: method
                };
                // Shorthand
                (method === 'GET') ? (opts.params = params) : (opts.data = params);
                return $http(opts).then(function (response) {
                    if (!notLoading)
                        $rootScope.loading = false;
                    cb && cb(response.data);
                    // redirect to login page if session timeout, invalid token (status 401 - unthorized)
                    if (response.data.status === 401) {
                        //$state.go('login');
                    }

                }, function (response) {
                    if (!notLoading)
                        $rootScope.loading = false;
                    errCb && errCb(response.data);
                    // session timeout
                    if (response.status === 401) {
                        if (!notLoading)
                            $rootScope.loading = false;
                        // redirect to login page
                        //$state.go('login');
                    } else {
                        if (!notLoading)
                            $rootScope.loading = false;
                        // default handle error
                        toastr.error('Unknown Error');
                    }

                });
            },
            // Get moment object
            // @param {Object|String} date : Can be date object or string format date
            // @param {String} format
            // @return {Object} Moment object
            getMoment: function (date, format) {
                return angular.isObject(date) ? moment(date) : moment(date, format || Patterns.DEFAULT_DATE_FORMAT);
            },
            // Format date with specific format
            // @param {Object|String} date : Can be date object or string format date
            // @param {String} format
            // @return {String} String date with specific format
            formatDate: function (date, format) {

                return this.getMoment(date, Patterns.DEFAULT_DATE_FORMAT).format(format || Patterns.DEFAULT_DATE_FORMAT);
            },
            // Format date based on the difference between 2 days
            formatDateBasedOnDiff: function (date1, date2) {
                var d1 = date1, d2 = date2;

                if (typeof date2 === "undefined") {

                    d1 = moment();
                    d2 = this.getMoment(date1);
                }

                if (d1.isSame(d2, "day")) {

                    return this.formatDate(date1, "HH:mm A");
                }
                return this.formatDate(date1);
            },
            showSuccessToast: function (msg) {

                if (msg) {
                    toastr.success(msg);
                }
            },
            showErrorToast: function (msg) {

                if (msg) {
                    toastr.error(msg);
                }
            },
            showErrorAPI: function (status) {
                var err = _.find(APIStatus, {status: status});
                if (err) {
                    toastr.error(err.msgKey);
                }
            },
            showConfirmModal: function (modalOpts, acceptCb, dismissCb) {

                var modalInstance = $uibModal.open({
                    templateUrl: 'assets/js/components/template/confirm_delete_modal.html',
                    controller: ['$scope', '$uibModalInstance', function ($scope, $uibModalInstance) {

                        $scope.title = modalOpts.title;
                        $scope.message = modalOpts.message;
                        $scope.opts = {
                            title: "Xóa tin tức",
                            message: "Bạn có muốn xóa không?",
                            lblAccept: "CÓ",
                            lblDismiss: "KHÔNG"
                        };

                        if (modalOpts) {
                            angular.extend($scope.opts, modalOpts);
                        } else {
                            acceptCb = modalOpts;
                            dismissCb = acceptCb;
                        }

                        $scope.onClose = function () {

                            dismissCb && dismissCb();
                            $uibModalInstance.close();
                        };

                        $scope.onDismiss = function () {

                            dismissCb && dismissCb();
                            $uibModalInstance.close();
                        };

                        $scope.onAccept = function () {

                            acceptCb && acceptCb();
                            $uibModalInstance.close();
                        };
                    }]
                }).result.catch(function (resp) {
                    if (['cancel', 'backdrop click', 'escape key press'].indexOf(resp) === -1) throw resp;
                });

                return modalInstance;
            },

            /**
             * Get value of specific param. Example: /pagename?name=tmhao#/child => return <tmhao>
             * @returns {Array}
             */
             getParameter: function () {
                var vars = [], hash;
                var path = window.location.href.slice(window.location.href.indexOf('?') + 1);
                var rPath = path;
                // Remove hash if have
                if (path.indexOf(location.hash) !== -1) {
                    rPath = path.substring(0, path.indexOf(location.hash));
                }

                var hashes = rPath.split('&');
                for (var i = 0; i < hashes.length; i++) {
                    hash = hashes[i].split('=');
                    vars.push(hash[0]);
                    vars[hash[0]] = hash[1];
                }
                return vars;
            }

        };

        return _util;
    }])

.directive("fileInput", function($parse) {

    return {
        link: function($scope, element, attrs){
            element.on('change', function(event){
                var files = event.target.files;
                $scope.fileName = files[0].name;
                $parse(attrs.fileInput).assign($scope, element[0].files);
                $scope.$apply();
            });
        }
    }
});