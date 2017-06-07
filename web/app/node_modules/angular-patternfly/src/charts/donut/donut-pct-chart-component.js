angular.module('patternfly.charts').component('pfDonutPctChart', {
  bindings: {
    config: '<',
    data: '<',
    chartHeight: '<?',
    centerLabel: '<?',
    onThresholdChange: '&'
  },
  templateUrl: 'charts/donut/donut-pct-chart.html',
  controller: function (pfUtils, $element, $timeout) {
    'use strict';
    var ctrl = this, prevData;

    ctrl.$onInit = function () {
      ctrl.donutChartId = 'donutPctChart';
      if (ctrl.config.chartId) {
        ctrl.donutChartId = ctrl.config.chartId + ctrl.donutChartId;
      }

      ctrl.updateAll();
    };

    ctrl.updateAvailable = function () {
      ctrl.data.available = ctrl.data.total - ctrl.data.used;
    };

    ctrl.getStatusColor = function (used, thresholds) {
      var threshold = "none";
      var color = pfUtils.colorPalette.blue;

      if (thresholds) {
        threshold = "ok";
        color = pfUtils.colorPalette.green;
        if (used >= thresholds.error) {
          threshold = "error";
          color = pfUtils.colorPalette.red;
        } else if (used >= thresholds.warning) {
          threshold = "warning";
          color = pfUtils.colorPalette.orange;
        }
      }

      if (!ctrl.threshold || ctrl.threshold !== threshold) {
        ctrl.threshold = threshold;
        ctrl.onThresholdChange({ threshold: ctrl.threshold });
      }

      return color;
    };

    ctrl.statusDonutColor = function () {
      var color, percentUsed;

      color = { pattern: [] };
      percentUsed = ctrl.data.used / ctrl.data.total * 100.0;
      color.pattern[0] = ctrl.getStatusColor(percentUsed, ctrl.config.thresholds);
      color.pattern[1] = pfUtils.colorPalette.black300;
      return color;
    };

    ctrl.donutTooltip = function () {
      return {
        contents: function (d) {
          var tooltipHtml;

          if (ctrl.config.tooltipFn) {
            tooltipHtml = '<span class="donut-tooltip-pf" style="white-space: nowrap;">' +
                              ctrl.config.tooltipFn(d) +
                         '</span>';
          } else {
            tooltipHtml = '<span class="donut-tooltip-pf" style="white-space: nowrap;">' +
                      Math.round(d[0].ratio * 100) + '%' + ' ' + ctrl.config.units + ' ' + d[0].name +
                   '</span>';
          }

          return tooltipHtml;
        }
      };
    };

    ctrl.getDonutData = function () {
      return {
        columns: [
          ['Used', ctrl.data.used],
          ['Available', ctrl.data.available]
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

    ctrl.getCenterLabelText = function () {
      var centerLabelText;

      // default to 'used' info.
      centerLabelText = { bigText: ctrl.data.used,
                          smText:  ctrl.config.units + ' Used' };

      if (ctrl.config.centerLabelFn) {
        centerLabelText.bigText = ctrl.config.centerLabelFn();
        centerLabelText.smText = '';
      } else if (ctrl.centerLabel === 'none') {
        centerLabelText.bigText = '';
        centerLabelText.smText = '';
      } else if (ctrl.centerLabel === 'available') {
        centerLabelText.bigText = ctrl.data.available;
        centerLabelText.smText = ctrl.config.units + ' Available';
      } else if (ctrl.centerLabel === 'percent') {
        centerLabelText.bigText = Math.round(ctrl.data.used / ctrl.data.total * 100.0) + '%';
        centerLabelText.smText = 'of ' + ctrl.data.total + ' ' + ctrl.config.units;
      }

      return centerLabelText;
    };

    ctrl.updateAll = function () {
      // Need to deep watch changes in chart data
      prevData = angular.copy(ctrl.data);

      ctrl.config = pfUtils.merge(patternfly.c3ChartDefaults().getDefaultDonutConfig(), ctrl.config);
      ctrl.updateAvailable();
      ctrl.config.data = pfUtils.merge(ctrl.config.data, ctrl.getDonutData());
      ctrl.config.color = ctrl.statusDonutColor(ctrl);
      ctrl.config.tooltip = ctrl.donutTooltip();
      ctrl.config.data.onclick = ctrl.config.onClickFn;
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
      donutChartTitle.selectAll('*').remove();
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
      if (changesObj.centerLabel) {
        ctrl.setupDonutChartTitle();
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
