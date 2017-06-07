/**
 * @ngdoc directive
 * @name patternfly.table.component:pfTableView - with Toolbar
 *
 * @description
 * Example configuring a table view with a toolbar.<br><br>
 * Please see {@link patternfly.toolbars.componenet:pfToolbar pfToolbar} for use in Toolbar View Switcher
 *
 * @param {object} config Optional configuration object
 * <ul style='list-style-type: none'>
 *   <li>.selectionMatchProp  - (string) Property of the items to use for determining matching, default is 'uuid'
 *   <li>.onCheckBoxChange    - ( function(item) ) Called to notify when a checkbox selection changes, default is none
 *   <li>.itemsAvailable      - (boolean) If 'false', displays the {@link patternfly.views.component:pfEmptyState Empty State} component.
 * </ul>
 * @param {object} dtOptions Optional angular-datatables DTOptionsBuilder configuration object.  See {@link http://l-lin.github.io/angular-datatables/archives/#/api angular-datatables: DTOptionsBuilder}
 * @param {array} items Array of items to display in the table view.
 * @param {array} columns Array of table column information to display in the table's header row
 * <ul style='list-style-type: none'>
 *   <li>.header     - (string) Text label for a column header
 *   <li>.itemField    - (string) Item field to associate with a particular column.
 * </ul>
 * @param {array} actionButtons List of action buttons in each row
 *   <ul style='list-style-type: none'>
 *     <li>.name - (String) The name of the action, displayed on the button
 *     <li>.title - (String) Optional title, used for the tooltip
 *     <li>.actionFn - (function(action)) Function to invoke when the action selected
 *   </ul>
 * @param {array} menuActions List of actions for dropdown menu in each row
 *   <ul style='list-style-type: none'>
 *     <li>.name - (String) The name of the action, displayed on the button
 *     <li>.title - (String) Optional title, used for the tooltip
 *     <li>.actionFn - (function(action)) Function to invoke when the action selected
 *   </ul>
 * @param {object} emptyStateConfig Optional configuration settings for the empty state component.  See the {@link patternfly.views.component:pfEmptyState Empty State} component
 * @example
<example module="patternfly.tableview.demo">
  <file name="index.html">
    <div ng-controller="ViewCtrl" class="row example-container">
      <div class="col-md-12">
        <pf-toolbar id="exampleToolbar" config="toolbarConfig"></pf-toolbar>
      </div>
      <div class="col-md-12">
        <pf-table-view config="tableConfig"
                       empty-state-config="emptyStateConfig"
                       dt-options="dtOptions"
                       colummns="colummns"
                       items="items"
                       action-buttons="tableActionButtons"
                       menu-actions="tableMenuActions">
        </pf-table-view>
      </div>
      <div class="col-md-12">
        <div class="form-group">
          <label class="checkbox-inline">
            <input type="checkbox" ng-model="tableConfig.itemsAvailable" ng-change="updateItemsAvailable()">Items Available</input>
          </label>
          <!-- //[WIP] issues dynamically changing displayLength and turning on/off pagination
            <label class="checkbox-inline">
              <input type="checkbox" ng-model="usePagination" ng-change="togglePagination()">Use Pagination</input>
            </label>
            <label>
              <input ng-model="dtOptions.displayLength" ng-disabled="!usePagination" style="width: 24px; padding-left: 6px;"> # Rows Per Page</input>
            </label> --!>
        </div>
      </div>
      <hr class="col-md-12">
      <div class="col-md-12">
        <label class="actions-label">Actions: </label>
      </div>
      <div class="col-md-12">
        <textarea rows="6" class="col-md-12">{{actionsText}}</textarea>
      </div>
    </div>
  </file>

  <file name="modules.js">
    angular.module('patternfly.tableview.demo', ['patternfly.toolbars','patternfly.table']);
  </file>

  <file name="script.js">
  angular.module('patternfly.tableview.demo').controller('ViewCtrl', ['$scope', 'pfViewUtils', '$filter',
    function ($scope, pfViewUtils, $filter) {
      $scope.actionsText = "";

      $scope.colummns = [
        { header: "Name", itemField: "name" },
        { header: "Age", itemField: "age"},
        { header: "Address", itemField: "address" },
        { header: "BirthMonth", itemField: "birthMonth"}
      ];

      $scope.dtOptions = {
        paginationType: 'full',
        displayLength: 10,
        dom: "tp"
      };

      // [WIP] attempt to dyamically change displayLength (#rows) and turn on/off pagination controls
      // See: issues turning on/off pagination. see: https://datatables.net/manual/tech-notes/3

      $scope.usePagination = true;
      $scope.togglePagination = function () {
        $scope.usePagination = !$scope.usePagination;
        console.log("---> togglePagination: " + $scope.usePagination);
        if($scope.usePagination) {
          $scope.dtOptions.displayLength = 3;
          $scope.dtOptions.dom = "tp";
          console.log("---> use pagination: " + $scope.dtOptions.displayLength + ":" + $scope.dtOptions.dom);
        } else {
          $scope.dtOptions.displayLength = undefined;
          $scope.dtOptions.dom = "t";
        }
      };

      $scope.allItems = [
        {
          name: "Fred Flintstone",
          age: 57,
          address: "20 Dinosaur Way, Bedrock, Washingstone",
          birthMonth: 'February'
        },
        {
          name: "John Smith",
          age: 23,
          address: "415 East Main Street, Norfolk, Virginia",
          birthMonth: 'October'
        },
        {
          name: "Frank Livingston",
          age: 71,
          address: "234 Elm Street, Pittsburgh, Pennsylvania",
          birthMonth: 'March'
        },
        {
          name: "Judy Green",
          age: 21,
          address: "2 Apple Boulevard, Cincinatti, Ohio",
          birthMonth: 'December'
        },
        {
          name: "Pat Thomas",
          age: 19,
          address: "50 Second Street, New York, New York",
          birthMonth: 'February'
        },
        {
          name: "Linda McGovern",
          age: 32,
          address: "22 Oak Stree, Denver, Colorado",
          birthMonth: 'March'
        },
        {
          name: "Jim Brown",
          age: 55,
          address: "72 Bourbon Way. Nashville. Tennessee",
          birthMonth: 'March'
        },
        {
          name: "Holly Nichols",
          age: 34,
          address: "21 Jump Street, Hollywood, California",
          birthMonth: 'March'
        },
        {
          name: "Wilma Flintstone",
          age: 47,
          address: "20 Dinosaur Way, Bedrock, Washingstone",
          birthMonth: 'February'
        },
        {
          name: "Jane Smith",
          age: 22,
          address: "415 East Main Street, Norfolk, Virginia",
          birthMonth: 'April'
        },
        {
          name: "Liz Livingston",
          age: 65,
          address: "234 Elm Street, Pittsburgh, Pennsylvania",
          birthMonth: 'November'
        },
        {
          name: "Jim Green",
          age: 23,
          address: "2 Apple Boulevard, Cincinatti, Ohio",
          birthMonth: 'January'
        },
        {
          name: "Chris Thomas",
          age: 21,
          address: "50 Second Street, New York, New York",
          birthMonth: 'October'
        },
        {
          name: "Larry McGovern",
          age: 34,
          address: "22 Oak Stree, Denver, Colorado",
          birthMonth: 'September'
        },
        {
          name: "July Brown",
          age: 51,
          address: "72 Bourbon Way. Nashville. Tennessee",
          birthMonth: 'May'
        },
        {
          name: "Henry Nichols",
          age: 36,
          address: "21 Jump Street, Hollywood, California",
          birthMonth: 'March'
        },
      ];

      $scope.items = $scope.allItems;

      var matchesFilter = function (item, filter) {
        var match = true;

        if (filter.id === 'name') {
          match = item.name.match(filter.value) !== null;
        } else if (filter.id === 'age') {
          match = item.age === parseInt(filter.value);
        } else if (filter.id === 'address') {
          match = item.address.match(filter.value) !== null;
        } else if (filter.id === 'birthMonth') {
          match = item.birthMonth === filter.value;
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

      var filterChange = function (filters) {
        applyFilters(filters);
        $scope.toolbarConfig.filterConfig.resultsCount = $scope.items.length;
      };

      var performAction = function (action) {
        var selectedItems = $filter('filter')($scope.allItems, {selected: true});
        if(!selectedItems) {
          selectedItems = [];
        }
        $scope.actionsText = "Toolbar Action: " + action.name + " on " + selectedItems.length + " selected items\n" + $scope.actionsText;
      };

      var performTableAction = function (action, item) {
        $scope.actionsText = "Table Row Action on '" + item.name + "' : " + action.name + "\r\n" + $scope.actionsText;
      };

      function handleCheckBoxChange (item) {
        var selectedItems = $filter('filter')($scope.allItems, {selected: true});
        if (selectedItems) {
          $scope.toolbarConfig.filterConfig.selectedCount = selectedItems.length;
        }
      }

      $scope.filterConfig = {
        fields: [
          {
            id: 'name',
            title:  'Name',
            placeholder: 'Filter by Name...',
            filterType: 'text'
          },
          {
            id: 'age',
            title:  'Age',
            placeholder: 'Filter by Age...',
            filterType: 'text'
          },
          {
            id: 'address',
            title:  'Address',
            placeholder: 'Filter by Address...',
            filterType: 'text'
          },
          {
            id: 'birthMonth',
            title:  'Birth Month',
            placeholder: 'Filter by Birth Month...',
            filterType: 'select',
            filterValues: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
          }
        ],
        resultsCount: $scope.items.length,
        totalCount: $scope.allItems.length,
        appliedFilters: [],
        onFilterChange: filterChange
      };

      var monthVals = {
        'January': 1,
        'February': 2,
        'March': 3,
        'April': 4,
        'May': 5,
        'June': 6,
        'July': 7,
        'August': 8,
        'September': 9,
        'October': 10,
        'November': 11,
        'December': 12
      };

      $scope.toolbarActionsConfig = {
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
            actionFn: performAction,
            title: 'Do something similar'
          }
        ],
        actionsInclude: true
      };

      $scope.toolbarConfig = {
        filterConfig: $scope.filterConfig,
        sortConfig: $scope.sortConfig,
        actionsConfig: $scope.toolbarActionsConfig,
        isTableView: true
      };

      $scope.tableConfig = {
        onCheckBoxChange: handleCheckBoxChange,
        selectionMatchProp: "name",
        itemsAvailable: true
      };

      $scope.emptyStateConfig = {
        icon: 'pficon-warning-triangle-o',
        title: 'No Items Available',
        info: "This is the Empty State component. The goal of a empty state pattern is to provide a good first impression that helps users to achieve their goals. It should be used when a view is empty because no objects exists and you want to guide the user to perform specific actions.",
        helpLink: {
           label: 'For more information please see',
           urlLabel: 'pfExample',
           url : '#/api/patternfly.views.component:pfEmptyState'
        }
      };

      $scope.tableActionButtons = [
        {
          name: 'Action',
          title: 'Perform an action',
          actionFn: performTableAction
        }
      ];

      $scope.tableMenuActions = [
        {
          name: 'Action',
          title: 'Perform an action',
          actionFn: performTableAction
        },
        {
          name: 'Another Action',
          title: 'Do something else',
          actionFn: performTableAction
        },
        {
          name: 'Disabled Action',
          title: 'Unavailable action',
          actionFn: performTableAction,
          isDisabled: true
        },
        {
          name: 'Something Else',
          title: '',
          actionFn: performTableAction
        },
        {
          isSeparator: true
        },
        {
          name: 'Grouped Action 1',
          title: 'Do something',
          actionFn: performTableAction
        },
        {
          name: 'Grouped Action 2',
          title: 'Do something similar',
          actionFn: performTableAction
        }
      ];

      $scope.updateItemsAvailable = function () {
        if(!$scope.tableConfig.itemsAvailable) {
          $scope.toolbarConfig.filterConfig.resultsCount = 0;
          $scope.toolbarConfig.filterConfig.totalCount = 0;
          $scope.toolbarConfig.filterConfig.selectedCount = 0;
       } else {
          $scope.toolbarConfig.filterConfig.resultsCount = $scope.items.length;
          $scope.toolbarConfig.filterConfig.totalCount = $scope.allItems.length;
          handleCheckBoxChange();
        }
      };
    }
  ]);
  </file>
</example>
 */
