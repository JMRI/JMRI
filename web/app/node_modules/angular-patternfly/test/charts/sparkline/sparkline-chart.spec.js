describe('Directive: pfSparklineChart', function() {
  var $scope, $compile, $timeout, element, isoloateScope;

  beforeEach(module(
    'patternfly.charts',
    'charts/empty-chart.html',
    'charts/sparkline/sparkline-chart.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_, _$timeout_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $timeout = _$timeout_;
  }));

  beforeEach(function() {
    $scope.config = {
      'chartId': 'testSparklineChart',
      'totalUnits': 'MHz'
    };

    var today = new Date();
    var dates = ['dates'];
    for (var d = 20 - 1; d >= 0; d--) {
      dates.push(new Date(today.getTime() - (d * 24 * 60 * 60 * 1000)));
    }

    $scope.data = {
      'total': '100',
      'yData': ['used', '10', '20', '30', '20', '30', '10', '14', '20', '25', '68', '54', '56', '78', '56', '67', '88', '76', '65', '87', '76'],
      'xData': dates
    };
  });

  var compileChart = function (markup, scope) {
    element = $compile(angular.element(markup))(scope);
    scope.$apply();
    isolateScope = element.isolateScope();

    return element;
  };

  it("should not show axis by default", function() {
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>',$scope);

    expect(isolateScope.sparklineChartId).toBe("testSparklineChartsparklineChart");
    expect(isolateScope.chartConfig.axis.x.show).toBe(false);
    expect(isolateScope.chartConfig.axis.y.show).toBe(false);
  });

  it("should allow attribute specifications to show x and y axis", function() {
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data" show-x-axis="true" show-y-axis="true"></div>', $scope);

    expect(isolateScope.sparklineChartId).toBe("testSparklineChartsparklineChart");
    expect(isolateScope.chartConfig.axis.x.show).toBe(true);
    expect(isolateScope.chartConfig.axis.y.show).toBe(true);
  });

  it("should update when the show x and y axis attributes change", function() {
    $scope.showX = false;
    $scope.showY = false;
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data" show-x-axis="showX" show-y-axis="showY"></div>', $scope);

    expect(isolateScope.chartConfig.axis.x.show).toBe(false);
    expect(isolateScope.chartConfig.axis.y.show).toBe(false);

    $scope.showX = true;
    $scope.showY = true;

    $scope.$digest();

    expect(isolateScope.chartConfig.axis.x.show).toBe(true);
    expect(isolateScope.chartConfig.axis.y.show).toBe(true);
  });

  it("should allow attribute specification of chart height", function() {
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data" chart-height="120"></div>', $scope);

    expect(isolateScope.sparklineChartId).toBe("testSparklineChartsparklineChart");
    expect(isolateScope.chartConfig.size.height).toBe(120);
  });

  it("should update when the chart height attribute changes", function() {
    $scope.chartHeight = 120;
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data" chart-height="chartHeight"></div>', $scope);

    expect(isolateScope.sparklineChartId).toBe("testSparklineChartsparklineChart");
    expect(isolateScope.chartConfig.size.height).toBe(120);

    $scope.chartHeight = 100;
    $scope.$digest();
    expect(isolateScope.chartConfig.size.height).toBe(100);
  });

  it("should setup C3 chart data correctly", function() {
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>', $scope);

    expect(isolateScope.config.data.x).toBe("dates");
    expect(isolateScope.config.data.columns.length).toBe(2);
    expect(isolateScope.config.data.columns[0][0]).toBe("dates");
    expect(isolateScope.config.data.columns[1][0]).toBe("used");
  });

  it("should update C3 chart data when data changes", function() {
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>', $scope);

    expect(isolateScope.config.data.x).toBe("dates");
    expect(isolateScope.config.data.columns.length).toBe(2);
    expect(isolateScope.config.data.columns[0][1].toString()).toBe($scope.data.xData[1].toString());
    expect(isolateScope.config.data.columns[1][1]).toBe('10');

    var now = new Date();
    $scope.data.xData[1] = now;
    $scope.data.yData[1] = '1000';

    $scope.$digest();

    expect(isolateScope.chartConfig.data.columns[0][1].toString()).toBe(now.toString());
    expect(isolateScope.chartConfig.data.columns[1][1]).toBe('1000');
  });

  it("should allow tooltip type specification", function() {
    $scope.config.tooltipType = "percentage"
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>', $scope);

    expect(isolateScope.config.tooltipType).toBe("percentage");
  });

  it("should allow using a tooltip function", function() {
    var functionCalled = false;
    var myTooltipFn = function(d) {
      if (d && d.length === 2) {
        functionCalled = true;
      }
    };

    $scope.config.tooltipFn = myTooltipFn;
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>', $scope);
    var dataPoint = [{value: 0, name: 'used'}, 0];
    isolateScope.sparklineTooltip(isolateScope).contents(dataPoint);

    expect(functionCalled).toBe(true);
  });

  it("should allow using C3 chart data formats", function() {
    $scope.config = {
      chartId: 'testSparklineChart',
      totalUnits: 'MHz',
      data: {
        xFormat: '%Y-%m-%d %H:%M:%S',
        x: 'dates',
        columns: [$scope.data.xData, $scope.data.yData]
      }
    };
    $scope.data = {
      total: 100
    };

    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>', $scope);

    expect(isolateScope.config.data.x).toBe("dates");
    expect(isolateScope.config.data.columns.length).toBe(2);
    expect(isolateScope.config.data.columns[0][0]).toBe("dates");
    expect(isolateScope.config.data.columns[1][0]).toBe("used");
  });

  it("should show empty chart when the dataAvailable is set to false", function() {
    element = compileChart('<div pf-sparkline-chart config="config" chart-data="data"></div>', $scope);
    var emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(0);

    $scope.data.dataAvailable = false;


    $scope.$digest();

    emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(1);
  });

});
