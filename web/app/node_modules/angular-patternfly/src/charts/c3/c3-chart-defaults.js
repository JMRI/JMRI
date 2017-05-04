(function (patternfly) {
  'use strict';

  var patternflyDefaults = patternfly.c3ChartDefaults();

  angular.module('patternfly.charts').constant('c3ChartDefaults', {
    getDefaultColors: patternflyDefaults.getDefaultColors,
    getDefaultDonut: patternflyDefaults.getDefaultDonut,
    getDefaultDonutSize: patternflyDefaults.getDefaultDonutSize,
    getDefaultDonutColor: patternflyDefaults.getDefaultDonutColors,
    getDefaultDonutLegend: patternflyDefaults.getDefaultDonutLegend,
    getDefaultDonutConfig: patternflyDefaults.getDefaultDonutConfig,
    getDefaultSparklineArea: patternflyDefaults.getDefaultSparklineArea,
    getDefaultSparklineSize: patternflyDefaults.getDefaultSparklineSize,
    getDefaultSparklineAxis: patternflyDefaults.getDefaultSparklineAxis,
    getDefaultSparklineColor: patternflyDefaults.getDefaultColors,
    getDefaultSparklineLegend: patternflyDefaults.getDefaultSparklineLegend,
    getDefaultSparklinePoint: patternflyDefaults.getDefaultSparklinePoint,
    getDefaultSparklineTooltip: patternflyDefaults.getDefaultSparklineTooltip,
    getDefaultSparklineConfig: patternflyDefaults.getDefaultSparklineConfig,
    getDefaultLineConfig: patternflyDefaults.getDefaultLineConfig
  });
})(patternfly);
