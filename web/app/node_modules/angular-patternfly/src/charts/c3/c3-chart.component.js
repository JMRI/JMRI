/**
 * @ngdoc directive
 * @name patternfly.charts.component:pfC3Chart
 * @restrict E
 *
 * @description
 *   Component for wrapping c3 library
 *
 *   Note: The 'patternfly.charts' module is not a dependency in the default angular 'patternfly' module.
 *   In order to use patternfly charts you must add 'patternfly.charts' as a dependency in your application.
 *
 *
 * @param {string} id the ID of the container that the chart should bind to
 * @param {expression} config the c3 configuration options for the chart
 * @param {function (chart))=} getChartCallback the callback user function to be called once the chart is generated, containing the c3 chart object
 * @example

 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl">
        <pf-c3-chart id="chartId" config="chartConfig" get-chart-callback="getChart"></pf-c3-chart>

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
         console.log("submitform");
         $scope.used = val;
         $scope.updateAvailable();
         $scope.chartConfig.data.columns = [["Used",$scope.used],["Available",$scope.available]];
       };
     });
   </file>
 </example>
 */
(function () {
  'use strict';

  angular.module('patternfly.charts').component('pfC3Chart', {
    bindings: {
      config: '<',
      getChartCallback: '<'
    },
    template: '<div id=""></div>',
    controller: function ($timeout, $attrs) {
      var ctrl = this, prevConfig;

      // store the chart object
      var chart;
      ctrl.generateChart = function () {
        var chartData;

        // Need to deep watch changes in chart config
        prevConfig = angular.copy(ctrl.config);

        $timeout(function () {
          chartData = ctrl.config;
          if (chartData) {
            chartData.bindto = '#' + $attrs.id;
            // always re-generate donut pct chart because it's colors
            // change based on data and thresholds
            if (!chart || $attrs.id.indexOf('donutPctChart')) {
              chart = c3.generate(chartData);
            } else {
              //if chart is already created, then we only need to re-load data
              chart.load(ctrl.config.data);
            }
            if (ctrl.getChartCallback) {
              ctrl.getChartCallback(chart);
            }
            prevConfig = angular.copy(ctrl.config);
          }
        });
      };

      ctrl.$doCheck = function () {
        // do a deep compare on config
        if (!angular.equals(ctrl.config, prevConfig)) {
          ctrl.generateChart();
        }
      };
    }
  });
}());
