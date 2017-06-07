/**
 * @ngdoc directive
 * @name patternfly.wizard.component:pfWizardStep
 * @restrict E
 *
 * @description
 * Component for rendering a Wizard step.  Each step can stand alone or have substeps.  This directive can only be used as a child of pf-wizard.
 *
 * @param {string} stepTitle The step title displayed in the header and used for the review screen when displayed
 * @param {string} stepId  Sets the text identifier of the step
 * @param {number} stepPriority  This sets the priority of this wizard step relative to other wizard steps.  They should be numbered sequentially in the order they should be viewed.
 * @param {boolean} substeps Sets whether this step has substeps
 * @param {boolean=} nextEnabled Sets whether the next button should be enabled when this step is first displayed
 * @param {boolean=} prevEnabled Sets whether the back button should be enabled when this step is first displayed
 * @param {string=} nextTooltip The text to display as a tooltip on the next button
 * @param {string=} prevTooltip The text to display as a tooltip on the back button
 * @param {boolean=} wzDisabled Disables the wizard when this page is shown
 * @param {boolean} okToNavAway Sets whether or not it's ok for the user to leave this page
 * @param {boolean} allowClickNav Sets whether the user can click on the numeric step indicators to navigate directly to this step
 * @param {string=} description The step description (optional)
 * @param {object} wizardData Data passed to the step that is shared by the entire wizard
 * @param {function()=} onShow The function called when the wizard shows this step
 * @param {boolean=} showReview Indicates whether review information should be displayed for this step when the review step is reached
 * @param {boolean=} showReviewDetails Indicators whether the review information should be expanded by default when the review step is reached
 * @param {string=} reviewTemplate The template that should be used for the review details screen
 */
