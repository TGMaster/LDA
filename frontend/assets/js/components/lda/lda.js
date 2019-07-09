'use strict';

angular.module('springboot', [])

	.controller("LDAController", [
		'$scope',
		'Util',
		'API',
		'$state',
		function ($scope, Util, API, $state) {

			$scope.submitting = false;
			$scope.trainModel = function () {
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
						$scope.result = "[" + response.data +"]";
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

			$scope.loadModel = function () {

				var json = {
					"name": "data",
					"children": [
						{
							"name": "topics",
							"children": angular.fromJson($scope.result)
						}
					]
				};
	
				var r = 1500,
					format = d3.format(",d"),
					fill = d3.scale.category20c();
	
				var bubble = d3.layout.pack()
					.sort(null)
					.size([r, r])
					.padding(1.5);
	
				var vis = d3.select(".model").append("svg")
					.attr("width", r)
					.attr("height", r)
					.attr("class", "bubble");
	
	
				var node = vis.selectAll("g.node")
					.data(bubble.nodes(classes(json))
						.filter(function (d) { return !d.children; }))
					.enter().append("g")
					.attr("class", "node")
					.attr("transform", function (d) { return "translate(" + d.x + "," + d.y + ")"; });
				color = d3.scale.category20();
	
				node.append("title")
					.text(function (d) { return d.className + ": " + format(d.value); });
	
				node.append("circle")
					.attr("r", function (d) { return d.r; })
					.style("fill", function (d) { return color(d.topicName); });
	
				var text = node.append("text")
					.attr("text-anchor", "middle")
					.attr("dy", ".3em")
					.text(function (d) { return d.className.substring(0, d.r / 3) });
	
				text.append("tspan")
					.attr("dy", "1.2em")
					.attr("x", 0)
					.text(function (d) { return Math.ceil(d.value * 10000) / 10000; });
			};
		}
		//
	]);

// Returns a flattened hierarchy containing all leaf nodes under the root.
function classes(root) {
	var classes = [];

	function recurse(term, node) {
		if (node.children) node.children.forEach(function (child) { recurse(node.term, child); });
		else classes.push({ topicName: node.topicId, className: node.term, value: node.probability });
	}

	recurse(null, root);
	return { children: classes };
};