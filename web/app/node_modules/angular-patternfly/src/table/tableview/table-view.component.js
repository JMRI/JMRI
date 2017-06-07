angular.module('patternfly.table').component('pfTableView', {
  bindings: {
    config: '<?',
    dtOptions: '<?',
    colummns: '<',
    items: '<',
    actionButtons: '<?',
    menuActions: '<?',
    emptyStateConfig: '=?'
  },
  templateUrl: 'table/tableview/table-view.html',
  controller: function (DTOptionsBuilder, DTColumnDefBuilder, $element, pfUtils, $log, $filter, $timeout) {
    'use strict';
    var ctrl = this, prevDtOptions, prevItems;

    // Once datatables is out of active development I'll remove log statements
    ctrl.debug = false;

    ctrl.selectAll = false;
    ctrl.dtInstance = {};

    ctrl.defaultDtOptions = {
      autoWidth: false,
      destroy: true,
      order: [[1, "asc"]],
      dom: "t",
      select: {
        selector: 'td:first-child input[type="checkbox"]',
        style: 'multi'
      }
    };

    ctrl.defaultConfig = {
      selectionMatchProp: 'uuid',
      onCheckBoxChange: null
    };

    ctrl.$onInit = function () {

      if (ctrl.debug) {
        $log.debug("$onInit");
      }

      if (angular.isUndefined(ctrl.dtOptions)) {
        ctrl.dtOptions = {};
      }
      if (angular.isUndefined(ctrl.config)) {
        ctrl.config = {};
      }

      ctrl.updateConfigOptions();

      setColumnDefs();
    };

    ctrl.updateConfigOptions = function () {
      var col, props = "";

      if (ctrl.debug) {
        $log.debug("  updateConfigOptions");
      }

      if (angular.isDefined(ctrl.dtOptions) && angular.isDefined(ctrl.dtOptions.displayLength)) {
        ctrl.dtOptions.displayLength = Number(ctrl.dtOptions.displayLength);
      }

      // Need to deep watch changes in dtOptions and items
      prevDtOptions = angular.copy(ctrl.dtOptions);
      prevItems = angular.copy(ctrl.items);

      // Setting bound variables to new variables loses it's one way binding
      //   ctrl.dtOptions = pfUtils.merge(ctrl.defaultDtOptions, ctrl.dtOptions);
      //   ctrl.config = pfUtils.merge(ctrl.defaultConfig, ctrl.config);

      // Instead, use _.defaults to update the existing variable
      _.defaults(ctrl.dtOptions, ctrl.defaultDtOptions);
      _.defaults(ctrl.config, ctrl.defaultConfig);
      // may need to use _.defaultsDeep, but not currently available in
      // lodash-amd a-pf is using

      if (!validSelectionMatchProp()) {
        angular.forEach(ctrl.colummns, function (col) {
          if (props.length === 0) {
            props = col.itemField;
          } else {
            props += ", " + col.itemField;
          }
        });
        throw new Error("pfTableView - " +
          "config.selectionMatchProp '" + ctrl.config.selectionMatchProp +
          "' does not match any property in 'config.colummns'! Please set config.selectionMatchProp " +
          "to one of these properties: " + props);
      }

      if (ctrl.items.length === 0) {
        ctrl.config.itemsAvailable = false;
      }
    };

    ctrl.dtInstanceCallback = function (_dtInstance) {
      var oTable, rows;
      if (ctrl.debug) {
        $log.debug("--> dtInstanceCallback");
      }

      ctrl.dtInstance = _dtInstance;
      listenForDraw();
      selectRowsByChecked();
    };

    ctrl.$onChanges = function (changesObj) {
      if (ctrl.debug) {
        $log.debug("$onChanges");
      }
      if ((changesObj.config && !changesObj.config.isFirstChange()) ) {
        if (ctrl.debug) {
          $log.debug("...updateConfigOptions");
        }
        ctrl.updateConfigOptions();
      }
    };

    ctrl.$doCheck = function () {
      if (ctrl.debug) {
        $log.debug("$doCheck");
      }
      // do a deep compare on dtOptions and items
      if (!angular.equals(ctrl.dtOptions, prevDtOptions)) {
        if (ctrl.debug) {
          $log.debug("  dtOptions !== prevDtOptions");
        }
        ctrl.updateConfigOptions();
      }
      if (!angular.equals(ctrl.items, prevItems)) {
        if (ctrl.debug) {
          $log.debug("  items !== prevItems");
        }
        prevItems = angular.copy(ctrl.items);
        //$timeout(function () {
        selectRowsByChecked();
        //});
      }
    };

    ctrl.$postLink = function () {
      if (ctrl.debug) {
        $log.debug(" $postLink");
      }
    };

    ctrl.$onDestroy = function () {
      if (ctrl.debug) {
        $log.debug(" $onDestroy");
      }
      ctrl.dtInstance = {};
    };

    function setColumnDefs () {
      var i = 0, actnBtns = 1;
      var item, prop;

      // add checkbox col, not sortable
      ctrl.dtColumnDefs = [ DTColumnDefBuilder.newColumnDef(i++).notSortable() ];
      // add column def. for each property of an item
      item = ctrl.items[0];
      for (prop in item) {
        if (item.hasOwnProperty(prop) && ctrl.isColItemFld(prop)) {
          ctrl.dtColumnDefs.push(DTColumnDefBuilder.newColumnDef(i++));
          // Determine selectionMatchProp column number
          if (ctrl.config.selectionMatchProp === prop) {
            ctrl.selectionMatchPropColNum = (i - 1);
          }
        }
      }
      // add actions col.
      if (ctrl.actionButtons && ctrl.actionButtons.length > 0) {
        for (actnBtns = 1; actnBtns <= ctrl.actionButtons.length; actnBtns++) {
          ctrl.dtColumnDefs.push(DTColumnDefBuilder.newColumnDef(i++).notSortable());
        }
      }
      if (ctrl.menuActions && ctrl.menuActions.length > 0) {
        ctrl.dtColumnDefs.push(DTColumnDefBuilder.newColumnDef(i++).notSortable());
      }
    }

    function listenForDraw () {
      var oTable;
      var dtInstance = ctrl.dtInstance;
      if (dtInstance && dtInstance.dataTable) {
        oTable = dtInstance.dataTable;
        ctrl.tableId = oTable[0].id;
        oTable.on('draw.dt', function () {
          if (ctrl.debug) {
            $log.debug("--> redraw");
          }
          selectRowsByChecked();
        });
      }
    }

    function validSelectionMatchProp () {
      var retVal = false, prop;
      var item = ctrl.items[0];

      if (!ctrl.items || ctrl.items.length === 0) {
        return true;    //ok to pass in empty items array
      }

      for (prop in item) {
        if (item.hasOwnProperty(prop)) {   //need this 'if' for eslint
          if (ctrl.config.selectionMatchProp === prop) {
            retVal = true;
          }
        }
      }
      return retVal;
    }
    /*
     *   Checkbox Selections
     */

    ctrl.toggleAll = function () {
      var item;
      var visibleRows = getVisibleRows();
      angular.forEach(visibleRows, function (row) {
        item = getItemFromRow(row);
        if (item.selected !== ctrl.selectAll) {
          item.selected = ctrl.selectAll;
          if (ctrl.config && ctrl.config.onCheckBoxChange) {
            ctrl.config.onCheckBoxChange(item);
          }
        }
      });
    };

    ctrl.toggleOne = function (item) {
      if (ctrl.config && ctrl.config.onCheckBoxChange) {
        ctrl.config.onCheckBoxChange(item);
      }
    };

    function getItemFromRow (matchPropValue) {
      var item, retVals;
      var filterObj = {};
      filterObj[ctrl.config.selectionMatchProp] = matchPropValue;
      retVals = $filter('filter')(ctrl.items, filterObj);

      if (retVals && retVals.length === 1) {
        item = retVals[0];
      }

      return item;
    }

    function selectRowsByChecked () {
      $timeout(function () {
        var oTable, rows, checked;

        oTable = ctrl.dtInstance.DataTable;

        if (ctrl.debug) {
          $log.debug("  selectRowsByChecked");
        }

        if (angular.isUndefined(oTable)) {
          return;
        }

        if (ctrl.debug) {
          $log.debug("  ...oTable defined");
        }

        // deselect all
        rows = oTable.rows();
        rows.deselect();

        // select those with checked checkboxes
        rows = oTable.rows( function ( idx, data, node ) {
          //         row      td     input type=checkbox
          checked = node.children[0].children[0].checked;
          return checked;
        });

        if (ctrl.debug) {
          $log.debug("   ... #checkedRows = " + rows[0].length);
        }

        if (rows[0].length > 0) {
          rows.select();
        }
        setSelectAllCheckbox();
      });
    }

    function setSelectAllCheckbox () {
      var numVisibleRows, numCheckedRows;

      if (ctrl.debug) {
        $log.debug("  setSelectAllCheckbox");
      }

      numVisibleRows = getVisibleRows().length;
      numCheckedRows = document.querySelectorAll("#" + ctrl.tableId + " tbody tr.even.selected").length +
                       document.querySelectorAll("#" + ctrl.tableId + " tbody tr.odd.selected").length;
      ctrl.selectAll = (numVisibleRows === numCheckedRows);
    }

    function getVisibleRows () {
      // Returns an array of visible 'selectionMatchProp' values
      // Ex. if selectionMatchProp === 'name' & selectionMatchPropColNum === 1 &
      //        page length === 3
      //     returns ['Mary Jane', 'Fred Flinstone', 'Frank Livingston']
      //
      var i, rowData, visibleRows = new Array();
      var oTable = ctrl.dtInstance.dataTable;

      var anNodes = document.querySelectorAll("#" + ctrl.tableId + "  tbody tr");

      for (i = 0; i < anNodes.length; ++i) {
        rowData = oTable.fnGetData(anNodes[i]);
        if (rowData !== null) {
          visibleRows.push(rowData[ctrl.selectionMatchPropColNum]);
        }
      }

      if (ctrl.debug) {
        $log.debug("    getVisibleRows (" + visibleRows.length + ")");
      }

      return visibleRows;
    }

    /*
     *   Action Buttons and Menus
     */

    ctrl.handleButtonAction = function (action, item) {
      if (action && action.actionFn) {
        action.actionFn(action, item);
      }
    };

    ctrl.isColItemFld = function (key) {
      var retVal = false;
      var tableCol = $filter('filter')(ctrl.colummns, {itemField: key});

      if (tableCol && tableCol.length === 1) {
        retVal = true;
      }

      return retVal;
    };

    ctrl.areActions = function () {
      return (ctrl.actionButtons && ctrl.actionButtons.length > 0) ||
        (ctrl.menuActions && ctrl.menuActions.length > 0);
    };

    ctrl.calcActionsColspan = function () {
      var colspan = 0;

      if (ctrl.actionButtons && ctrl.actionButtons.length > 0) {
        colspan += ctrl.actionButtons.length;
      }

      if (ctrl.menuActions && ctrl.menuActions.length > 0) {
        colspan += 1;
      }

      return colspan;
    };

    ctrl.handleMenuAction = function (action, item) {
      if (!ctrl.checkDisabled(item) && action && action.actionFn && (action.isDisabled !== true)) {
        action.actionFn(action, item);
      }
    };

    ctrl.setupActions = function (item, event) {
      /* Ignore disabled items completely
       if (ctrl.checkDisabled(item)) {
       return;
       }*/

      // update the actions based on the current item
      // $scope.updateActions(item);

      $timeout(function () {
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

    ctrl.checkDisabled = function (item) {
      return false;
    };

    function setDropMenuLocation (parentDiv) {
      var dropButton = parentDiv.querySelector('.dropdown-toggle');
      var dropMenu =  parentDiv.querySelector('.dropdown-menu');
      var parentRect = $element[0].getBoundingClientRect();
      var buttonRect = dropButton.getBoundingClientRect();
      var menuRect = dropMenu.getBoundingClientRect();
      var menuTop = buttonRect.top - menuRect.height;
      var menuBottom = buttonRect.top + buttonRect.height + menuRect.height;

      if ((menuBottom <= parentRect.top + parentRect.height) || (menuTop < parentRect.top)) {
        ctrl.dropdownClass = 'dropdown';
      } else {
        ctrl.dropdownClass = 'dropup';
      }
    }
  }
});
