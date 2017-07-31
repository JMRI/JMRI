/**
 * @ngdoc directive
 * @name patternfly.views.directive:pfListView
 *
 * @description
 *   Directive for rendering a list view.
 *   Pass a customScope object containing any scope variables/functions you need to access from the transcluded source, access these
 *   via 'customScope' in your transcluded hmtl.
 *   <br><br>
 *   If using expanding rows, use a list-expanded-content element containing expandable content for each row.  Item data can be accessed inside list-expanded-content by using $parent.item.property.  For each item in the items array, the expansion can be disabled by setting disableRowExpansion to true on the item.
 *
 * @param {array} items Array of items to display in the list view. If an item in the array has a 'rowClass' field, the value of this field will be used as a class specified on the row (list-group-item).
 * @param {object} config Configuration settings for the list view:
 * <ul style='list-style-type: none'>
 * <li>.showSelectBox          - (boolean) Show item selection boxes for each item, default is true
 * <li>.selectItems            - (boolean) Allow row selection, default is false
 * <li>.dlbClick               - (boolean) Handle double clicking (item remains selected on a double click). Default is false.
 * <li>.dragEnabled            - (boolean) Enable drag and drop. Default is false.
 * <li>.dragEnd                - ( function() ) Function to call when the drag operation ended, default is none
 * <li>.dragMoved              - ( function() ) Function to call when the drag operation moved an element, default is none
 * <li>.dragStart              - ( function(item) ) Function to call when the drag operation started, default is none
 * <li>.multiSelect            - (boolean) Allow multiple row selections, selectItems must also be set, not applicable when dblClick is true. Default is false
 * <li>.useExpandingRows       - (boolean) Allow row expansion for each list item.
 * <li>.selectionMatchProp     - (string) Property of the items to use for determining matching, default is 'uuid'
 * <li>.selectedItems          - (array) Current set of selected items
 * <li>.checkDisabled          - ( function(item) ) Function to call to determine if an item is disabled, default is none
 * <li>.onCheckBoxChange       - ( function(item) ) Called to notify when a checkbox selection changes, default is none
 * <li>.onSelect               - ( function(item, event) ) Called to notify of item selection, default is none
 * <li>.onSelectionChange      - ( function(items) ) Called to notify when item selections change, default is none
 * <li>.onClick                - ( function(item, event) ) Called to notify when an item is clicked, default is none. Note: row expansion is the default behavior after onClick performed, but user can stop such default behavior by adding the sentence "return false;" to the end of onClick function body
 * <li>.onDblClick             - ( function(item, event) ) Called to notify when an item is double clicked, default is none
 * </ul>
 * @param {array} actionButtons List of action buttons in each row
 *   <ul style='list-style-type: none'>
 *     <li>.name - (String) The name of the action, displayed on the button
 *     <li>.title - (String) Optional title, used for the tooltip
 *     <li>.class - (String) Optional class to add to the action button
 *     <li>.include - (String) Optional include src for the button. Used for custom button layouts (icons, dropdowns, etc)
 *     <li>.includeClass - (String) Optional class to set on the include src div (only relevant when include is set).
 *     <li>.actionFn - (function(action)) Function to invoke when the action selected
 *   </ul>
 * @param {function (action, item))} enableButtonForItemFn function(action, item) Used to enabled/disable an action button based on the current item
 * @param {array} menuActions List of actions for dropdown menu in each row
 *   <ul style='list-style-type: none'>
 *     <li>.name - (String) The name of the action, displayed on the button
 *     <li>.title - (String) Optional title, used for the tooltip
 *     <li>.actionFn - (function(action)) Function to invoke when the action selected
 *     <li>.isVisible - (Boolean) set to false to hide the action
 *     <li>.isDisabled - (Boolean) set to true to disable the action
 *     <li>.isSeparator - (Boolean) set to true if this is a placeholder for a separator rather than an action
 *   </ul>
 * @param {function (item))} hideMenuForItemFn function(item) Used to hide all menu actions for a particular item
 * @param {function (item))} menuClassForItemFn function(item) Used to specify a class for an item's dropdown kebab
 * @param {function (action, item))} updateMenuActionForItemFn function(action, item) Used to update a menu action based on the current item
 * @param {object} customScope Object containing any variables/functions used by the transcluded html, access via customScope.<xxx>
 * @example
<example module="patternfly.views" deps="patternfly.utils">
  <file name="index.html">
    <div ng-controller="ViewCtrl" class="row example-container">
      <div class="col-md-12 list-view-container">
        <div pf-list-view class="example-list-view" id="exampleListView"
                          config="config" items="items"
                          action-buttons="actionButtons"
                          enable-button-for-item-fn="enableButtonForItemFn"
                          menu-actions="menuActions"
                          update-menu-action-for-item-fn="updateMenuActionForItemFn"
                          menu-class-for-item-fn="getMenuClass"
                          hide-menu-for-item-fn="hideMenuActions">
          <div class="list-view-pf-description">
            <div class="list-group-item-heading">
              {{item.name}}
            </div>
            <div class="list-group-item-text">
              {{item.address}}
            </div>
          </div>
          <div class="list-view-pf-additional-info">
            <div class="list-view-pf-additional-info-item">
              {{item.city}}
            </div>
            <div class="list-view-pf-additional-info-item">
              {{item.state}}
            </div>
          </div>
          <list-expanded-content>
           <div class="row">
            <div class="col-md-3">
              <div pf-donut-pct-chart config="exampleChartConfig" data="{'used': '350','total': '1000'}" center-label="'Percent Used'"></div>
            </div>
            <div class="col-md-9">
               <dl class="dl-horizontal">
                 <dt>Host</dt>
                 <dd>{{$parent.item.city}}</dd>
                 <dt>Admin</dt>
                 <dd>{{$parent.item.name}}</dd>
                 <dt>Time</dt>
                 <dd>January 15, 2016 10:45:11 AM</dd>
                 <dt>Severity</dt>
                 <dd>Warning</dd>
                 <dt>Cluster</dt>
                 <dd>Cluster 1</dd>
               </dl>
               <p>
                 Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod
                 tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,
                 quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
                 consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse
                 cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non
                 proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
               </p>
             </div>
           </div>
          </list-expanded-content>
        </div>
      </div>
      <hr class="col-md-12">
      <div class="col-md-12">
        <form role="form">
          <div class="form-group">
            <label>Selection</label>
            </br>
            <label class="radio-inline">
              <input type="radio" ng-model="selectType" value="checkbox" ng-change="updateSelectionType()">Checkbox</input>
            </label>
            <label class="radio-inline">
              <input type="radio" ng-model="selectType" value="row" ng-change="updateSelectionType()">Row</input>
            </label>
            <label class="radio-inline">
              <input type="radio" ng-model="selectType" value="none" ng-change="updateSelectionType()">None</input>
            </label>
          </div>
        </form>
      </div>
      <div class="col-md-12">
        <form role="form">
          <div class="form-group">
            <label class="checkbox-inline">
              <input type="checkbox" ng-model="config.dblClick">Double Click</input>
            </label>
            <label class="checkbox-inline">
              <input type="checkbox" ng-model="config.multiSelect" ng-disabled="config.dblClick">Multi Select</input>
            </label>
          </div>
        </form>
      </div>
      <div class="col-md-12">
        <form role="form">
          <div class="form-group">
            <label class="checkbox-inline">
              <input type="checkbox" ng-model="showDisabled">Show Disabled Rows</input>
            </label>
           <label class="checkbox-inline">
              <input type="checkbox" ng-model="config.useExpandingRows">Show Expanding Rows</input>
           </label>
          </div>
        </form>
      </div>
      <div class="col-md-12">
        <form role="form">
          <div class="form-group">
            <label class="checkbox-inline">
              <input type="checkbox" ng-model="config.dragEnabled">Drag and Drop</input>
            </label>
          </div>
        </form>
      </div>
      <div class="col-md-12">
        <label style="font-weight:normal;vertical-align:center;">Events: </label>
      </div>
      <div class="col-md-12">
        <textarea rows="10" class="col-md-12">{{eventText}}</textarea>
      </div>
    </div>
  </file>

  <file name="script.js">
 angular.module('patternfly.views').controller('ViewCtrl', ['$scope', '$templateCache',
      function ($scope, $templateCache) {
        $scope.eventText = '';
        var handleSelect = function (item, e) {
          $scope.eventText = item.name + ' selected\r\n' + $scope.eventText;
        };
        var handleSelectionChange = function (selectedItems, e) {
          $scope.eventText = selectedItems.length + ' items selected\r\n' + $scope.eventText;
        };
        var handleClick = function (item, e) {
          $scope.eventText = item.name + ' clicked\r\n' + $scope.eventText;
        };
        var handleDblClick = function (item, e) {
          $scope.eventText = item.name + ' double clicked\r\n' + $scope.eventText;
        };
        var handleCheckBoxChange = function (item, selected, e) {
          $scope.eventText = item.name + ' checked: ' + item.selected + '\r\n' + $scope.eventText;
        };

        var checkDisabledItem = function(item) {
          return $scope.showDisabled && (item.name === "John Smith");
        };

        var dragEnd = function() {
          $scope.eventText = 'drag end\r\n' + $scope.eventText;
        };
        var dragMoved = function() {
          var index = -1;

          for (var i = 0; i < $scope.items.length; i++) {
            if ($scope.items[i] === $scope.dragItem) {
              index = i;
            }
          }
          if (index >= 0) {
            $scope.items.splice(index, 1);
          }
          $scope.eventText = 'drag moved\r\n' + $scope.eventText;
        };
        var dragStart = function(item) {
          $scope.dragItem = item;
          $scope.eventText = item.name + ': drag start\r\n' + $scope.eventText;
        };

        $scope.enableButtonForItemFn = function(action, item) {
          return !((action.name ==='Action 2') && (item.name === "Frank Livingston")) &&
                 !(action.name === 'Start' && item.started);
        };

        $scope.updateMenuActionForItemFn = function(action, item) {
          if (action.name === 'Another Action') {
            action.isVisible = (item.name !== "John Smith");
          }
        };

        $scope.exampleChartConfig = {
          'chartId': 'pctChart',
          'units': 'GB',
          'thresholds': {
            'warning':'60',
            'error':'90'
          }
        };

        $scope.selectType = 'checkbox';
        $scope.updateSelectionType = function() {
          if ($scope.selectType === 'checkbox') {
            $scope.config.selectItems = false;
            $scope.config.showSelectBox = true;
          } else if ($scope.selectType === 'row') {
            $scope.config.selectItems = true;
            $scope.config.showSelectBox = false;
          } else {
            $scope.config.selectItems = false
            $scope.config.showSelectBox = false;
          }
        };

        $scope.showDisabled = false;

        $scope.config = {
         selectItems: false,
         multiSelect: false,
         dblClick: false,
         dragEnabled: false,
         dragEnd: dragEnd,
         dragMoved: dragMoved,
         dragStart: dragStart,
         selectionMatchProp: 'name',
         selectedItems: [],
         checkDisabled: checkDisabledItem,
         showSelectBox: true,
         useExpandingRows: false,
         onSelect: handleSelect,
         onSelectionChange: handleSelectionChange,
         onCheckBoxChange: handleCheckBoxChange,
         onClick: handleClick,
         onDblClick: handleDblClick
        };

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
            disableRowExpansion: true
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

        $scope.getMenuClass = function (item) {
          var menuClass = "";
          if (item.name === "Jim Brown") {
            menuClass = 'red';
          }
          return menuClass;
        };

        $scope.hideMenuActions = function (item) {
          return (item.name === "Marie Edwards");
        };

        var performAction = function (action, item) {
          $scope.eventText = item.name + " : " + action.name + "\r\n" + $scope.eventText;
        };

        var startServer = function (action, item) {
          $scope.eventText = item.name + " : " + action.name + "\r\n" + $scope.eventText;
          item.started = true;
        };

        var buttonInclude = '<span class="fa fa-plus"></span>{{actionButton.name}}';
        $templateCache.put('my-button-template', buttonInclude);

        var startButtonInclude = '<span ng-disabled="item.started">{{item.started ? "Starting" : "Start"}}</span>';
        $templateCache.put('start-button-template', startButtonInclude);

        $scope.actionButtons = [
          {
            name: 'Start',
            class: 'btn-primary',
            include: 'start-button-template',
            title: 'Start the server',
            actionFn: startServer
          },
          {
            name: 'Action 1',
            title: 'Perform an action',
            actionFn: performAction
          },
          {
            name: 'Action 2',
            title: 'Do something else',
            actionFn: performAction
          },
          {
            name: 'Action 3',
            include: 'my-button-template',
            title: 'Do something special',
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
angular.module('patternfly.views').directive('pfListView', function ($window, pfUtils) {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      config: '=?',
      items: '=',
      actionButtons: '=?',
      enableButtonForItemFn: '=?',
      menuActions: '=?',
      hideMenuForItemFn: '=?',
      menuClassForItemFn: '=?',
      updateMenuActionForItemFn: '=?',
      actions: '=?',
      updateActionForItemFn: '=?',
      customScope: '=?'
    },
    transclude: {
      expandedContent: '?listExpandedContent'
    },
    templateUrl: 'views/listview/list-view.html',
    controller:
      function ($scope, $element) {
        var setDropMenuLocation = function (parentDiv) {
          var dropButton = parentDiv.querySelector('.dropdown-toggle');
          var dropMenu =  parentDiv.querySelector('.dropdown-menu');
          var parentRect = $element[0].getBoundingClientRect();
          var buttonRect = dropButton.getBoundingClientRect();
          var menuRect = dropMenu.getBoundingClientRect();
          var menuTop = buttonRect.top - menuRect.height;
          var menuBottom = buttonRect.top + buttonRect.height + menuRect.height;

          if ((menuBottom <= parentRect.top + parentRect.height) || (menuTop < parentRect.top)) {
            $scope.dropdownClass = 'dropdown';
          } else {
            $scope.dropdownClass = 'dropup';
          }
        };

        $scope.defaultConfig = {
          selectItems: false,
          multiSelect: false,
          dblClick: false,
          dragEnabled: false,
          dragEnd: null,
          dragMoved: null,
          dragStart: null,
          selectionMatchProp: 'uuid',
          selectedItems: [],
          checkDisabled: false,
          useExpandingRows: false,
          showSelectBox: true,
          onSelect: null,
          onSelectionChange: null,
          onCheckBoxChange: null,
          onClick: null,
          onDblClick: null
        };

        $scope.config = pfUtils.merge($scope.defaultConfig, $scope.config);
        if ($scope.config.selectItems && $scope.config.showSelectBox) {
          throw new Error('pfListView - ' +
          'Illegal use of pListView directive! ' +
          'Cannot allow both select box and click selection in the same list view.');
        }
        $scope.dropdownClass = 'dropdown';

        $scope.handleButtonAction = function (action, item) {
          if (!$scope.checkDisabled(item) && action && action.actionFn && $scope.enableButtonForItem(action, item)) {
            action.actionFn(action, item);
          }
        };

        $scope.handleMenuAction = function (action, item) {
          if (!$scope.checkDisabled(item) && action && action.actionFn && (action.isDisabled !== true)) {
            action.actionFn(action, item);
          }
        };

        $scope.enableButtonForItem = function (action, item) {
          var enable = true;
          if (typeof $scope.enableButtonForItemFn === 'function') {
            return $scope.enableButtonForItemFn(action, item);
          }
          return enable;
        };

        $scope.updateActions = function (item) {
          if (typeof $scope.updateMenuActionForItemFn === 'function') {
            $scope.menuActions.forEach(function (action) {
              $scope.updateMenuActionForItemFn(action, item);
            });
          }
        };

        $scope.getMenuClassForItem = function (item) {
          var menuClass = '';
          if (angular.isFunction($scope.menuClassForItemFn)) {
            menuClass = $scope.menuClassForItemFn(item);
          }

          return menuClass;
        };

        $scope.hideMenuForItem = function (item) {
          var hideMenu = false;
          if (angular.isFunction($scope.hideMenuForItemFn)) {
            hideMenu = $scope.hideMenuForItemFn(item);
          }

          return hideMenu;
        };

        $scope.toggleItemExpansion = function (item) {
          item.isExpanded = !item.isExpanded;
        };

        $scope.setupActions = function (item, event) {
          // Ignore disabled items completely
          if ($scope.checkDisabled(item)) {
            return;
          }

          // update the actions based on the current item
          $scope.updateActions(item);

          $window.requestAnimationFrame(function () {
            var parentDiv = undefined;
            var nextElement;

            nextElement = event.target;
            while (nextElement && !parentDiv) {
              if (nextElement.className.indexOf('dropdown-kebab-pf') !== -1) {
                parentDiv = nextElement;
                if (nextElement.className.indexOf('open') !== -1) {
                  setDropMenuLocation (parentDiv);
                }
              }
              nextElement = nextElement.parentElement;
            }
          });
        };
      },

    link: function (scope, element, attrs) {
      attrs.$observe('config', function () {
        scope.config = pfUtils.merge(scope.defaultConfig, scope.config);
        if (!scope.config.selectItems) {
          scope.config.selectedItems = [];
        }
        if (!scope.config.multiSelect && scope.config.selectedItems && scope.config.selectedItems.length > 0) {
          scope.config.selectedItems = [scope.config.selectedItems[0]];
        }
      });

      scope.itemClick = function (e, item) {
        var alreadySelected;
        var selectionChanged = false;
        var continueEvent = true;
        var enableRowExpansion = scope.config && scope.config.useExpandingRows && item && !item.disableRowExpansion;

        // Ignore disabled item clicks completely
        if (scope.checkDisabled(item)) {
          return continueEvent;
        }

        if (scope.config && scope.config.selectItems && item) {
          if (scope.config.multiSelect && !scope.config.dblClick) {

            alreadySelected = _.find(scope.config.selectedItems, function (itemObj) {
              return itemObj === item;
            });

            if (alreadySelected) {
              // already selected so deselect
              scope.config.selectedItems = _.without(scope.config.selectedItems, item);
            } else {
              // add the item to the selected items
              scope.config.selectedItems.push(item);
              selectionChanged = true;
            }
          } else {
            if (scope.config.selectedItems[0] === item) {
              if (!scope.config.dblClick) {
                scope.config.selectedItems = [];
                selectionChanged = true;
              }
              continueEvent = false;
            } else {
              scope.config.selectedItems = [item];
              selectionChanged = true;
            }
          }

          if (selectionChanged && scope.config.onSelect) {
            scope.config.onSelect(item, e);
          }
          if (selectionChanged && scope.config.onSelectionChange) {
            scope.config.onSelectionChange(scope.config.selectedItems, e);
          }
        }
        if (scope.config.onClick) {
          if (scope.config.onClick(item, e) !== false && enableRowExpansion) {
            scope.toggleItemExpansion(item);
          }
        } else if (enableRowExpansion) {
          scope.toggleItemExpansion(item);
        }

        return continueEvent;
      };

      scope.dblClick = function (e, item) {
        // Ignore disabled item clicks completely
        if (scope.checkDisabled(item)) {
          return continueEvent;
        }

        if (scope.config.onDblClick) {
          scope.config.onDblClick(item, e);
        }
      };

      scope.checkBoxChange = function (item) {
        if (scope.config.onCheckBoxChange) {
          scope.config.onCheckBoxChange(item);
        }
      };

      scope.isSelected = function (item) {
        var matchProp = scope.config.selectionMatchProp;
        var selected = false;

        if (scope.config.showSelectBox) {
          selected = item.selected;
        } else if (scope.config.selectItems && scope.config.selectedItems.length) {
          selected = _.find(scope.config.selectedItems, function (itemObj) {
            return itemObj[matchProp] === item[matchProp];
          });
        }
        return selected;
      };

      scope.checkDisabled = function (item) {
        return scope.config.checkDisabled && scope.config.checkDisabled(item);
      };

      scope.dragEnd = function () {
        if (angular.isFunction(scope.config.dragEnd)) {
          scope.config.dragEnd();
        }
      };

      scope.dragMoved = function () {
        if (angular.isFunction(scope.config.dragMoved)) {
          scope.config.dragMoved();
        }
      };

      scope.isDragOriginal = function (item) {
        return (item === scope.dragItem);
      };

      scope.dragStart = function (item) {
        scope.dragItem = item;

        if (angular.isFunction(scope.config.dragStart)) {
          scope.config.dragStart(item);
        }
      };
    }
  };
});
