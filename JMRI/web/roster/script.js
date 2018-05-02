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

  // load translations
  $translatePartialLoader.addPart('web/roster');

  // true if loading indicator should be shown; false otherwise
  $scope.loading = true;

  // columns for table view with default text
  $scope.columns = [
    {header: 'ID', itemField: 'name'},
    {header: 'DCC Address', itemField: 'address'},
    {header: 'Road Name', itemField: 'road'},
    {header: 'Road Number', itemField: 'number'},
    {header: 'Owner', itemField: 'owner'}
  ];
  // translate column headers
  for (var i in $scope.columns) {
    var c = $scope.columns[i];
    $translate('ROSTER.FIELD.' + c.itemField).then(function(translation) {
      c.header = translation;
    }, function(translationId) {
      $log.error('Unable to translate ' + translationId);
    });
    $scope.columns[i] = c;
  }

  // the roster items, initially an empty list
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
    icon: 'pficon-warning-triangle-o', // replace with pficon-add-circle-o once patternfly/angular-patterfly#510 is addressed
    title: 'No Roster Entries',
    info: 'There are no roster entries to show. If this is unexpected, review the roster preferences in JMRI.',
    helpLink: {
       label: 'For more information please see',
       urlLabel: 'JMRI: DecoderPro User Guide',
       url: '/help/en/html/apps/DecoderPro/Roster.shtml'
    }
  };
  $scope.emptyStateActions = [
    {name: 'Upload Roster Entry', title: 'Upload a roster entry.', actionFn: uploadEntry, type: 'main'}
  ];
  $translate(['ROSTER.EMPTY.TITLE', 'ROSTER.EMPTY.INFO', 'ROSTER.EMPTY.HELP.LABEL', 'ROSTER.EMPTY.HELP.URL_LABEL', 'ROSTER.ACTION.UPLOAD.FULLNAME', 'ROSTER.ACTION.UPLOAD.TITLE'])
  .then(function(translations) {
    $scope.emptyStateConfig.title = translations['ROSTER.EMPTY.TITLE'];
    $scope.emptyStateConfig.info = translations['ROSTER.EMPTY.INFO'];
    $scope.emptyStateConfig.helpLink.label = translations['ROSTER.EMPTY.HELP.LABEL'];
    $scope.emptyStateConfig.helpLink.urlLabel = translations['ROSTER.EMPTY.HELP.URL_LABEL'];
    $scope.emptyStateActions[0].name = translations['ROSTER.ACTION.UPLOAD.FULLNAME'];
    $scope.emptyStateActions[0].title = translations['ROSTER.ACTION.UPLOAD.TITLE'];
  }, function(translationIds) {
    $log.error('Unable to translate emptyStateConfig elements');
  });

  $scope.tableConfig = {
    onCheckBoxChange: handleCheckBoxChange,
    selectionMatchProp: 'name',
    itemsAvailable: true,
  };

  $scope.dtOptions = {
    dom: 't'
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
    } else if (filter.id === 'road') {
      match = item.road.match(re) !== null;
    } else if (filter.id === 'number') {
      match = item.number.match(re) !== null;
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
      {id: 'road', title: 'Road Name', placeholder: 'Filter by Road Name...', filterType: 'text'},
      {id: 'number', title: 'Road Number', placeholder: 'Filter by Road Number...', filterType: 'text'},
      {id: 'owner', title: 'Owner', placeholder: 'Filter by Owner...', filterType: 'text'}
    ],
    resultsCount: $scope.items.length,
    totalCount: $scope.allItems.length,
    appliedFilters: [],
    onFilterChange: filterChange
  };
  for (var i in $scope.filterConfig.fields) {
    var f = $scope.filterConfig.fields[i];
    $translate('ROSTER.FIELD.' + f.id).then(function(translation) {
      f.title = translation;
    }, function(translationId) {
      $log.error('Unable to translate ' + translationId);
    });
    $translate('ROSTER.FILTER_PLACEHOLDER', f.title, 'filterTitle').then(function(translation) {
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
    } else if ($scope.sortConfig.currentField.id === 'road') {
      compValue = item1.road.localeCompare(item2.road);
    } else if ($scope.sortConfig.currentField.id === 'number') {
      compValue = item1.number - item2.number;
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
      {id: 'road', title: 'Road Name', sortType: 'alpha'},
      {id: 'number', title: 'Road Number', sortType: 'numeric'},
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

  function uploadEntry() {

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
            $scope.toolbarConfig.filterConfig.totalCount = $scope.allItems.length;
          }
        }
        $scope.items = $scope.allItems;
        filterChange($scope.toolbarConfig.filterConfig.appliedFilters);
      } else {
        var entry = response.data;
        if (entry.type === 'rosterEntry') {
          $scope.allItems.push(entry.data);
          $scope.toolbarConfig.filterConfig.totalCount = $scope.allItems.length;
          $scope.items = $scope.allItems;
          filterChange($scope.toolbarConfig.filterConfig.appliedFilters);
        }
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
