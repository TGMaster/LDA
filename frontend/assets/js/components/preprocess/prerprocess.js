angular.module('springboot', [])

	.controller("PreprocessController", [
		'$scope',
		'Util',
		'API',
		function ($scope, Util, API) {

			$scope.submitting = false;
			$scope.preProcess = function () {
				var params = {
					"training": $scope.lda.training,
					"k": $scope.lda.k,
					"iteration": $scope.lda.iteration,
					"optimizer": $scope.lda.optimizer
				};
				$scope.submitting = true;
				Util.createRequest(API.PREPROCESS, params, function (response) {
					var status = response.status;
					if (status === 200) {
						Util.showSuccessToast("Have fun with your dataset!");
					} else {
						var err = _.find(APIStatus, { status: status });
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