angular.module('springboot', [])

	.controller("LDAController", [
		'$scope',
		'Util',
		'API',
		function ($scope, Util, API) {

			$scope.loadModel = function () {
				d3.selectAll('.model').selectAll('svg').remove();

				var groupBy = function (xs, key) {
					return xs.reduce(function (rv, x) {
						(rv[x[key]] = rv[x[key]] || []).push(x);
						return rv;
					}, {});
				};

				function count(obj) {
					var c = 0;
					for (var key in obj) {
						if (obj.hasOwnProperty(key))++c;
					}
					return c;
				}

				wordScale = d3.scale.linear().domain([1, 100, 1000, 10000]).range([10, 20, 40, 80]).clamp(true);
				wordColor = d3.scale.linear().domain([10, 20, 40, 80]).range(["blue", "green", "orange", "red"]);

				var topic = angular.fromJson($scope.result);
				var grouped = groupBy(topic, 'topicId');
				for (x = 0; x < count(grouped); x++) {
					viz = d3.select(".model").append("svg")
						.attr("width", 400)
						.attr("height", 440)
						.attr("id", "svg" + x);
				}

				for (x = 0; x < count(grouped); x++) {

					d3.layout.cloud().size([400, 400])
						// .words([{"text":"test","size":wordScale(1000)},{"text":"bad","size":wordScale(1)}])
						.words(grouped[x])
						.rotate(function () { return ~~(Math.random() * 2) * 5; })
						.fontSize(function (d) { return wordScale(d.probability * 100000); })
						.on("end", draw)
						.start();

					function draw(words) {
						viz = d3.select("#svg" + x);

						viz.append("g")
							.attr("transform", "translate(200,220)")
							.selectAll("text")
							.data(words)
							.enter().append("text")
							.style("font-size", function (d) { return d.size + "px"; })
							.style("fill", function (d) { return wordColor(d.size); })
							.style("opacity", 1.)
							.attr("text-anchor", "middle")
							.attr("transform", function (d) {
								return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
							})
							.text(function (d) { return d.text; });

						viz
							.append("text")
							.data([grouped[x][0]])
							.style("font-size", 20)
							.style("font-weight", 900)
							.attr("x", 100)
							.attr("y", 20)
							.text(function (d) { return "TOPIC " + d.topicId; })

						//  d3.select("#svg"+x).append("svg:text").text("Topic " + x);	
						//    viz.enter().append("svg:text").text("Topic " + x);

					}
				}
			};


			$scope.submitting = false;
			$scope.trainModel = function () {
				var params = {
					"k": $scope.lda.k,
					"alpha": $scope.lda.alpha,
					"beta": $scope.lda.beta
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

			click();
		}
		//
	]);

function click() {
	$('#tabs-icons-text-2-tab').click();
}