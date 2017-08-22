/**
  * @ngdoc directive
  * @name patternfly.wizard.directive:pfWizard
  *
  * @description
  * Directive for rendering a Wizard modal.  Each wizard dynamically creates the step navigation both in the header and the left-hand side based on nested steps.
  * Use the pf-wizardstep to define individual steps within a wizard and pf-wizardsubstep to define portions of pf-wizardsteps if so desired.  For instance, Step one can have two substeps - 1A and 1B when it is logical to group those together.
  * <br /><br />
  * The basic structure should be:
  * <pre>
  * <div pf-wizard>
  *   <div pf-wizardstep>
  *     <div pf-wizardsubstep><!-- content here --></div>
  *     <div pf-wizardsubstep><!-- content here --></div>
  *   </div>
  *   <div pf-wizardstep><!-- additional configuration can be added here with substeps if desired --></div>
  *   <div pf-wizardstep><!-- review steps and final command here --></div>
  * </div>
  * </pre>
  *
  * @param {string} title The wizard title displayed in the header
  * @param {boolean=} hideIndicators  Hides the step indicators in the header of the wizard
  * @param {string=} currentStep The current step can be changed externally - this is the title of the step to switch the wizard to
  * @param {string=} cancelTitle The text to display on the cancel button
  * @param {string=} backTitle The text to display on the back button
  * @param {string=} nextTitle The text to display on the next button
  * @param {function(step)=} backCallback Called to notify when the back button is clicked
  * @param {function(step)=} nextCallback Called to notify when the next button is clicked
  * @param {function()=} onFinish Called to notify when when the wizard is complete.  Returns a boolean value to indicate if the finish operation is complete
  * @param {function()=} onCancel Called when the wizard is canceled, returns a boolean value to indicate if cancel is successful
  * @param {boolean} wizardReady Value that is set when the wizard is ready
  * @param {boolean=} wizardDone  Value that is set when the wizard is done
  * @param {string} loadingWizardTitle The text displayed when the wizard is loading
  * @param {string=} loadingSecondaryInformation Secondary descriptive information to display when the wizard is loading
  * @param {string=} contentHeight The height the wizard content should be set to.  This defaults to 300px if the property is not supplied.
  * @param {boolean=} embedInPage Value that indicates wizard is embedded in a page (not a modal).  This moves the navigation buttons to the left hand side of the footer and removes the close button.
  *
  * @example
  <example module="patternfly.wizard" deps="patternfly.form">
  <file name="index.html">
    <div ng-controller="WizardModalController">
      <button ng-click="openWizardModel()" class="btn btn-default">Launch Wizard</button>
    </div>
  </file>
  <file name="wizard-container.html">
  <div pf-wizard title="Wizard Title"
    wizard-ready="deployProviderReady"
    on-finish="finishedWizard()"
    on-cancel="cancelDeploymentWizard()"
    next-title="nextButtonTitle"
    next-callback="nextCallback"
    back-callback="backCallback"
    wizard-done="deployComplete || deployInProgress"
    content-height="'600px'"
    loading-secondary-information="secondaryLoadInformation">
      <div pf-wizard-step step-title="First Step" substeps="true" step-id="details" step-priority="0" show-review="true" show-review-details="true">
        <div ng-include="'detail-page.html'">
        </div>
        <div pf-wizard-substep step-title="Details - Extra" next-enabled="true" step-id="details-extra" step-priority="1" show-review="true" show-review-details="true" review-template="review-second-template.html">
          <form class="form-horizontal">
            <div pf-form-group pf-label="Lorem" required>
              <input id="new-lorem" name="lorem" ng-model="data.lorem" type="text" required/>
            </div>
            <div pf-form-group pf-label="Ipsum">
              <input id="new-ipsum" name="ipsum" ng-model="data.ipsum" type="text" />
            </div>
          </form>
        </div>
      </div>
      <div pf-wizard-step step-title="Second Step" substeps="false" step-id="configuration" step-priority="1" show-review="true" review-template="review-second-template.html" >
        <form class="form-horizontal">
          <h3>Wizards should make use of substeps consistently throughout (either using them or not using them).  This is an example only.</h3>
          <div pf-form-group pf-label="Lorem">
            <input id="new-lorem" name="lorem" ng-model="data.lorem" type="text"/>
          </div>
          <div pf-form-group pf-label="Ipsum">
            <input id="new-ipsum" name="ipsum" ng-model="data.ipsum" type="text" />
          </div>
        </form>
      </div>
      <div pf-wizard-step step-title="Review" substeps="true" step-id="review" step-priority="2">
        <div ng-include="'summary.html'"></div>
        <div ng-include="'deployment.html'"></div>
      </div>
   </div>
  </file>
  <file name="detail-page.html">
    <div ng-controller="DetailsGeneralController">
       <div pf-wizard-substep step-title="General" next-enabled="detailsGeneralComplete" step-id="details-general" step-priority="0" on-show="onShow" review-template="{{reviewTemplate}}" show-review-details="true">
         <form class="form-horizontal">
           <div pf-form-group pf-label="Name" required>
            <input id="new-name" name="name" ng-model="data.name" type="text" ng-change="updateName()" required/>
           </div>
           <div pf-form-group pf-label="Description">
            <input id="new-description" name="description" ng-model="data.description" type="text" />
           </div>
         </form>
      </div>
    </div>
  </file>
  <file name="review-template.html">
  <div ng-controller="DetailsReviewController">
    <form class="form">
      <div class="wizard-pf-review-item">
        <span class="wizard-pf-review-item-label">Name:</span>
        <span class="wizard-pf-review-item-value">{{data.name}}</span>
      </div>
      <div class="wizard-pf-review-item">
        <span class="wizard-pf-review-item-label">Description:</span>
        <span class="wizard-pf-review-item-value">{{data.description}}</span>
      </div>
    </form>
  </div>
  </file>
  <file name="review-second-template.html">
  <div ng-controller="DetailsReviewController">
    <form class="form">
      <div class="wizard-pf-review-item">
        <span class="wizard-pf-review-item-label">Lorem:</span>
        <span class="wizard-pf-review-item-value">{{data.lorem}}</span>
      </div>
      <div class="wizard-pf-review-item">
        <span class="wizard-pf-review-item-label">Ipsum:</span>
        <span class="wizard-pf-review-item-value">{{data.ipsum}}</span>
      </div>
    </form>
  </div>
  </file>
  <file name="summary.html">
  <div ng-controller="SummaryController">
    <div pf-wizard-substep step-title="Summary" step-id="review-summary" step-priority="0" next-enabled="true" prev-enabled="true" ok-to-nav-away="true" wz-disabled="false" on-show="onShow">
      <div pf-wizard-review-page shown="pageShown" wizard-data="data"></div>
    </div>
  </div>
  </file>
  <file name="deployment.html">
  <div ng-controller="DeploymentController">
    <div pf-wizard-substep step-title="Deploy" step-id="review-progress" step-priority="1" next-enabled="true" prev-enabled="false" ok-to-nav-away="true" wz-disabled="false" on-show="onShow">
      <div class="wizard-pf-contents" ng-controller="DeploymentController">
        <div class="wizard-pf-process blank-slate-pf" ng-if="!deploymentComplete">
          <div class="spinner spinner-lg blank-slate-pf-icon"></div>
          <h3 class="blank-slate-pf-main-action">Deployment in progress</h3>
          <p class="blank-slate-pf-secondary-action">Lorem ipsum dolor sit amet, porta at suspendisse ac, ut wisi vivamus, lorem sociosqu eget nunc amet. </p>
        </div>
        <div class="wizard-pf-complete blank-slate-pf" ng-if="deploymentComplete">
          <div class="wizard-pf-success-icon"><span class="glyphicon glyphicon-ok-circle"></span></div>
          <h3 class="blank-slate-pf-main-action">Deployment was successful</h3>
          <p class="blank-slate-pf-secondary-action">Lorem ipsum dolor sit amet, porta at suspendisse ac, ut wisi vivamus, lorem sociosqu eget nunc amet. </p>
          <button type="button" class="btn btn-lg btn-primary">View Deployment</button>
        </div>
     </div>
   </div>
  </div>
  </file>
  <file name="script.js">
  angular.module('patternfly.wizard').controller('WizardModalController', ['$scope', '$timeout', '$uibModal', '$rootScope',
    function ($scope, $timeout, $uibModal, $rootScope) {
      $scope.openWizardModel = function () {
        var wizardDoneListener,
            modalInstance = $uibModal.open({
              animation: true,
              backdrop: 'static',
              templateUrl: 'wizard-container.html',
              controller: 'WizardController',
              size: 'lg'
            });

        var closeWizard = function (e, reason) {
          modalInstance.dismiss(reason);
          wizardDoneListener();
        };

        modalInstance.result.then(function () { }, function () { });

        wizardDoneListener = $rootScope.$on('wizard.done', closeWizard);
      };
    }
  ]);
  angular.module('patternfly.wizard').controller('WizardController', ['$scope', '$timeout', '$rootScope',
    function ($scope, $timeout, $rootScope) {


      var initializeWizard = function () {
        $scope.data = {
          name: '',
          description: '',
          lorem: 'default setting',
          ipsum: ''
        };
        $scope.secondaryLoadInformation = 'ipsum dolor sit amet, porta at suspendisse ac, ut wisi vivamus, lorem sociosqu eget nunc amet.';
        $timeout(function () {
          $scope.deployReady = true;
        }, 1000);
        $scope.nextButtonTitle = "Next >";
      };

      var startDeploy = function () {
        $timeout(function() { }, 2000);
        $scope.deployInProgress = true;
      };

      $scope.data = {};

      $scope.nextCallback = function (step) {
        // call startdeploy after deploy button is clicked on review-summary tab
        if (step.stepId === 'review-summary') {
          startDeploy();
        }
        return true;
      };
      $scope.backCallback = function (step) {
        return true;
      };

      $scope.$on("wizard:stepChanged", function (e, parameters) {
        if (parameters.step.stepId === 'review-summary') {
          $scope.nextButtonTitle = "Deploy";
        } else if (parameters.step.stepId === 'review-progress') {
          $scope.nextButtonTitle = "Close";
        } else {
          $scope.nextButtonTitle = "Next >";
        }
      });

      $scope.cancelDeploymentWizard = function () {
        $rootScope.$emit('wizard.done', 'cancel');
      };

      $scope.finishedWizard = function () {
        $rootScope.$emit('wizard.done', 'done');
        return true;
      };

      initializeWizard();
     }
  ]);

  angular.module('patternfly.wizard').controller('DetailsGeneralController', ['$rootScope', '$scope',
    function ($rootScope, $scope) {
      'use strict';

      $scope.reviewTemplate = "review-template.html";
      $scope.detailsGeneralComplete = false;

      $scope.onShow = function() { };

      $scope.updateName = function() {
        $scope.detailsGeneralComplete = angular.isDefined($scope.data.name) && $scope.data.name.length > 0;
      };
    }
  ]);

  angular.module('patternfly.wizard').controller('DetailsReviewController', ['$rootScope', '$scope',
    function ($rootScope, $scope) {
      'use strict';

      // Find the data!
      var next = $scope;
      while (angular.isUndefined($scope.data)) {
        next = next.$parent;
        if (angular.isUndefined(next)) {
          $scope.data = {};
        } else {
          $scope.data = next.wizardData;
        }
      }
    }
  ]);

  angular.module('patternfly.wizard').controller('SummaryController', ['$rootScope', '$scope', '$timeout',
    function ($rootScope, $scope, $timeout) {
      'use strict';
      $scope.pageShown = false;

      $scope.onShow = function () {
        $scope.pageShown = true;
        $timeout(function () {
          $scope.pageShown = false;  // done so the next time the page is shown it updates
        });
      }
    }
  ]);

  angular.module('patternfly.wizard').controller('DeploymentController', ['$rootScope', '$scope', '$timeout',
    function ($rootScope, $scope, $timeout) {
      'use strict';

      $scope.onShow = function() {
        $scope.deploymentComplete = false;
        $timeout(function() {
          $scope.deploymentComplete = true;
        }, 2500);
      };
    }
  ]);
</file>
</example>
*/

