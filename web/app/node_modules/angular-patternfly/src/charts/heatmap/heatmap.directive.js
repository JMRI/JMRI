/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfHeatMap
 *
 * @description
 *   Directive for rendering a heatmap chart.
 *
 * @param {object} data data for the chart:<br/>
 * <ul style='list-style-type: none'>
 * <li>.id            - the id of the measurement
 * <li>.value         - the value of the measurement
 * <li>.tooltip       - message to be displayed on hover
 * </ul>
 *
 * @param {boolean=} chartDataAvailable flag if the chart data is available - default: true
 * @param {number=} height height of the chart (no units) - default: 200
 * @param {string=} chartTitle title of the chart
 * @param {boolean=} showLegend flag to show the legend, defaults to true
 * @param {array=} legendLabels the labels for the legend - defaults: ['< 70%', '70-80%' ,'80-90%', '> 90%']
 * @param {number=} maxBlockSize the maximum size for blocks in the heatmap. Default: 50, Range: 5 - 50
 * @param {number=} minBlockSize the minimum size for blocks in the heatmap. Default: 2
 * @param {number=} blockPadding the padding in pixels between blocks (default: 2)
 * @param {array=} thresholds the threshold values for the heapmap - defaults: [0.7, 0.8, 0.9]
 * @param {array=} heatmapColorPattern the colors that correspond to the various threshold values (lowest to hightest value ex: <70& to >90%) - defaults: ['#d4f0fa', '#F9D67A', '#EC7A08', '#CE0000']
 * @param {function=} clickAction function(block) function to call when a block is clicked on
 * @param {number=} rangeHoverSize the maximum size for highlighting blocks in the same range. Default: 15
 * @param {boolean=} rangeOnHover flag to highlight blocks in the same range on hover, defaults to true
 * @param {array=} rangeTooltips the tooltips for blocks in the same range - defaults: ['< 70%', '70-80%' ,'80-90%', '> 90%']
 * @example
 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl">
       <div class="row">
         <div class="col-md-5 example-heatmap-container">
           <div pf-heatmap id="id" chart-title="title" data="data" chart-data-available="dataAvailable"
                show-legend="showLegends"></div>
         </div>
         <div class="col-md-3 example-heatmap-container">
           <div pf-heatmap id="id" chart-title="titleAlt" data="data" chart-data-available="dataAvailable"
                show-legend="showLegends" legend-labels="legendLabels"  max-block-size="20" block-padding="5"
                heatmap-color-pattern="heatmapColorPattern" thresholds="thresholds"
                click-action="clickAction"></div>
         </div>
         <div class="col-md-3 example-heatmap-container">
           <div pf-heatmap id="id" chart-title="titleSmall" data="data" chart-data-available="dataAvailable"
                show-legend="showLegends" max-block-size="15" range-tooltips="rangeTooltips"></div>
           </div>
       </div>
       <div class="row">
         <div class="col-md-3">
           <form role="form">
             <div class="form-group">
               <label class="checkbox-inline">
                 <input type="checkbox" ng-model="dataAvailable">Data Available</input>
               </label>
             </div>
           </form>
         </div>
         <div class="col-md-3">
           <form role="form">
             <div class="form-group">
               <label class="checkbox-inline">
                 <input type="checkbox" ng-model="showLegends">Show Legends</input>
               </label>
             </div>
           </form>
         </div>
       </div>
     </div>
   </file>
   <file name="script.js">
     angular.module( 'patternfly.charts' ).controller( 'ChartCtrl', function( $scope) {
       $scope.data = [
       {'id': 9,'value': 0.96,'tooltip': 'Node 8 : My OpenShift Provider<br\>96% : 96 Used of 100 Total<br\>4 Available'},
       {'id': 44, 'value': 0.94, 'tooltip': 'Node 19 : My Kubernetes Provider<br\>94% : 94 Used of 100 Total<br\>6 Available'},
       {'id': 0, 'value': 0.91, 'tooltip': 'Node 9 : My OpenShift Provider<br\>91% : 91 Used of 100 Total<br\>9 Available'},
       {'id': 43, 'value': 0.9, 'tooltip': 'Node 18 : My Kubernetes Provider<br\>90% : 90 Used of 100 Total<br\>10 Available'},
       {'id': 7, 'value': 0.89, 'tooltip': 'Node 12 : My OpenShift Provider<br\>89% : 89 Used of 100 Total<br\>11 Available'},
       {'id': 41, 'value': 0.82, 'tooltip': 'Node 16 : My Kubernetes Provider<br\>82% : 82 Used of 100 Total<br\>18 Available'},
       {'id': 21, 'value': 0.81, 'tooltip': 'Node 21 : My OpenShift Provider<br\>81% : 81 Used of 100 Total<br\>19 Available'},
       {'id': 26, 'value': 0.8, 'tooltip': 'Node 1 : My Kubernetes Provider<br\>80% : 80 Used of 100 Total<br\>20 Available'},
       {'id': 48, 'value': 0.74, 'tooltip': 'Node 23 : My Kubernetes Provider<br\>74% : 74 Used of 100 Total<br\>26 Available'},
       {'id': 27, 'value': 0.72, 'tooltip': 'Node 2 : My Kubernetes Provider<br\>72% : 72 Used of 100 Total<br\>28 Available'},
       {'id': 42, 'value': 0.71, 'tooltip': 'Node 17 : My Kubernetes Provider<br\>71% : 71 Used of 100 Total<br\>29 Available'},
       {'id': 23, 'value': 0.71, 'tooltip': 'Node 23 : My OpenShift Provider<br\>71% : 71 Used of 100 Total<br\>29 Available'},
       {'id': 22, 'value': 0.69, 'tooltip': 'Node 22 : My OpenShift Provider<br\>69% : 69 Used of 100 Total<br\>31 Available'},
       {'id': 2, 'value': 0.66, 'tooltip': 'Node 2 : M8y OpenShift Provider<br\>66% : 66 Used of 100 Total<br\>34 Available'},
       {'id': 39, 'value': 0.66, 'tooltip': 'Node 14 : My Kubernetes Provider<br\>66% : 66 Used of 100 Total<br\>34 Available'},
       {'id': 3, 'value': 0.65, 'tooltip': 'Node 39 : My OpenShift Provider<br\>65% : 65 Used of 100 Total<br\>35 Available'},
       {'id': 29, 'value': 0.65, 'tooltip': 'Node 4 : My Kubernetes Provider<br\>65% : 65 Used of 100 Total<br\>35 Available'},
       {'id': 32, 'value': 0.56, 'tooltip': 'Node 7 : My Kubernetes Provider<br\>56% : 56 Used of 100 Total<br\>44 Available'},
       {'id': 13, 'value': 0.56, 'tooltip': 'Node 13 : My OpenShift Provider<br\>56% : 56 Used of 100 Total<br\>44 Available'},
       {'id': 49, 'value': 0.52, 'tooltip': 'Node 24 : My Kubernetes Provider<br\>52% : 52 Used of 100 Total<br\>48 Available'},
       {'id': 36, 'value': 0.5, 'tooltip': 'Node 11 : My Kubernetes Provider<br\>50% : 50 Used of 100 Total<br\>50 Available'},
       {'id': 6, 'value': 0.5, 'tooltip': 'Node 5 : My OpenShift Provider<br\>50% : 50 Used of 100 Total<br\>50 Available'},
       {'id': 38, 'value': 0.49, 'tooltip': 'Node 13 : My Kubernetes Provider<br\>49% : 49 Used of 100 Total<br\>51 Available'},
       {'id': 15, 'value': 0.48, 'tooltip': 'Node 15 : My OpenShift Provider<br\>48% : 48 Used of 100 Total<br\>52 Available'},
       {'id': 30, 'value': 0.48, 'tooltip': 'Node 5 : My Kubernetes Provider<br\>48% : 48 Used of 100 Total<br\>52 Available'},
       {'id': 11, 'value': 0.47, 'tooltip': 'Node 11 : My OpenShift Provider<br\>47% : 47 Used of 100 Total<br\>53 Available'},
       {'id': 17, 'value': 0.46, 'tooltip': 'Node 17 : My OpenShift Provider<br\>46% : 46 Used of 100 Total<br\>54 Available'},
       {'id': 25, 'value': 0.45, 'tooltip': 'Node 0 : My Kubernetes Provider<br\>45% : 45 Used of 100 Total<br\>55 Available'},
       {'id': 50, 'value': 0.45, 'tooltip': 'Node 25 : My Kubernetes Provider<br\>45% : 45 Used of 100 Total<br\>55 Available'},
       {'id': 46, 'value': 0.45, 'tooltip': 'Node 21 : My Kubernetes Provider<br\>45% : 45 Used of 100 Total<br\>55 Available'},
       {'id': 47, 'value': 0.45, 'tooltip': 'Node 22 : My Kubernetes Provider<br\>45% : 45 Used of 100 Total<br\>55 Available'},
       {'id': 1, 'value': 0.44, 'tooltip': 'Node 1 : My OpenShift Provider<br\>44% : 44 Used of 100 Total<br\>56 Available'},
       {'id': 31, 'value': 0.44, 'tooltip': 'Node 6 : My Kubernetes Provider<br\>44% : 44 Used of 100 Total<br\>56 Available'},
       {'id': 37, 'value': 0.44, 'tooltip': 'Node 12 : My Kubernetes Provider<br\>44% : 44 Used of 100 Total<br\>56 Available'},
       {'id': 24, 'value': 0.44, 'tooltip': 'Node 24 : My OpenShift Provider<br\>44% : 44 Used of 100 Total<br\>56 Available'},
       {'id': 40, 'value': 0.43, 'tooltip': 'Node 40 : My Kubernetes Provider<br\>43% : 43 Used of 100 Total<br\>57 Available'},
       {'id': 20, 'value': 0.39, 'tooltip': 'Node 20 : My OpenShift Provider<br\>39% : 39 Used of 100 Total<br\>61 Available'},
       {'id': 8, 'value': 0.39, 'tooltip': 'Node 8 : My OpenShift Provider<br\>39% : 39 Used of 100 Total<br\>61 Available'},
       {'id': 5, 'value': 0.38, 'tooltip': 'Node 5 : My OpenShift Provider<br\>38% : 38 Used of 100 Total<br\>62 Available'},
       {'id': 45, 'value': 0.37, 'tooltip': 'Node 20 : My Kubernetes Provider<br\>37% : 37 Used of 100 Total<br\>63 Available'},
       {'id': 12, 'value': 0.37, 'tooltip': 'Node 12 : My OpenShift Provider<br\>37% : 37 Used of 100 Total<br\>63 Available'},
       {'id': 34, 'value': 0.37, 'tooltip': 'Node 9 : My Kubernetes Provider<br\>37% : 37 Used of 100 Total<br\>63 Available'},
       {'id': 33, 'value': 0.33, 'tooltip': 'Node 8 : My Kubernetes Provider<br\>33% : 33 Used of 100 Total<br\>67 Available'},
       {'id': 16, 'value': 0.32, 'tooltip': 'Node 16 : My OpenShift Provider<br\>32% : 32 Used of 100 Total<br\>68 Available'},
       {'id': 10, 'value': 0.29, 'tooltip': 'Node 10 : My OpenShift Provider<br\>28% : 29 Used of 100 Total<br\>71 Available'},
       {'id': 35, 'value': 0.28, 'tooltip': 'Node 35 : My Kubernetes Provider<br\>28% : 28 Used of 100 Total<br\>72 Available'},
       {'id': 18, 'value': 0.27, 'tooltip': 'Node 18 : My OpenShift Provider<br\>27% : 27 Used of 100 Total<br\>73 Available'},
       {'id': 4, 'value': 0.26, 'tooltip': 'Node 4 : My OpenShift Provider<br\>26% : 26 Used of 100 Total<br\>74 Available'},
       {'id': 19, 'value': 0.25, 'tooltip': 'Node 19 : My OpenShift Provider<br\>25% : 25 Used of 100 Total<br\>75 Available'},
       {'id': 28, 'value': 0.25, 'tooltip': 'Node 3 : My Kubernetes Provider<br\>25% : 25 Used of 100 Total<br\>75 Available'},
       {'id': 51, 'value': 0.22, 'tooltip': 'Node 26 : My Kubernetes Provider<br\>22% : 22 Used of 100 Total<br\>78 Available'},
       {'id': 14, 'value': 0.2, 'tooltip': 'Node 14 : My OpenShift Provider<br\>20% : 20 Used of 100 Total<br\>80 Available'}];

       $scope.dataAvailable = true;
       $scope.title = 'Utilization - Using Defaults';
       $scope.titleAlt = 'Utilization - Overriding Defaults';
       $scope.titleSmall = 'Utilization - Small Blocks';
       $scope.legendLabels = ['< 60%','70%', '70-80%' ,'80-90%', '> 90%'];
       $scope.rangeTooltips = ['Memory Utilization < 70%<br\>40 Nodes', 'Memory Utilization 70-80%<br\>4 Nodes', 'Memory Utilization 80-90%<br\>4 Nodes', 'Memory Utilization > 90%<br\>4 Nodes'];
       $scope.thresholds = [0.6, 0.7, 0.8, 0.9];
       $scope.heatmapColorPattern = ['#d4f0fa', '#F9D67A', '#EC7A08', '#CE0000', '#f00'];

       $scope.showLegends = true;
       var clickAction = function (block) {
          console.log(block);
       };
       $scope.clickAction = clickAction;
     });
   </file>
 </example>
 */
