/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfUtilizationBarChart
 *
 * @description
 *   Directive for rendering a utilization bar chart
 *   There are three possible fill colors for Used Percentage, dependent on whether or not there are thresholds:<br/>
 *   <ul>
 *   <li>When no thresholds exist, or if the used percentage has not surpassed any thresholds, the indicator is blue.
 *   <li>When the used percentage has surpassed the warning threshold, but not the error threshold, the indicator is orange.
 *   <li>When the used percentage has surpassed the error threshold, the indicator is is red.
 *   </ul>
 *
 * @param {object} chartData the data to be shown in the utilization bar chart<br/>
 * <ul style='list-style-type: none'>
 * <li>.used          - number representing the amount used
 * <li>.total         - number representing the total amount
 * <li>.dataAvailable - Flag if there is data available - default: true
 * </ul>
 *
 * @param {object=} chart-title The title displayed on the left-hand side of the chart
 * @param {object=} chart-footer The label displayed on the right-hand side of the chart.  If chart-footer is not
 * specified, the automatic footer-label-format will be used.
 * @param {object=} layout Various alternative layouts the utilization bar chart may have:<br/>
 * <ul style='list-style-type: none'>
 * <li>.type - The type of layout to use.  Valid values are 'regular' (default) displays the standard chart layout,
 * and 'inline' displays a smaller, inline layout.</li>
 * <li>.titleLabelWidth - Width of the left-hand title label when using 'inline' layout. Example values are "120px", "20%", "10em", etc..</li>
 * <li>.footerLabelWidth - Width of the right-hand used label when using 'inline' layout. Example values are "120px", "20%", "10em", etc..</li>
 * </ul>
 * @param {string=} footer-label-format The auto-format of the label on the right side of the bar chart when chart-footer
 * has not been specified. Values may be:<br/>
 * <ul style='list-style-type: none'>
 * <li>'actual' - (default) displays the standard label of '(n) of (m) (units) Used'.
 * <li>'percent' - displays a percentage label of '(n)% Used'.</li>
 * </ul>
 * @param {object=} units to be displayed on the chart. Examples: "GB", "MHz", "I/Ops", etc...
 * @param {string=} threshold-error The percentage used, when reached, denotes an error.  Valid values are 1-100. When the error threshold
 * has been reached, the used donut arc will be red.
 * @param {string=} threshold-warning The percentage usage, when reached, denotes a warning.  Valid values are 1-100. When the warning threshold
 * has been reached, the used donut arc will be orange.
 *
 * @example
 <example module="patternfly.example">
   <file name="index.html">
     <div ng-controller="ChartCtrl">

       <label class="label-title">Default Layout, no Thresholds</label>
       <div pf-utilization-bar-chart chart-data=data1 chart-title=title1
       units=units1></div>
       <br>
       <label class="label-title">Inline Layouts with Error, Warning, and Ok Thresholds</label>
       <div pf-utilization-bar-chart chart-data=data5 chart-title=title5 layout=layoutInline units=units5 threshold-error="85" threshold-warning="60">../utilization-trend/utilization-trend-chart-directive.js</div>
       <div pf-utilization-bar-chart chart-data=data3 chart-title=title3 layout=layoutInline units=units3 threshold-error="85" threshold-warning="60"></div>
       <div pf-utilization-bar-chart chart-data=data2 chart-title=title2 layout=layoutInline units=units2 threshold-error="85" threshold-warning="60"></div>
       <br>
       <label class="label-title">layout='inline', footer-label-format='percent', and custom chart-footer labels</label>
       <div pf-utilization-bar-chart chart-data=data2 chart-title=title2 layout=layoutInline footer-label-format='percent' units=units2 threshold-error="85" threshold-warning="60"></div>
       <div pf-utilization-bar-chart chart-data=data3 chart-title=title3 layout=layoutInline footer-label-format='percent' units=units3 threshold-error="85" threshold-warning="60"></div>
       <div pf-utilization-bar-chart chart-data=data4 chart-title=title4 chart-footer=footer1 layout=layoutInline units=units4 threshold-error="85" threshold-warning="60"></div>
       <div pf-utilization-bar-chart chart-data=data5 chart-title=title5 chart-footer=footer2 layout=layoutInline units=units5 threshold-error="85" threshold-warning="60"></div>
       <div class="row">
         <div class="col-md-6">
           <form role="form"">
             <div class="form-group">
               <label class="checkbox-inline">
                 <input type="checkbox" ng-model="data1.dataAvailable">Data Available</input>
               </label>
             </div>
           </form>
         </div>
       </div>
     </div>
   </file>

   <file name="script.js">
   angular.module( 'patternfly.example', ['patternfly.charts', 'patternfly.card']);

   angular.module( 'patternfly.example' ).controller( 'ChartCtrl', function( $scope ) {

    $scope.title1 = 'RAM Usage';
    $scope.units1 = 'MB';

    $scope.data1 = {
      'dataAvailable': true,
      'used': '8',
      'total': '24'
    };

    $scope.title2      = 'Memory';
    $scope.units2      = 'GB';

    $scope.data2 = {
      'used': '25',
      'total': '100'
    };

    $scope.title3 = 'CPU Usage';
    $scope.units3 = 'MHz';

    $scope.data3 = {
      'used': '420',
      'total': '500',
    };

    $scope.title4 = 'Disk Usage';
    $scope.units4 = 'TB';
    $scope.data4 = {
      'used': '350',
      'total': '500',
    };

    $scope.title5 = 'Disk I/O';
    $scope.units5 = 'I/Ops';
    $scope.data5 = {
      'used': '450',
      'total': '500',
    };

    $scope.layoutInline = {
      'type': 'inline'
    };

    $scope.footer1 = '<strong>500 TB</strong> Total';
    $scope.footer2 = '<strong>450 of 500</strong> Total';

   });
   </file>
 </example>
*/

angular.module('patternfly.charts').directive('pfUtilizationBarChart', function ($timeout) {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      chartData: '=',
      chartTitle: '=',
      chartFooter: '=',
      units: '=',
      thresholdError: '=?',
      thresholdWarning: '=?',
      footerLabelFormat: '@?',
      layout: '=?'
    },

    templateUrl: 'charts/utilization-bar/utilization-bar-chart.html',
    link: function (scope) {
      scope.$watch('chartData', function (newVal, oldVal) {
        if (typeof(newVal) !== 'undefined') {
          //Calculate the percentage used
          scope.chartData.percentageUsed = Math.round(100 * (scope.chartData.used / scope.chartData.total));

          if (scope.thresholdError || scope.thresholdWarning) {
            scope.isError = (scope.chartData.percentageUsed >= scope.thresholdError);
            scope.isWarn  = (scope.chartData.percentageUsed >= scope.thresholdWarning &&
                             scope.chartData.percentageUsed < scope.thresholdError);
            scope.isOk    = (scope.chartData.percentageUsed < scope.thresholdWarning);
          }

          //Animate in the chart load.
          scope.animate = true;
          $timeout(function () {
            scope.animate = false;
          }, 0);
        }
      });


    }
  };
});
