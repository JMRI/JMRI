(function () {
  'use strict';
  function pfWizardButtonComponent (action) {
    angular.module('patternfly.wizard')
      .component(action, {
        bindings: {
          callback: "=?"
        },
        controller: function ($element, $scope) {
          var ctrl = this;

          var findWizard = function (scope) {
            var wizard;

            if (scope) {
              if (angular.isDefined(scope.wizard)) {
                wizard = scope.wizard;
              } else {
                wizard = findWizard(scope.$parent);
              }
            }

            return wizard;
          };

          ctrl.$onInit = function () {
            $scope.wizard = findWizard($scope);
          };

          ctrl.$postLink = function () {
            $element.on("click", function (e) {
              e.preventDefault();
              $scope.$apply(function () {
                // scope apply in button module
                $scope.wizard[action.replace("pfWiz", "").toLowerCase()]($scope.callback);
              });
            });
          };
        }
      });
  }

  pfWizardButtonComponent('pfWizNext');
  pfWizardButtonComponent('pfWizPrevious');
  pfWizardButtonComponent('pfWizFinish');
  pfWizardButtonComponent('pfWizCancel');
  pfWizardButtonComponent('pfWizReset');
})();
