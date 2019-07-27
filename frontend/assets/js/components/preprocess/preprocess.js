angular.module('springboot', [])

    .controller("PreprocessController", [
        '$scope',
        'Util',
        'API',
        function ($scope, Util, API) {
            // Add the following code if you want the name of the file appear on select
            $(".custom-file-input").on("change", function () {
                var fileName = $(this).val().split("\\").pop();
                $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
                $scope.dataset = fileName;
                $scope.preview();
            });

            $scope.submitting = false;
            $scope.preProcess = function () {
                var params = {
                    "filename": $scope.dataset,
                    "column": $scope.column
                };
                $scope.submitting = true;
                Util.createRequest(API.PREPROCESS, params, function (response) {
                    var status = response.status;
                    if (status === 200) {
                        Util.showSuccessToast("Have fun with your dataset!");
                        $scope.results = angular.fromJson("[" + response.data + "]");
                    } else {
                        var err = _.find(APIStatus, { status: status });
                        if (err) {
                            Util.showErrorAPI(err.msgKey);
                        }
                    }
                    $scope.submitting = false;
                });
            };

            $scope.preview = function () {
                var request = {
                    "path": API.PREVIEW.path + "?dataset=" + $scope.dataset,
                    "method": API.PREVIEW.method
                };
                Util.createRequest(request, null, function (response) {
                    var status = response.status;
                    if (status === 200) {
                        $scope.columns = response.data;
                    } else {
                        var err = _.find(APIStatus, { status: status });
                        if (err) {
                            Util.showErrorAPI(err.msgKey);
                        }
                    }
                });
            };

            click();
        }
        //
    ]);

function click() {
    $('#tabs-icons-text-1-tab').click();
}