angular.module('patternfly.wizard').directive('pfWizard', function ($window) {
  'use strict';
  return {
    restrict: 'A',
    transclude: true,
    scope: {
      title: '@',
      hideIndicators: '=?',
      currentStep: '=?',
      cancelTitle: '=?',
      backTitle: '=?',
      nextTitle: '=?',
      backCallback: '=?',
      nextCallback: '=?',
      onFinish: '&',
      onCancel: '&',
      wizardReady: '=?',
      wizardDone: '=?',
      loadingWizardTitle: '=?',
      loadingSecondaryInformation: '=?',
      contentHeight: '=?',
      embedInPage: '=?'
    },
    templateUrl: 'wizard/wizard.html',
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
          if (step.title === titleToFind) {
            foundStep = step;
          }
        });
        return foundStep;
      };

      $scope.steps = [];
      $scope.context = {};
      this.context = $scope.context;

      if (angular.isUndefined($scope.wizardReady)) {
        $scope.wizardReady = true;
      }

      if (angular.isUndefined($scope.contentHeight)) {
        $scope.contentHeight = '300px';
      }
      this.contentHeight = $scope.contentHeight;
      $scope.contentStyle = {
        'height': $scope.contentHeight,
        'max-height': $scope.contentHeight,
        'overflow-y': 'auto'
      };
      this.contentStyle = $scope.contentStyle;

      $scope.nextEnabled = false;
      $scope.prevEnabled = false;

      if (!$scope.cancelTitle) {
        $scope.cancelTitle = "Cancel";
      }
      if (!$scope.backTitle) {
        $scope.backTitle = "< Back";
      }
      if (!$scope.nextTitle) {
        $scope.nextTitle = "Next >";
      }

      $scope.getEnabledSteps = function () {
        return $scope.steps.filter(function (step) {
          return step.disabled !== 'true';
        });
      };

      this.getReviewSteps = function () {
        return $scope.steps.filter(function (step) {
          return !step.disabled &&
            (!angular.isUndefined(step.reviewTemplate) || step.getReviewSteps().length > 0);
        });
      };

      $scope.currentStepNumber = function () {
        //retrieve current step number
        return stepIdx($scope.selectedStep) + 1;
      };

      $scope.getStepNumber = function (step) {
        return stepIdx(step) + 1;
      };

      //watching changes to currentStep
      $scope.$watch('currentStep', function (step) {
        //checking to make sure currentStep is truthy value
        if (!step) {
          return;
        }

        //setting stepTitle equal to current step title or default title
        if ($scope.selectedStep && $scope.selectedStep.title !== $scope.currentStep) {
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
            if (stepIndex >= ($scope.currentStepNumber() - 1)) {
              step.completed = false;
            }
          });
        }
      }, true);

      $scope.goTo = function (step, resetStepNav) {
        if ($scope.wizardDone || ($scope.selectedStep && !$scope.selectedStep.okToNavAway) || step === $scope.selectedStep) {
          return;
        }

        if (firstRun || ($scope.getStepNumber(step) < $scope.currentStepNumber() && $scope.selectedStep.isPrevEnabled()) || $scope.selectedStep.isNextEnabled()) {
          unselectAll();

          if (!firstRun && resetStepNav && step.substeps) {
            step.resetNav();
          }

          $scope.selectedStep = step;
          step.selected = true;

          $timeout(function () {
            if (angular.isFunction(step.onShow)) {
              step.onShow();
            }
          }, 100);

          watchSelectedStep();

          // Make sure current step is not undefined
          $scope.currentStep = step.title;

          //emit event upwards with data on goTo() invocation
          if (!step.substeps) {
            $scope.$emit('wizard:stepChanged', {step: step, index: stepIdx(step)});
          }
          firstRun = false;
        }

        if (!$scope.selectedStep.substeps) {
          $scope.firstStep =  stepIdx($scope.selectedStep) === 0;
        } else {
          $scope.firstStep = stepIdx($scope.selectedStep) === 0 && $scope.selectedStep.currentStepNumber() === 1;
        }
      };

      $scope.stepClick = function (step) {
        if (step.allowClickNav) {
          $scope.goTo(step, true);
        }
      };

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

        if ($scope.wizardReady && ($scope.getEnabledSteps().length > 0) && (step === $scope.getEnabledSteps()[0])) {
          $scope.goTo($scope.getEnabledSteps()[0]);
        }
      };

      this.isWizardDone = function () {
        return $scope.wizardDone;
      };

      this.updateSubStepNumber = function (value) {
        $scope.firstStep =  stepIdx($scope.selectedStep) === 0 && value === 0;
      };

      this.currentStepTitle = function () {
        return $scope.selectedStep.title;
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

      this.getStepNumber = function (step) {
        return $scope.getStepNumber(step);
      };

      // Allow access to any step
      this.goTo = function (step, resetStepNav) {
        var enabledSteps = $scope.getEnabledSteps();
        var stepTo;

        if (angular.isNumber(step)) {
          stepTo = enabledSteps[step];
        } else {
          stepTo = stepByTitle(step);
        }

        $scope.goTo(stepTo, resetStepNav);
      };

      // Method used for next button within step
      this.next = function (callback) {
        var enabledSteps = $scope.getEnabledSteps();

        // Save the step  you were on when next() was invoked
        var index = stepIdx($scope.selectedStep);

        if ($scope.selectedStep.substeps) {
          if ($scope.selectedStep.next(callback)) {
            return;
          }
        }

        // Check if callback is a function
        if (angular.isFunction(callback)) {
          if (callback($scope.selectedStep)) {
            if (index === enabledSteps.length - 1) {
              this.finish();
            } else {
              // Go to the next step
              if (enabledSteps[index + 1].substeps) {
                enabledSteps[index + 1].resetNav();
              }
            }
          } else {
            return;
          }
        }

        // Completed property set on scope which is used to add class/remove class from progress bar
        $scope.selectedStep.completed = true;

        // Check to see if this is the last step.  If it is next behaves the same as finish()
        if (index === enabledSteps.length - 1) {
          this.finish();
        } else {
          // Go to the next step
          $scope.goTo(enabledSteps[index + 1]);
        }
      };

      this.previous = function (callback) {
        var index = stepIdx($scope.selectedStep);

        if ($scope.selectedStep.substeps) {
          if ($scope.selectedStep.previous(callback)) {
            return;
          }
        }

        // Check if callback is a function
        if (angular.isFunction(callback)) {
          if (callback($scope.selectedStep)) {
            if (index === 0) {
              throw new Error("Can't go back. It's already in step 0");
            } else {
              $scope.goTo($scope.getEnabledSteps()[index - 1]);
            }
          }
        }
      };

      this.finish = function () {
        if ($scope.onFinish) {
          if ($scope.onFinish() !== false) {
            this.reset();
          }
        }
      };

      this.cancel = function () {
        if ($scope.onCancel) {
          if ($scope.onCancel() !== false) {
            this.reset();
          }
        }
      };

      //reset
      this.reset = function () {
        //traverse steps array and set each "completed" property to false
        angular.forEach($scope.getEnabledSteps(), function (step) {
          step.completed = false;
        });
        //go to first step
        this.goTo(0);
      };
    },
    link: function ($scope) {
      $scope.$watch('wizardReady', function () {
        if ($scope.wizardReady) {
          $scope.goTo($scope.getEnabledSteps()[0]);
        }
      });
    }
  };
});
