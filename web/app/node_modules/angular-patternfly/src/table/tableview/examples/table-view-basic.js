/**
  * @ngdoc directive
  * @name patternfly.table.component:pfTableView - Basic
  *
  * @description
  * Component for rendering a simple table view.<br><br>
  * See {@link patternfly.table.component:pfTableView%20-%20with%20Toolbar pfTableView - with Toolbar} for use with a Toolbar<br>
  * See {@link patternfly.toolbars.componenet:pfToolbar pfToolbar} for use in Toolbar View Switcher
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
 <div ng-controller="TableCtrl" class="row example-container">
   <div class="col-md-12">
     <pf-table-view id="exampleTableView"
          config="config"
          empty-state-config="emptyStateConfig"
          dt-options="dtOptions"
          colummns="colummns"
          items="items"
          action-buttons="actionButtons"
          menu-actions="menuActions">
     </pf-table-view>
   </div>
   <div class="col-md-12" style="padding-top: 12px;">
     <div class="form-group">
       <label class="checkbox-inline">
         <input type="checkbox" ng-model="config.itemsAvailable">Items Available</input>
       </label>
     </div>
   </div>
   <hr class="col-md-12">
   <div class="col-md-12">
         <div class="col-md-12" style="padding-top: 12px;">
           <label style="font-weight:normal;vertical-align:center;">Events: </label>
         </div>
         <div class="col-md-12">
           <textarea rows="10" class="col-md-12">{{eventText}}</textarea>
         </div>
   </div>
 </file>

 <file name="modules.js">
   angular.module('patternfly.tableview.demo', ['patternfly.views','patternfly.table']);
 </file>

 <file name="script.js">
 angular.module('patternfly.tableview.demo').controller('TableCtrl', ['$scope',
 function ($scope) {
        $scope.dtOptions = {
          order: [[2, "asc"]],
        };

        $scope.colummns = [
          { header: "Name", itemField: "name" },
          { header: "Address", itemField: "address"},
          { header: "City", itemField: "city" },
          { header: "State", itemField: "state"}
        ];

        $scope.items = [
          {
            name: "Fred Flintstone",
            address: "20 Dinosaur Way",
            city: "Bedrock",
            state: "Washingstone"
          },
          {
            name: "John Smith",
            address: "415 East Main Street",
            city: "Norfolk",
            state: "Virginia",
          },
          {
            name: "Frank Livingston",
            address: "234 Elm Street",
            city: "Pittsburgh",
            state: "Pennsylvania"
          },
          {
            name: "Linda McGovern",
            address: "22 Oak Street",
            city: "Denver",
            state: "Colorado"
          },
          {
            name: "Jim Brown",
            address: "72 Bourbon Way",
            city: "Nashville",
            state: "Tennessee"
          },
          {
            name: "Holly Nichols",
            address: "21 Jump Street",
            city: "Hollywood",
            state: "California"
          },
          {
            name: "Marie Edwards",
            address: "17 Cross Street",
            city: "Boston",
            state: "Massachusetts"
          },
          {
            name: "Pat Thomas",
            address: "50 Second Street",
            city: "New York",
            state: "New York"
          },
        ];

        $scope.eventText = "";

        $scope.config = {
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

        function handleCheckBoxChange (item) {
          $scope.eventText = item.name + ' checked: ' + item.selected + '\r\n' + $scope.eventText;
        };

        var performAction = function (action, item) {
          $scope.eventText = item.name + " : " + action.name + "\r\n" + $scope.eventText;
        };

        $scope.actionButtons = [
          {
            name: 'Action',
            title: 'Perform an action',
            actionFn: performAction
          }
        ];

        $scope.menuActions = [
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
        ];
      }
    ]);
  </file>
</example>
*/
