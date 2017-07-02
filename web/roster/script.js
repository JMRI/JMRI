/**
 * Create a roster view.
 *
 * @type undefined
 * @param {scope} $scope the controller's scope
 * @param {loader} $translatePartialLoader I18N support provider
 * @param {angularService} $http Angular HTTP provider
 * @param {angularService} $log Angular logger
 * @param {angularService} jmriWebSocket JMRI web socket provider
 * @param {angularService} pfViewUtils Patternfly utilities for handling data views
 * @param {angularService} $filter Patternfly filter service
 */
angular.module('jmri.app').controller('RosterCtrl', function RosterCtrl($scope, $translate, $translatePartialLoader, $http, $log, jmriWebSocket, pfViewUtils, $filter) {

  $translatePartialLoader.addPart('web/roster');

  $scope.loading = true;

  $scope.columns = [
    {header: 'Railroad', itemField: 'road'},
    {header: 'Number', itemField: 'number'},
    {header: 'DCC Address', itemField: 'address'},
    {header: 'ID', itemField: 'name'},
    {header: 'Owner', itemField: 'owner'}
  ];
  for (var i in $scope.columns) {
    var c = $scope.columns[i];
    $translate('ROSTER.FIELD.' + c.itemField).then(function(translation) {
      c.header = translation;
    }, function(translationId) {
      $log.error('Unable to translate ' + translationId);
    });
    $scope.columns[i] = c;
  }

  $scope.allItems = [];
  $scope.items = $scope.allItems;

  //
  // Cards/List/Table Views
  //

  $scope.listConfig = {
    selectionMatchProp: 'name',
    checkDisabled: false,
    itemsAvailable: true,
    onCheckBoxChange: handleCheckBoxChange
  };

  $scope.emptyStateConfig = {
    icon: 'pficon-warning-triangle-o',
    title: 'No Items Available',
    info: 'This is the Empty State component. The goal of a empty state pattern is to provide a good first impression that helps users to achieve their goals. It should be used when a view is empty because no objects exists and you want to guide the user to perform specific actions.',
    helpLink: {
       label: 'For more information please see',
       urlLabel: 'pfExample',
       url : '#/api/patternfly.views.component:pfEmptyState'
    }
  };

  $scope.tableConfig = {
    onCheckBoxChange: handleCheckBoxChange,
    selectionMatchProp: 'name',
    itemsAvailable: true,
  };

  $scope.dtOptions = {
    displayLength: 3,
    dom: 'tp'
  }

  //
  // Toolbar
  //

  // View handling

  var viewSelected = function(viewId) {
    $scope.viewType = viewId;
    $scope.sortConfig.show = ($scope.viewType === 'tableView' ? false : true);
  };

  $scope.viewsConfig = {
    views: [pfViewUtils.getListView(), pfViewUtils.getCardView(), pfViewUtils.getTableView()],
    onViewSelect: viewSelected
  };

  $scope.viewsConfig.currentView = $scope.viewsConfig.views[0].id;
  $scope.viewType = $scope.viewsConfig.currentView;

  // Filter handling

  var matchesFilter = function (item, filter) {
    var match = true;
    var re = new RegExp(filter.value, 'i');

    if (filter.id === 'name') {
      match = item.name.match(re) !== null;
    } else if (filter.id === 'number') {
      match = item.number === parseInt(filter.value);
    } else if (filter.id === 'address') {
      match = item.address === parseInt(filter.value);
    } else if (filter.id === 'owner') {
      match = item.owner.match(re) !== null;
    }
    return match;
  };

  var matchesFilters = function (item, filters) {
    var matches = true;

    filters.forEach(function(filter) {
      if (!matchesFilter(item, filter)) {
        matches = false;
        return false;
      }
    });
    return matches;
  };

  var applyFilters = function (filters) {
    $scope.items = [];
    if (filters && filters.length > 0) {
      $scope.allItems.forEach(function (item) {
        if (matchesFilters(item, filters)) {
          $scope.items.push(item);
        }
      });
    } else {
      $scope.items = $scope.allItems;
    }
  };

  var filterChange = function(filters) {
    applyFilters(filters);
    $scope.toolbarConfig.filterConfig.resultsCount = $scope.items.length;
  };

  $scope.filterConfig = {
    fields: [
      {id: 'name', title: 'ID', placeholder: 'Filter by ID...', filterType: 'text'},
      {id: 'address', title: 'DCC Address', placeholder: 'Filter by DCC Address...', filterType: 'text'},
      {id: 'owner', title: 'Owner', placeholder: 'Filter by Owner...', filterType: 'text'}
    ],
    resultsCount: $scope.items.length,
    totalCount: $scope.allItems.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  for (var i in $scope.filterConfig.fields) {
    var f = $scope.filterConfig.fields[i];
    $scope.filterTitle = f.title;
    $translate('ROSTER.FIELD.' + f.id).then(function(translation) {
      f.title = translation;
    }, function(translationId) {
      $log.error('Unable to translate ' + translationId);
    });
    $scope.filterTitle = f.title;
    $translate('ROSTER.FILTER_PLACEHOLDER').then(function(translation) {
      f.placeholder = translation;
    }, function(translationId) {
      $log.error('Unable to translate ' + translationId);
    });
    $scope.filterConfig.fields[i] = f;
  }

  var compareFn = function(item1, item2) {
    var compValue = 0;
    if ($scope.sortConfig.currentField.id === 'name') {
      compValue = item1.name.localeCompare(item2.name);
    } else if ($scope.sortConfig.currentField.id === 'address') {
      compValue = item1.address - item2.address;
    } else if ($scope.sortConfig.currentField.id === 'owner') {
      compValue = item1.owner.localeCompare(item2.owner);
    }

    if (!$scope.sortConfig.isAscending) {
      compValue = compValue * -1;
    }

    return compValue;
  };

  var sortChange = function (sortId, isAscending) {
    $scope.items.sort(compareFn);
  };

  $scope.sortConfig = {
    fields: [
      {id: 'name', title: 'ID', sortType: 'alpha'},
      {id: 'address', title: 'DCC Address', sortType: 'numeric'},
      {id: 'owner', title: 'Owner', sortType: 'alpha'}
    ],
    onSortChange: sortChange
  };
  for (var i in $scope.sortConfig.fields) {
    var c = $scope.sortConfig.fields[i];
    $translate('ROSTER.FIELD.' + c.id).then(function(translation) {
      c.title = translation;
    }, function(translationId) {
      $log.error('Unable to translate ' + translationId);
    });
    $scope.sortConfig.fields[i] = c;
  }

  $scope.toolbarConfig = {
    viewsConfig: $scope.viewsConfig,
    filterConfig: $scope.filterConfig,
    sortConfig: $scope.sortConfig
    // actionsConfig: $scope.actionsConfig
  };

  //
  // Handlers
  //

  function handleCheckBoxChange (item) {
    var selectedItems = $filter('filter')($scope.allItems, {selected: true});
    if (selectedItems) {
      $scope.toolbarConfig.filterConfig.selectedCount = selectedItems.length;
    }
  }

  //
  // Websockets
  //

  // register a listener for sensors with the jmriWebSocket service
  // this listener determines if a notification needs to be shown or cleared
//  this.webSocket = jmriWebSocket.register();

  $http.get('/json/roster').then(
    // handle a successful get for the roster
    function(response) {
      if (Array.isArray(response.data)) {
        for (var i in response.data) {
          var entry = response.data[i];
          if (entry.type === 'rosterEntry') {
            $scope.allItems.push(entry.data);
          }
        }
        $scope.items = $scope.allItems;
      }
      jmriWebSocket.get('roster', {});
      $scope.loading = false;
    },
    // handle a failed get for the roster
    function(response) {
      $log.error('Unable to query JMRI web server for roster.');
      $scope.loading = false;
    }
  );
});
