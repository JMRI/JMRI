describe('Directive: pfDonutPctChart', function() {
  var $scope, isoScope, $compile, $timeout, element;

  beforeEach(module(
    'patternfly.charts',
    'charts/empty-chart.html',
    'charts/donut/donut-pct-chart.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_, _$timeout_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $timeout = _$timeout_;
  }));

  beforeEach(function() {
    $scope.config = {
      'units': 'MHz',
      'thresholds':{'warning':'75.0','error':'90.00'}
    };

    $scope.data = {
      "used": 950,
      "total": 1000
    };

  });

  var compileDonut = function (markup, scope) {
    var el = $compile(angular.element(markup))(scope);
    scope.$apply();
    isoScope = el.isolateScope();
    return el;
  };

  it("should trigger error threshold", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);

    expect(isoScope.statusDonutColor(isoScope).pattern[0]).toBe('#cc0000');  //red
  });

  it("should trigger warning threshold", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);

    $scope.data.used = 850;
    $scope.$digest();
    expect(isoScope.statusDonutColor(isoScope).pattern[0]).toBe('#ec7a08');  //orange
  });

  it("should trigger ok threshold", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);

    $scope.data.used = 550;
    $scope.$digest();
    expect(isoScope.statusDonutColor(isoScope).pattern[0]).toBe('#3f9c35');  //green
  });

  it("should show no threshold", function() {
    $scope.config = {
      'units': 'MHz'
    };

    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);

    expect(isoScope.statusDonutColor(isoScope).pattern[0]).toBe('#0088ce');  //blue
  });

  it("should show 'used' center label by default", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);

    expect(isoScope.getCenterLabelText(isoScope).smText).toContain('Used');
  });

  it("should show 'available' center label", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data" center-label="cntrLabel"></div>', $scope);

    $scope.cntrLabel = 'available';
    $scope.$digest();
    expect(isoScope.getCenterLabelText(isoScope).smText).toContain('Available');
  });

  it("should show 'percent' center label", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data" center-label="cntrLabel"></div>', $scope);

    $scope.cntrLabel = 'percent';
    $scope.$digest();
    expect(isoScope.getCenterLabelText(isoScope).bigText).toContain('%');
  });

  it("should show no center label", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data" center-label="cntrLabel"></div>', $scope);

    $scope.cntrLabel = 'none';
    $scope.$digest();
    expect(isoScope.getCenterLabelText(isoScope).bigText).toBe('');
    expect(isoScope.getCenterLabelText(isoScope).smText).toBe('');
  });

  it("should show 'used' center label", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data" center-label="cntrLabel"></div>', $scope);

    $scope.cntrLabel = 'used';
    $scope.$digest();
    expect(isoScope.getCenterLabelText(isoScope).smText).toContain('Used');
  });

  it("should use center label funtion", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);

    $scope.config.centerLabelFn = function () {
      return '<tspan dy="0" x="0" class="donut-title-big-pf">' + $scope.data.available + '</tspan>' +
        '<tspan dy="20" x="0" class="donut-title-small-pf">Free</tspan>';
    };

    $scope.$digest();
    expect(isoScope.getCenterLabelText(isoScope).bigText).toContain('50');
    expect(isoScope.getCenterLabelText(isoScope).bigText).toContain('Free');
    expect(isoScope.getCenterLabelText(isoScope).smText).toBe('');
  });

  it("should show empty chart when the dataAvailable is set to false", function() {
    element = compileDonut('<div pf-donut-pct-chart config="config" data="data"></div>', $scope);
    var emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(0);

    $scope.data.dataAvailable = false;
    $scope.$digest();

    emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(1);
  });

});
