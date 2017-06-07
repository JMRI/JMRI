(function () {
  'use strict';

  angular.module('patternfly.canvas')
    .directive('nodeToolbar', nodeToolbarDirective);

  function nodeToolbarDirective ($document) {
    var directive = {
      restrict: 'E',
      scope: {
        node: '=',
        nodeActions: '=',
      },
      controller: NodeToolbarController,
      templateUrl: 'canvas-view/canvas/node-toolbar.html',
      controllerAs: 'vm',
      bindToController: true,
    };

    return directive;

    function NodeToolbarController ($scope) {
      var vm = this;
      vm.selectedAction = "none";

      $scope.actionIconClicked = function (action) {
        vm.selectedAction = action;
        $scope.$emit('nodeActionClicked', {'action': action, 'node': vm.node});
      };

      $scope.close = function () {
        vm.selectedAction = 'none';
        $scope.$emit('nodeActionClosed');
      };
    }
  }
})();
