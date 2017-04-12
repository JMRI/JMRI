/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfC3Chart
 *
 * @description
 *   Directive for wrapping c3 library
 *
 *   Note: The 'patternfly.charts' module is not a dependency in the default angular 'patternfly' module.
 *   In order to use patternfly charts you must add 'patternfly.charts' as a dependency in your application.
 *
 *
 * @param {string} id the ID of the container that the chart should bind to
 * @param {expression} config the c3 configuration options for the chart
 * @param {function (chart))=} getChartCallback the callback user function to be called once the chart is generated, containing the c3 chart object
 *
 * @example

 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl">
        <div pf-c3-chart id="chartId" config="chartConfig" get-chart-callback="getChart"></div>

        <form role="form" style="width:300px">
          Total = {{total}}, Used = {{used}}, Available = {{available}}
          <div class="form-group">
            <label>Used</label>
            <input type="text" class="form-control" ng-model="newUsed">
          </div>
          <input type="button" ng-click="submitform(newUsed)" value="Set Used" />
          <input type="button" ng-click="focusUsed()" value="Focus Used" />
        </form>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.charts' ).controller( 'ChartCtrl', function( $scope ) {
       $scope.used = 950;
       $scope.total = 1000;
       $scope.available =  $scope.total - $scope.used;

       $scope.chartConfig = patternfly.c3ChartDefaults().getDefaultDonutConfig('MHz Used');
       $scope.chartConfig.data = {
         type: "donut",
         columns: [
           ["Used", $scope.used],
           ["Available", $scope.total - $scope.used]
         ],
         groups: [
           ["used", "available"]
         ],
         order: null
       };

       $scope.getChart = function (chart) {
         $scope.chart = chart;
       }

       $scope.focusUsed = function () {
         $scope.chart.focus("Used");
       }

       $scope.updateAvailable = function (val) {
         $scope.available =  $scope.total - $scope.used;
       }

       $scope.submitform = function (val) {
         $scope.used = val;
         $scope.updateAvailable();
         $scope.chartConfig.data.columns = [["Used",$scope.used],["Available",$scope.available]];
       };
     });
   </file>
 </example>
 */
(function (patternfly) {
  'use strict';

  angular.module('patternfly.charts').directive('pfC3Chart', function ($timeout) {
    return {
      restrict: 'A',
      scope: {
        config: '=',
        getChartCallback: '='
      },
      template: '<div id=""></div>',
      replace: true,
      link: function (scope, element, attrs) {
        // store the chart object
        var chart = undefined;
        scope.$watch('config', function () {
          $timeout(function () {
            //generate c3 chart data
            var chartData = scope.config;
            if (chartData) {
              chartData.bindto = '#' + attrs.id;
              //checks if the chart is created
              if (!chart) {
                chart = c3.generate(chartData);
              }else {
                //if it is created, then, we only need to load changes
                chart.load(scope.config.data);
              }
              if (scope.getChartCallback) {
                scope.getChartCallback(chart);
              }
            }
          });
        }, true);
      }
    };
  });
}(patternfly));
