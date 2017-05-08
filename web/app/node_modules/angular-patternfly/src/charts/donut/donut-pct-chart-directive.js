/**
 * @ngdoc directive
 * @name patternfly.charts.directive:pfDonutPctChart
 *
 * @description
 *   Directive for rendering a percentage used donut/radial chart.  The Used Percentage fill starts at 12 o’clock and
 *   moves clockwise.  Whatever portion of the donut not Used, will be represented as Available, and rendered as a
 *   gray fill.
 *   There are three possible fill colors for Used Percentage, dependent on whether or not there are thresholds:<br/>
 *   <ul>
 *   <li>When no thresholds exist, or if the used percentage has not surpassed any thresholds, the indicator is blue.
 *   <li>When the used percentage has surpassed the warning threshold, but not the error threshold, the indicator is orange.
 *   <li>When the used percentage has surpassed the error threshold, the indicator is is red.
 *   </ul>
 *   The directive will calculate the Available Percentage (Total - Used), and display it as a grey radial fill.
 *
 *   <br><br>
 *   See http://c3js.org/reference.html for a full list of C3 chart options.
 *
 * @param {object} config configuration properties for the donut chart:<br/>
 * <ul style='list-style-type: none'>
 * <li>.chartId        - the unique id of the donut chart
 * <li>.units          - unit label for values, ex: 'MHz','GB', etc..
 * <li>.thresholds     - warning and error percentage thresholds used to determine the Usage Percentage fill color (optional)
 * <li>.tooltipFn(d)   - user defined function to customize the tool tip (optional)
 * <li>.centerLabelFn  - user defined function to customize the text of the center label (optional)
 * <li>.onClickFn(d,i) - user defined function to handle when donut arc is clicked upon.
 * </ul>
 *
 * @param {object} data the Total and Used values for the donut chart.  Available is calculated as Total - Used.<br/>
 * <ul style='list-style-type: none'>
 * <li>.used          - number representing the amount used
 * <li>.total         - number representing the total amount
 * <li>.dataAvailable - Flag if there is data available - default: true
 * </ul>
 *
 * @param {string=} center-label specifies the contents of the donut's center label.<br/>
 * <strong>Values:</strong>
 * <ul style='list-style-type: none'>
 * <li> 'used'      - displays the Used amount in the center label (default)
 * <li> 'available' - displays the Available amount in the center label
 * <li> 'percent'   - displays the Usage Percent of the Total amount in the center label
 * <li> 'none'      - does not display the center label
 * </ul>
 *
 * @param {int=} chartHeight height of the donut chart

 * @example
 <example module="patternfly.charts">
   <file name="index.html">
     <div ng-controller="ChartCtrl">
       <div class="container-fluid">
         <div class="row">
           <div class="col-md-3 text-center">
             <label>Error Threshold</label>
             <div pf-donut-pct-chart config="configErr" data="dataErr"></div>
           </div>
           <div class="col-md-3 text-center"">
             <label>Warning Threshold</label>
             <div pf-donut-pct-chart config="configWarn" data="dataWarn"></div>
           </div>
           <div class="col-md-3 text-center"">
             <label>Ok</label>
             <div pf-donut-pct-chart config="configOk" data="dataOk"></div>
           </div>
           <div class="col-md-3 text-center"">
             <label>No Threshold</label>
             <div pf-donut-pct-chart config="configNoThresh" data="dataNoThresh"></div>
           </div>
         </div>

         <div class="row">
           <div class="col-md-12">
             <hr>
           </div>
         </div>

         <div class="row">
           <div class="col-md-3 text-center">
             <div pf-donut-pct-chart config="usedConfig" data="usedData" center-label="usedLabel"></div>
             <label>center-label = 'used'</label>
           </div>
           <div class="col-md-3 text-center">
             <div pf-donut-pct-chart config="availConfig" data="availData" center-label="availLabel"></div>
             <label>center-label = 'available'</label>
           </div>
           <div class="col-md-3 text-center">
             <div pf-donut-pct-chart config="pctConfig" data="pctData" center-label="pctLabel"></div>
             <label>center-label = 'percent'</label>
           </div>
           <div class="col-md-3 text-center">
             <div pf-donut-pct-chart config="noneConfig" data="noneData" center-label="noLabel"></div>
             <label>center-label = ' none'</label>
           </div>
         </div>

         <div class="row">
           <div class="col-md-12">
             <hr>
           </div>
         </div>

         <div class="row">
           <div class="col-md-12 text-center">
             <label>Custom Tooltip, Legend, Click handling, and Center Label</label><br>
             <label><strong>Click on Donut Arc!</strong></label>
             <div pf-donut-pct-chart config="custConfig" chart-height="custChartHeight" data="custData"></div>
           </div>
         </div>
         <div class="row">
           <div class="col-md-3">
             <form role="form"">
               <div class="form-group">
                 <label class="checkbox-inline">
                   <input type="checkbox" ng-model="custData.dataAvailable">Data Available</input>
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
         </div>
       </div>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.charts' ).controller( 'ChartCtrl', function( $scope ) {
       $scope.configErr = {
         'chartId': 'chartErr',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.dataErr = {
         'used': '950',
         'total': '1000'
       };

       $scope.configWarn = {
         'chartId': 'chartWarn',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.dataWarn = {
         'used': '650',
         'total': '1000'
       };

       $scope.configOk = {
         'chartId': 'chartOk',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.dataOk = {
         'used': '550',
         'total': '1000'
       };

       $scope.configNoThresh = {
         'chartId': 'chartNoThresh',
         'units': 'GB',
       };

       $scope.dataNoThresh = {
         'used': '750',
         'total': '1000'
       };

       $scope.usedConfig = {
         'chartId': 'usedChart',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.usedData = {
         'used': '350',
         'total': '1000'
       };

       $scope.usedLabel = "used";

       $scope.availConfig = {
         'chartId': 'availChart',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.availData = {
          'used': '350',
          'total': '1000'
        };

       $scope.availLabel = "available";

       $scope.pctConfig = {
         'chartId': 'pctChart',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.pctData = {
         'used': '350',
         'total': '1000'
       };

       $scope.pctLabel = "percent";

       $scope.noneConfig = {
         'chartId': 'noneChart',
         'units': 'GB',
         'thresholds':{'warning':'60','error':'90'}
       };

       $scope.noneData = {
         'used': '350',
         'total': '1000'
       };

       $scope.noLabel = "none";

       $scope.custConfig = {
         'chartId': 'custChart',
         'units': 'MHz',
         'thresholds':{'warning':'60','error':'90'},
         "legend":{"show":true},
         'tooltipFn': function (d) {
           return '<span class="donut-tooltip-pf"style="white-space: nowrap;">' +
                    d[0].value + ' ' + d[0].name +
                  '</span>';
           },
         'centerLabelFn': function () {
           return $scope.custData.available + " GB";
           },
         'onClickFn': function (d, i) {
           alert("You Clicked On The Donut!");
           }
         };

       $scope.custData = {
         'dataAvailable': true,
         'used': '670',
         'total': '1000'
       };

       $scope.custChartHeight = 200;
     });
   </file>
 </example>
 */
