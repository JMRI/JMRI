describe('Directive:  pfWizard', function () {
  var $scope,
      $rootScope,
      $compile,
      $httpBackend,
      $templateCache,
      $timeout,
      element;

  // load the controller's module
  beforeEach(module(
    'patternfly.wizard',
    'wizard/wizard-substep.html',
    'wizard/wizard-step.html',
    'wizard/wizard-review-page.html',
    'wizard/wizard.html',
    'form/form-group/form-group.html',
    'test/wizard/deployment.html',
    'test/wizard/detail-page.html',
    'test/wizard/review-second-template.html',
    'test/wizard/review-template.html',
    'test/wizard/summary.html',
    'test/wizard/wizard-container.html'
  ));

  beforeEach(inject(function (_$compile_, _$rootScope_, _$httpBackend_, _$templateCache_, _$timeout_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $rootScope = _$rootScope_;
    $httpBackend = _$httpBackend_;
    $templateCache = _$templateCache_;
    $timeout = _$timeout_;
  }));

  var compileHtml = function (markup, scope) {
    var element = angular.element(markup);
    $compile(element)(scope);
    scope.$digest();
    return element;
  };

  var setupWizardScope = function () {
    var initializeWizard = function () {
      $scope.data = {
        name: '',
        description: '',
        lorem: 'default setting',
        ipsum: ''
      };
      $scope.hideIndicators = false;

      $scope.secondaryLoadInformation = 'ipsum dolor sit amet, porta at suspendisse ac, ut wisi vivamus, lorem sociosqu eget nunc amet.';
      $timeout(function () {
        $scope.deployReady = true;
      });
      $scope.nextButtonTitle = "Next >";
    };

    var startDeploy = function () {
      $timeout(function() { }, 2000);
      $scope.deployInProgress = true;
    };

    $scope.data = {};

    $scope.nextCallback = function () {
      return true;
    };

    $scope.backCallback = function () {
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
  };

  beforeEach(function () {
    setupWizardScope();
    var modalHtml = $templateCache.get('test/wizard/wizard-container.html');
    element = compileHtml(modalHtml, $scope);
    $scope.$digest();

    // there are two dependent timeouts in the wizard that need to be flushed
    $timeout.flush();
    $timeout.flush();
  });

  it('should dispatch the cancel event on the close button click', function () {
    var closeButton = element.find('.close');
    spyOn($rootScope, '$emit');
    eventFire(closeButton[0], 'click');
    $scope.$digest();
    expect($rootScope.$emit).toHaveBeenCalledWith('wizard.done', 'cancel');
  });

  it('should have three step indicators in the header', function () {
    var stepsIndicator = element.find('.wizard-pf-steps .wizard-pf-step');
    expect(stepsIndicator.length).toBe(3);
  });

  it('should have two sections in the left-hand pane', function () {
    var stepsIndicator = element.find('.wizard-pf-sidebar .list-group-item');
    var hiddenStepsIndicator = element.find('section.ng-hide .wizard-pf-sidebar .list-group-item');

    //find all hidden steps and remove them from total step count to make sure correct number are visible
    expect(stepsIndicator.length - hiddenStepsIndicator.length).toBe(2);
  });

  it('should have disabled the next button', function () {
    var checkDisabled = element.find('.wizard-pf-next').attr('disabled');
    expect(checkDisabled).toBe('disabled');
  });

  it('should have enabled the next button after input and allowed navigation', function () {
    var nextButton = element.find('.wizard-pf-next');
    var nameBox = element.find('#new-name');
    nameBox.val('test').triggerHandler('input');
    eventFire(nextButton[0], 'click');
    var stepIndicator = element.find('.wizard-pf-sidebar .list-group-item.active .wizard-pf-substep-number');
    expect(stepIndicator.text()).toBe('1B.');
  });

  it('should have allowed moving back to first page after input and allowed navigation', function () {
    var nextButton = element.find('.wizard-pf-next');
    var nameBox = element.find('#new-name');
    nameBox.val('test').triggerHandler('input');
    eventFire(nextButton[0], 'click');
    var stepIndicator = element.find('.wizard-pf-sidebar .list-group-item.active .wizard-pf-substep-number');
    expect(stepIndicator.text()).toBe('1B.');

    var backButton = element.find('#backButton');
    eventFire(backButton[0], 'click');
    var stepIndicator = element.find('.wizard-pf-sidebar .list-group-item.active .wizard-pf-substep-number');
    expect(stepIndicator.text()).toBe('1A.');
  });

  it('should have allowed navigation to review page', function () {
    var nextButton = element.find('.wizard-pf-next');
    var nameBox = element.find('#new-name');
    nameBox.val('test').triggerHandler('input');
    $scope.$digest();

    $scope.currentStep = 'Review';
    $scope.$digest();
    $timeout.flush();
    $timeout.flush();

    var stepIndicator = element.find('section.current .wizard-pf-sidebar .list-group-item.active .wizard-pf-substep-number');
    expect(stepIndicator.text()).toBe('3A.');
  });

  it('should hide indicators if the property is set', function () {
    var modalHtml = $templateCache.get('test/wizard/wizard-container.html');
    element = compileHtml(modalHtml, $scope);
    $scope.hideIndicators = true;
    $scope.$digest();
    var indicators = element.find('.wizard-pf-steps');
    expect(indicators.children().length).toBe(0);

    // make sure indicators can be turned back on
    $scope.hideIndicators = false;
    $scope.$digest();
    var indicators = element.find('.wizard-pf-steps');
    expect(indicators.children().length).toBe(1);
  });

  it('clicking indicators should navigate wizard', function () {
    var indicator = element.find('.wizard-pf-steps .wizard-pf-step a');
    var nameBox = element.find('#new-name');
    nameBox.val('test').triggerHandler('input');

    eventFire(indicator[1], 'click');
    $scope.$digest();

    var selectedSectionTitle = element.find('.wizard-pf-step.active .wizard-pf-step-title').text();
    expect(selectedSectionTitle).toBe('Second Step');
  });

  it('clicking indicators should not navigate wizard if prevented from doing so', function () {
    var indicator = element.find('.wizard-pf-steps .wizard-pf-step a');
    eventFire(indicator[1], 'click');
    $scope.$digest();

    var selectedSectionTitle = element.find('.wizard-pf-row section.current').attr("step-title");
    expect(selectedSectionTitle).not.toBe('Second Step');
  });
});
