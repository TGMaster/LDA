angular.module('springboot', [])

	.controller("PreprocessController", [
		'$scope',
		'Util',
		'API',
		function ($scope, Util, API) {

			$scope.submitting = false;
			$scope.preProcess = function () {
				var params = {
					"filename": $scope.preprocess.dataset,
					"column": $scope.preprocess.column
				};
				$scope.submitting = true;
				Util.createRequest(API.PREPROCESS, params, function (response) {
					var status = response.status;
					if (status === 200) {
						console.log(response.data);
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
			click();
		}
		//
	]);

function click() {
	$('#tabs-icons-text-1-tab').click();
}
