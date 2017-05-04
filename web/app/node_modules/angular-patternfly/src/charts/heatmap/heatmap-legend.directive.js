angular.module('patternfly.charts').directive('pfHeatmapLegend',
  function () {
    'use strict';
    return {
      restrict: 'A',
      scope: {
        legend: '=?',
        legendColors: '=?'
      },
      templateUrl: 'charts/heatmap/heatmap-legend.html',
      controller: function ($scope) {
        var heatmapColorPatternDefaults = ['#d4f0fa', '#F9D67A', '#EC7A08', '#CE0000'];
        var legendLabelDefaults = ['< 70%', '70-80%' ,'80-90%', '> 90%'];

        //Allow overriding of defaults
        if (!$scope.legendColors) {
          $scope.legendColors = heatmapColorPatternDefaults;
        }
        if (!$scope.legend) {
          $scope.legend = legendLabelDefaults;
        }
      },
      link: function ($scope) {
        var items = [];
        var index;
        for (index = $scope.legend.length - 1; index >= 0; index--) {
          items.push({
            text: $scope.legend[index],
            color: $scope.legendColors[index]
          });
        }
        $scope.legendItems = items;
      }
    };
  }
);
