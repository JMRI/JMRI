/* eslint-disable */
(function() {
  'use strict';

  angular.module('patternfly.canvas')
    .directive('toolboxItems', toolboxItemsDirective);

  function toolboxItemsDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        items: '=',
        startDragCallback: '=',
        clickCallback: '=',
        searchText: '='
      },
      controller: toolboxItemsController,
      templateUrl: 'canvas-view/canvas-editor/toolbox-items.html',
      controllerAs: 'vm',
      bindToController: true
    };

    return directive;

    function toolboxItemsController() {
      var vm = this;

      vm.clickCallbackfmDir = function(item) {
        if (!item.disableInToolbox) {
          vm.clickCallback(item);
        }
      };

      vm.startDragCallbackfmDir = function(event, ui, item) {
        vm.startDragCallback(event, ui, item);
      };
    }
  }
})();
