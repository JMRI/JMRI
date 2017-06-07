describe('Component:  pfEnptyState', function () {
  var $scope;
  var $compile;
  var element;
  var performedAction;
  var updateCount;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.views', 'patternfly.utils', 'views/empty-state.html');
  });

  beforeEach(inject(function (_$compile_, _$rootScope_, _$timeout_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $timeout = _$timeout_;
  }));

  var compileHTML = function (markup, scope) {
    element = angular.element(markup);
    $compile(element)(scope);

    scope.$digest();
  };

  beforeEach(function () {
    $scope.config = {
      icon: 'pficon-add-circle-o',
      title: 'Empty State Title',
      info: "This is the Empty State component. The goal of a empty state pattern is to provide a good first impression that helps users to achieve their goals. It should be used when a view is empty because no objects exists and you want to guide the user to perform specific actions.",
      helpLink: {
        label: 'For more information please see',
        urlLabel: 'pfExample',
        url : '#/api/patternfly.views.component:pfEmptyState'
      }
    };

    var performAction = function (action) {
      $scope.eventText = action.name + " executed. \r\n" + $scope.eventText;
    };

    $scope.actionButtons = [
      {
        name: 'Main Action',
        title: 'Perform main action',
        actionFn: performAction,
        type: 'main'
      },
      {
        name: 'Secondary Action 1',
        title: 'Perform secondary action 1',
        actionFn: performAction
      },
      {
        name: 'Secondary Action 2',
        title: 'Perform secondary action 2',
        actionFn: performAction
      },
      {
        name: 'Secondary Action 3',
        title: 'Perform secondary action 3',
        actionFn: performAction
      }
    ];
  });

  it('should display correct information from config and actionButtons', function () {
    compileHTML('<pf-empty-state config="config" action-buttons="actionButtons"></pf-empty-state>', $scope);

    expect(element.find('.pficon-add-circle-o').length).toBe(1);
    expect(element.find('#title').text()).toContain('Empty State Title');
    expect(element.find('#info').text()).toContain('This is the Empty State component');
    expect(element.find('#helpLink').text()).toContain('For more information please see');
    expect(element.find('a').text()).toContain('pfExample');
    expect(element.find('a').prop('href')).toContain('#/api/patternfly.views.component:pfEmptyState');

    var buttons = element.find('button');
    expect(buttons.length).toBe(4);
    expect(angular.element(buttons[0]).text()).toContain('Main Action');
    expect(angular.element(buttons[0]).prop('title')).toContain('Perform main action');
    expect(angular.element(buttons[1]).text()).toContain('Secondary Action 1');
    expect(angular.element(buttons[1]).prop('title')).toContain('Perform secondary action 1');
    expect(angular.element(buttons[2]).text()).toContain('Secondary Action 2');
    expect(angular.element(buttons[2]).prop('title')).toContain('Perform secondary action 2');
    expect(angular.element(buttons[3]).text()).toContain('Secondary Action 3');
    expect(angular.element(buttons[3]).prop('title')).toContain('Perform secondary action 3');
  });

  it('should only display main default title when no config and actionButtons defined', function () {
    compileHTML('<pf-empty-state></pf-empty-state>', $scope);

    expect(element.find('#title').text()).toContain('No Items Available');

    expect(element.find('.blank-slate-pf-icon').length).toBe(0);
    expect(element.find('#info').length).toBe(0);
    expect(element.find('#helpLink').length).toBe(0);
    expect(element.find('button').length).toBe(0);
  });
});
