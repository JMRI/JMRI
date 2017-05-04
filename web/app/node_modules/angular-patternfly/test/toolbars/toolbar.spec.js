describe('Directive:  pfToolbar', function () {
  var $scope;
  var $compile;
  var element;
  var $pfViewUtils;
  var performedAction;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.toolbars', 'patternfly.views', 'patternfly.filters', 'patternfly.select', 'toolbars/toolbar.html',
           'filters/filter.html', 'filters/filter-fields.html', 'filters/filter-results.html',
           'sort/sort.html');
  });

  beforeEach(inject(function (_$compile_, _$rootScope_, pfViewUtils) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $pfViewUtils = pfViewUtils;
  }));

  var compileHTML = function (markup, scope) {
    element = angular.element(markup);
    $compile(element)(scope);

    scope.$digest();
  };

  beforeEach(function () {

    performedAction = undefined;
    var performAction = function (action) {
      performedAction = action;
    };

    $scope.config = {
      viewsConfig: {
        views: [$pfViewUtils.getDashboardView(), $pfViewUtils.getListView(), $pfViewUtils.getCardView(), $pfViewUtils.getTableView(), $pfViewUtils.getTopologyView()]
      },
      sortConfig: {
        fields: [
          {
            id: 'name',
            title:  'Name',
            sortType: 'alpha'
          },
          {
            id: 'age',
            title:  'Age',
            sortType: 'numeric'
          },
          {
            id: 'address',
            title:  'Address',
            sortType: 'alpha'
          }
        ]
      },
      filterConfig: {
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
      },
      actionsConfig: {
        primaryActions: [
          {
            name: 'Action 1',
            title: 'Do the first thing',
            actionFn: performAction
          },
          {
            name: 'Action 2',
            title: 'Do something else',
            actionFn: performAction
          }
        ],
        moreActions: [
          {
            name: 'Action',
            title: 'Perform an action',
            actionFn: performAction
          },
          {
            name: 'Another Action',
            title: 'Do something else',
            actionFn: performAction
          },
          {
            name: 'Disabled Action',
            title: 'Unavailable action',
            actionFn: performAction,
            isDisabled: true
          },
          {
            name: 'Something Else',
            title: '',
            actionFn: performAction
          },
          {
            isSeparator: true
          },
          {
            name: 'Grouped Action 1',
            title: 'Do something',
            actionFn: performAction
          },
          {
            name: 'Grouped Action 2',
            title: 'Do something similar',
            actionFn: performAction
          }
        ]
      }
    };

    var htmlTmp = '<div pf-toolbar config="config"></div>';

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

    $scope.config.filterConfig.resultsCount = 10;

    $scope.$digest();

    results = element.find('h5');
    expect(results.length).toBe(1);
    expect(results.html()).toBe("10 Results");
  });

  it('should show active filters and clear filters button when there are filters', function () {
    var activeFilters = element.find('.active-filter');
    expect(activeFilters.length).toBe(0);
    expect(element.find('.clear-filters').length).toBe(0);

    $scope.config.filterConfig.appliedFilters = [
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
    var pfSelects = element.find('.filter-select');
    var fields = element.find('.filter-field');

    expect(pfSelects.length).toBe(0);
    eventFire(fields[2], 'click');
    $scope.$digest();
    pfSelects = element.find('.filter-select');
    expect(pfSelects.length).toBe(2); // 2 because it is a directive

    var items = pfSelects.find('li');
    expect(items.length).toBe($scope.config.filterConfig.fields[2].filterValues.length + 1); // +1 for the null value
  });

  it ('should clear a filter when the close button is clicked', function () {
    var closeButtons;

    closeButtons = element.find('.pficon-close');
    expect(closeButtons.length).toBe(0);

    $scope.config.filterConfig.appliedFilters = [
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

    $scope.config.filterConfig.appliedFilters = [
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

  it ('should not show filters when a filter config is not supplied', function () {
    var filter = element.find('.filter-pf');
    expect(filter.length).toBe(2);

    $scope.config = {
      viewsConfig: {
        views: [$pfViewUtils.getListView(), $pfViewUtils.getCardView()]
      }
    };

    var htmlTmp = '<div pf-toolbar config="config"></div>';

    compileHTML(htmlTmp, $scope);

    filter = element.find('.filter-pf');
    expect(filter.length).toBe(0);
  });

  it ('should show the correct view selection buttons', function () {
    var selectors = element.find('.toolbar-pf-view-selector .btn-link');
    expect(selectors.length).toBe(5);

    expect(element.find('.fa-dashboard').length).toBe(1);
    expect(element.find('.fa-th').length).toBe(1);
    expect(element.find('.fa-th-list').length).toBe(1);
    expect(element.find('.fa-table').length).toBe(1);
    expect(element.find('.fa-sitemap').length).toBe(1);
  });

  it ('should show the currently selected view', function () {
    var viewSelector = element.find('.toolbar-pf-view-selector');
    var active = element.find('.active');

    expect(viewSelector.length).toBe(1);
    expect(active.length).toBe(0);

    $scope.config.viewsConfig.currentView = $scope.config.viewsConfig.views[0].id;
    $scope.$apply();

    active = element.find('.active');
    expect(active.length).toBe(1);
  });

  it ('should update the currently selected view when a view selector clicked', function () {
    var viewSelector = element.find('.toolbar-pf-view-selector');
    var active = element.find('.active');
    var listSelector = element.find('.toolbar-pf-view-selector .btn-link');

    expect(viewSelector.length).toBe(1);
    expect(active.length).toBe(0);
    expect(listSelector.length).toBe(5);

    eventFire(listSelector[0], 'click');
    $scope.$apply();

    active = element.find('.active');
    expect(active.length).toBe(1);
  });

  it ('should call the callback function when a view selector clicked', function () {
    var listSelector = element.find('.toolbar-pf-view-selector .btn-link');
    var functionCalled = false;

    var onViewSelect = function () {
      functionCalled = true;
    };

    $scope.config.viewsConfig.onViewSelect = onViewSelect;
    expect(functionCalled).toBeFalsy();
    expect(listSelector.length).toBe(5);

    eventFire(listSelector[0], 'click');
    $scope.$apply();

    expect(functionCalled).toBeTruthy();
  });

  it ('should not show view selectors when no viewsConfig is supplied', function () {
    var viewSelector = element.find('.toolbar-pf-view-selector');
    expect(viewSelector.length).toBe(1);

    $scope.config.viewsConfig = undefined;
    $scope.$digest();

    viewSelector = element.find('.toolbar-pf-view-selector');
    expect(viewSelector.length).toBe(0);
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
    expect(results.html().trim().slice(0,'Address'.length)).toBe("Address");
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
    expect(results.html().trim().slice(0,'Age'.length)).toBe("Age");
    expect(sortIcon.length).toBe(1);

  });

  it('should reverse the sort direction when the direction button is clicked', function () {
    var sortIcon = element.find('.sort-pf .fa-sort-alpha-asc');
    var sortButton = element.find('.sort-pf .btn.btn-link');
    expect(sortIcon.length).toBe(1);
    expect(sortButton.length).toBe(1);

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

    $scope.config.sortConfig.onSortChange = watchForNotify;


    expect(fields.length).toBe(3);

    eventFire(fields[2], 'click');
    $scope.$digest();

    expect(notified).toBeTruthy();
    expect(chosenField).toBe($scope.config.sortConfig.fields[2]);
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

    $scope.config.sortConfig.onSortChange = watchForNotify;

    expect(sortButton.length).toBe(1);

    eventFire(sortButton[0], 'click');
    $scope.$digest();

    expect(notified).toBeTruthy();
    expect(chosenField).toBe($scope.config.sortConfig.fields[0]);
    expect(chosenDir).toBeFalsy();
  });

  it ('should not show sort components when a sort config is not supplied', function () {
    var filter = element.find('.sort-pf');
    expect(filter.length).toBe(1);

    $scope.config = {
      viewsConfig: {
        views: [$pfViewUtils.getListView(), $pfViewUtils.getCardView()]
      }
    };

    var htmlTmp = '<div pf-toolbar config="config"></div>';

    compileHTML(htmlTmp, $scope);

    filter = element.find('.sort-pf');
    expect(filter.length).toBe(0);
  });

  it('should have correct number of primary actions', function () {
    var fields = element.find('.toolbar-pf-actions .primary-action');
    expect(fields.length).toBe(2);
  });

  it('should have correct number of secondary actions', function () {
    var fields = element.find('.toolbar-pf-actions .secondary-action');
    expect(fields.length).toBe(6);
  });

  it('should have correct number of separators', function () {
    var fields = element.find('.toolbar-pf-actions .divider');
    expect(fields.length).toBe(1);
  });

  it('should correctly disable actions', function () {
    var fields = element.find('.toolbar-pf-actions .disabled');
    expect(fields.length).toBe(1);
  });

  it('should not show more actions menu when there are no more actions', function () {
    var menus = element.find('.fa-ellipsis-v');
    expect(menus.length).toBe(1);

    $scope.config.actionsConfig.moreActions = undefined;
    $scope.$digest();

    menus = element.find('.toolbar-pf-actions .fa-ellipsis-v');
    expect(menus.length).toBe(0);
  });

  it('should call the action function with the appropriate action when an action is clicked', function () {
    var primaryActions = element.find('.toolbar-pf-actions .primary-action');
    var moreActions = element.find('.toolbar-pf-actions .secondary-action');
    expect(primaryActions.length).toBe(2);
    expect(moreActions.length).toBe(6);

    eventFire(primaryActions[0], 'click');
    $scope.$digest();

    expect(performedAction.name).toBe('Action 1');

    eventFire(moreActions[3], 'click');
    $scope.$digest();

    expect(performedAction.name).toBe('Something Else');
  });

  it('should not call the action function when a disabled action is clicked', function () {
    var primaryActions = element.find('.toolbar-pf-actions .primary-action');
    var moreActions = element.find('.toolbar-pf-actions .secondary-action');
    expect(primaryActions.length).toBe(2);
    expect(moreActions.length).toBe(6);

    eventFire(moreActions[2], 'click');
    $scope.$digest();

    expect(performedAction).toBe(undefined);

    eventFire(primaryActions[1], 'click');
    $scope.$digest();

    expect(performedAction.name).toBe('Action 2');

    performedAction = undefined;
    $scope.config.actionsConfig.primaryActions[1].isDisabled = true;
    $scope.$digest();

    eventFire(primaryActions[1], 'click');
    $scope.$digest();

    expect(performedAction).toBe(undefined);
  });

  it ('should not show action components when an action config is not supplied', function () {
    var filter = element.find('.toolbar-actions');
    expect(filter.length).toBe(1);

    $scope.config = {
      viewsConfig: {
        views: [$pfViewUtils.getListView(), $pfViewUtils.getCardView()]
      }
    };

    var htmlTmp = '<div pf-toolbar config="config"></div>';

    compileHTML(htmlTmp, $scope);

    filter = element.find('.toolbar-actions');
    expect(filter.length).toBe(0);
  });

  it ('should add custom actions in the correct location', function () {
    var actionBar = element.find('.toolbar-actions');
    expect(actionBar.length).toBe(1);

    var includeActions = actionBar.find('.toolbar-pf-include-actions');
    expect(includeActions.length).toBe(0);

    $scope.config.actionsConfig.actionsInclude = true;

    var includeHtml = '<div pf-toolbar config="config"><actions><button class="btn btn-default add-action" type="button">Add Action</button></actions></div>';
    compileHTML(includeHtml, $scope);

    actionBar = element.find('.toolbar-actions');
    expect(actionBar.length).toBe(1);

    includeActions = actionBar.find('.toolbar-pf-include-actions');
    expect(includeActions.length).toBe(1);

    var addAction = includeActions.find('.add-action');
    expect(addAction.length).toBe(1);
  });
});
