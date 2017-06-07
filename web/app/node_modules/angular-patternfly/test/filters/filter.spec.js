describe('Directive:  pfFilter', function () {
  var $scope;
  var $compile;
  var element;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.filters', 'filters/filter.html', 'filters/filter-fields.html', 'filters/filter-results.html');
  });

  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileHTML = function (markup, scope) {
    element = angular.element(markup);
    var el = $compile(element)(scope);

    scope.$digest();
  };

  beforeEach(function () {
    $scope.filterConfig = {
      fields: [
        {
          id: 'name',
          title:  'Name',
          placeholder: 'Filter by Name',
          filterType: 'text'
        },
        {
          id: 'address',
          title:  'Address',
          placeholder: 'Filter by Address',
          filterType: 'text'
        },
        {
          id: 'birthMonth',
          title:  'Birth Month',
          placeholder: 'Filter by Birth Month',
          filterType: 'select',
          filterValues: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
        }
      ],
      resultsCount: 5,
      appliedFilters: []
    };

    var htmlTmp = '<pf-filter config="filterConfig"></pf-filter>';

    compileHTML(htmlTmp, $scope);
  });

  it('should have correct number of filter fields', function () {
    var fields = element.find('.filter-field');
    expect(fields.length).toBe(3);
  });

  it('should have correct number of results', function () {
    var results = element.find('h5');
    expect(results.length).toBe(1);
    expect(results.html()).toBe("5 Results");

    $scope.filterConfig.resultsCount = 10;

    $scope.$digest();

    results = element.find('h5');
    expect(results.length).toBe(1);
    expect(results.html()).toBe("10 Results");
  });

  it('should show active filters and clear filters button when there are filters', function () {
    var activeFilters = element.find('.active-filter');
    expect(activeFilters.length).toBe(0);
    expect(element.find('.clear-filters').length).toBe(0);

    $scope.filterConfig.appliedFilters = [
      {
        id: 'address',
        title: 'Address',
        value: 'New York'
      }
    ];

    $scope.$digest();
    activeFilters = element.find('.active-filter');
    expect(activeFilters.length).toBe(1);
    expect(element.find('.clear-filters').length).toBe(1);
  });

  it ('should add a dropdown select when a select type is chosen', function() {
    var filterSelect = element.find('.filter-select');
    var fields = element.find('.filter-field');

    expect(filterSelect.length).toBe(0);
    eventFire(fields[2], 'click');
    $scope.$digest();
    filterSelect = element.find('.filter-select');
    expect(filterSelect.length).toBe(1);

    var items = filterSelect.find('li');
    expect(items.length).toBe($scope.filterConfig.fields[2].filterValues.length + 1); // +1 for the null value
  });

  it ('should clear a filter when the close button is clicked', function () {
    var closeButtons;

    closeButtons = element.find('.pficon-close');
    expect(closeButtons.length).toBe(0);

    $scope.filterConfig.appliedFilters = [
      {
        id: 'address',
        title: 'Address',
        value: 'New York'
      }
    ];

    $scope.$digest();

    closeButtons = element.find('.pficon-close');
    expect(closeButtons.length).toBe(1);

    eventFire(closeButtons[0], 'click');
    $scope.$digest();
    expect(element.find('.pficon-close').length).toBe(0);
  });

  it ('should clear all filters when the clear all filters button is clicked', function () {
    var clearButtons = element.find('.clear-filters');
    var activeFilters = element.find('.active-filter');

    expect(activeFilters.length).toBe(0);
    expect(clearButtons.length).toBe(0);

    $scope.filterConfig.appliedFilters = [
      {
        id: 'address',
        title: 'Address',
        value: 'New York'
      }
    ];

    $scope.$digest();

    activeFilters = element.find('.active-filter');
    clearButtons = element.find('.clear-filters');

    expect(activeFilters.length).toBe(1);
    expect(clearButtons.length).toBe(1);

    eventFire(clearButtons[0], 'click');
    $scope.$digest();

    activeFilters = element.find('.active-filter');
    clearButtons = element.find('.clear-filters');
    expect(activeFilters.length).toBe(0);
    expect(clearButtons.length).toBe(0);
  });
})
