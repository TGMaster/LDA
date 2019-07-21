angular.module('springboot', [])

	.controller("SearchController", [
		'$scope',
		'Util',
		'API',
		function ($scope, Util, API) {

			$scope.submitting = false;
			$scope.search = function () {
				var params = {
					"training": $scope.lda.training,
					"k": $scope.lda.k,
					"iteration": $scope.lda.iteration,
					"optimizer": $scope.lda.optimizer
				};
				$scope.submitting = true;
				Util.createRequest(API.LDA_MODEL, params, function (response) {
					var status = response.status;
					if (status === 200) {
						Util.showSuccessToast("Train LDA Model Successfully!");
						$scope.submitting = false;
						$scope.result = "[" + response.data + "]";
						$scope.loadModel();
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