angular.module('patternfly.charts').directive('pfHeatmap', function ($compile, $window) {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      data: '=',
      chartDataAvailable: '=?',
      height: '=?',
      chartTitle: '=?',
      showLegend: '=?',
      legendLabels: '=?',
      maxBlockSize: '@',
      minBlockSize: '@',
      blockPadding: '@',
      thresholds: '=?',
      heatmapColorPattern: '=?',
      clickAction: '=?',
      rangeOnHover: '=?',
      rangeHoverSize: '@',
      rangeTooltips: '=?'
    },
    templateUrl: 'charts/heatmap/heatmap.html',
    controller: function ($scope) {
      var thresholdDefaults = [0.7, 0.8, 0.9];
      var heatmapColorPatternDefaults = ['#d4f0fa', '#F9D67A', '#EC7A08', '#CE0000'];
      var legendLabelDefaults = ['< 70%', '70-80%' ,'80-90%', '> 90%'];
      var rangeTooltipDefaults = ['< 70%', '70-80%' ,'80-90%', '> 90%'];
      var heightDefault = 200;

      //Allow overriding of defaults
      if ($scope.maxBlockSize === undefined || isNaN($scope.maxBlockSize)) {
        $scope.maxSize = 64;
      } else {
        $scope.maxSize = parseInt($scope.maxBlockSize);
        if ($scope.maxSize < 5) {
          $scope.maxSize = 5;
        } else if ($scope.maxSize > 50) {
          $scope.maxSize = 50;
        }
      }

      if ($scope.minBlockSize === undefined || isNaN($scope.minBlockSize)) {
        $scope.minSize = 2;
      } else {
        $scope.minSize = parseInt($scope.minBlockSize);
      }

      if ($scope.blockPadding === undefined || isNaN($scope.blockPadding)) {
        $scope.padding = 2;
      } else {
        $scope.padding = parseInt($scope.blockPadding);
      }

      if ($scope.rangeHoverSize === undefined || isNaN($scope.rangeHoverSize)) {
        $scope.rangeHoverSize = 15;
      } else {
        $scope.rangeHoverSize = parseInt($scope.rangeHoverSize);
      }

      $scope.rangeOnHover = ($scope.rangeOnHover === undefined || $scope.rangeOnHover) ? true : false;

      if (!$scope.rangeTooltips) {
        $scope.rangeTooltips = rangeTooltipDefaults;
      }

      if (!$scope.thresholds) {
        $scope.thresholds = thresholdDefaults;
      }

      if (!$scope.heatmapColorPattern) {
        $scope.heatmapColorPattern = heatmapColorPatternDefaults;
      }

      if (!$scope.legendLabels) {
        $scope.legendLabels = legendLabelDefaults;
      }
      $scope.height = $scope.height || heightDefault;
      $scope.showLegend = $scope.showLegend || ($scope.showLegend === undefined);
      $scope.loadingDone = false;
    },
    link: function (scope, element, attrs) {
      var thisComponent = element[0].querySelector('.heatmap-pf-svg');
      var containerWidth, containerHeight, blockSize, numberOfRows;

      var setStyles = function () {
        scope.containerStyles = {
          height: scope.height + 'px',
          display: scope.chartDataAvailable === false ? 'none' : 'block'
        };
      };

      var setSizes = function () {
        var parentContainer = element[0].querySelector('.heatmap-container');
        containerWidth = parentContainer.clientWidth;
        containerHeight = parentContainer.clientHeight;
        blockSize = determineBlockSize();

        if ((blockSize - scope.padding) > scope.maxSize) {
          blockSize = scope.padding + scope.maxSize;

          // Attempt to square off the area, check if square fits
          numberOfRows = Math.ceil(Math.sqrt(scope.data.length));
          if (blockSize * numberOfRows > containerWidth ||
              blockSize * numberOfRows > containerHeight) {
            numberOfRows = (blockSize === 0) ? 0 : Math.floor(containerHeight / blockSize);
          }
        } else if ((blockSize - scope.padding) < scope.minSize) {
          blockSize = scope.padding + scope.minSize;

          // Attempt to square off the area, check if square fits
          numberOfRows = Math.ceil(Math.sqrt(scope.data.length));
          if (blockSize * numberOfRows > containerWidth ||
              blockSize * numberOfRows > containerHeight) {
            numberOfRows = (blockSize === 0) ? 0 : Math.floor(containerHeight / blockSize);
          }
        } else {
          numberOfRows = (blockSize === 0) ? 0 : Math.floor(containerHeight / blockSize);
        }
      };

      var determineBlockSize = function () {
        var x = containerWidth;
        var y = containerHeight;
        var n = scope.data ? scope.data.length : 0;
        var px = Math.ceil(Math.sqrt(n * x / y));
        var py = Math.ceil(Math.sqrt(n * y / x));
        var sx, sy;

        if (Math.floor(px * y / x) * px < n) {
          sx = y / Math.ceil(px * y / x);
        } else {
          sx = x / px;
        }

        if (Math.floor(py * x / y) * py < n) {
          sy = x / Math.ceil(x * py / y);
        } else {
          sy = y / py;
        }
        return Math.max(sx, sy);
      };

      var redraw = function () {
        var data = scope.data;
        var color = d3.scale.threshold().domain(scope.thresholds).range(scope.heatmapColorPattern);
        var rangeTooltip = d3.scale.threshold().domain(scope.thresholds).range(scope.rangeTooltips);
        var blocks;
        var fillSize = blockSize - scope.padding;
        var highlightBlock = function (block, active) {
          block.style('fill-opacity', active ? 1 : 0.4);
        };
        var highlightBlockColor = function (block, fillColor) {
          // Get fill color from given block
          var blockColor = color(block.map(function (d) {
            return d[0].__data__.value;
          }));
          // If given color matches, apply highlight
          if (blockColor === fillColor) {
            block.style('fill-opacity', 1);
          }
        };

        var svg = window.d3.select(thisComponent);
        svg.selectAll('*').remove();
        blocks = svg.selectAll('rect').data(data).enter().append('rect');
        blocks.attr('x', function (d, i) {
          return Math.floor(i / numberOfRows) * blockSize;
        }).attr('y', function (d, i) {
          return i % numberOfRows * blockSize;
        }).attr('width', fillSize).attr('height', fillSize).style('fill', function (d) {
          return color(d.value);
        }).attr('uib-tooltip-html', function (d, i) { //tooltip-html is throwing an exception
          if (scope.rangeOnHover && fillSize <= scope.rangeHoverSize) {
            return '"' + rangeTooltip(d.value) + '"';
          }
          return "'" + d.tooltip + "'";
        }).attr('tooltip-append-to-body', function (d, i) {
          return true;
        }).attr('tooltip-animation', function (d, i) {
          return false;
        });

        //Adding events
        blocks.on('mouseover', function () {
          var fillColor;
          blocks.call(highlightBlock, false);
          if (scope.rangeOnHover && fillSize <= scope.rangeHoverSize) {
            // Get fill color for current block
            fillColor = color(d3.select(this).map(function (d) {
              return d[0].__data__.value;
            }));
            // Highlight all blocks matching fill color
            blocks[0].forEach(function (block) {
              highlightBlockColor(d3.select(block), fillColor);
            });
          } else {
            d3.select(this).call(highlightBlock, true);
          }
        });
        blocks.on('click', function (d) {
          if (scope.clickAction) {
            scope.clickAction(d);
          }
        });

        //Compiles the tooltips
        angular.forEach(angular.element(blocks), function (block) {
          var el = angular.element(block);
          $compile(el)(scope);
        });

        svg.on('mouseleave', function () {
          blocks.call(highlightBlock, true);
        });
      };

      scope.$watch('data', function (newVal, oldVal) {
        if (typeof(newVal) !== 'undefined') {
          scope.loadingDone = true;
          setStyles();
          if (scope.chartDataAvailable !== false) {
            setSizes();
            redraw();
          }
        }
      });
      scope.$watch('chartDataAvailable', function () {
        if (scope.chartDataAvailable === false) {
          scope.loadingDone = true;
        }
        setStyles();
      });

      angular.element($window).bind('resize', function () {
        setSizes();
        redraw();
      });

      scope.$watch(
        function () {
          return [element[0].offsetWidth, element[0].offsetHeight].join('x');
        },
        function (value) {
          setSizes();
          redraw();
        }
      );
    }
  };
});
