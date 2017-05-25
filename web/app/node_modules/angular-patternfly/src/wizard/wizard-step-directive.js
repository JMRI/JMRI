/**
 * @ngdoc directive
 * @name patternfly.wizard.directive:pfWizardStep
 *
 * @description
 * Directive for rendering a Wizard step.  Each step can stand alone or have substeps.  This directive can only be used as a child of pf-wizard.
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
angular.module('patternfly.wizard').directive('pfWizardStep', function () {
  'use strict';
  return {
    restrict: 'A',
    transclude: true,
    scope: {
      stepTitle: '@',
      stepId: '@',
      stepPriority: '@',
      substeps: '=?',
      nextEnabled: '=?',
      prevEnabled: '=?',
      nextTooltip: '=?',
      prevTooltip: '=?',
      disabled: '@?wzDisabled',
      okToNavAway: '=?',
      allowClickNav: '=?',
      description: '@',
      wizardData: '=',
      onShow: '=?',
      showReview: '@?',
      showReviewDetails: '@?',
      reviewTemplate: '@?'
    },
    require: '^pf-wizard',
    templateUrl: 'wizard/wizard-step.html',
    controller: function ($scope, $timeout) {
      var firstRun = true;

      var stepIdx = function (step) {
        var idx = 0;
        var res = -1;
        angular.forEach($scope.getEnabledSteps(), function (currStep) {
          if (currStep === step) {
            res = idx;
          }
          idx++;
        });
        return res;
      };

      var unselectAll = function () {
        //traverse steps array and set each "selected" property to false
        angular.forEach($scope.getEnabledSteps(), function (step) {
          step.selected = false;
        });
        //set selectedStep variable to null
        $scope.selectedStep = null;
      };

      var watchSelectedStep = function () {
        // Remove any previous watchers
        if ($scope.nextStepEnabledWatcher) {
          $scope.nextStepEnabledWatcher();
        }
        if ($scope.nextStepTooltipWatcher) {
          $scope.nextStepTooltipWatcher();
        }
        if ($scope.prevStepEnabledWatcher) {
          $scope.prevStepEnabledWatcher();
        }
        if ($scope.prevStepTooltipWatcher) {
          $scope.prevStepTooltipWatcher();
        }

        // Add watchers for the selected step
        $scope.nextStepEnabledWatcher = $scope.$watch('selectedStep.nextEnabled', function (value) {
          $scope.nextEnabled = value;
        });
        $scope.nextStepTooltipWatcher = $scope.$watch('selectedStep.nextTooltip', function (value) {
          $scope.nextTooltip = value;
        });
        $scope.prevStepEnabledWatcher = $scope.$watch('selectedStep.prevEnabled', function (value) {
          $scope.prevEnabled = value;
        });
        $scope.prevStepTooltipWatcher = $scope.$watch('selectedStep.prevTooltip', function (value) {
          $scope.prevTooltip = value;
        });
      };

      var stepByTitle = function (titleToFind) {
        var foundStep = null;
        angular.forEach($scope.getEnabledSteps(), function (step) {
          if (step.stepTitle === titleToFind) {
            foundStep = step;
          }
        });
        return foundStep;
      };

      $scope.steps = [];
      $scope.context = {};
      this.context = $scope.context;

      if (angular.isUndefined($scope.nextEnabled)) {
        $scope.nextEnabled = true;
      }
      if (angular.isUndefined($scope.prevEnabled)) {
        $scope.prevEnabled = true;
      }
      if (angular.isUndefined($scope.showReview)) {
        $scope.showReview = false;
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

      $scope.getEnabledSteps = function () {
        return $scope.steps.filter(function (step) {
          return step.disabled !== 'true';
        });
      };

      $scope.getReviewSteps = function () {
        var reviewSteps = $scope.getEnabledSteps().filter(function (step) {
          return !angular.isUndefined(step.reviewTemplate);
        });
        return reviewSteps;
      };

      $scope.resetNav = function () {
        $scope.goTo($scope.getEnabledSteps()[0]);
      };

      $scope.currentStepNumber = function () {
        //retreive current step number
        return stepIdx($scope.selectedStep) + 1;
      };

      $scope.getStepNumber = function (step) {
        return stepIdx(step) + 1;
      };

      $scope.isNextEnabled = function () {
        var enabled = angular.isUndefined($scope.nextEnabled) || $scope.nextEnabled;
        if ($scope.substeps) {
          angular.forEach($scope.getEnabledSteps(), function (step) {
            enabled = enabled && step.nextEnabled;
          });
        }
        return enabled;
      };

      $scope.isPrevEnabled = function () {
        var enabled = angular.isUndefined($scope.prevEnabled) || $scope.prevEnabled;
        if ($scope.substeps) {
          angular.forEach($scope.getEnabledSteps(), function (step) {
            enabled = enabled && step.prevEnabled;
          });
        }
        return enabled;
      };

      $scope.getStepDisplayNumber = function (step) {
        return $scope.pageNumber +  String.fromCharCode(65 + stepIdx(step)) + ".";
      };

      //watching changes to currentStep
      $scope.$watch('currentStep', function (step) {
        //checking to make sure currentStep is truthy value
        if (!step) {
          return;
        }

        //setting stepTitle equal to current step title or default title
        if ($scope.selectedStep && $scope.selectedStep.stepTitle !== $scope.currentStep) {
          $scope.goTo(stepByTitle($scope.currentStep));
        }
      });

      //watching steps array length and editMode value, if edit module is undefined or null the nothing is done
      //if edit mode is truthy, then all steps are marked as completed
      $scope.$watch('[editMode, steps.length]', function () {
        var editMode = $scope.editMode;
        if (angular.isUndefined(editMode) || (editMode === null)) {
          return;
        }

        if (editMode) {
          angular.forEach($scope.getEnabledSteps(), function (step) {
            step.completed = true;
          });
        } else {
          angular.forEach($scope.getEnabledSteps(), function (step, stepIndex) {
            if (stepIndex >= $scope.currentStepNumber() - 1) {
              step.completed = false;
            }
          });
        }
      }, true);

      $scope.prevStepsComplete = function (nextStep) {
        var nextIdx = stepIdx(nextStep);
        var complete = true;
        angular.forEach($scope.getEnabledSteps(), function (step, stepIndex) {
          if (stepIndex <  nextIdx) {
            complete = complete && step.nextEnabled;
          }
        });
        return complete;
      };

      $scope.goTo = function (step) {
        if ($scope.wizard.isWizardDone() || !step.okToNavAway || step === $scope.selectedStep) {
          return;
        }

        if (firstRun || ($scope.getStepNumber(step) < $scope.currentStepNumber() && $scope.selectedStep.prevEnabled) || $scope.prevStepsComplete(step)) {
          unselectAll();

          $scope.selectedStep = step;
          if (step) {
            step.selected = true;

            if (angular.isFunction ($scope.selectedStep.onShow)) {
              $scope.selectedStep.onShow();
            }

            watchSelectedStep();
            $scope.currentStep = step.stepTitle;

            //emit event upwards with data on goTo() invocation
            if ($scope.selected) {
              $scope.$emit('wizard:stepChanged', {step: step, index: stepIdx(step)});
              firstRun = false;
            }
          }
          $scope.wizard.updateSubStepNumber (stepIdx($scope.selectedStep));
        }
      };

      $scope.stepClick = function (step) {
        if (step.allowClickNav) {
          $scope.goTo(step);
        }
      };

      $scope.$watch('selected', function () {
        if ($scope.selected && $scope.selectedStep) {
          $scope.$emit('wizard:stepChanged', {step: $scope.selectedStep, index: stepIdx( $scope.selectedStep)});
        }
      });

      this.addStep = function (step) {
        // Insert the step into step array
        var insertBefore = _.find($scope.steps, function (nextStep) {
          return nextStep.stepPriority > step.stepPriority;
        });
        if (insertBefore) {
          $scope.steps.splice($scope.steps.indexOf(insertBefore), 0, step);
        } else {
          $scope.steps.push(step);
        }
      };

      this.currentStepTitle = function () {
        return $scope.selectedStep.stepTitle;
      };

      this.currentStepDescription = function () {
        return $scope.selectedStep.description;
      };

      this.currentStep = function () {
        return $scope.selectedStep;
      };

      this.totalStepCount = function () {
        return $scope.getEnabledSteps().length;
      };

      this.getEnabledSteps = function () {
        return $scope.getEnabledSteps();
      };

      //Access to current step number from outside
      this.currentStepNumber = function () {
        return $scope.currentStepNumber();
      };

      // Allow access to any step
      this.goTo = function (step) {
        var enabledSteps = $scope.getEnabledSteps();
        var stepTo;

        if (angular.isNumber(step)) {
          stepTo = enabledSteps[step];
        } else {
          stepTo = stepByTitle(step);
        }

        $scope.goTo(stepTo);
      };

      // Method used for next button within step
      $scope.next = function (callback) {
        var enabledSteps = $scope.getEnabledSteps();

        // Save the step  you were on when next() was invoked
        var index = stepIdx($scope.selectedStep);

        // Check if callback is a function
        if (angular.isFunction (callback)) {
          if (callback($scope.selectedStep)) {
            if (index === enabledSteps.length - 1) {
              return false;
            }
            // Go to the next step
            $scope.goTo(enabledSteps[index + 1]);
            return true;
          }
          return true;
        }

        // Completed property set on scope which is used to add class/remove class from progress bar
        $scope.selectedStep.completed = true;

        // Check to see if this is the last step.  If it is next behaves the same as finish()
        if (index === enabledSteps.length - 1) {
          return false;
        }
        // Go to the next step
        $scope.goTo(enabledSteps[index + 1]);
        return true;
      };

      $scope.previous = function (callback) {
        var index = stepIdx($scope.selectedStep);
        var goPrev = false;

        // Check if callback is a function
        if (angular.isFunction (callback)) {
          if (callback($scope.selectedStep)) {
            if (index !== 0) {
              $scope.goTo($scope.getEnabledSteps()[index - 1]);
              goPrev = true;
            }
          }
        }

        return goPrev;
      };

      if ($scope.substeps && !$scope.onShow) {
        $scope.onShow = function () {
          $timeout(function () {
            if (!$scope.selectedStep) {
              $scope.goTo($scope.getEnabledSteps()[0]);
            }
          }, 10);
        };
      }
    },
    link: function ($scope, $element, $attrs, wizard) {
      $scope.$watch($attrs.ngShow, function (value) {
        $scope.pageNumber = wizard.getStepNumber($scope);
      });
      $scope.title =  $scope.stepTitle;
      $scope.contentStyle = wizard.contentStyle;
      wizard.addStep($scope);
      $scope.wizard = wizard;
    }
  };
});
