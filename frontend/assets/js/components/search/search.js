angular.module('springboot', [])

	.controller("SearchController", [
		'$scope',
		'Util',
		'API',
		function ($scope, Util, API) {

			$scope.isShowResult = false;
			$scope.submitting = false;
			$scope.search = function () {
				$scope.submitting = true;
				Util.createRequest(API.SEARCH, $scope.keyword, function (response) {
					var status = response.status;
					if (status === 200) {
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
				// set the dimensions and margins of the graph
				var margin = { top: 10, right: 30, bottom: 70, left: 30 },
					width = 900 - margin.left - margin.right,
					height = 600 - margin.top - margin.bottom;

				// append the svg object to the body of the page
				var svg = d3.select(".model")
					.append("svg")
					.attr("width", width + margin.left + margin.right)
					.attr("height", height + margin.top + margin.bottom)
					.append("g")
					.attr("transform",
						"translate(" + margin.left + "," + margin.top + ")");

				// Parse the Data
				var data = $scope.topics;

				// X axis
				var x = d3.scaleBand()
					.range([0, width])
					.domain(data.map(function (d) { return d.topic; }))
					.padding(0.2);
				svg.append("g")
					.attr("transform", "translate(0," + height + ")")
					.call(d3.axisBottom(x))
					.selectAll("text")
					.style("text-anchor", "middle");

				// text label for the x axis
				svg.append("text")
					.attr("x", width + 20)
					.attr("y", height + 20)
					.style("font-size", "12px")
					.style("text-anchor", "end")
					.attr("font-weight", "bold")
					.text("Topic");

				// Add Y axis
				var y = d3.scaleLinear()
					.domain([0, 100])
					.range([height, 0]);
				svg.append("g")
					.call(d3.axisLeft(y));

				// Text Y axis
				svg.append("g")
					.attr("class", "axis")
					.call(d3.axisLeft(y).ticks(null, "s"))
					.append("text")
					.attr("x", 10)
					.style("font-size", "12px")
					.attr("fill", "#000")
					.attr("font-weight", "bold")
					.attr("text-anchor", "start")
					.text("Percentage");

				// Bars
				svg.selectAll("mybar")
					.data(data)
					.enter()
					.append("rect")
					.attr("x", function (d) { return x(d.topic); })
					.attr("width", x.bandwidth())
					.attr("fill", "#69b3a2")
					// no bar at the beginning thus:
					.attr("height", function (d) { return height - y(0); }) // always equal to 0
					.attr("y", function (d) { return y(0); });

				// Legend
				svg.append("text")
					.attr("x", (width / 1.7))
					.attr("y", 10 + (margin.top / 2))
					.attr("text-anchor", "middle")
					.style("font-size", "16px")
					.style("text-decoration", "underline")
					.style("font-weight", "bold")
					.text("Topic distribution of the new text");

				// Animation
				svg.selectAll("rect")
					.transition()
					.duration(1000)
					.attr("y", function (d) { return y(d.probability); })
					.attr("height", function (d) { return height - y(d.probability); })
					.delay(function (d, i) { return (i) });
			}

			click();
		}
		//
	]);

function click() {
	$('#tabs-icons-text-3-tab').click();
}