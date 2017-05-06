describe('Directive: pfC3Chart', function() {
  var $scope, $compile, element;

  beforeEach(module(
    'patternfly.charts'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  beforeEach(function() {
    $scope.myChart = "myChartId";
    $scope.chartConfig = {};
    element = '<div pf-c3-chart id="myChart" config="chartConfig"></div>';
    element = $compile(element)($scope);
    $scope.$digest();
  });

  it("chart should find empty template", function() {
    expect(angular.element(element).html()).toBe("");
  });

});
