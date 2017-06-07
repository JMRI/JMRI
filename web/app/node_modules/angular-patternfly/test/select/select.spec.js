describe('Component:  pfSelect', function () {
  var $scope;
  var $compile;
  var element;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.select', 'select/select.html');
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
    $scope.items = [
      {
        id: 1,
        title:  'A'
      },
      {
        id: 2,
        title:  'B'
      },
      {
        id: 3,
        title:  'C'
      },
      {
        id: 4,
        title:  'D'
      }
    ];

    $scope.selected = $scope.items[1];

    var htmlTmp = '<pf-select selected="selected" options="items" display-field="title"></pf-select>';

    compileHTML(htmlTmp, $scope);
  });

  it('should have correct number of options', function () {
    var menuItems = element.find('.dropdown-menu li');
    expect(menuItems.length).toBe(4);
  });

  it('should have the correct item selected', function () {
    var results = element.find('.dropdown-toggle');
    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0, $scope.items[1].title.length)).toBe($scope.items[1].title);
  });

  it('should update the selected item when one is clicked', function () {
    var menuItems = element.find('.dropdown-menu li > a');
    expect(menuItems.length).toBe(4);

    eventFire(menuItems[2], 'click');
    $scope.$digest();

    expect($scope.selected.id).toBe(3);
    var results = element.find('.dropdown-toggle');
    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0, $scope.items[1].title.length)).toBe($scope.items[2].title);
  });

  it('should add a default item when empty value is given', function () {
    var htmlTmp = '<pf-select selected="selected" options="items" display-field="title" empty-value="nothing"></pf-select>';

    compileHTML(htmlTmp, $scope);

    var menuItems = element.find('.dropdown-menu li');
    expect(menuItems.length).toBe(5);
  });

  it('should show the default when no selection is given', function () {

    $scope.selected = null;
    var htmlTmp = '<pf-select selected="selected" options="items" display-field="title" empty-value="nothing"></pf-select>';

    compileHTML(htmlTmp, $scope);

    var menuItems = element.find('.dropdown-menu li');
    expect(menuItems.length).toBe(5);

    var results = element.find('.dropdown-toggle');
    expect(results.length).toBe(1);
    expect(results.html().trim().slice(0, "nothing".length)).toBe("nothing");
  });

  it('should call the onSelect function on selection', function () {

    var onSelectCalled = false;
    var selectedItem = null;
    $scope.onSelect = function(item) {
      onSelectCalled = true;
      selectedItem = item;
    };

    $scope.selected = null;
    var htmlTmp = '<pf-select selected="selected" options="items" display-field="title" empty-value="nothing" on-select="onSelect"></pf-select>';

    compileHTML(htmlTmp, $scope);

    var menuItems = element.find('.dropdown-menu li > a');
    expect(menuItems.length).toBe(5);

    expect(onSelectCalled).toBe(false);
    expect(selectedItem).toBe(null);

    eventFire(menuItems[2], 'click');
    $scope.$digest();

    expect(onSelectCalled).toBe(true);
    expect(selectedItem.id).toBe(2);
  });

});
