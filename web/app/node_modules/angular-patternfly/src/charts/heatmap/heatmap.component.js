/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfHeatMap
 * @restrict E
 *
 * @description
 *   Component for rendering a heatmap chart.
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
           <pf-heatmap id="id" chart-title="title" data="data" chart-data-available="dataAvailable"
                show-legend="showLegends"></pf-heatmap>
         </div>
         <div class="col-md-3 example-heatmap-container">
           <pf-heatmap id="id" chart-title="titleAlt" data="data" chart-data-available="dataAvailable"
                show-legend="showLegends" legend-labels="legendLabels"  max-block-size="20" block-padding="5"
                heatmap-color-pattern="heatmapColorPattern" thresholds="thresholds"
                click-action="clickAction"></pf-heatmap>
         </div>
         <div class="col-md-3 example-heatmap-container">
           <pf-heatmap id="id" chart-title="titleSmall" data="data" chart-data-available="dataAvailable"
                show-legend="showLegends" max-block-size="15" range-tooltips="rangeTooltips"></pf-heatmap>
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
angular.module('patternfly.charts').component('pfHeatmap', {
  bindings: {
    data: '<',
    chartDataAvailable: '<?',
    height: '<?',
    chartTitle: '<?',
    showLegend: '<?',
    legendLabels: '<?',
    maxBlockSize: '@',
    minBlockSize: '@',
    blockPadding: '@',
    thresholds: '<?',
    heatmapColorPattern: '<?',
    clickAction: '<?',
    rangeOnHover: '<?',
    rangeHoverSize: '@',
    rangeTooltips: '<?'
  },
  templateUrl: 'charts/heatmap/heatmap.html',
  controller: function ($element, $window, $compile, $scope, $timeout) {
    'use strict';
    var ctrl = this, prevData;

    var containerWidth, containerHeight, blockSize, numberOfRows;

    var thresholdDefaults = [0.7, 0.8, 0.9];
    var heatmapColorPatternDefaults = ['#d4f0fa', '#F9D67A', '#EC7A08', '#CE0000'];
    var legendLabelDefaults = ['< 70%', '70-80%' ,'80-90%', '> 90%'];
    var rangeTooltipDefaults = ['< 70%', '70-80%' ,'80-90%', '> 90%'];
    var heightDefault = 200;

    var setStyles = function () {
      ctrl.containerStyles = {
        height: ctrl.height + 'px',
        display: ctrl.chartDataAvailable === false ? 'none' : 'block'
      };
    };

    var setSizes = function () {
      var parentContainer = $element[0].querySelector('.heatmap-container');
      containerWidth = parentContainer.clientWidth;
      containerHeight = parentContainer.clientHeight;
      blockSize = determineBlockSize();

      if ((blockSize - ctrl.padding) > ctrl.maxSize) {
        blockSize = ctrl.padding + ctrl.maxSize;

        // Attempt to square off the area, check if square fits
        numberOfRows = Math.ceil(Math.sqrt(ctrl.data.length));
        if (blockSize * numberOfRows > containerWidth ||
          blockSize * numberOfRows > containerHeight) {
          numberOfRows = (blockSize === 0) ? 0 : Math.floor(containerHeight / blockSize);
        }
      } else if ((blockSize - ctrl.padding) < ctrl.minSize) {
        blockSize = ctrl.padding + ctrl.minSize;

        // Attempt to square off the area, check if square fits
        numberOfRows = Math.ceil(Math.sqrt(ctrl.data.length));
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
      var n = ctrl.data ? ctrl.data.length : 0;
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
      var data = ctrl.data;
      var color = d3.scale.threshold().domain(ctrl.thresholds).range(ctrl.heatmapColorPattern);
      var rangeTooltip = d3.scale.threshold().domain(ctrl.thresholds).range(ctrl.rangeTooltips);
      var blocks;
      var fillSize = blockSize - ctrl.padding;
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

      var svg = window.d3.select(ctrl.thisComponent);
      svg.selectAll('*').remove();
      blocks = svg.selectAll('rect').data(data).enter().append('rect');
      blocks.attr('x', function (d, i) {
        return Math.floor(i / numberOfRows) * blockSize;
      }).attr('y', function (d, i) {
        return i % numberOfRows * blockSize;
      }).attr('width', fillSize).attr('height', fillSize).style('fill', function (d) {
        return color(d.value);
      }).attr('uib-tooltip-html', function (d, i) { //tooltip-html is throwing an exception
        if (ctrl.rangeOnHover && fillSize <= ctrl.rangeHoverSize) {
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
        if (ctrl.rangeOnHover && fillSize <= ctrl.rangeHoverSize) {
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
        if (ctrl.clickAction) {
          ctrl.clickAction(d);
        }
      });

      //Compiles the tooltips
      angular.forEach(angular.element(blocks), function (block) {
        var el = angular.element(block);
        // TODO: get heatmap tooltips to work without using $compile or $scope
        $compile(el)($scope);
      });

      svg.on('mouseleave', function () {
        blocks.call(highlightBlock, true);
      });
    };

    ctrl.updateAll = function () {
      // Need to deep watch changes in chart data
      prevData = angular.copy(ctrl.data);

      //Allow overriding of defaults
      if (ctrl.maxBlockSize === undefined || isNaN(ctrl.maxBlockSize)) {
        ctrl.maxSize = 64;
      } else {
        ctrl.maxSize = parseInt(ctrl.maxBlockSize);
        if (ctrl.maxSize < 5) {
          ctrl.maxSize = 5;
        } else if (ctrl.maxSize > 50) {
          ctrl.maxSize = 50;
        }
      }

      if (ctrl.minBlockSize === undefined || isNaN(ctrl.minBlockSize)) {
        ctrl.minSize = 2;
      } else {
        ctrl.minSize = parseInt(ctrl.minBlockSize);
      }

      if (ctrl.blockPadding === undefined || isNaN(ctrl.blockPadding)) {
        ctrl.padding = 2;
      } else {
        ctrl.padding = parseInt(ctrl.blockPadding);
      }

      if (ctrl.rangeHoverSize === undefined || isNaN(ctrl.rangeHoverSize)) {
        ctrl.rangeHoverSize = 15;
      } else {
        ctrl.rangeHoverSize = parseInt(ctrl.rangeHoverSize);
      }

      ctrl.rangeOnHover = (ctrl.rangeOnHover === undefined || ctrl.rangeOnHover) ? true : false;

      if (!ctrl.rangeTooltips) {
        ctrl.rangeTooltips = rangeTooltipDefaults;
      }

      if (!ctrl.thresholds) {
        ctrl.thresholds = thresholdDefaults;
      }

      if (!ctrl.heatmapColorPattern) {
        ctrl.heatmapColorPattern = heatmapColorPatternDefaults;
      }

      if (!ctrl.legendLabels) {
        ctrl.legendLabels = legendLabelDefaults;
      }
      ctrl.height = ctrl.height || heightDefault;
      ctrl.showLegend = ctrl.showLegend || (ctrl.showLegend === undefined);
      ctrl.loadingDone = false;

      angular.element($window).on('resize', function () {
        setSizes();
        redraw();
      });

      ctrl.thisComponent = $element[0].querySelector('.heatmap-pf-svg');

      $timeout(function () {
        setStyles();
        setSizes();
        redraw();
      });
    };

    ctrl.$onChanges = function (changesObj) {
      if (changesObj.chartDataAvailable && !changesObj.chartDataAvailable.isFirstChange()) {
        setStyles();
      } else {
        ctrl.updateAll();
        ctrl.loadingDone = true;
      }
    };

    ctrl.$doCheck = function () {
      // do a deep compare on chartData and config
      if (!angular.equals(ctrl.data, prevData)) {
        setStyles();
        if (ctrl.chartDataAvailable !== false) {
          setSizes();
          redraw();
        }
      }
    };

    ctrl.$postLink = function () {
      setStyles();
      setSizes();
      redraw();
    };
  }
});
