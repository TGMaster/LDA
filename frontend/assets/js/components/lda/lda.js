'use strict';

angular.module('springboot', [])

.controller("LDAController", [
	'$scope',
	'Util',
	'API',
	'$state',
	function($scope, Util, API, $state) {

		$scope.submitting = false;
		$scope.trainModel = function() {
			var params = {
				"training": $scope.lda.training,
				"k": $scope.lda.k,
				"iteration": $scope.lda.iteration,
				"optimizer": $scope.lda.optimizer
			};
			$scope.submitting = true;
			Util.createRequest(API.LDA_MODEL, params, function(response) {
				var status = response.status;
				if (status === 200) {
					Util.showSuccessToast("Train LDA Model Successfully!");
					$scope.model = response.data;
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