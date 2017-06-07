describe('Component: pfToastNotification', function () {

  var $scope;
  var $compile;
  var element;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.notification', 'notification/toast-notification.html');
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

  var setupHTML = function (notificationType, header, showClose, primaryAction, showMenu, data) {
    $scope.type = notificationType;
    $scope.header = header;
    $scope.message = "Test Toast Notification Message";
    $scope.showClose = showClose;
    $scope.primaryAction = primaryAction;
    $scope.data = data

    $scope.closeData = undefined;
    $scope.closeCallback = function (data) {
      $scope.closeData = data;
    };

    $scope.actionData = undefined;
    $scope.handleAction = function (data) {
      $scope.actionData = data;
    };

    $scope.menuActions = undefined;
    if (showMenu) {

      $scope.menuAction = undefined;
      $scope.menuData = undefined;
      var handleMenuAction = function (menuAction, data) {
        $scope.menuAction = menuAction;
        $scope.menuData = data;
      };

      $scope.menuActions = [
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
    }

    $scope.data = {
      title: "Test Notification"
    };
    var htmlTmp = '<pf-toast-notification notification-type="{{type}}" header="{{header}}" message="{{message}}"' +
                  '    show-close="{{showClose}}" close-callback="closeCallback"' +
                  '    action-title="{{primaryAction}}" action-callback="handleAction"' +
                  '    menu-actions="menuActions" data="data">' +
                  '    </pf-toast-notification>';

    compileHTML(htmlTmp, $scope);
  };

  it('should have the correct header and message', function () {
    setupHTML ("info", "Test Header", false, '', false);
    header = element.find('.toast-pf span strong');
    expect(header.length).toBe(1);
    expect(header.text()).toBe("Test Header");
    message = element.find('.toast-pf span');
    expect(message.length).toBe(2);
    expect(angular.element(message[1]).text()).toContain("Test Toast Notification Message");
  });

  it('should have the correct message when no header is given', function () {
    setupHTML ("info", "", false, '', false);
    var header = element.find('.toast-pf span strong');
    expect(header.length).toBe(0);
    var message = element.find('.toast-pf span');
    expect(message.length).toBe(2);
    expect(angular.element(message[1]).text()).toContain("Test Toast Notification Message");
  });

  it('should have the correct status icon', function () {
    setupHTML ("success", "Test Header", false, '', false);
    var okIcon = element.find('.pficon.pficon-ok');
    var infoIcon = element.find('.pficon.pficon-info');
    var errorIcon = element.find('.pficon.pficon-error-circle-o');
    var warnIcon = element.find('.pficon.pficon-warning-triangle-o');
    expect(okIcon.length).toBe(1);
    expect(infoIcon.length).toBe(0);
    expect(errorIcon.length).toBe(0);
    expect(warnIcon.length).toBe(0);

    setupHTML ("info", "Test Header", false, '', false);
    okIcon = element.find('.pficon.pficon-ok');
    infoIcon = element.find('.pficon.pficon-info');
    errorIcon = element.find('.pficon.pficon-error-circle-o');
    warnIcon = element.find('.pficon.pficon-warning-triangle-o');
    expect(okIcon.length).toBe(0);
    expect(infoIcon.length).toBe(1);
    expect(errorIcon.length).toBe(0);
    expect(warnIcon.length).toBe(0);

    setupHTML ("danger", "Test Header", false, '', false);
    okIcon = element.find('.pficon.pficon-ok');
    infoIcon = element.find('.pficon.pficon-info');
    errorIcon = element.find('.pficon.pficon-error-circle-o');
    warnIcon = element.find('.pficon.pficon-warning-triangle-o');
    expect(okIcon.length).toBe(0);
    expect(infoIcon.length).toBe(0);
    expect(errorIcon.length).toBe(1);
    expect(warnIcon.length).toBe(0);

    setupHTML ("warning", "Test Header", false, '', false);
    okIcon = element.find('.pficon.pficon-ok');
    infoIcon = element.find('.pficon.pficon-info');
    errorIcon = element.find('.pficon.pficon-error-circle-o');
    warnIcon = element.find('.pficon.pficon-warning-triangle-o');
    expect(okIcon.length).toBe(0);
    expect(infoIcon.length).toBe(0);
    expect(errorIcon.length).toBe(0);
    expect(warnIcon.length).toBe(1);

  });

  it('should have the close button when specified', function () {
    setupHTML ("success", "Test Header", false, 'Test Action', true);
    var closeButton = element.find('button.close');
    expect(closeButton.length).toBe(0);

    setupHTML ("success", "Test Header", true, 'Test Action', false);
    closeButton = element.find('button.close');
    expect(closeButton.length).toBe(1);

    expect($scope.closeData).toBeUndefined();

    eventFire(closeButton[0], 'click');
    $scope.$digest();

    expect($scope.closeData).toBeDefined();
    expect($scope.closeData.title).toBe("Test Notification");

    // No close button even if specified when menu actions exist
    setupHTML ("success", "Test Header", true, 'Test Action', true);
    closeButton = element.find('button.close');
    expect(closeButton.length).toBe(0);
  });

  it('should have the correct primary action and call the correct callback when clicked', function () {
    setupHTML ("success", "Test Header", false, 'Test Action', false);
    var actionButton = element.find('.toast-pf-action > a');
    expect(actionButton.length).toBe(1);
    expect($scope.actionData).toBeUndefined();

    eventFire(actionButton[0], 'click');
    $scope.$digest();

    expect($scope.actionData).toBeDefined();
    expect($scope.actionData.title).toBe("Test Notification");
  });

  it('should have the correct kebab menu and call the correct callback when items are clicked', function () {
    setupHTML ("success", "Test Header", false, 'Test Action', true);
    var menuIndicator = element.find('.dropdown-kebab-pf');
    expect(menuIndicator.length).toBe(1);
    var menuItems = element.find('.dropdown-kebab-pf .dropdown-menu li');
    expect(menuItems.length).toBe(7);
    var menuActions = element.find('.dropdown-kebab-pf .dropdown-menu li > a');
    expect(menuActions.length).toBe(6);

    expect($scope.menuAction).toBeUndefined();
    expect($scope.menuData).toBeUndefined();

    eventFire(menuActions[1], 'click');
    $scope.$digest();

    expect($scope.menuAction).toBeDefined();
    expect($scope.menuAction.name).toBe("Another Action");
    expect($scope.menuData).toBeDefined();
    expect($scope.menuData.title).toBe("Test Notification");
  });

  it('should have correct number of separators', function () {
    setupHTML ("success", "Test Header", false, 'Test Action', true);
    var fields = element.find('.dropdown-kebab-pf .dropdown-menu .divider');
    expect(fields.length).toBe(1);
  });

  it('should correctly disable actions and not call the callback if clicked', function () {
    setupHTML ("success", "Test Header", false, 'Test Action', true);
    var fields = element.find('.dropdown-kebab-pf .dropdown-menu .disabled > a');
    expect(fields.length).toBe(1);

    expect($scope.menuAction).toBeUndefined();
    expect($scope.menuData).toBeUndefined();

    eventFire(fields[0], 'click');
    $scope.$digest();

    expect($scope.menuAction).toBeUndefined();
    expect($scope.menuData).toBeUndefined();
  });
});
