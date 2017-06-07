describe('Directive: pfDonutPctChart', function() {
  var $scope, ctrl, $compile, $timeout, element;

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

  var compileDonut = function (markup) {
    var el = $compile(angular.element(markup))($scope);
    $scope.$apply();
    ctrl = el.controller('pfDonutPctChart');
    return el;
  };

  var compileSimpleDonut = function () {
    element = compileDonut('<pf-donut-pct-chart config="config" data="data"></pf-donut-pct-chart>');
  };

  var compileDonutCenterLabel = function () {
    element = compileDonut('<pf-donut-pct-chart config="config" data="data" center-label="cntrLabel"></pf-donut-pct-chart>');
  };

  it("should trigger error threshold", function() {
    compileSimpleDonut();
    expect(ctrl.statusDonutColor().pattern[0]).toBe('#cc0000');  //red
  });

  it("should trigger warning threshold", function() {
    compileSimpleDonut();
    $scope.data.used = 850;
    $scope.$digest();
    expect(ctrl.statusDonutColor().pattern[0]).toBe('#ec7a08');  //orange
  });

  it("should trigger ok threshold", function() {
    compileSimpleDonut();
    $scope.data.used = 550;
    $scope.$digest();
    expect(ctrl.statusDonutColor().pattern[0]).toBe('#3f9c35');  //green
  });

  it("should show no threshold", function() {
    $scope.config = {
      'units': 'MHz'
    };
    compileSimpleDonut();
    expect(ctrl.statusDonutColor().pattern[0]).toBe('#0088ce');  //blue
  });

  it("should show 'used' center label by default", function() {
    compileSimpleDonut();
    expect(ctrl.getCenterLabelText().smText).toContain('Used');
  });

  it("should show 'available' center label", function() {
    compileDonutCenterLabel();
    $scope.cntrLabel = 'available';
    $scope.$digest();
    expect(ctrl.getCenterLabelText().smText).toContain('Available');
  });

  it("should show 'percent' center label", function() {
    compileDonutCenterLabel();
    $scope.cntrLabel = 'percent';
    $scope.$digest();
    expect(ctrl.getCenterLabelText().bigText).toContain('%');
  });

  it("should show no center label", function() {
    compileDonutCenterLabel();
    $scope.cntrLabel = 'none';
    $scope.$digest();
    expect(ctrl.getCenterLabelText().bigText).toBe('');
    expect(ctrl.getCenterLabelText().smText).toBe('');
  });

  it("should show 'used' center label", function() {
    compileDonutCenterLabel();
    $scope.cntrLabel = 'used';
    $scope.$digest();
    expect(ctrl.getCenterLabelText().smText).toContain('Used');
  });


  it("should use center label funtion", function() {
    compileDonutCenterLabel();

    $scope.config.centerLabelFn = function () {
      return '<tspan dy="0" x="0" class="donut-title-big-pf">' + $scope.data.available + '</tspan>' +
        '<tspan dy="20" x="0" class="donut-title-small-pf">Free</tspan>';
    };

    // hack to trigger component $onChanges
    $scope.config = angular.copy($scope.config);

    $scope.$digest();
    expect(ctrl.getCenterLabelText().bigText).toContain('50');
    expect(ctrl.getCenterLabelText().bigText).toContain('Free');
    expect(ctrl.getCenterLabelText().smText).toBe('');
  });

  it("should show empty chart when the dataAvailable is set to false", function() {
    element = compileDonut('<pf-donut-pct-chart config="config" data="data"></pf-donut-pct-chart>');
    var emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(0);

    $scope.data.dataAvailable = false;
    $scope.$digest();

    emptyChart = element.find('.empty-chart-content');
    expect(emptyChart.length).toBe(1);
  });

});
