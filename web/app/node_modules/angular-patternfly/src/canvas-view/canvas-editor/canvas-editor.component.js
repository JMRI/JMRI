(function () {
  'use strict';

  angular.module('patternfly.canvas').component('pfCanvasEditor', {

    bindings: {
      chartDataModel: "=",
      chartViewModel: "=?",
      toolboxTabs: "=",
      readOnly: '<?'
    },
    transclude: true,
    templateUrl: "canvas-view/canvas-editor/canvas-editor.html",
    controller: function ($timeout) {
      var ctrl = this;
      var newNodeCount = 0;
      var prevClickedOnChart, prevInConnectingMode;

      ctrl.$onInit = function () {
        ctrl.toolboxVisible = false;
        ctrl.hideConnectors = false;
        ctrl.draggedItem = null;
      };

      // need to get these in next digest cycle, after pfCanvas sets chartViewModel
      $timeout(function () {
        prevClickedOnChart = ctrl.chartViewModel.clickedOnChart;
        prevInConnectingMode = ctrl.chartViewModel.inConnectingMode;
      });

      ctrl.$doCheck = function () {
        if (angular.isDefined(prevClickedOnChart) && angular.isDefined(prevInConnectingMode)) {
          if (!angular.equals(ctrl.chartViewModel.clickedOnChart, prevClickedOnChart)) {
            if (ctrl.chartViewModel.clickedOnChart) {
              ctrl.chartViewModel.clickedOnChart = false;
              ctrl.hideToolbox();
            }
            prevClickedOnChart = ctrl.chartViewModel.clickedOnChart;
          }
          if (!angular.equals(ctrl.chartViewModel.inConnectingMode, prevInConnectingMode)) {
            if (ctrl.chartViewModel.inConnectingMode) {
              ctrl.hideConnectors = false;
              ctrl.hideToolbox();
            }
            prevInConnectingMode = ctrl.chartViewModel.inConnectingMode;
          }
        }
      };

      ctrl.addNodeToCanvas = function (newNode) {
        ctrl.chartViewModel.addNode(newNode);
      };

      /*** Toolbox Methods ***/

      ctrl.showToolbox = function () {
        ctrl.toolboxVisible = true;
        // add class to subtabs to apply PF style and
        // focus to filter input box

        $timeout(function () {
          angular.element(".subtabs>ul").addClass('nav-tabs-pf');
          angular.element("#filterFld").focus();
        });
      };

      ctrl.hideToolbox = function () {
        ctrl.toolboxVisible = false;
      };

      ctrl.toggleToolbox = function () {
        if (!ctrl.readOnly && !ctrl.chartViewModel.inConnectingMode) {
          if (ctrl.toolboxVisible === true) {
            ctrl.hideToolbox();
          } else {
            ctrl.showToolbox();
          }
        }
      };

      ctrl.tabClicked = function () {
        angular.element("#filterFld").focus();
      };

      /*** Toolbox ***/

      ctrl.startCallback = function (event, ui, item) {
        ctrl.draggedItem = item;
      };

      ctrl.dropCallback = function (event, ui) {
        var newNode = angular.copy(ctrl.draggedItem);
        newNodeCount++;
        newNode.x = event.clientX - 600;
        newNode.y = event.clientY - 200;
        newNode.backgroundColor = newNode.backgroundColor ? newNode.backgroundColor : '#fff';

        ctrl.chartViewModel.addNode(newNode);
      };

      ctrl.addNodeByClick = function (item) {
        var newNode = angular.copy(item);
        newNodeCount++;
        newNode.x = 250 + (newNodeCount * 4 + 160);
        newNode.y = 200 + (newNodeCount * 4 + 160);
        newNode.backgroundColor = newNode.backgroundColor ? newNode.backgroundColor : '#fff';

        ctrl.chartViewModel.addNode(newNode);
      };

      ctrl.tabClicked = function () {
        angular.element("#filterFld").focus();
      };

      ctrl.activeTab = function () {
        return ctrl.toolboxTabs.filter(function (tab) {
          return tab.active;
        })[0];
      };

      ctrl.activeSubTab = function () {
        var activeTab = ctrl.activeTab();
        if (activeTab && activeTab.subtabs) {
          return activeTab.subtabs.filter(function (subtab) {
            return subtab.active;
          })[0];
        }
      };

      ctrl.activeSubSubTab = function () {
        var activeSubTab = ctrl.activeSubTab();
        if (activeSubTab && activeSubTab.subtabs) {
          return activeSubTab.subtabs.filter(function (subsubtab) {
            return subsubtab.active;
          })[0];
        }
      };

      /*** Zoom ***/

      ctrl.maxZoom = function () {
        if (ctrl.chartViewModel && ctrl.chartViewModel.zoom) {
          return ctrl.chartViewModel.zoom.isMax();
        }

        return false;
      };

      ctrl.minZoom = function () {
        if (ctrl.chartViewModel && ctrl.chartViewModel.zoom) {
          return ctrl.chartViewModel.zoom.isMin();
        }

        return false;
      };

      ctrl.zoomIn = function () {
        ctrl.chartViewModel.zoom.in();
      };

      ctrl.zoomOut = function () {
        ctrl.chartViewModel.zoom.out();
      };
    } // controller
  }); // module
})();
