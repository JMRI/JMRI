/**
 * @ngdoc directive
 * @name patternfly.card.component:pfCard - Utilization
 * @restrict E
 *
 * @param {string} headTitle Title for the card
 * @param {string=} subTitle Sub-Title for the card
 * @param {boolean=} showTopBorder Show/Hide the blue top border. True shows top border, false (default) hides top border
 * @param {boolean=} showTitlesSeparator Show/Hide the grey line between the title and sub-title.
 * True (default) shows the line, false hides the line
 * @param {object=} footer footer configuration properties:<br/>
 * <ul style='list-style-type: none'>
 * <li>.iconClass  - (optional) the icon to show on the bottom left of the footer panel
 * <li>.text       - (optional) the text to show on the bottom left of the footer panel, to the right of the icon
 * <li>.href       - (optional) the href link to navigate to when the footer href is clicked
 * <li>.callBackFn - (optional) user defined function to call when the footer href is clicked
 * </ul>
 * *Note: If a href link and a callBackFn are specified, the href link will be called
 * @param {object=} filter filter configuration properties:<br/>
 * <ul style='list-style-type: none'>
 * <li>.filters    - drop down items for the filter.
 *<pre class=''>
 *  Ex:  'filters' : [{label:'Last 30 Days', value:'30'},
 *                    {label:'Last 15 Days', value:'15'},
 *                    {label:'Today', value:'today'}]</pre>
 * <li>.defaultFilter - integer, 0 based index into the filters array
 * <li>.callBackFn - user defined function to call when a filter is selected
 * </ul>
 * @description
 * Component for easily displaying a card with html content
 *
 * @example
 <example module="demo">

 <file name="index.html">
   <div ng-controller="ChartCtrl">
     <label class="label-title">Card With Multiple Utilization Bars</label>
     <pf-card head-title="System Resources" show-top-border="true" style="width: 65%">
       <pf-utilization-bar-chart chart-data=data2 chart-title=title2 layout=layoutInline units=units2 threshold-error="85" threshold-warning="60"></pf-utilization-bar-chart>
       <pf-utilization-bar-chart chart-data=data3 chart-title=title3 layout=layoutInline units=units3 threshold-error="85" threshold-warning="60"></pf-utilization-bar-chart>
       <pf-utilization-bar-chart chart-data=data4 chart-title=title4 layout=layoutInline units=units4 threshold-error="85" threshold-warning="60"></pf-utilization-bar-chart>
       <pf-utilization-bar-chart chart-data=data5 chart-title=title5 layout=layoutInline units=units5 threshold-error="85" threshold-warning="60"></pf-utilization-bar-chart>
     </pf-card>
   </div>
 </file>
 <file name="script.js">
 angular.module( 'demo', ['patternfly.charts', 'patternfly.card'] ).controller( 'ChartCtrl', function( $scope ) {

       $scope.title2 = 'Memory';
       $scope.units2 = 'GB';

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
     });
 </file>
 </example>
 */
angular.module('patternfly.card').component('pfCard', {
  transclude: true,
  templateUrl: 'card/basic/card.html',
  bindings: {
    headTitle: '@',
    subTitle: '@?',
    showTopBorder: '@?',
    showTitlesSeparator: '@?',
    footer: '=?',
    filter: '=?'
  },
  controller: function () {
    'use strict';
    var ctrl = this;
    if (ctrl.filter && !ctrl.currentFilter) {
      if (ctrl.filter.defaultFilter) {
        ctrl.currentFilter = ctrl.filter.filters[ctrl.filter.defaultFilter];
      } else {
        ctrl.currentFilter = ctrl.filter.filters[0];
      }
    }

    ctrl.footerCallBackFn = function () {
      ctrl.footerCallBackResult = ctrl.footer.callBackFn();
    };

    ctrl.filterCallBackFn = function (f) {
      ctrl.currentFilter = f;
      if (ctrl.filter.callBackFn) {
        ctrl.filterCallBackResult = ctrl.filter.callBackFn(f);
      }
    };

    ctrl.showHeader = function () {
      return (ctrl.headTitle || ctrl.showFilterInHeader());
    };

    ctrl.showFilterInHeader = function () {
      return (ctrl.filter && ctrl.filter.filters && ctrl.filter.position && ctrl.filter.position === 'header');
    };

    ctrl.showFilterInFooter = function () {
      return (ctrl.filter && ctrl.filter.filters && (!ctrl.filter.position || ctrl.filter.position === 'footer'));
    };

    ctrl.$onInit = function () {
      ctrl.shouldShowTitlesSeparator = (!ctrl.showTitlesSeparator || ctrl.showTitlesSeparator === 'true');
    };
  }
});
