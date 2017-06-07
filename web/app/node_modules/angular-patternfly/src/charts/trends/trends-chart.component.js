/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfTrendsChart
 * @restrict E
 *
 * @description
 *   Component for rendering a trend chart. The trend chart combines overall data with a
 *   pfSparklineChart.
 *   <br><br>
 *   See http://c3js.org/reference.html for a full list of C3 chart options.<br>
 *   See also: {@link patternfly.charts.component:pfSparklineChart}
 *
 * @param {object} config configuration settings for the trends chart:<br/>
 * <ul style='list-style-type: none'>
 * <li>.chartId    - the unique id of this trends chart
 * <li>.title      - (optional) title of the Trends chart
 * <li>.layout     - (optional) the layout and sizes of titles and chart. Values are 'large' (default), 'small', 'compact', and 'inline'
 * <li>.trendLabel - (optional) the trend label used in the 'inline' layout
 * <li>.timeFrame  - (optional) the time frame for the data in the pfSparklineChart, ex: 'Last 30 Days'
 * <li>.units      - unit label for values, ex: 'MHz','GB', etc..
 * <li>.valueType  - (optional) the format of the latest data point which is shown in the title. Values are 'actual'(default) or 'percentage'
 * </ul>
 *
 * @param {object} chartData the data to be shown in the sparkline charts<br/>
 * <ul style='list-style-type: none'>
 * <li>.total  - number representing the total amount
 * <li>.xData  - Array, X values for the data points, first element must be the name of the data
 * <li>.yData  - Array, Y Values for the data points, first element must be the name of the data
 * <li>.dataAvailable - Flag if there is data available - default: true
 * </ul>
 *
 * @param {int=} chartHeight   height of the sparkline chart
 * @param {boolean=} showXAxis override sparkline config settings for showing the X Axis
 * @param {boolean=} showYAxis override sparkline config settings for showing the Y Axis
 * @example
 <example module="demo">
 <file name="index.html">
   <div ng-controller="ChartCtrl" class="row" style="display:inline-block; width: 100%;">
     <div class="col-md-12">
       <pf-trends-chart config="config" chart-data="data"
            show-x-axis="custShowXAxis" show-y-axis="custShowYAxis"></pf-trends-chart>
     </div>
     <hr class="col-md-12">
     <div class="col-md-12">
       <div class="row">
         <div class="col-md-4">
           <form role="form"">
             <div class="form-group">
               <label>Show</label></br>
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
               <label>Layout</label></br>
               <div class="btn-group" uib-dropdown>
                 <button type="button" uib-dropdown-toggle class="btn btn-default">
                   {{layout.title}}
                   <span class="caret"></span>
                 </button>
                 <ul uib-dropdown-menu class="dropdown-menu-right" role="menu">
                   <li ng-repeat="item in layouts" ng-class="{'selected': item === layout}">
                     <a role="menuitem" tabindex="-1" ng-click="updateLayout(item)">
                       {{item.title}}
                     </a>
                   </li>
                 </ul>
               </div>
             </div>
           </form>
         </div>
         <div class="col-md-3">
           <form role="form" ng-hide="layout == 'inline'">
             <div class="form-group">
               <label>Title Value Type</label></br>
               <div class="btn-group" uib-dropdown>
                 <button type="button" uib-dropdown-toggle class="btn btn-default">
                   {{valueType.title}}
                   <span class="caret"></span>
                 </button>
                 <ul uib-dropdown-menu class="dropdown-menu-right" role="menu">
                   <li ng-repeat="item in valueTypes" ng-class="{'selected': item === valueType}">
                     <a role="menuitem" tabindex="-1" ng-click="updateValueType(item)">
                       {{item.title}}
                     </a>
                   </li>
                 </ul>
               </div>
             </div>
           </form>
         </div>
         <div class="col-md-2">
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
 angular.module( 'demo', ['patternfly.charts', 'patternfly.card', 'ui.bootstrap'] ).controller( 'ChartCtrl', function( $scope ) {

       $scope.config = {
         chartId      : 'exampleTrendsChart',
         title        : 'Network Utilization Trends',
         layout       : 'large',
         trendLabel   : 'Virtual Disk I/O',
         valueType    : 'actual',
         timeFrame    : 'Last 15 Minutes',
         units        : 'MHz',
         tooltipType  : 'percentage'
       };

       $scope.footerConfig = {
         iconClass : 'fa fa-plus-circle',
         text      : 'Add New Cluster',
         callBackFn: function () {
            alert("Footer Callback Fn Called");
          }
       }

       $scope.filterConfig = {
         filters : [{label:'Last 30 Days', value:'30'},
                      {label:'Last 15 Days', value:'15'},
                      {label:'Today', value:'today'}],
         callBackFn: function (f) {
            alert("Filter Callback Fn Called for '" + f.label + "' value = " + f.value);
          }
       }

       $scope.layouts = [
         {
           title: "Large",
           value: "large"
         },
         {
           title: "Small",
           value: "small"
         },
         {
           title: "Compact",
           value: "compact"
         },
         {
           title: "Inline",
           value: "inline"
         }
       ];

       $scope.layout = $scope.layouts[0];

       $scope.updateLayout = function(item) {
         $scope.layout = item;
         $scope.config.layout = item.value;
       };

       $scope.valueTypes = [
         {
           title: "Actual",
           value: "actual"
         },
         {
           title: "Percentage",
           value: "percentage"
         }
       ];

       $scope.valueType = $scope.valueTypes[0];

       $scope.updateValueType = function(item) {
         $scope.valueType = item;
         $scope.config.valueType = item.value;
       };

      var today = new Date();
      var dates = ['dates'];
      for (var d = 20 - 1; d >= 0; d--) {
          dates.push(new Date(today.getTime() - (d * 24 * 60 * 60 * 1000)));
      }

       $scope.data = {
           dataAvailable: true,
           total: 250,
           xData: dates,
           yData: ['used', 10, 20, 30, 20, 30, 10, 14, 20, 25, 68, 54, 56, 78, 56, 67, 88, 76, 65, 87, 76]
       };

       $scope.custShowXAxis = false;
       $scope.custShowYAxis = false;

       $scope.addDataPoint = function () {
         $scope.data.xData.push(new Date($scope.data.xData[$scope.data.xData.length - 1].getTime() + (24 * 60 * 60 * 1000)));
         $scope.data.yData.push(Math.round(Math.random() * 100));
       };
     });
 </file>
 </example>
 */
