angular.module('patternfly.charts').component('pfDonutChart', {
  bindings: {
    config: '<',
    data: '<',
    chartHeight: '<?'
  },
  templateUrl: 'charts/donut/donut-chart.html',
  controller: function (pfUtils, $element, $timeout, $log) {
    'use strict';
    var ctrl = this, prevData;

    ctrl.$onInit = function () {
      ctrl.donutChartId = 'donutChart';
      if (ctrl.config.chartId) {
        ctrl.donutChartId = ctrl.config.chartId + ctrl.donutChartId;
      }

      ctrl.updateAll();
    };

    ctrl.getDonutData = function () {
      return {
        type: 'donut',
        columns: ctrl.data,
        order: null,
        colors: ctrl.config.colors
      };
    };

    ctrl.updateAll = function () {
      // Need to deep watch changes in chart data
      prevData = angular.copy(ctrl.data);

      ctrl.config = pfUtils.merge(patternfly.c3ChartDefaults().getDefaultDonutConfig(), ctrl.config);
      ctrl.config.tooltip = { contents: patternfly.pfDonutTooltipContents };
      ctrl.config.data = ctrl.getDonutData();
      ctrl.config.data.onclick = ctrl.config.onClickFn;

    };

    ctrl.getTotal = function () {
      var total = 0;
      angular.forEach(ctrl.data, function (value) {
        angular.forEach(value, function (value) {
          if (!isNaN(value)) {
            total += Number(value);
          }
        });
      });
      return total;
    };

    ctrl.getCenterLabelText = function () {
      var centerLabelText;

      // default
      centerLabelText = { bigText: ctrl.getTotal(),
                          smText:  ctrl.config.donut.title};

      if (ctrl.config.centerLabelFn) {
        centerLabelText.bigText = ctrl.config.centerLabelFn();
        centerLabelText.smText = '';
      }

      return centerLabelText;
    };

    ctrl.setupDonutChartTitle = function () {
      var donutChartTitle, centerLabelText;

      if (angular.isUndefined(ctrl.chart)) {
        return;
      }

      donutChartTitle = d3.select(ctrl.chart.element).select('text.c3-chart-arcs-title');
      if (!donutChartTitle) {
        return;
      }

      centerLabelText = ctrl.getCenterLabelText();

      // Remove any existing title.
      donutChartTitle.text('');
      if (centerLabelText.bigText && !centerLabelText.smText) {
        donutChartTitle.text(centerLabelText.bigText);
      } else {
        donutChartTitle.insert('tspan').text(centerLabelText.bigText).classed('donut-title-big-pf', true).attr('dy', 0).attr('x', 0);
        donutChartTitle.insert('tspan').text(centerLabelText.smText).classed('donut-title-small-pf', true).attr('dy', 20).attr('x', 0);
      }
    };

    ctrl.setChart = function (chart) {
      ctrl.chart = chart;
      ctrl.setupDonutChartTitle();
    };

    ctrl.$onChanges = function (changesObj) {
      if (changesObj.config || changesObj.data) {
        ctrl.updateAll();
      }
      if (changesObj.chartHeight) {
        ctrl.config.size.height = changesObj.chartHeight.currentValue;
      }
    };

    ctrl.$doCheck = function () {
      // do a deep compare on data
      if (!angular.equals(ctrl.data, prevData)) {
        ctrl.updateAll();
      }
    };
  }
});
