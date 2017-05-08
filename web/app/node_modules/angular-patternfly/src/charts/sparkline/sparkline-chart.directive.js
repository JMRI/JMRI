/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfSparklineChart
 *
 * @description
 *   Directive for rendering a sparkline chart.
 *   <br><br>
 *   See http://c3js.org/reference.html for a full list of C3 chart options.
 *
 * @param {object} config configuration settings for the sparkline chart:<br/>
 * <ul style='list-style-type: none'>
 * <li>.chartId            - the ID of the container that the chart should bind to
 * <li>.units              - unit label for values, ex: 'MHz','GB', etc..
 * <li>.tooltipType        - (optional) set the type of tooltip, valid values:
 * <ul style='list-style-type: none'>
 * <li>'default'           - show the data point value and the data point name.
 * <li>'usagePerDay'       - show the date, percent used, and used value for the data point.
 * <li>'valuePerDay'       - show the date and value for the data point.
 * <li>'percentage'        - show the current data point as a percentage.
 * </ul>
 * <li>.tooltipFn          - (optional) override the tooltip contents generation functions. Should take a data point and
 *                           return HTML markup for the tooltip contents. Setting this overrides the tooltipType value.
 * <li>.area               - (optional) overrides the default Area properties of the C3 chart
 * <li>.size               - (optional) overrides the default Size properties of the C3 chart
 * <li>.axis               - (optional) overrides the default Axis properties of the C3 chart
 * <li>.color              - (optional) overrides the default Color properties of the C3 chart
 * <li>.legend             - (optional) overrides the default Legend properties of the C3 chart
 * <li>.point              - (optional) overrides the default Point properties of the C3 chart
 * </ul>
 *
 * @param {object} chartData the data to be shown as an area chart<br/>
 * <ul style='list-style-type: none'>
 * <li>.xData         - Array, X values for the data points, first element must be the name of the data
 * <li>.yData         - Array, Y Values for the data points, first element must be the name of the data
 * <li>.total         - (optional) The Total amount, used when determining percentages
 * <li>.dataAvailable - Flag if there is data available - default: true
 * </ul>
 *
 * @param {int=} chartHeight   height of the sparkline chart
 * @param {boolean=} showXAxis override config settings for showing the X Axis
 * @param {boolean=} showYAxis override config settings for showing the Y Axis

 * @example
 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl" class="row" style="display:inline-block; width: 100%;">
       <div class="col-md-12">
         <div pf-sparkline-chart config="config" chart-data="data" chart-height="custChartHeight" show-x-axis="custShowXAxis" show-y-axis="custShowYAxis"></div>
       </div>
       <hr class="col-md-12">
       <div class="col-md-12">
         <form role="form">
           <div class="form-group">
             <label>Tooltip Type</label>
             </br>
             <label class="radio-inline">
               <input type="radio" ng-model="config.tooltipType" value="default">Default</input>
             </label>
             <label class="radio-inline">
               <input type="radio" ng-model="config.tooltipType" value="usagePerDay">Usage Per Day</input>
             </label>
             <label class="radio-inline">
               <input type="radio" ng-model="config.tooltipType" value="valuePerDay">Value Per Day</input>
             </label>
             <label class="radio-inline">
               <input type="radio" ng-model="config.tooltipType" value="percentage">Percentage</input>
             </label>
           </div>
         </form>
         <div class="row">
           <div class="col-md-6">
             <form role="form"">
               <div class="form-group">
                 <label>Show</label>
                 </br>
                 <label class="checkbox-inline">
                   <input type="checkbox" ng-model="custShowXAxis">X Axis</input>
                 </label>
                 <label class="checkbox-inline">
                   <input type="checkbox" ng-model="custShowYAxis">Y Axis</input>
                 </label>
               </div>
             </form>
           </div>
           <div class="col-md-3">
             <form role="form" >
               <div class="form-group">
                 <label>Chart Height</label>
                 </br>
                 <input style="height:25px; width:60px;" type="number" ng-model="custChartHeight"></input>
               </div>
             </form>
           </div>
           <div class="col-md-3">
                 <button ng-click="addDataPoint()">Add Data Point</button>
           </div>
         </div>
        <div class="row">
         <div class="col-md-6">
           <form role="form"">
             <div class="form-group">
               <label class="checkbox-inline">
                 <input type="checkbox" ng-model="data.dataAvailable">Data Available</input>
               </label>
             </div>
           </form>
         </div>
       </div>
       </div>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.charts' ).controller( 'ChartCtrl', function( $scope ) {

       $scope.config = {
         chartId: 'exampleSparkline',
         tooltipType: 'default'
       };

       var today = new Date();
       var dates = ['dates'];
       for (var d = 20 - 1; d >= 0; d--) {
         dates.push(new Date(today.getTime() - (d * 24 * 60 * 60 * 1000)));
       }

       $scope.data = {
         dataAvailable: true,
         total: 100,
         xData: dates,
         yData: ['used', 10, 20, 30, 20, 30, 10, 14, 20, 25, 68, 54, 56, 78, 56, 67, 88, 76, 65, 87, 76]
       };

       $scope.custShowXAxis = false;
       $scope.custShowYAxis = false;
       $scope.custChartHeight = 60;

       $scope.addDataPoint = function () {
         $scope.data.xData.push(new Date($scope.data.xData[$scope.data.xData.length - 1].getTime() + (24 * 60 * 60 * 1000)));
         $scope.data.yData.push(Math.round(Math.random() * 100));
       };
     });
   </file>
 </example>
 */