angular.module('patternfly.charts').component('pfTrendsChart', {
  bindings: {
    config: '<',
    chartData: '<',
    chartHeight: '<?',
    showXAxis: '<?',
    showYAxis: '<?'
  },
  templateUrl: 'charts/trends/trends-chart.html',
  controller: function (pfUtils) {
    'use strict';
    var ctrl = this, prevChartData, prevConfig;
    var SMALL = 30, LARGE = 60;

    ctrl.updateAll = function () {
      // Need to deep watch changes
      prevChartData = angular.copy(ctrl.chartData);
      prevConfig = angular.copy(ctrl.config);

      ctrl.showLargeCardLayout = (!ctrl.config.layout || ctrl.config.layout === 'large');
      ctrl.showSmallCardLayout = (ctrl.config.layout === 'small');
      ctrl.showActualValue = (!ctrl.config.valueType || ctrl.config.valueType === 'actual');
      ctrl.showPercentageValue = (ctrl.config.valueType === 'percentage');
    };

    ctrl.getPercentageValue = function () {
      var pctValue = 0;

      if (ctrl.chartData.dataAvailable !== false && ctrl.chartData.total > 0) {
        pctValue = Math.round(ctrl.getLatestValue() / ctrl.chartData.total * 100.0);
      }
      return pctValue;
    };
    ctrl.getLatestValue = function () {
      var latestValue = 0;
      if (ctrl.chartData.yData && ctrl.chartData.yData.length > 0) {
        latestValue = ctrl.chartData.yData[ctrl.chartData.yData.length - 1];
      }
      return latestValue;
    };
    ctrl.getChartHeight = function () {
      var retValue = LARGE;
      if (ctrl.chartHeight) {
        retValue = ctrl.chartHeight;
      } else if (ctrl.config.layout === 'small') {
        retValue = SMALL;
      }
      return retValue;
    };

    ctrl.$onChanges = function (changesObj) {
      ctrl.updateAll();
    };

    ctrl.$doCheck = function () {
      // do a deep compare on chartData and config
      if (!angular.equals(ctrl.chartData, prevChartData) || !angular.equals(ctrl.config, prevConfig)) {
        ctrl.updateAll();
      }
    };
  }
});
