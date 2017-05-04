describe('Directive: pfTrendsChart', function() {
  var $scope, $compile, element, isolateScope, trendCard;

  beforeEach(module(
    'patternfly.charts',
    'charts/empty-chart.html',
    'charts/trends/trends-chart.html',
    'card/basic/card.html',
    'charts/sparkline/sparkline-chart.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  beforeEach(function() {

    $scope.config = {
      chartId  : 'testSparklineChart',
      title    : 'Network Utilization Trends',
      timeFrame: 'Last 15 Minutes',
      units    : 'MHz'
    };

    var today = new Date();
    var dates = ['dates'];
    for (var d = 20 - 1; d >= 0; d--) {
      dates.push(new Date(today.getTime() - (d * 24 * 60 * 60 * 1000)));
    }

    $scope.data = {
      total: 100,
      yData: ['used', 10, 20, 30, 20, 30, 10, 14, 20, 25, 68, 54, 56, 78, 56, 67, 88, 76, 65, 87, 76],
      xData: dates
    };

    element = compileChart('<div pf-trends-chart config="config" chart-data="data"></div>',$scope);
  });

  var compileChart = function (markup, scope) {
    var el = $compile(markup)(scope);
    scope.$digest();
    return angular.element(el);
  };

  it("should show the last data point of sparkline chart as the trend heading", function() {
    expect(element.find('.trend-title-big-pf').html()).toBe("76");
    expect(element.find('.trend-title-small-pf').html()).toBe("MHz");
  });

  it("should show the correct card heading and time frame", function() {
    expect(element.find('.trend-header-pf').html()).toBe("Network Utilization Trends");
    expect(element.find('.trend-footer-pf').html()).toBe("Last 15 Minutes");
  });

  it("should show the percentage in the trend heading", function() {

    $scope.config.valueType = 'percentage';
    $scope.$digest();

    expect(element.find('.trend-title-big-pf').html()).toBe("76%");
    expect(element.find('.trend-title-small-pf').html()).toBe("of 100 MHz");
  });

  it("should show large or small trend card layouts", function() {
    // by default, should show a large card
    trendCard = element.find('.trend-card-large-pf');
    expect(trendCard.size()).toBe(1);
    // check small card isn't being shown by default
    expect(trendCard.hasClass('.trend-card-small-pf')).toBeFalsy();

    $scope.config.layout = 'small';
    $scope.$digest();
    trendCard = element.find('.trend-card-small-pf');
    expect(trendCard.size()).toBe(1);
    expect(trendCard.hasClass('.trend-card-large-pf')).toBeFalsy();

    $scope.config.layout = 'large';
    $scope.$digest();
    trendCard = element.find('.trend-card-large-pf');
    expect(trendCard.size()).toBe(1);
    expect(trendCard.hasClass('.trend-card-small-pf')).toBeFalsy();
  });

  it("should show compact card layout", function() {
    $scope.config.layout = 'compact';
    $scope.$digest();

    trendCard = element.find('.trend-row');
    expect(trendCard.size()).toBe(1);
    trendCard = element.find('.trend-title-compact-big-pf');
    expect(trendCard.size()).toBe(1);
    trendCard = element.find('.trend-title-compact-small-pf');
    expect(trendCard.size()).toBe(1);
  });

  it("should show inline card layout", function() {
    $scope.config.layout = 'inline';
    $scope.$digest();

    trendCard = element.find('.trend-row');
    expect(trendCard.size()).toBe(1);
    trendCard = element.find('.trend-flat-col');
    expect(trendCard.size()).toBe(2);
    trendCard = element.find('.trend-label-flat-strong-pf');
    expect(trendCard.size()).toBe(1);

    trendCard = element.find('.trend-title-flat-big-pf');
    expect(trendCard.html()).toBe('76%');

    trendCard = element.find('.trend-label-flat-pf');
    expect(trendCard.html()).toBe('76 of 100 MHz');
  });

  it("should show empty chart when the dataAvailable is set to false", function() {
    var emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(0);

    $scope.data.dataAvailable = false;

    $scope.$digest();

    emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(1);
  });
});
