/** @ngdoc directive
* @name patternfly.wizard.component:pfWizardSubstep
* @restrict E
*
* @description
* Component for rendering a Wizard substep.  Each substep must be a child of a pf-wizardstep in a pf-wizard directive.
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
angular.module('patternfly.wizard').component('pfWizardSubstep', {
  transclude: true,
  bindings: {
    stepTitle: '@',
    stepId: '@',
    stepPriority: '@',
    nextEnabled: '<?',
    prevEnabled: '<?',
    okToNavAway: '<?',
    allowClickNav: '<?',
    disabled: '@?wzDisabled',
    description: '@',
    wizardData: '=',
    onShow: '=?',
    showReviewDetails: '@?',
    reviewTemplate: '@?'
  },
  templateUrl: 'wizard/wizard-substep.html',
  controller: function ($scope) {
    'use strict';
    var ctrl = this;

    ctrl.$onInit = function () {
      var findWizardStep = function (scope) {
        var wizardStep;

        if (scope) {
          if (angular.isDefined(scope.wizardStep)) {
            wizardStep = scope.wizardStep;
          } else {
            wizardStep = findWizardStep(scope.$parent);
          }
        }

        return wizardStep;
      };

      ctrl.step = findWizardStep($scope);

      if (angular.isUndefined(ctrl.nextEnabled)) {
        ctrl.nextEnabled = true;
      }
      if (angular.isUndefined(ctrl.prevEnabled)) {
        ctrl.prevEnabled = true;
      }
      if (angular.isUndefined(ctrl.showReviewDetails)) {
        ctrl.showReviewDetails = false;
      }
      if (angular.isUndefined(ctrl.stepPriority)) {
        ctrl.stepPriority = 999;
      } else {
        ctrl.stepPriority = parseInt(ctrl.stepPriority);
      }
      if (angular.isUndefined(ctrl.okToNavAway)) {
        ctrl.okToNavAway = true;
      }
      if (angular.isUndefined(ctrl.allowClickNav)) {
        ctrl.allowClickNav = true;
      }


      ctrl.step.nextEnabled = ctrl.nextEnabled;
      ctrl.step.prevEnabled = ctrl.prevEnabled;
      ctrl.step.okToNavAway = ctrl.okToNavAway;
      ctrl.step.allowClickNav = ctrl.allowClickNav;

      ctrl.title = ctrl.stepTitle;
      ctrl.step.addStep(ctrl);
    };

    ctrl.$onChanges = function (changesObj) {
      if (!ctrl.step) {
        return;
      }

      if (changesObj.nextEnabled) {
        ctrl.step.nextEnabled = changesObj.nextEnabled.currentValue;
      }

      if (changesObj.prevEnabled) {
        ctrl.step.prevEnabled = changesObj.prevEnabled.currentValue;
      }

      if (changesObj.okToNavAway) {
        ctrl.step.okToNavAway = changesObj.okToNavAway.currentValue;
      }

      if (changesObj.allowClickNav) {
        ctrl.step.allowClickNav = changesObj.allowClickNav.currentValue;
      }
    };

    ctrl.isPrevEnabled = function () {
      var enabled = angular.isUndefined(ctrl.prevEnabled) || ctrl.prevEnabled;
      if (ctrl.substeps) {
        angular.forEach(ctrl.getEnabledSteps(), function (step) {
          enabled = enabled && step.prevEnabled;
        });
      }
      return enabled;
    };
  }
});
