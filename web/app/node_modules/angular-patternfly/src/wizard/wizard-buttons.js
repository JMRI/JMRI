(function () {
  'use strict';
  function pfWizardButtonDirective (action) {
    angular.module('patternfly.wizard')
      .directive(action, function () {
        return {
          restrict: 'A',
          scope: {
            callback: "=?"
          },
          controller: function ($scope) {
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
            $scope.wizard = findWizard($scope);
          },
          link: function ($scope, $element, $attrs) {
            $element.on("click", function (e) {
              e.preventDefault();
              $scope.$apply(function () {
                // scope apply in button module
                $scope.$eval($attrs[action]);
                $scope.wizard[action.replace("pfWiz", "").toLowerCase()]($scope.callback);
              });
            });
          }
        };
      });
  }

  pfWizardButtonDirective('pfWizNext');
  pfWizardButtonDirective('pfWizPrevious');
  pfWizardButtonDirective('pfWizFinish');
  pfWizardButtonDirective('pfWizCancel');
  pfWizardButtonDirective('pfWizReset');
})();