angular.module('patternfly.wizard').component('pfWizardStep', {
  transclude: true,
  bindings: {
    stepTitle: '@',
    stepId: '@',
    stepPriority: '@',
    substeps: '=?',
    nextEnabled: '<?',
    prevEnabled: '<?',
    nextTooltip: '<?',
    prevTooltip: '<?',
    disabled: '@?wzDisabled',
    okToNavAway: '<?',
    allowClickNav: '<?',
    description: '@',
    wizardData: '=',
    onShow: '=?',
    showReview: '@?',
    showReviewDetails: '@?',
    reviewTemplate: '@?'
  },
  templateUrl: 'wizard/wizard-step.html',
  controller: function ($timeout, $scope) {
    'use strict';

    var ctrl = this,
      firstRun;

    var stepIdx = function (step) {
      var idx = 0;
      var res = -1;
      angular.forEach(ctrl.getEnabledSteps(), function (currStep) {
        if (currStep === step) {
          res = idx;
        }
        idx++;
      });
      return res;
    };

    var unselectAll = function () {
      //traverse steps array and set each "selected" property to false
      angular.forEach(ctrl.getEnabledSteps(), function (step) {
        step.selected = false;
      });
      //set selectedStep variable to null
      ctrl.selectedStep = null;
    };

    var stepByTitle = function (titleToFind) {
      var foundStep = null;
      angular.forEach(ctrl.getEnabledSteps(), function (step) {
        if (step.stepTitle === titleToFind) {
          foundStep = step;
        }
      });
      return foundStep;
    };

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
      firstRun = true;
      ctrl.steps = [];
      ctrl.context = {};
      ctrl.title =  ctrl.stepTitle;
      ctrl.wizard = findWizard($scope.$parent);
      ctrl.contentStyle = ctrl.wizard.contentStyle;

      // Provide wizard step controls to sub-steps
      $scope.wizardStep = this;

      ctrl.wizard.addStep(ctrl);
      ctrl.pageNumber = ctrl.wizard.getStepNumber(ctrl);

      if (angular.isUndefined(ctrl.nextEnabled)) {
        ctrl.nextEnabled = true;
      }
      if (angular.isUndefined(ctrl.prevEnabled)) {
        ctrl.prevEnabled = true;
      }
      if (angular.isUndefined(ctrl.showReview)) {
        ctrl.showReview = false;
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

      if (ctrl.substeps && !ctrl.onShow) {
        ctrl.onShow = function () {
          $timeout(function () {
            if (!ctrl.selectedStep) {
              ctrl.goTo(ctrl.getEnabledSteps()[0]);
            }
          }, 10);
        };
      }
    };

    ctrl.$onChanges = function (changesObj) {
      if (changesObj.nextTooltip) {
        ctrl.wizard.nextTooltip = changesObj.nextTooltip.currentValue;
      }

      if (changesObj.prevTooltip) {
        ctrl.wizard.prevTooltip = changesObj.prevTooltip.currentValue;
      }
    };

    ctrl.getEnabledSteps = function () {
      return ctrl.steps.filter(function (step) {
        return step.disabled !== 'true';
      });
    };

    ctrl.getReviewSteps = function () {
      var reviewSteps = ctrl.getEnabledSteps().filter(function (step) {
        return !angular.isUndefined(step.reviewTemplate);
      });
      return reviewSteps;
    };

    ctrl.resetNav = function () {
      ctrl.goTo(ctrl.getEnabledSteps()[0]);
    };

    ctrl.currentStepNumber = function () {
      //retrieve current step number
      return stepIdx(ctrl.selectedStep) + 1;
    };

    ctrl.getStepNumber = function (step) {
      return stepIdx(step) + 1;
    };

    ctrl.isNextEnabled = function () {
      var enabled = angular.isUndefined(ctrl.nextEnabled) || ctrl.nextEnabled;
      if (ctrl.substeps) {
        angular.forEach(ctrl.getEnabledSteps(), function (step) {
          enabled = enabled && step.nextEnabled;
        });
      }
      return enabled;
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

    ctrl.getStepDisplayNumber = function (step) {
      return ctrl.pageNumber +  String.fromCharCode(65 + stepIdx(step)) + ".";
    };

    ctrl.prevStepsComplete = function (nextStep) {
      var nextIdx = stepIdx(nextStep);
      var complete = true;
      angular.forEach(ctrl.getEnabledSteps(), function (step, stepIndex) {
        if (stepIndex <  nextIdx) {
          complete = complete && step.nextEnabled;
        }
      });
      return complete;
    };

    ctrl.goTo = function (step) {
      if (ctrl.wizard.isWizardDone() || !step.okToNavAway || step === ctrl.selectedStep) {
        return;
      }

      if (firstRun || (ctrl.getStepNumber(step) < ctrl.currentStepNumber() && ctrl.selectedStep.prevEnabled) || ctrl.prevStepsComplete(step)) {
        unselectAll();
        ctrl.selectedStep = step;
        if (step) {
          step.selected = true;
          ctrl.wizard.setPageSelected(step);

          if (angular.isFunction (ctrl.selectedStep.onShow)) {
            ctrl.selectedStep.onShow();
          }

          ctrl.currentStep = step.stepTitle;

          firstRun = false;
        }
        ctrl.wizard.updateSubStepNumber (stepIdx(ctrl.selectedStep));
      }
    };

    ctrl.stepClick = function (step) {
      if (step.allowClickNav) {
        ctrl.goTo(step);
      }
    };

    ctrl.addStep = function (step) {
      // Insert the step into step array
      var insertBefore = _.find(ctrl.steps, function (nextStep) {
        return nextStep.stepPriority > step.stepPriority;
      });
      if (insertBefore) {
        ctrl.steps.splice(ctrl.steps.indexOf(insertBefore), 0, step);
      } else {
        ctrl.steps.push(step);
      }
    };

    ctrl.currentStepTitle = function () {
      return ctrl.selectedStep.stepTitle;
    };

    ctrl.currentStepDescription = function () {
      return ctrl.selectedStep.description;
    };

    ctrl.currentStep = function () {
      return ctrl.selectedStep;
    };

    ctrl.totalStepCount = function () {
      return ctrl.getEnabledSteps().length;
    };

    // Method used for next button within step
    ctrl.next = function (callback) {
      var enabledSteps = ctrl.getEnabledSteps();

      // Save the step  you were on when next() was invoked
      var index = stepIdx(ctrl.selectedStep);

      // Check if callback is a function
      if (angular.isFunction (callback)) {
        if (callback(ctrl.selectedStep)) {
          if (index === enabledSteps.length - 1) {
            return false;
          }
          // Go to the next step
          ctrl.goTo(enabledSteps[index + 1]);
          return true;
        }
        return true;
      }

      // Completed property set on scope which is used to add class/remove class from progress bar
      ctrl.selectedStep.completed = true;

      // Check to see if this is the last step.  If it is next behaves the same as finish()
      if (index === enabledSteps.length - 1) {
        return false;
      }
      // Go to the next step
      ctrl.goTo(enabledSteps[index + 1]);
      return true;
    };

    ctrl.previous = function (callback) {
      var index = stepIdx(ctrl.selectedStep);
      var goPrev = false;

      // Check if callback is a function
      if (!angular.isFunction (callback) || callback(ctrl.selectedStep)) {
        if (index !== 0) {
          ctrl.goTo(ctrl.getEnabledSteps()[index - 1]);
          goPrev = true;
        }
      }
      return goPrev;
    };
  }
});
