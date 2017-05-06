/** @ngdoc directive
* @name patternfly.wizard.directive:pfWizardSubstep
*
* @description
* Directive for rendering a Wizard substep.  Each substep must be a child of a pf-wizardstep in a pf-wizard directive.
*
* @param {string} stepTitle The step title displayed in the header and used for the review screen when displayed
* @param {string} stepId  Sets the text identifier of the step
* @param {number} stepPriority  This sets the priority of this wizard step relative to other wizard steps.  They should be numbered sequentially in the order they should be viewed.
* @param {boolean=} nextEnabled Sets whether the next button should be enabled when this step is first displayed
* @param {boolean=} prevEnabled Sets whether the back button should be enabled when this step is first displayed
* @param {boolean=} wzDisabled Disables the wizard when this page is shown
* @param {boolean} okToNavAway Sets whether or not it's ok for the user to leave this page
* @param {boolean=} allowClickNav Sets whether the user can click on the numeric step indicators to navigate directly to this step
* @param {string=} description The step description
* @param {object} wizardData Data passed to the step that is shared by the entire wizard
* @param {function()=} onShow The function called when the wizard shows this step
* @param {boolean=} showReviewDetails Indicators whether the review information should be expanded by default when the review step is reached
* @param {string=} reviewTemplate The template that should be used for the review details screen
*/
angular.module('patternfly.wizard').directive('pfWizardSubstep', function () {
  'use strict';

  return {
    restrict: 'A',
    transclude: true,
    scope: {
      stepTitle: '@',
      stepId: '@',
      stepPriority: '@',
      nextEnabled: '=?',
      prevEnabled: '=?',
      okToNavAway: '=?',
      allowClickNav: '=?',
      disabled: '@?wzDisabled',
      description: '@',
      wizardData: '=',
      onShow: '=?',
      showReviewDetails: '@?',
      reviewTemplate: '@?'
    },
    require: '^pf-wizard-step',
    templateUrl: 'wizard/wizard-substep.html',
    controller: function ($scope) {
      if (angular.isUndefined($scope.nextEnabled)) {
        $scope.nextEnabled = true;
      }
      if (angular.isUndefined($scope.prevEnabled)) {
        $scope.prevEnabled = true;
      }
      if (angular.isUndefined($scope.showReviewDetails)) {
        $scope.showReviewDetails = false;
      }
      if (angular.isUndefined($scope.stepPriority)) {
        $scope.stepPriority = 999;
      } else {
        $scope.stepPriority = parseInt($scope.stepPriority);
      }
      if (angular.isUndefined($scope.okToNavAway)) {
        $scope.okToNavAway = true;
      }
      if (angular.isUndefined($scope.allowClickNav)) {
        $scope.allowClickNav = true;
      }

      $scope.isPrevEnabled = function () {
        var enabled = angular.isUndefined($scope.prevEnabled) || $scope.prevEnabled;
        if ($scope.substeps) {
          angular.forEach($scope.getEnabledSteps(), function (step) {
            enabled = enabled && step.prevEnabled;
          });
        }
        return enabled;
      };

    },
    link: function ($scope, $element, $attrs, step) {
      $scope.title = $scope.stepTitle;
      step.addStep($scope);
    }
  };
});
