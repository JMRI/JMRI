angular.module('patternfly.charts').component('pfHeatmapLegend', {
  bindings: {
    legend: '<?',
    legendColors: '<?'
  },
  templateUrl: 'charts/heatmap/heatmap-legend.html',
  controller: function () {
    'use strict';
    var ctrl = this;

    var heatmapColorPatternDefaults = ['#d4f0fa', '#F9D67A', '#EC7A08', '#CE0000'];
    var legendLabelDefaults = ['< 70%', '70-80%', '80-90%', '> 90%'];

    ctrl.$onInit = function () {
      ctrl.updateAll();
    };

    ctrl.updateAll = function () {
      var items = [];
      var index;

      //Allow overriding of defaults
      if (!ctrl.legendColors) {
        ctrl.legendColors = heatmapColorPatternDefaults;
      }
      if (!ctrl.legend) {
        ctrl.legend = legendLabelDefaults;
      }
      for (index = ctrl.legend.length - 1; index >= 0; index--) {
        items.push({
          text: ctrl.legend[index],
          color: ctrl.legendColors[index]
        });
      }
      ctrl.legendItems = items;
    };

    ctrl.$onChanges = function (changesObj) {
      if (changesObj.legend && !changesObj.legend.isFirstChange()) {
        ctrl.updateAll();
      }
      if (changesObj.legendColors && !changesObj.legendColors.isFirstChange()) {
        ctrl.updateAll();
      }
    };
  }
});
