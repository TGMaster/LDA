angular.module('springboot', [])

	.controller("SearchController", [
		'$scope',
		'Util',
		'API',
		function ($scope, Util, API) {

			$scope.submitting = false;
			$scope.search = function () {
				$scope.submitting = true;
				Util.createRequest(API.SEARCH, $scope.keyword, function (response) {
					var status = response.status;
					if (status === 200) {
						$scope.submitting = false;
						$scope.topics = angular.fromJson(response.data);
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
	$('#tabs-icons-text-3-tab').click();
}