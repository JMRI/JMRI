describe('Directive: pfHeatmapLegend', function() {
  var $scope, $compile, element, legendItem, legendText;

  beforeEach(module(
    'patternfly.charts',
    'charts/heatmap/heatmap-legend.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileChart = function (markup, scope) {
    var el = $compile(markup)(scope);
    scope.$digest();
    return angular.element(el);
  };

  beforeEach(function() {
  });

  it("should use the default legend text and colors", function() {
    element = compileChart('<div pf-heatmap-legend ></div>',$scope);
    expect(angular.element(element).find('li').size()).toBe(4);

    legendItem = angular.element(element).find('li')[0];
    legendText = legendItem.querySelector('.legend-pf-text');

    expect(legendText.innerHTML).toBe("&gt; 90%");
  });

  it("should set the legend text and colors", function() {
    $scope.legendLabels = ['<= 70%', '> 70%'];
    $scope.heatmapColorPattern = ['#d4f0fa', '#F9D67A'];

    element = compileChart('<div pf-heatmap-legend legend="legendLabels" legend-colors="heatmapColorPattern"></div>',$scope);
    expect(angular.element(element).find('li').size()).toBe(2);

    legendItem = angular.element(element).find('li')[0];
    legendText = legendItem.querySelector('.legend-pf-text');

    expect(legendText.innerHTML).toBe("&gt; 70%");
  });


});
