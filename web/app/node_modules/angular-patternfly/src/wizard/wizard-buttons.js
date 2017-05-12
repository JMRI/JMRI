(function () {
  'use strict';
  function pfWizardButtonDirective (action) {
    angular.module('patternfly.wizard')
      .directive(action, function () {
        return {
          restrict: 'A',
          require: '^pf-wizard',
          scope: {
            callback: "=?"
          },
          link: function ($scope, $element, $attrs, wizard) {
            $element.on("click", function (e) {
              e.preventDefault();
              $scope.$apply(function () {
                // scope apply in button module
                $scope.$eval($attrs[action]);
                wizard[action.replace("pfWiz", "").toLowerCase()]($scope.callback);
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
