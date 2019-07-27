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
						$scope.loadDiagram();
					} else {
						var err = _.find(APIStatus, { status: status });
						if (err) {
							Util.showErrorAPI(err.msgKey);
						}
					}
					$scope.submitting = false;
				});
			};

			$scope.loadDiagram = function () {
				var margin = { top: 20, right: 20, bottom: 70, left: 40 },
					width = 600 - margin.left - margin.right,
					height = 300 - margin.top - margin.bottom;

				var x = d3.scale.ordinal().rangeRoundBands([0, width], .05);

				var y = d3.scale.linear().range([height, 0]);

				var xAxis = d3.svg.axis()
					.scale(x)
					.orient("bottom");

				var yAxis = d3.svg.axis()
					.scale(y)
					.orient("left")
					.ticks(10);

				var svg = d3.select(".model").append("svg")
					.attr("width", width + margin.left + margin.right)
					.attr("height", height + margin.top + margin.bottom)
					.append("g")
					.attr("transform",
						"translate(" + margin.left + "," + margin.top + ")");

				var data = $scope.topics;
				console.log(data);

				x.domain(data.map(function (d) { return d.topic; }));
				y.domain([0, d3.max(data, function (d) { return d.probability; })]);

				svg.append("g")
					.attr("class", "x axis")
					.attr("transform", "translate(0," + height + ")")
					.call(xAxis)
					.selectAll("text")
					.style("text-anchor", "end")
					.attr("dx", "-.8em")
					.attr("dy", "-.55em")
					.attr("transform", "rotate(-90)");

				svg.append("g")
					.attr("class", "y axis")
					.call(yAxis)
					.append("text")
					.attr("transform", "rotate(-90)")
					.attr("y", 6)
					.attr("dy", ".71em")
					.style("text-anchor", "end")
					.text("Percentage");

				svg.selectAll("bar")
					.data(data)
					.enter().append("rect")
					.style("fill", "steelblue")
					.attr("x", function (d) { return x(d.topic); })
					.attr("width", x.rangeBand())
					.attr("y", function (d) { return y(d.probability); })
					.attr("height", function (d) { return height - y(d.probability); });

			}

			click();
		}
		//
	]);

function click() {
	$('#tabs-icons-text-3-tab').click();
}