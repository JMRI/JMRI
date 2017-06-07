describe('Component: pfToastNotificationList', function () {

  var $scope;
  var $compile;
  var element;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.notification', 'notification/toast-notification-list.html', 'notification/toast-notification.html');
  });

  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileHTML = function (markup, scope) {
    element = angular.element(markup);
    $compile(element)(scope);

    scope.$digest();
  };

  beforeEach(function () {
    $scope.closeData = undefined;
    $scope.handleClose = function (data) {
      $scope.closeData = data;
    };

    $scope.actionData = undefined;
    var handleAction = function (data) {
      $scope.actionData = data;
    };

    $scope.menuAction = undefined;
    $scope.menuData = undefined;
    var handleMenuAction = function (menuAction, data) {
      $scope.menuAction = menuAction;
      $scope.menuData = data;
    };

    var menuActions = [
      {
        name: 'Action',
        title: 'Perform an action',
        actionFn: handleMenuAction
      },
      {
        name: 'Another Action',
        title: 'Do something else',
        actionFn: handleMenuAction
      },
      {
        name: 'Disabled Action',
        title: 'Unavailable action',
        actionFn: handleMenuAction,
        isDisabled: true
      },
      {
        name: 'Something Else',
        title: '',
        actionFn: handleMenuAction
      },
      {
        isSeparator: true
      },
      {
        name: 'Grouped Action 1',
        title: 'Do something',
        actionFn: handleMenuAction
      },
      {
        name: 'Grouped Action 2',
        title: 'Do something similar',
        actionFn: handleMenuAction
      }
    ];

    $scope.notifications = [
      {
        type: 'info',
        header: 'Header 1',
        message: 'Message 1',
        isPersistent: true,
        actionTitle: "Action 1",
        actionCallback: handleAction,
        menuActions: menuActions
      },
      {
        type: 'danger',
        header: 'Header 2',
        message: 'Message 2',
        isPersistent: false,
        actionTitle: "Action 2",
        actionCallback: handleAction,
        menuActions: menuActions
      },
      {
        type: 'warning',
        header: 'Header 3',
        message: 'Message 3',
        isPersistent: true,
        actionTitle: "Action 3",
        actionCallback: handleAction,
        menuActions: menuActions
      },
      {
        type: 'success',
        header: 'Header 4',
        message: 'Message 4',
        isPersistent: true,
        actionTitle: "Action 4",
        actionCallback: handleAction,
        menuActions: menuActions
      }
    ];

    var htmlTmp = '<pf-toast-notification-list notifications="notifications" show-close="false" close-callback="handleClose"></pf-toast-notification-list>';

    compileHTML(htmlTmp, $scope);
  });

  it('should have the correct number of toast notifications', function () {
    var toasts = element.find('.toast-notifications-list-pf .toast-pf');
    expect(toasts.length).toBe(4);

    var okIcon = element.find('.pficon.pficon-ok');
    var infoIcon = element.find('.pficon.pficon-info');
    var errorIcon = element.find('.pficon.pficon-error-circle-o');
    var warnIcon = element.find('.pficon.pficon-warning-triangle-o');
    expect(okIcon.length).toBe(1);
    expect(infoIcon.length).toBe(1);
    expect(errorIcon.length).toBe(1);
    expect(warnIcon.length).toBe(1);

    $scope.notifications.splice(2, 1);
    $scope.$digest();

    toasts = element.find('.toast-notifications-list-pf .toast-pf');
    expect(toasts.length).toBe(3);

    okIcon = element.find('.pficon.pficon-ok');
    infoIcon = element.find('.pficon.pficon-info');
    errorIcon = element.find('.pficon.pficon-error-circle-o');
    warnIcon = element.find('.pficon.pficon-warning-triangle-o');
    expect(okIcon.length).toBe(1);
    expect(infoIcon.length).toBe(1);
    expect(errorIcon.length).toBe(1);
    expect(warnIcon.length).toBe(0);
  });

  it('should get the close callback invoked when an item is closed', function () {
    // No close buttons when there are menu actions
    var closeButton = element.find('.toast-notifications-list-pf .toast-pf button.close');
    expect(closeButton.length).toBe(0);

    // No Menu Actions
    $scope.notifications.forEach(function(nextItem) {
      nextItem.menuActions = undefined;
    })
    var htmlTmp = '<pf-toast-notification-list notifications="notifications" show-close="false" close-callback="handleClose"></pf-toast-notification-list>';

    compileHTML(htmlTmp, $scope);

    var closeButton = element.find('.toast-notifications-list-pf .toast-pf button.close');
    expect(closeButton.length).toBe(3);

    expect($scope.closeData).toBeUndefined();

    eventFire(closeButton[1], 'click');
    $scope.$digest();

    expect($scope.closeData).toBeDefined();
    expect($scope.closeData.header).toBe("Header 3");
  });

  it('should get the action callback invoked when an action button is closed', function () {
    // No close buttons when there are menu actions
    var closeButton = element.find('.toast-notifications-list-pf .toast-pf .toast-pf-action > a');
    expect(closeButton.length).toBe(4);

    expect($scope.actionData).toBeUndefined();

    eventFire(closeButton[1], 'click');
    $scope.$digest();

    expect($scope.actionData).toBeDefined();
    expect($scope.actionData.header).toBe("Header 2");
  });

  it('should have the correct kebab menu and call the correct callback when items are clicked', function () {
    var menuIndicator = element.find('.dropdown-kebab-pf');
    expect(menuIndicator.length).toBe(4);
    var menuItems = angular.element(menuIndicator[0]).find('.dropdown-menu li');
    expect(menuItems.length).toBe(7);
    var menuActions = angular.element(menuIndicator[0]).find('.dropdown-menu li > a');
    expect(menuActions.length).toBe(6);

    expect($scope.menuAction).toBeUndefined();
    expect($scope.menuData).toBeUndefined();

    eventFire(menuActions[1], 'click');
    $scope.$digest();

    expect($scope.menuAction).toBeDefined();
    expect($scope.menuAction.name).toBe("Another Action");
    expect($scope.menuData).toBeDefined();
    expect($scope.menuData.header).toBe("Header 1");
  });
});
