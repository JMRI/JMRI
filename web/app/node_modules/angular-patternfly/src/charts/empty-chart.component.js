/**
 *
 * @description
 *   Directive for rendering an empty chart. This is used by chart directives when the data
 *   available flag is set to false.
 *
 * @param {string=} chartHeight height of the chart (no units) - default: 40
 */
angular.module('patternfly.charts').component('pfEmptyChart', {
  bindings: {
    chartHeight: '<?'
  },
  templateUrl: 'charts/empty-chart.html',
  controller: function () {
    'use strict';
    var ctrl = this;

    ctrl.setSizeStyles = function () {
      var height = ctrl.chartHeight || 40;
      var topPadding = Math.min(Math.round((height - 40) / 2), 20);
      ctrl.sizeStyles = {
        height: height + 'px',
        'padding-top': topPadding + 'px'
      };
    };
    ctrl.setSizeStyles();

    ctrl.$onChanges =  function (changesObj) {
      if (changesObj.chartHeight) {
        ctrl.setSizeStyles();
      }
    };
  }
});
