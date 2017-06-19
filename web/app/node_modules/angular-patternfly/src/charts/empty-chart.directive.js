/**
 *
 * @description
 *   Directive for rendering an empty chart. This is used by chart directives when the data
 *   available flag is set to false.
 *
 * @param {string=} chartHeight height of the chart (no units) - default: 40
 */
angular.module('patternfly.charts').directive('pfEmptyChart', function () {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      chartHeight: '=?'
    },
    replace: true,
    templateUrl: 'charts/empty-chart.html',
    controller: function ($scope) {
      $scope.setSizeStyles = function () {
        var height = $scope.chartHeight || 40;
        var topPadding = Math.min(Math.round((height - 40) / 2), 20);
        $scope.sizeStyles = {
          height: height + 'px',
          'padding-top': topPadding + 'px'
        };
      };
      $scope.setSizeStyles();
    },
    link: function (scope) {
      scope.$watch('chartHeight', function () {
        scope.setSizeStyles();
      });
    }
  };
});
