describe('Directive:  pfSort', function () {
  var $scope;
  var $compile;
  var element;
  var isolateScope;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.sort', 'sort/sort.html');
  });

  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileHTML = function (markup, scope) {
    element = angular.element(markup);
    $compile(element)(scope);

    scope.$digest();
    isolateScope = element.isolateScope();
  };

  beforeEach(function () {
    $scope.sortConfig = {
      fields: [
        {
          id: 'name',
          title:  'Name',
          sortType: 'alpha'
        },
        {
          id: 'count',
          title:  'Count',
          sortType: 'numeric'
        },
        {
          id: 'description',
          title:  'Description',
          sortType: 'alpha'
        }
      ]
    };

    var htmlTmp = '<div pf-sort config="sortConfig"></div>';

    compileHTML(htmlTmp, $scope);
  });

  it('should have correct number of sort fields', function () {
    var fields = element.find('.sort-pf .sort-field');
    expect(fields.length).toBe(3);
  });

  it('should have default to the first sort field', function () {
    var results = element.find('.sort-pf .dropdown-toggle');
    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0,'Name'.length)).toBe("Name");
  });

  it('should default to ascending sort', function () {
    var sortIcon = element.find('.sort-pf .fa-sort-alpha-asc');
    expect(sortIcon.length).toBe(1);
  });

  it('should update the current sort when one is selected', function () {
    var results = element.find('.sort-pf .dropdown-toggle');
    var fields = element.find('.sort-pf .sort-field');

    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0,'Name'.length)).toBe("Name");
    expect(fields.length).toBe(3);

    eventFire(fields[2], 'click');
    $scope.$digest();

    results = element.find('.sort-pf .dropdown-toggle');
    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0,'Description'.length)).toBe("Description");
  });

  it('should update the direction icon when the sort type changes', function () {
    var results = element.find('.sort-pf .dropdown-toggle');
    var fields = element.find('.sort-pf .sort-field');
    var sortIcon = element.find('.sort-pf .fa-sort-alpha-asc');

    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0,'Name'.length)).toBe("Name");
    expect(fields.length).toBe(3);
    expect(sortIcon.length).toBe(1);

    eventFire(fields[1], 'click');
    $scope.$digest();

    results = element.find('.sort-pf .dropdown-toggle');
    sortIcon = element.find('.sort-pf .fa-sort-numeric-asc');
    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0,'Count'.length)).toBe("Count");
    expect(sortIcon.length).toBe(1);

  });

  it('should reverse the sort direction when the direction button is clicked', function () {
    var sortButton = element.find('.sort-pf .btn.btn-link');
    var sortIcon = element.find('.sort-pf .fa-sort-alpha-asc');
    expect(sortButton.length).toBe(1);
    expect(sortIcon.length).toBe(1);

    eventFire(sortButton[0], 'click');
    $scope.$digest();

    sortIcon = element.find('.sort-pf .fa-sort-alpha-desc');
    expect(sortIcon.length).toBe(1);
  });

  it ('should notify when a new sort field is chosen', function() {
    var notified = false;
    var chosenField = '';
    var chosenDir = '';
    var fields = element.find('.sort-pf .sort-field');

    var watchForNotify = function (sortField, isAscending) {
      notified = true;
      chosenField = sortField;
      chosenDir = isAscending;
    };

    $scope.sortConfig.onSortChange = watchForNotify;


    expect(fields.length).toBe(3);

    eventFire(fields[2], 'click');
    $scope.$digest();

    expect(notified).toBeTruthy();
    expect(chosenField).toBe($scope.sortConfig.fields[2]);
    expect(chosenDir).toBeTruthy();
  });

  it ('should notify when the sort direction changes', function() {
    var notified = false;
    var chosenField = '';
    var chosenDir = '';
    var sortButton = element.find('.sort-pf .btn.btn-link');

    var watchForNotify = function (sortField, isAscending) {
      notified = true;
      chosenField = sortField;
      chosenDir = isAscending;
    };

    $scope.sortConfig.onSortChange = watchForNotify;

    expect(sortButton.length).toBe(1);

    eventFire(sortButton[0], 'click');
    $scope.$digest();

    expect(notified).toBeTruthy();
    expect(chosenField).toBe($scope.sortConfig.fields[0]);
    expect(chosenDir).toBeFalsy();
  });
  it ('should return appropriate icons for current sort type and direction', function () {
    $scope.sortConfig.currentField = $scope.sortConfig.fields[0];
    $scope.sortConfig.isAscending = true;
    $scope.$digest();
    expect(isolateScope.getSortIconClass()).toBe('fa fa-sort-alpha-asc');

    $scope.sortConfig.currentField = $scope.sortConfig.fields[0];
    $scope.sortConfig.isAscending = false;
    $scope.$digest();
    expect(isolateScope.getSortIconClass()).toBe('fa fa-sort-alpha-desc');

    $scope.sortConfig.currentField = $scope.sortConfig.fields[1];
    $scope.sortConfig.isAscending = true;
    $scope.$digest();
    expect(isolateScope.getSortIconClass()).toBe('fa fa-sort-numeric-asc');

    $scope.sortConfig.currentField = $scope.sortConfig.fields[1];
    $scope.sortConfig.isAscending = false;
    $scope.$digest();
    expect(isolateScope.getSortIconClass()).toBe('fa fa-sort-numeric-desc');
  });
})
