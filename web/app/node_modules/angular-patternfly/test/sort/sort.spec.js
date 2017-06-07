describe('Directive:  pfSort', function () {
  var $scope;
  var $compile;
  var element;

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

    var htmlTmp = '<pf-sort config="sortConfig"></pf-sort>';

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
    var alphaSortAsc = element.find('.fa.fa-sort-alpha-asc');
    var alphaSortDesc = element.find('.fa.fa-sort-alpha-desc');
    var numericSortAsc = element.find('.fa.fa-sort-numeric-asc');
    var numericSortDesc = element.find('.fa.fa-sort-numeric-desc');
    expect(alphaSortAsc.length).toBe(1);
    expect(alphaSortDesc.length).toBe(0);
    expect(numericSortAsc.length).toBe(0);
    expect(numericSortDesc.length).toBe(0);

    $scope.sortConfig.currentField = $scope.sortConfig.fields[0];
    $scope.sortConfig.isAscending = false;
    $scope.$digest();
    alphaSortAsc = element.find('.fa.fa-sort-alpha-asc');
    alphaSortDesc = element.find('.fa.fa-sort-alpha-desc');
    numericSortAsc = element.find('.fa.fa-sort-numeric-asc');
    numericSortDesc = element.find('.fa.fa-sort-numeric-desc');
    expect(alphaSortAsc.length).toBe(0);
    expect(alphaSortDesc.length).toBe(1);
    expect(numericSortAsc.length).toBe(0);
    expect(numericSortDesc.length).toBe(0);

    $scope.sortConfig.currentField = $scope.sortConfig.fields[1];
    $scope.sortConfig.isAscending = true;
    $scope.$digest();
    alphaSortAsc = element.find('.fa.fa-sort-alpha-asc');
    alphaSortDesc = element.find('.fa.fa-sort-alpha-desc');
    numericSortAsc = element.find('.fa.fa-sort-numeric-asc');
    numericSortDesc = element.find('.fa.fa-sort-numeric-desc');
    expect(alphaSortAsc.length).toBe(0);
    expect(alphaSortDesc.length).toBe(0);
    expect(numericSortAsc.length).toBe(1);
    expect(numericSortDesc.length).toBe(0);

    $scope.sortConfig.currentField = $scope.sortConfig.fields[1];
    $scope.sortConfig.isAscending = false;
    $scope.$digest();
    alphaSortAsc = element.find('.fa.fa-sort-alpha-asc');
    alphaSortDesc = element.find('.fa.fa-sort-alpha-desc');
    numericSortAsc = element.find('.fa.fa-sort-numeric-asc');
    numericSortDesc = element.find('.fa.fa-sort-numeric-desc');
    expect(alphaSortAsc.length).toBe(0);
    expect(alphaSortDesc.length).toBe(0);
    expect(numericSortAsc.length).toBe(0);
    expect(numericSortDesc.length).toBe(1);
  });
})
