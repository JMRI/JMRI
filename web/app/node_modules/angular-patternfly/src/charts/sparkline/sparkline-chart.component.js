/**
 * @ngdoc directive
 * @name patternfly.charts.component:pfSparklineChart
 * @restrict E
 *
 * @description
 *   Component for rendering a sparkline chart.
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
         <pf-sparkline-chart config="config" chart-data="data" chart-height="custChartHeight" show-x-axis="custShowXAxis" show-y-axis="custShowYAxis"></pf-sparkline-chart>
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
angular.module('patternfly.charts').component('pfSparklineChart', {
  bindings: {
    config: '<',
    chartData: '<',
    chartHeight: '<?',
    showXAxis: '<?',
    showYAxis: '<?'
  },
  templateUrl: 'charts/sparkline/sparkline-chart.html',
  controller: function (pfUtils) {
    'use strict';
    var ctrl = this, prevChartData;

    ctrl.updateAll = function () {
      // Need to deep watch changes in chart data
      prevChartData = angular.copy(ctrl.chartData);

      // Create an ID for the chart based on the chartId in the config if given
      if (ctrl.sparklineChartId === undefined) {
        ctrl.sparklineChartId = 'sparklineChart';
        if (ctrl.config.chartId) {
          ctrl.sparklineChartId = ctrl.config.chartId + ctrl.sparklineChartId;
        }
      }

      /*
       * Setup Axis options. Default is to not show either axis. This can be overridden in two ways:
       *   1) in the config, setting showAxis to true will show both axes
       *   2) in the attributes showXAxis and showYAxis will override the config if set
       *
       * By default only line and the tick marks are shown, no labels. This is a sparkline and should be used
       * only to show a brief idea of trending. This can be overridden by setting the config.axis options per C3
       */

      if (ctrl.showXAxis === undefined) {
        ctrl.showXAxis = (ctrl.config.showAxis !== undefined) && ctrl.config.showAxis;
      }

      if (ctrl.showYAxis === undefined) {
        ctrl.showYAxis = (ctrl.config.showAxis !== undefined) && ctrl.config.showAxis;
      }

      ctrl.defaultConfig = patternfly.c3ChartDefaults().getDefaultSparklineConfig();
      ctrl.defaultConfig.axis = {
        x: {
          show: ctrl.showXAxis === true,
          type: 'timeseries',
          tick: {
            format: function () {
              return '';
            }
          }
        },
        y: {
          show: ctrl.showYAxis === true,
          tick: {
            format: function () {
              return '';
            }
          }
        }
      };

      // Setup the default configuration
      ctrl.defaultConfig.tooltip = ctrl.sparklineTooltip();
      if (ctrl.chartHeight) {
        ctrl.defaultConfig.size.height = ctrl.chartHeight;
      }
      ctrl.defaultConfig.units = '';

      // Convert the given data to C3 chart format
      ctrl.config.data = pfUtils.merge(ctrl.config.data, ctrl.getSparklineData(ctrl.chartData));

      // Override defaults with callers specifications
      ctrl.chartConfig = pfUtils.merge(ctrl.defaultConfig, ctrl.config);
    };

    /*
     * Convert the config data to C3 Data
     */
    ctrl.getSparklineData = function (chartData) {
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

    ctrl.getTooltipTableHTML = function (tipRows) {
      return '<div class="module-triangle-bottom">' +
        '  <table class="c3-tooltip">' +
        '    <tbody>' +
        tipRows +
        '    </tbody>' +
        '  </table>' +
        '</div>';
    };

    ctrl.sparklineTooltip = function () {
      return {
        contents: function (d) {
          var tipRows;
          var percentUsed = 0;

          if (ctrl.config.tooltipFn) {
            tipRows = ctrl.config.tooltipFn(d);
          } else {
            switch (ctrl.config.tooltipType) {
            case 'usagePerDay':
              if (ctrl.chartData.dataAvailable !== false && ctrl.chartData.total > 0) {
                percentUsed = Math.round(d[0].value / ctrl.chartData.total * 100.0);
              }
              tipRows =
                '<tr>' +
                '  <th colspan="2">' + d[0].x.toLocaleDateString() + '</th>' +
                '</tr>' +
                '<tr>' +
                '  <td class="name">' + percentUsed + '%:' + '</td>' +
                '  <td class="value text-nowrap">' + d[0].value + ' ' +  (ctrl.config.units ? ctrl.config.units + ' ' : '') + d[0].name + '</td>' +
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
              percentUsed = Math.round(d[0].value / ctrl.chartData.total * 100.0);
              tipRows =
                '<tr>' +
                '  <td class="name">' + percentUsed + '%' + '</td>' +
                '</tr>';
              break;
            default:
              tipRows = patternfly.c3ChartDefaults().getDefaultSparklineTooltip().contents(d);
            }
          }
          return ctrl.getTooltipTableHTML(tipRows);
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
            chartBox = document.querySelector('#' + ctrl.sparklineChartId).getBoundingClientRect();
            graphOffsetX = document.querySelector('#' + ctrl.sparklineChartId + ' g.c3-axis-y').getBoundingClientRect().right;
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

    ctrl.$onChanges = function (changesObj) {
      ctrl.updateAll();
    };

    ctrl.$doCheck = function () {
      // do a deep compare on chartData
      if (!angular.equals(ctrl.chartData, prevChartData)) {
        ctrl.updateAll();
      }
    };
  }
});
