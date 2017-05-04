/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfUtilizationTrendChart
 *
 * @description
 *   Directive for rendering a utilization trend chart. The utilization trend chart combines overall
 *   data with a pfDonutPctChart and a pfSparklineChart. Add the options for the pfDonutChart via
 *   the donutConfig parameter. Add the options for the pfSparklineChart via the sparklineConfig
 *   parameter.
 *   <br><br>
 *   See http://c3js.org/reference.html for a full list of C3 chart options.
 *
 * @param {object} config configuration settings for the utilization trend chart:<br/>
 * <ul style='list-style-type: none'>
 * <li>.title        - title of the Utilization chart
 * <li>.units        - unit label for values, ex: 'MHz','GB', etc..
 * </ul>
 *
 * @param {object} donutConfig configuration settings for the donut pct chart, see pfDonutPctChart for specifics<br/>
 * @param {object} sparklineConfig configuration settings for the sparkline chart, see pfSparklineChart for specifics<br/>
 *
 * @param {object} chartData the data to be shown in the donut and sparkline charts<br/>
 * <ul style='list-style-type: none'>
 * <li>.used   - number representing the amount used
 * <li>.total  - number representing the total amount
 * <li>.xData  - Array, X values for the data points, first element must be the name of the data
 * <li>.yData  - Array, Y Values for the data points, first element must be the name of the data
 * <li>.dataAvailable - Flag if there is data available - default: true
 * </ul>
 *
 * @param {string=} donutCenterLabel specifies the contents of the donut's center label.<br/>
 * <strong>Values:</strong>
 * <ul style='list-style-type: none'>
 * <li> 'used'      - displays the Used amount in the center label (default)
 * <li> 'available' - displays the Available amount in the center label
 * <li> 'percent'   - displays the Usage Percent of the Total amount in the center label
 * <li> 'none'      - does not display the center label
 * </ul>
 * @param {int=} sparklineChartHeight   height of the sparkline chart
 * @param {boolean=} showSparklineXAxis override sparkline config settings for showing the X Axis
 * @param {boolean=} showSparklineYAxis override sparkline config settings for showing the Y Axis

 * @example
 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl" class="row" style="display:inline-block; width: 100%;">
       <div class="col-md-12">
         <div pf-utilization-trend-chart config="config"
              chart-data="data" center-label="centerLabel"
              donut-config="donutConfig" sparkline-config="sparklineConfig"
              sparkline-chart-height="custChartHeight"
              show-sparkline-x-axis="custShowXAxis"
              show-sparkline-y-axis="custShowYAxis">
         </div>
       </div>
       <hr class="col-md-12">
       <div class="col-md-12">
         <form role="form">
           <div class="form-group">
           <label>Donut Center Label Type</label>
           </br>
           <label class="radio-inline">
             <input type="radio" ng-model="centerLabel" value="used">Used</input>
           </label>
           <label class="radio-inline">
             <input type="radio" ng-model="centerLabel" value="available">Available</input>
           </label>
           <label class="radio-inline">
             <input type="radio" ng-model="centerLabel" value="percent">Percent</input>
           </label>
           <label class="radio-inline">
             <input type="radio" ng-model="centerLabel" value="none">None</input>
           </label>
           </div>
         </form>
         <form role="form">
           <div class="form-group">
             <label>Sparkline Tooltip Type</label>
               </br>
             <label class="radio-inline">
               <input type="radio" ng-model="sparklineConfig.tooltipType" value="default">Default</input>
             </label>
             <label class="radio-inline">
               <input type="radio" ng-model="sparklineConfig.tooltipType" value="usagePerDay">Usage Per Day</input>
             </label>
             <label class="radio-inline">
               <input type="radio" ng-model="sparklineConfig.tooltipType" value="valuePerDay">Value Per Day</input>
             </label>
             <label class="radio-inline">
               <input type="radio" ng-model="sparklineConfig.tooltipType" value="percentage">Percentage</input>
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
                   <input type="checkbox" ng-model="custShowXAxis">Sparkline X Axis</input>
                 </label>
                 <label class="checkbox-inline">
                   <input type="checkbox" ng-model="custShowYAxis">Sparkline Y Axis</input>
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
                   <input type="checkbox" ng-model="data.dataAvailable" ng-change="updateDataAvailable()">Data Available</input>
                 </label>
               </div>
             </form>
         </div>
       </div>
     </div>
   </file>

   <file name="script.js">
   angular.module( 'patternfly.charts' ).controller( 'ChartCtrl', function( $scope ) {
     $scope.config = {
       title: 'Memory',
       units: 'GB'
     };
     $scope.donutConfig = {
       chartId: 'chartA',
       thresholds: {'warning':'60','error':'90'}
     };
     $scope.sparklineConfig = {
       'chartId': 'exampleSparkline',
       'tooltipType': 'default',
       'units': 'GB'
     };

    var today = new Date();
    var dates = ['dates'];
    for (var d = 20 - 1; d >= 0; d--) {
        dates.push(new Date(today.getTime() - (d * 24 * 60 * 60 * 1000)));
    }

     $scope.data = {
         dataAvailable: true,
         used: 76,
         total: 100,
         xData: dates,
         yData: ['used', '10', '20', '30', '20', '30', '10', '14', '20', '25', '68', '54', '56', '78', '56', '67', '88', '76', '65', '87', '76']
     };

     $scope.centerLabel = 'used';

     $scope.custShowXAxis = false;
     $scope.custShowYAxis = false;
     $scope.custChartHeight = 60;

     $scope.addDataPoint = function () {
       var newData = Math.round(Math.random() * 100);
       var newDate = new Date($scope.data.xData[$scope.data.xData.length - 1].getTime() + (24 * 60 * 60 * 1000));

       $scope.data.used = newData;
       $scope.data.xData.push(newDate);
       $scope.data.yData.push(newData);
     };
   });
   </file>
 </example>
 */
angular.module('patternfly.charts').directive('pfUtilizationTrendChart', function () {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      chartData: '=',
      config: '=',
      centerLabel: '=?',
      donutConfig: '=',
      sparklineConfig: '=',
      sparklineChartHeight: '=?',
      showSparklineXAxis: '=?',
      showSparklineYAxis: '=?'
    },
    replace: true,
    templateUrl: 'charts/utilization-trend/utilization-trend-chart.html',
    controller: ['$scope',
      function ($scope) {
        if ($scope.centerLabel === undefined) {
          $scope.centerLabel = 'used';

        }
        if ($scope.donutConfig.units === undefined) {
          $scope.donutConfig.units = $scope.config.units;
        }
        if ($scope.chartData.available === undefined) {
          $scope.chartData.available = $scope.chartData.total - $scope.chartData.used;
        }
        $scope.config.units = $scope.config.units || $scope.units;
      }
    ],
    link: function (scope, element) {
      var setupCurrentValues = function () {
        if (scope.centerLabel === 'available') {
          scope.currentValue = scope.chartData.used;
          scope.currentText = 'Used';
        } else {
          scope.currentValue = scope.chartData.total - scope.chartData.used;
          scope.currentText = 'Available';
        }
      };
      scope.$watchGroup(['centerLabel', 'chartData.used', 'chartData.available', 'chartData.total'], function () {
        setupCurrentValues();
      });
    }
  };
});