(function (patternfly) {
  'use strict';
  angular.module('patternfly.charts').directive('pfDonutPctChart', function (pfUtils, $timeout) {
    return {
      restrict: 'A',
      scope: {
        config: '=',
        data: '=',
        chartHeight: '=?',
        centerLabel: '=?'
      },
      replace: true,
      templateUrl: 'charts/donut/donut-pct-chart.html',
      controller: ['$scope',
        function ($scope) {
          var donutTooltip;

          $scope.donutChartId = 'donutChart';
          if ($scope.config.chartId) {
            $scope.donutChartId = $scope.config.chartId + $scope.donutChartId;
          }

          $scope.updateAvailable = function () {
            $scope.data.available = $scope.data.total - $scope.data.used;
          };

          if ($scope.data.available === undefined) {
            $scope.updateAvailable();
          }

          $scope.getStatusColor = function (used, thresholds) {
            var color = pfUtils.colorPalette.blue;

            if (thresholds) {
              color = pfUtils.colorPalette.green;
              if (used >= thresholds.error) {
                color = pfUtils.colorPalette.red;
              } else if (used >= thresholds.warning) {
                color = pfUtils.colorPalette.orange;
              }
            }

            return color;
          };

          $scope.statusDonutColor = function (scope) {
            var color, percentUsed;

            color = { pattern: [] };
            percentUsed = scope.data.used / scope.data.total * 100.0;
            color.pattern[0] = $scope.getStatusColor(percentUsed, scope.config.thresholds);
            color.pattern[1] = pfUtils.colorPalette.black300;
            return color;
          };

          donutTooltip = function (scope) {
            return {
              contents: function (d) {
                var tooltipHtml;

                if (scope.config.tooltipFn) {
                  tooltipHtml = '<span class="donut-tooltip-pf" style="white-space: nowrap;">' +
                                  scope.config.tooltipFn(d) +
                               '</span>';
                } else {
                  tooltipHtml = '<span class="donut-tooltip-pf" style="white-space: nowrap;">' +
                            Math.round(d[0].ratio * 100) + '%' + ' ' + $scope.config.units + ' ' + d[0].name +
                         '</span>';
                }

                return tooltipHtml;
              }
            };
          };

          $scope.getDonutData = function (scope) {
            return {
              columns: [
                ['Used', scope.data.used],
                ['Available', scope.data.available]
              ],
              type: 'donut',
              donut: {
                label: {
                  show: false
                }
              },
              groups: [
                ['used', 'available']
              ],
              order: null
            };
          };

          $scope.getCenterLabelText = function () {
            var centerLabelText;

            // default to 'used' info.
            centerLabelText = { bigText: $scope.data.used,
                                smText:  $scope.config.units + ' Used' };

            if ($scope.config.centerLabelFn) {
              centerLabelText.bigText = $scope.config.centerLabelFn();
              centerLabelText.smText = '';
            } else if ($scope.centerLabel === 'none') {
              centerLabelText.bigText = '';
              centerLabelText.smText = '';
            } else if ($scope.centerLabel === 'available') {
              centerLabelText.bigText = $scope.data.available;
              centerLabelText.smText = $scope.config.units + ' Available';
            } else if ($scope.centerLabel === 'percent') {
              centerLabelText.bigText = Math.round($scope.data.used / $scope.data.total * 100.0) + '%';
              centerLabelText.smText = 'of ' + $scope.data.total + ' ' + $scope.config.units;
            }

            return centerLabelText;
          };


          $scope.updateAll = function (scope) {
            $scope.updateAvailable();
            $scope.config.data = pfUtils.merge($scope.config.data, $scope.getDonutData($scope));
            $scope.config.color = $scope.statusDonutColor($scope);
            $scope.config.tooltip = donutTooltip(scope);
            $scope.config.data.onclick = $scope.config.onClickFn;
          };

          $scope.config = pfUtils.merge(patternfly.c3ChartDefaults().getDefaultDonutConfig(), $scope.config);
          $scope.updateAll($scope);


        }
      ],
      link: function (scope, element) {
        var setupDonutChartTitle = function () {
          $timeout(function () {
            var donutChartTitle, centerLabelText;

            donutChartTitle = d3.select(element[0]).select('text.c3-chart-arcs-title');
            if (!donutChartTitle) {
              return;
            }

            centerLabelText = scope.getCenterLabelText();

            // Remove any existing title.
            donutChartTitle.selectAll('*').remove();
            if (centerLabelText.bigText && !centerLabelText.smText) {
              donutChartTitle.text(centerLabelText.bigText);
            } else {
              donutChartTitle.insert('tspan').text(centerLabelText.bigText).classed('donut-title-big-pf', true).attr('dy', 0).attr('x', 0);
              donutChartTitle.insert('tspan').text(centerLabelText.smText).classed('donut-title-small-pf', true).attr('dy', 20).attr('x', 0);
            }
          }, 300);
        };

        scope.$watch('config', function () {
          scope.updateAll(scope);
          setupDonutChartTitle();
        }, true);
        scope.$watch('chartHeight', function () {
          if (scope.chartHeight) {
            scope.config.size.height = scope.chartHeight;
          }
        });
        scope.$watch('data', function () {
          scope.updateAll(scope);
          setupDonutChartTitle();
        }, true);

        scope.$watch('centerLabel', function () {
          setupDonutChartTitle();
        });
      }
    };
  });
}(patternfly));
