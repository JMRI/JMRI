/**
 * @ngdoc directive
 * @name patternfly.wizard.directive:pfWizardReviewPage
 *
 * @description
 * Directive for rendering a Wizard Review Page - should only be used within a wizard.
 *
 * @param {boolean} shown Value watched internally by the wizard review page to know when it is visible.
 * @param {object} wizardData  Sets the internal content of the review page to apply wizard data to the review templates.
 *
 */
angular.module('patternfly.wizard').directive('pfWizardReviewPage', function () {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      shown: '=',
      wizardData: "="
    },
    templateUrl: 'wizard/wizard-review-page.html',
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

      $scope.wizard = findWizard($scope.$parent);

      $scope.toggleShowReviewDetails = function (step) {
        if (step.showReviewDetails === true) {
          step.showReviewDetails = false;
        } else {
          step.showReviewDetails = true;
        }
      };
      $scope.getSubStepNumber = function (step, substep) {
        return step.getStepDisplayNumber(substep);
      };
      $scope.getReviewSubSteps = function (reviewStep) {
        return reviewStep.getReviewSteps();
      };
      $scope.reviewSteps = [];
      $scope.updateReviewSteps = function (wizard) {
        $scope.reviewSteps = wizard.getReviewSteps();
      };
    },
    link: function ($scope, $element, $attrs) {
      $scope.$watch('shown', function (value) {
        if (value) {
          $scope.updateReviewSteps($scope.wizard);
        }
      });
    }
  };
});
