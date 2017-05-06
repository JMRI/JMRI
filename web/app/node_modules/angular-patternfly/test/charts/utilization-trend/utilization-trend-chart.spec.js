describe('Directive: pfUtilizationTrendChart', function() {
  var $scope, $compile, $timeout, element, isolateScope;

  beforeEach(module(
    'patternfly.charts',
    'charts/empty-chart.html',
    'charts/utilization-trend/utilization-trend-chart.html',
    'charts/donut/donut-pct-chart.html',
    'charts/sparkline/sparkline-chart.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_, _$timeout_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $timeout = _$timeout_;
  }));

  beforeEach(function() {

    $scope.config = {
      title: 'Memory',
      units: 'GB'
    };
    $scope.donutConfig = {
      chartId: 'testDonutChart',
      units: 'GB',
      thresholds: {'warning':'60','error':'90'}
    };
    $scope.sparklineConfig = {
      chartId: 'testSparklineChart',
      tooltipType: 'default'
    };

    var today = new Date();
    var dates = ['dates'];
    for (var d = 20 - 1; d >= 0; d--) {
      dates.push(new Date(today.getTime() - (d * 24 * 60 * 60 * 1000)));
    }

    $scope.data = {
      used: 76,
      total: 100,
      yData: ['used', 10, 20, 30, 20, 30, 10, 14, 20, 25, 68, 54, 56, 78, 56, 67, 88, 76, 65, 87, 76],
      xData: dates
    };
  });

  var compileChart = function (markup, scope) {
    element = $compile(angular.element(markup))(scope);
    scope.$apply();
    isolateScope = element.isolateScope();

    return element;
  };

  it("should show used for the center label by default", function() {
    element = compileChart('<div pf-utilization-trend-chart config="config" chart-data="data" donut-config="donutConfig" sparkline-config="sparklineConfig"></div>',$scope);

    expect(isolateScope.centerLabel).toBe('used');
  });

  it("should show 'Available' for the current label by default", function() {
    element = compileChart('<div pf-utilization-trend-chart config="config" chart-data="data" donut-config="donutConfig" sparkline-config="sparklineConfig"></div>',$scope);

    expect(isolateScope.currentText).toBe('Available');
    expect(isolateScope.currentValue).toBe(24);
  });

  it("should show the correct available value when only used and total are given", function() {
    element = compileChart('<div pf-utilization-trend-chart config="config" chart-data="data" donut-config="donutConfig" sparkline-config="sparklineConfig"></div>',$scope);

    expect(isolateScope.chartData.available).toBe(24);
  });

  it("should show correct units", function() {
    element = compileChart('<div pf-utilization-trend-chart config="config" chart-data="data" donut-config="donutConfig" sparkline-config="sparklineConfig"></div>',$scope);
    expect(isolateScope.config.units).toBe('GB');
  });

  it("should update the current and center labels when attribute changes", function() {
    $scope.cLabel = 'used';
    element = compileChart('<div pf-utilization-trend-chart config="config" chart-data="data" center-label="cLabel" donut-config="donutConfig" sparkline-config="sparklineConfig"></div>',$scope);

    expect(isolateScope.centerLabel).toBe('used');
    expect(isolateScope.currentText).toBe('Available');
    expect(isolateScope.currentValue).toBe(24);

    $scope.cLabel = 'available';
    $scope.$digest();

    expect(isolateScope.centerLabel).toBe('available');
    expect(isolateScope.currentText).toBe('Used');
    expect(isolateScope.currentValue).toBe(76);
  });

  it("should show empty chart when the dataAvailable flag is set to false", function() {
    element = compileChart('<div pf-utilization-trend-chart config="config" chart-data="data" donut-config="donutConfig" sparkline-config="sparklineConfig"></div>',$scope);

    var emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(0);

    $scope.data.dataAvailable = false;

    $scope.$digest();

    emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(1);
  });
});
