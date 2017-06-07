/**
 * @ngdoc directive
 * @name patternfly.charts.component:pfDonutChart
 * @restrict E
 *
 * @description
 *   Component for rendering a donut chart which shows the relationships of a set of values to a whole.  When using a
 *   Donut Chart to show the relationship of a set of values to a whole, there should be no more than six
 *   categories.
 *
 *   <br><br>
 *   See http://c3js.org/reference.html for a full list of C3 chart options.
 *
 * @param {object} config configuration properties for the donut chart:<br/>
 * <ul style='list-style-type: none'>
 * <li>.chartId        - the unique id of the donut chart
 * <li>.centerLabelFn  - user defined function to customize the text of the center label (optional)
 * <li>.onClickFn(d,i) - user defined function to handle when donut arc is clicked upon.
 * </ul>
 *
 * @param {object} data an array of values for the donut chart.<br/>
 * <ul style='list-style-type: none'>
 * <li>.key           - string representing an arc within the donut chart
 * <li>.value         - number representing the value of the arc
 * </ul>
 *
 * @param {number} chartHeight height of the donut chart

 * @example
 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl">
       <div class="container-fluid">
         <div class="row">
           <div class="col-md-6 text-center">
             <label>Donut Chart</label>
           </div>
           <div class="col-md-6 text-center">
             <label>Small Donut Chart</label>
           </div>
         </div>
       </div>
       <div class="row">
         <div class="col-md-6 text-center">
           <pf-donut-chart config="config" data="data"></pf-donut-chart>
         </div>
         <div class="col-md-6 text-center">
           <pf-donut-chart config="custConfig" data="data" chart-height="chartHeight"></pf-donut-chart>
         </div>
       </div>
      </div>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.charts' ).controller( 'ChartCtrl', function( $scope, $interval ) {
       $scope.config = {
         'chartId': 'chartOne',
         'legend': {"show":true},
         'colors' : {
           'Cats': '#0088ce',     // blue
           'Hamsters': '#3f9c35', // green
           'Fish': '#ec7a08',     // orange
           'Dogs': '#cc0000'      // red
         },
         donut: {
           title: "Animals"
         },
         'onClickFn': function (d, i) {
           alert("You clicked on donut arc: " + d.id);
          }
       };

       $scope.custConfig = angular.copy($scope.config);
       $scope.custConfig.chartId = 'chartTwo';
       $scope.custConfig.legend.position = 'right';
       $scope.custConfig.centerLabelFn = function () {
         return "Pets";
       };
       $scope.chartHeight = 120;

       $scope.data = [
         ['Cats', 2],
         ['Hamsters', 1],
         ['Fish', 3],
         ['Dogs', 2]
       ];


     });
   </file>
 </example>
 */
