describe('Directive: pfUtilizationBarChart', function() {
  var $scope, $compile, $sanitize, element, utilizationBar, title, subTitle;

  beforeEach(module(
    'patternfly.charts',
    'charts/empty-chart.html',
    'charts/utilization-bar/utilization-bar-chart.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_, _$sanitize_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $sanitize = _$sanitize_;
  }));

  beforeEach(function() {

    $scope.data = {
      'used': '8',
      'total': '10'
    };

    $scope.title = 'CPU Usage';
    $scope.units = 'GB';

    element = compileChart('<div pf-utilization-bar-chart chart-data=data chart-title=title units=units></div>', $scope);

  });

  var compileChart = function (markup, scope) {
    var el = $compile(markup)(scope);
    scope.$digest();
    return el;
  };

  it("should set the width of the inner bar to be 80%", function() {
    utilizationBar = angular.element(element).find('.progress-bar').css('width');
    expect(utilizationBar).toBe("80%");
  });

  it("should set aria-valuenow values", function() {
    used = angular.element(element).find('.progress-bar').not('.progress-bar-remaining');
    expect(used.attr('aria-valuenow')).toBe("80");
  });

  it("should set the charts title and usage label", function() {
    title = angular.element(element).find('.progress-description').html();
    expect(title).toBe("CPU Usage");

    subTitle = angular.element(element).find('.progress-bar span').text();
    expect(subTitle).toBe("8 of 10 GB Used");

    //test 'percent' used-label-format
    element = compileChart("<div pf-utilization-bar-chart chart-data=data footer-label-format='percent' chart-title=title units=units></div>", $scope);
    subTitle = angular.element(element).find('.progress-bar span').text();
    expect(subTitle).toBe("80% Used");
  });

  it("should set the layout to be 'inline', and use custom widths", function() {
    $scope.layoutInline = {
      'type': 'inline',
      'titleLabelWidth': '120px',
      'footerLabelWidth': '60px'
    };

    element = compileChart("<div pf-utilization-bar-chart chart-data=data layout=layoutInline chart-title=title units=units></div>", $scope);
    utilizationBar = angular.element(element).find('.progress-container');
    expect(utilizationBar.size()).toBe(1);

    utilizationBar = angular.element(element).find('.progress-container').css('padding-left');
    expect(utilizationBar).toBe("120px");
    utilizationBar = angular.element(element).find('.progress-container').css('padding-right');
    expect(utilizationBar).toBe("60px");
  });

  it("should set the error and warning thresholds", function() {
    element = compileChart("<div pf-utilization-bar-chart chart-data=data threshold-error='85' threshold-warning='45' chart-title=title units=units></div>", $scope);

    utilizationBar = angular.element(element).find('.progress-bar-warning');
    expect(utilizationBar.size()).toBe(1);

    element = compileChart("<div pf-utilization-bar-chart chart-data=data threshold-error='45' threshold-warning='15' chart-title=title units=units></div>", $scope);

    utilizationBar = angular.element(element).find('.progress-bar-danger');
    expect(utilizationBar.size()).toBe(1);
  });

  it("should use custom footer labels", function() {
    $scope.custfooter = '<strong>500 TB</strong> Total';

    element = compileChart("<div pf-utilization-bar-chart chart-data=data threshold-error='85' threshold-warning='45' chart-title=title chart-footer=custfooter units=units></div>", $scope);

    subTitle = angular.element(element).find('.progress-bar span').html();
    expect(subTitle).toBe("<strong>500 TB</strong> Total");
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