(function (patternfly) {
  'use strict';
  angular.module('patternfly.charts').directive('pfSparklineChart', function (pfUtils) {
    return {
      restrict: 'A',
      scope: {
        config: '=',
        chartData: '=',
        chartHeight: '=?',
        showXAxis: '=?',
        showYAxis: '=?'
      },
      replace: true,
      templateUrl: 'charts/sparkline/sparkline-chart.html',
      controller: ['$scope',
        function ($scope) {

          // Create an ID for the chart based on the chartId in the config if given
          $scope.sparklineChartId = 'sparklineChart';
          if ($scope.config.chartId) {
            $scope.sparklineChartId = $scope.config.chartId + $scope.sparklineChartId;
          }

          /*
           * Convert the config data to C3 Data
           */
          $scope.getSparklineData = function (chartData) {
            var sparklineData  = {
              type: 'area'
            };

            if (chartData && chartData.dataAvailable !== false && chartData.xData && chartData.yData) {
              sparklineData.x = chartData.xData[0];
              sparklineData.columns = [
                chartData.xData,
                chartData.yData
              ];
            }

            return sparklineData;
          };

          $scope.getTooltipTableHTML = function (tipRows) {
            return '<div class="module-triangle-bottom">' +
              '  <table class="c3-tooltip">' +
              '    <tbody>' +
              tipRows +
              '    </tbody>' +
              '  </table>' +
              '</div>';
          };

          $scope.sparklineTooltip = function () {
            return {
              contents: function (d) {
                var tipRows;
                var percentUsed = 0;

                if ($scope.config.tooltipFn) {
                  tipRows = $scope.config.tooltipFn(d);
                } else {
                  switch ($scope.config.tooltipType) {
                  case 'usagePerDay':
                    if ($scope.chartData.dataAvailable !== false && $scope.chartData.total > 0) {
                      percentUsed = Math.round(d[0].value / $scope.chartData.total * 100.0);
                    }
                    tipRows =
                      '<tr>' +
                      '  <th colspan="2">' + d[0].x.toLocaleDateString() + '</th>' +
                      '</tr>' +
                      '<tr>' +
                      '  <td class="name">' + percentUsed + '%:' + '</td>' +
                      '  <td class="value text-nowrap">' + d[0].value + ' ' +  ($scope.config.units ? $scope.config.units + ' ' : '') + d[0].name + '</td>' +
                      '</tr>';
                    break;
                  case 'valuePerDay':
                    tipRows =
                      '<tr>' +
                      '  <td class="value">' +  d[0].x.toLocaleDateString() + '</td>' +
                      '  <td class="value text-nowrap">' +  d[0].value + ' ' + d[0].name + '</td>' +
                      '</tr>';
                    break;
                  case 'percentage':
                    percentUsed = Math.round(d[0].value / $scope.chartData.total * 100.0);
                    tipRows =
                      '<tr>' +
                      '  <td class="name">' + percentUsed + '%' + '</td>' +
                      '</tr>';
                    break;
                  default:
                    tipRows = patternfly.c3ChartDefaults().getDefaultSparklineTooltip().contents(d);
                  }
                }
                return $scope.getTooltipTableHTML(tipRows);
              },
              position: function (data, width, height, element) {
                var center;
                var top;
                var chartBox;
                var graphOffsetX;
                var x;

                try {
                  center = parseInt(element.getAttribute('x'));
                  top = parseInt(element.getAttribute('y'));
                  chartBox = document.querySelector('#' + $scope.sparklineChartId).getBoundingClientRect();
                  graphOffsetX = document.querySelector('#' + $scope.sparklineChartId + ' g.c3-axis-y').getBoundingClientRect().right;
                  x = Math.max(0, center + graphOffsetX - chartBox.left - Math.floor(width / 2));

                  return {
                    top: top - height,
                    left: Math.min(x, chartBox.width - width)
                  };
                } catch (e) {
                }
              }
            };
          };

          /*
           * Setup Axis options. Default is to not show either axis. This can be overridden in two ways:
           *   1) in the config, setting showAxis to true will show both axes
           *   2) in the attributes showXAxis and showYAxis will override the config if set
           *
           * By default only line and the tick marks are shown, no labels. This is a sparkline and should be used
           * only to show a brief idea of trending. This can be overridden by setting the config.axis options per C3
           */

          if ($scope.showXAxis === undefined) {
            $scope.showXAxis = ($scope.config.showAxis !== undefined) && $scope.config.showAxis;
          }

          if ($scope.showYAxis === undefined) {
            $scope.showYAxis = ($scope.config.showAxis !== undefined) && $scope.config.showAxis;
          }

          $scope.defaultConfig = patternfly.c3ChartDefaults().getDefaultSparklineConfig();
          $scope.defaultConfig.axis = {
            x: {
              show: $scope.showXAxis === true,
              type: 'timeseries',
              tick: {
                format: function () {
                  return '';
                }
              }
            },
            y: {
              show: $scope.showYAxis === true,
              tick: {
                format: function () {
                  return '';
                }
              }
            }
          };

          // Setup the default configuration
          $scope.defaultConfig.tooltip = $scope.sparklineTooltip();
          if ($scope.chartHeight) {
            $scope.defaultConfig.size.height = $scope.chartHeight;
          }
          $scope.defaultConfig.units = '';

          // Convert the given data to C3 chart format
          $scope.config.data = pfUtils.merge($scope.config.data, $scope.getSparklineData($scope.chartData));

          // Override defaults with callers specifications
          $scope.chartConfig = pfUtils.merge($scope.defaultConfig, $scope.config);
        }
      ],

      link: function (scope) {
        scope.$watch('config', function () {
          scope.config.data = pfUtils.merge(scope.config.data, scope.getSparklineData(scope.chartData));
          scope.chartConfig = pfUtils.merge(scope.defaultConfig, scope.config);
        }, true);
        scope.$watch('chartHeight', function () {
          if (scope.chartHeight) {
            scope.chartConfig.size.height = scope.chartHeight;
          }
        });
        scope.$watch('showXAxis', function () {
          scope.chartConfig.axis.x.show = scope.showXAxis === true;
        });
        scope.$watch('showYAxis', function () {
          scope.chartConfig.axis.y.show = scope.showYAxis === true;
        });
        scope.$watch('chartData', function () {
          scope.chartConfig.data = pfUtils.merge(scope.chartConfig.data, scope.getSparklineData(scope.chartData));
        }, true);
      }
    };
  });
}(patternfly));
