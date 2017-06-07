/**
  * @ngdoc directive
  * @name patternfly.wizard.component:pfWizard
  * @restrict E
  *
  * @description
  * Component for rendering a Wizard modal.  Each wizard dynamically creates the step navigation both in the header and the left-hand side based on nested steps.
  * Use pf-wizard-step to define individual steps within a wizard and pf-wizard-substep to define portions of pf-wizard-steps if so desired.  For instance, Step one can have two substeps - 1A and 1B when it is logical to group those together.
  * <br /><br />
  * The basic structure should be:
  * <pre>
  * <pf-wizard>
  *   <pf-wizard-step>
  *     <pf-wizard-substep><!-- content here --></pf-wizard-substep>
  *     <pf-wizard-substep><!-- content here --></pf-wizard-substep>
  *   </pf-wizard-step>
  *   <pf-wizard-step><!-- additional configuration can be added here with substeps if desired --></pf-wizard-step>
  *   <pf-wizard-step><!-- review steps and final command here --></pf-wizard-step>
  * </pf-wizard>
  * </pre>
  *
  * @param {string} title The wizard title displayed in the header
  * @param {boolean=} hideIndicators  Hides the step indicators in the header of the wizard
  * @param {boolean=} hideSidebar  Hides page navigation sidebar on the wizard pages
  * @param {boolean=} hideHeader Optional value to hide the title bar. Default is false.
  * @param {boolean=} hideBackButton Optional value to hide the back button, useful in 2 step wizards. Default is false.
  * @param {string=} stepClass Optional CSS class to be given to the steps page container. Used for the sidebar panel as well unless a sidebarClass is provided.
  * @param {string=} sidebarClass Optional CSS class to be give to the sidebar panel. Only used if the stepClass is also provided.
  * @param {string=} contentHeight The height the wizard content should be set to. This is used ONLY if the stepClass is not given. This defaults to 300px if the property is not supplied.
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
  * @param {boolean=} embedInPage Value that indicates wizard is embedded in a page (not a modal).  This moves the navigation buttons to the left hand side of the footer and removes the close button.
  * @param {function(step, index)=} onStepChanged Called when the wizard step is changed, passes in the step and the step index of the step changed to
  *
  * @example
  <example module="patternfly.wizard" deps="patternfly.form">
  <file name="index.html">
    <div ng-controller="WizardModalController">
      <button ng-click="openWizardModel()" class="btn btn-default">Launch Wizard</button>
    </div>
  </file>
  <file name="wizard-container.html">
  <pf-wizard title="Wizard Title"
    wizard-ready="deployProviderReady"
    on-finish="finishedWizard()"
    on-cancel="cancelDeploymentWizard()"
    next-title="nextButtonTitle"
    next-callback="nextCallback"
    back-callback="backCallback"
    wizard-done="deployComplete || deployInProgress"
    sidebar-class="example-wizard-sidebar"
    step-class="example-wizard-step"
    loading-secondary-information="secondaryLoadInformation"
    on-step-changed="stepChanged(step, index)">
      <pf-wizard-step step-title="First Step" substeps="true" step-id="details" step-priority="0" show-review="true" show-review-details="true">
        <div ng-include="'detail-page.html'">
        </div>
        <pf-wizard-substep step-title="Details - Extra" next-enabled="true" step-id="details-extra" step-priority="1" show-review="true" show-review-details="true" review-template="review-second-template.html">
          <form class="form-horizontal">
            <pf-form-group pf-label="Lorem" required>
              <input id="new-lorem" name="lorem" ng-model="data.lorem" type="text" required/>
            </pf-form-group>
            <pf-form-group pf-label="Ipsum">
              <input id="new-ipsum" name="ipsum" ng-model="data.ipsum" type="text" />
            </pf-form-group>
          </form>
        </pf-wizard-substep>
      </pf-wizard-step>
      <pf-wizard-step step-title="Second Step" substeps="false" step-id="configuration" step-priority="1" show-review="true" review-template="review-second-template.html" >
        <form class="form-horizontal">
          <h3>Wizards should make use of substeps consistently throughout (either using them or not using them).  This is an example only.</h3>
          <pf-form-group pf-label="Lorem">
            <input id="new-lorem" name="lorem" ng-model="data.lorem" type="text"/>
          </pf-form-group>
          <pf-form-group pf-label="Ipsum">
            <input id="new-ipsum" name="ipsum" ng-model="data.ipsum" type="text" />
          </pf-form-group>
        </form>
      </pf-wizard-step>
      <pf-wizard-step step-title="Review" substeps="true" step-id="review" step-priority="2">
        <div ng-include="'summary.html'"></div>
        <div ng-include="'deployment.html'"></div>
      </pf-wizard-step>
   </pf-wizard>
  </file>
  <file name="detail-page.html">
    <div ng-controller="DetailsGeneralController">
       <pf-wizard-substep step-title="General" next-enabled="detailsGeneralComplete" step-id="details-general" step-priority="0" on-show="onShow" review-template="{{reviewTemplate}}" show-review-details="true">
         <form class="form-horizontal">
           <pf-form-group pf-label="Name" required>
              <input id="new-name" name="name" ng-model="data.name" type="text" ng-change="updateName()" required/>
           </pf-form-group>
           <pf-form-group pf-label="Description">
             <input id="new-description" name="description" ng-model="data.description" type="text" />
           </pf-form-group>
         </form>
      </pf-wizard-substep>
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
    <pf-wizard-substep step-title="Summary" step-id="review-summary" step-priority="0" next-enabled="true" prev-enabled="true" ok-to-nav-away="true" wz-disabled="false" on-show="onShow">
      <pf-wizard-review-page shown="pageShown" wizard-data="data"></pf-wizard-review-page>
    </pf-wizard-substep>
  </div>
  </file>
  <file name="deployment.html">
  <div ng-controller="DeploymentController">
    <pf-wizard-substep step-title="Deploy" step-id="review-progress" step-priority="1" next-enabled="true" prev-enabled="false" ok-to-nav-away="true" wz-disabled="false" on-show="onShow">
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
   </pf-wizard-substep>
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
        $timeout(function() { }, 10000);
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

      $scope.stepChanged = function (step, index) {
        if (step.stepId === 'review-summary') {
          $scope.nextButtonTitle = "Deploy";
        } else if (step.stepId === 'review-progress') {
          $scope.nextButtonTitle = "Close";
        } else {
          $scope.nextButtonTitle = "Next >";
        }
      };

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
          $scope.data = next.$ctrl.wizardData;
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

angular.module('patternfly.wizard').component('pfWizard', {
  transclude: true,
  bindings: {
    title: '@',
    hideIndicators: '=?',
    hideSidebar: '@',
    hideHeader: '@',
    hideBackButton: '@',
    sidebarClass: '@',
    stepClass: '@',
    contentHeight: '=?',
    currentStep: '<?',
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
    embedInPage: '=?',
    onStepChanged: '&?'
  },
  templateUrl: 'wizard/wizard.html',
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
        if (step.title === titleToFind) {
          foundStep = step;
        }
      });
      return foundStep;
    };

    ctrl.$onInit = function () {
      firstRun = true;
      ctrl.steps = [];
      ctrl.context = {};
      ctrl.hideHeader = ctrl.hideHeader === 'true';
      ctrl.hideSidebar = ctrl.hideSidebar === 'true';
      ctrl.hideBaackButton = ctrl.hideBackButton === 'true';

      // If a step class is given use it for all steps
      if (angular.isDefined(ctrl.stepClass)) {

        // If a sidebarClass is given, us it for sidebar panel, if not, apply the stepsClass to the sidebar panel
        if (angular.isUndefined(ctrl.sidebarClass)) {
          ctrl.sidebarClass = ctrl.stepClass;
        }
      } else {
        // No step claass give, setup the content style to allow scrolling and a fixed height
        if (angular.isUndefined(ctrl.contentHeight)) {
          ctrl.contentHeight = '300px';
        }
        ctrl.contentStyle = {
          'height': ctrl.contentHeight,
          'max-height': ctrl.contentHeight,
          'overflow-y': 'auto'
        };
      }

      if (angular.isUndefined(ctrl.wizardReady)) {
        ctrl.wizardReady = true;
      }

      if (!ctrl.cancelTitle) {
        ctrl.cancelTitle = "Cancel";
      }
      if (!ctrl.backTitle) {
        ctrl.backTitle = "< Back";
      }
      if (!ctrl.nextTitle) {
        ctrl.nextTitle = "Next >";
      }
    };

    ctrl.$onChanges = function (changesObj) {
      var step;

      if (changesObj.wizardReady && changesObj.wizardReady.currentValue) {
        ctrl.goTo(ctrl.getEnabledSteps()[0]);
      }

      if (changesObj.currentStep) {
        //checking to make sure currentStep is truthy value
        step = changesObj.currentStep.currentValue;
        if (!step) {
          return;
        }

        //setting stepTitle equal to current step title or default title
        if (ctrl.selectedStep && ctrl.selectedStep.title !== step) {
          ctrl.goTo(stepByTitle(step));
        }
      }
    };

    ctrl.getEnabledSteps = function () {
      return ctrl.steps.filter(function (step) {
        return step.disabled !== 'true';
      });
    };

    ctrl.getReviewSteps = function () {
      return ctrl.steps.filter(function (step) {
        return !step.disabled &&
          (!angular.isUndefined(step.reviewTemplate) || step.getReviewSteps().length > 0);
      });
    };

    ctrl.currentStepNumber = function () {
      //retrieve current step number
      return stepIdx(ctrl.selectedStep) + 1;
    };

    ctrl.getStepNumber = function (step) {
      return stepIdx(step) + 1;
    };

    ctrl.goTo = function (step, resetStepNav) {
      if (ctrl.wizardDone || (ctrl.selectedStep && !ctrl.selectedStep.okToNavAway) || step === ctrl.selectedStep) {
        return;
      }

      if (firstRun || (ctrl.getStepNumber(step) < ctrl.currentStepNumber() && ctrl.selectedStep.isPrevEnabled()) || ctrl.selectedStep.isNextEnabled()) {
        unselectAll();

        if (!firstRun && resetStepNav && step.substeps) {
          step.resetNav();
        }

        ctrl.selectedStep = step;
        step.selected = true;

        $timeout(function () {
          if (angular.isFunction(step.onShow)) {
            step.onShow();
          }
        }, 100);

        // Make sure current step is not undefined
        ctrl.currentStep = step.title;

        //emit event upwards with data on goTo() invocation
        if (!step.substeps) {
          ctrl.setPageSelected(step);
        }
        firstRun = false;
      }

      if (!ctrl.selectedStep.substeps) {
        ctrl.firstStep =  stepIdx(ctrl.selectedStep) === 0;
      } else {
        ctrl.firstStep = stepIdx(ctrl.selectedStep) === 0 && ctrl.selectedStep.currentStepNumber() === 1;
      }
    };

    ctrl.allowStepIndicatorClick = function (step) {
      return step.allowClickNav &&
        !ctrl.wizardDone &&
        ctrl.selectedStep.okToNavAway &&
        (ctrl.selectedStep.nextEnabled || (step.stepPriority < ctrl.selectedStep.stepPriority)) &&
        (ctrl.selectedStep.prevEnabled || (step.stepPriority > ctrl.selectedStep.stepPriority));
    };

    ctrl.stepClick = function (step) {
      if (step.allowClickNav) {
        ctrl.goTo(step, true);
      }
    };

    ctrl.setPageSelected = function (step) {
      if (angular.isFunction(ctrl.onStepChanged)) {
        ctrl.onStepChanged({step: step, index: stepIdx(step)});
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

      if (ctrl.wizardReady && (ctrl.getEnabledSteps().length > 0) && (step === ctrl.getEnabledSteps()[0])) {
        ctrl.goTo(ctrl.getEnabledSteps()[0]);
      }
    };

    ctrl.isWizardDone = function () {
      return ctrl.wizardDone;
    };

    ctrl.updateSubStepNumber = function (value) {
      ctrl.firstStep =  stepIdx(ctrl.selectedStep) === 0 && value === 0;
    };

    ctrl.currentStepTitle = function () {
      return ctrl.selectedStep.title;
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

    // Allow access to any step
    ctrl.goToStep = function (step, resetStepNav) {
      var enabledSteps = ctrl.getEnabledSteps();
      var stepTo;

      if (angular.isNumber(step)) {
        stepTo = enabledSteps[step];
      } else {
        stepTo = stepByTitle(step);
      }

      ctrl.goTo(stepTo, resetStepNav);
    };

    // Method used for next button within step
    ctrl.next = function (callback) {
      var enabledSteps = ctrl.getEnabledSteps();

      // Save the step  you were on when next() was invoked
      var index = stepIdx(ctrl.selectedStep);

      if (ctrl.selectedStep.substeps) {
        if (ctrl.selectedStep.next(callback)) {
          return;
        }
      }

      // Check if callback is a function
      if (angular.isFunction(callback)) {
        if (callback(ctrl.selectedStep)) {
          if (index <= enabledSteps.length - 1) {
            // Go to the next step
            if (enabledSteps[index + 1].substeps) {
              enabledSteps[index + 1].resetNav();
            }
          } else {
            ctrl.finish();
          }
        } else {
          return;
        }
      }

      // Completed property set on ctrl which is used to add class/remove class from progress bar
      ctrl.selectedStep.completed = true;

      // Check to see if this is the last step.  If it is next behaves the same as finish()
      if (index === enabledSteps.length - 1) {
        ctrl.finish();
      } else {
        // Go to the next step
        ctrl.goTo(enabledSteps[index + 1]);
      }
    };

    ctrl.previous = function (callback) {
      var index = stepIdx(ctrl.selectedStep);

      if (ctrl.selectedStep.substeps) {
        if (ctrl.selectedStep.previous(callback)) {
          return;
        }
      }

      // Check if callback is a function
      if (!angular.isFunction(callback) || callback(ctrl.selectedStep)) {

        if (index === 0) {
          throw new Error("Can't go back. It's already in step 0");
        } else {
          ctrl.goTo(ctrl.getEnabledSteps()[index - 1]);
        }
      }
    };

    ctrl.finish = function () {
      if (ctrl.onFinish) {
        if (ctrl.onFinish() !== false) {
          ctrl.reset();
        }
      }
    };

    ctrl.cancel = function () {
      if (ctrl.onCancel) {
        if (ctrl.onCancel() !== false) {
          ctrl.reset();
        }
      }
    };

    //reset
    ctrl.reset = function () {
      //traverse steps array and set each "completed" property to false
      angular.forEach(ctrl.getEnabledSteps(), function (step) {
        step.completed = false;
      });
      //go to first step
      ctrl.goToStep(0);
    };

    // Provide wizard controls to steps and sub-steps
    $scope.wizard = this;
  }
});
