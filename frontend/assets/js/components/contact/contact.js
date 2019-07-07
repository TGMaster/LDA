'use strict';

angular.module('springboot', [])

.controller("ContactController", [
	'$scope',
	'Util',
	'API',
	'$state',
	function($scope, Util, API, $state) {

		$scope.submitting = false;
		$scope.sendEmail = function() {
			var params = {
				"name": $scope.contact.name,
				"email": $scope.contact.email,
				"phone": $scope.contact.phone,
				"message": $scope.contact.message
			};
			$scope.submitting = true;
			Util.createRequest(API.SEND_EMAIL, params, function(response) {
				var status = response.status;
				if (status === 200) {
					Util.showSuccessToast("Đã gửi lời nhắn, chúng tôi sẽ liên lạc sớm nhất có thể");
				} else {
					var err = _.find(APIStatus, {status: status});
					if (err) {
						Util.showErrorAPI(err.msgKey);
					}
				}
				$scope.submitting = false;
			});
		};
	}
//
]);