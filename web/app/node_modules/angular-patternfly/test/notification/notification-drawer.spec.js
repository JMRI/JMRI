describe('Component:  pfNotificationDrawer', function () {
  var $scope;
  var $compile;
  var element;
  var $pfViewUtils;
  var performedAction;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.notification', 'patternfly.utils', 'notification/notification-drawer.html', 'test/notification/title.html',
           'test/notification/heading.html', 'test/notification/subheading.html', 'test/notification/notification-body.html',
           'test/notification/notification-footer.html');
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
    var currentTime = (new Date()).getTime();
    $scope.hideDrawer = true;
    $scope.toggleShowDrawer = function () {
      $scope.hideDrawer = !$scope.hideDrawer;
    };

    var menuActions = [
      {
        name: 'Action1'
      },
      {
        name: 'Action2'
      },
      {
        name: 'Action3'
      }
    ];


    $scope.groups = [
      {
        heading: "Group 1",
        subHeading: "1 New Events",
        notifications: [
          {
            unread: true,
            message: "A New Event! Huzzah! Bold",
            status: 'info',
            actions: menuActions,
            timeStamp: currentTime - (1 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (2 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (10 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (12 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "A New Event! Huzzah! Bold",
            status: 'info',
            actions: menuActions,
            timeStamp: currentTime - (1 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (2 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (10 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (12 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'info',
            actions: menuActions,
            timeStamp: currentTime - (240 * 60 * 60 * 1000)
          }
        ],
        isLoading: true
      },
      {
        heading: "Group 2",
        subHeading: "2 New Events",
        notifications: [
          {
            unread: true,
            message: "A New Event! Huzzah! Bold",
            status: 'info',
            actions: menuActions,
            timeStamp: currentTime - (1 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (2 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (10 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (12 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (240 * 60 * 60 * 1000)
          }
        ]
      },
      {
        heading: "Group 3",
        subHeading: "3 New Events",
        notifications: [
          {
            unread: true,
            message: "A New Event! Huzzah! Bold",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (1 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (2 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'info',
            actions: menuActions,
            timeStamp: currentTime - (10 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (12 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (240 * 60 * 60 * 1000)
          }
        ]
      },
      {
        heading: "Group 4",
        subHeading: "4 New Events",
        notifications: [
          {
            unread: true,
            message: "A New Event! Huzzah! Bold",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (1 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (2 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'ok',
            actions: menuActions,
            timeStamp: currentTime - (10 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (12 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'info',
            actions: menuActions,
            timeStamp: currentTime - (240 * 60 * 60 * 1000)
          }
        ]
      },
      {
        heading: "Group 5",
        subHeading: "5 New Events",
        notifications: [
          {
            unread: true,
            message: "A New Event! Huzzah! Bold",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (1 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (2 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (10 * 60 * 60 * 1000)
          },
          {
            unread: false,
            message: "Another Event Notification",
            status: 'warning',
            actions: menuActions,
            timeStamp: currentTime - (12 * 60 * 60 * 1000)
          },
          {
            unread: true,
            message: "Another Event Notification",
            status: 'error',
            actions: menuActions,
            timeStamp: currentTime - (240 * 60 * 60 * 1000)
          }
        ]
      }
    ];

    $scope.actionButtonClicked = '';
    $scope.actionButtonCB = function (group) {
      $scope.actionButtonClicked = group.heading;
    };

    //
    // Define customScope to contain anything that needs to be accessed from the included source
    // html files (heading, subheading, or notificaton body).
    //

    $scope.customScope = {};
    $scope.customScope.getNotficationStatusIconClass = function (notification) {
      var retClass = '';
      if (notification && notification.status) {
        if (notification.status === 'info') {
          retClass = "pficon pficon-info";
        } else if (notification.status === 'error') {
          retClass = "pficon pficon-error-circle-o";
        } else if (notification.status === 'warning') {
          retClass = "pficon pficon-warning-triangle-o";
        } else if (notification.status === 'ok') {
          retClass = "pficon pficon-ok";
        }
      }
      return retClass;
    };
    $scope.actionPerformed = undefined;
    $scope.actionItem = undefined;
    $scope.customScope.handleAction = function (item, action) {
      $scope.actionItem = item ;
      $scope.actionPerformed = action;
    };

    var htmlTmp = '<pf-notification-drawer drawer-hidden="hideDrawer" drawer-title="Notifications Drawer"  title-include="test/notification/title.html" ' +
                  '     action-button-title="Mark All Read" action-button-callback="actionButtonCB" notification-groups="groups"' +
                  '     heading-include="test/notification/heading.html" subheading-include="test/notification/subheading.html" notification-body-include="test/notification/notification-body.html"' +
                  '     notification-footer-include="test/notification/notification-footer.html" custom-scope="customScope">' +
                  '</pf-notification-drawer>';

      compileHTML(htmlTmp, $scope);
  });

  it('should have the correct title', function () {
    var title = element.find('.drawer-pf-title .text-center');
    expect(title.length).toBe(1);
    expect(title.text()).toBe("Notifications Drawer");

    var customTitle = element.find('.drawer-pf-title .title-class');
    expect(customTitle.length).toBe(1);
  });

  it('should have the correct headings', function () {
    var heading = element.find('.heading-class');
    expect(heading.length).toBe(5);
    expect(angular.element(heading[0]).text()).toBe("Group 1");
    expect(angular.element(heading[1]).text()).toBe("Group 2");
    expect(angular.element(heading[2]).text()).toBe("Group 3");
    expect(angular.element(heading[3]).text()).toBe("Group 4");
    expect(angular.element(heading[4]).text()).toBe("Group 5");
  });

  it('should have the correct sub headings', function () {
    var subheading = element.find('.subheading-class');
    expect(subheading.length).toBe(5);
    expect(angular.element(subheading[0]).text()).toBe("1 New Events");
    expect(angular.element(subheading[1]).text()).toBe("2 New Events");
    expect(angular.element(subheading[2]).text()).toBe("3 New Events");
    expect(angular.element(subheading[3]).text()).toBe("4 New Events");
    expect(angular.element(subheading[4]).text()).toBe("5 New Events");
  });

  it('should have the correct notification footer', function () {
    var footers = element.find('.footer-class');
    expect(footers.length).toBe(5);

    expect($scope.actionPerformed).toBeUndefined();
    expect($scope.actionItem).toBeUndefined();

    eventFire(footers[2], 'click');
    $scope.$digest();

    expect($scope.actionPerformed).toBe('Clear All');
    expect($scope.actionItem.heading).toBe('Group 3');
  });

  it('should have the correct status icons', function () {
    var infoEvents = element.find('.pficon.pficon-info');
    expect(infoEvents.length).toBe(6);

    var errorEvents = element.find('.pficon.pficon-error-circle-o');
    expect(errorEvents.length).toBe(8);

    var warningEvents = element.find('.pficon.pficon-warning-triangle-o');
    expect(warningEvents.length).toBe(8);

    var okEvents = element.find('.pficon.pficon-ok');
    expect(okEvents.length).toBe(7);
  });

  it ('should show the notification drawer when the drawerHidden flag is false', function () {
    var hiddenDrawer = element.find('.drawer-pf.hide');
    expect(hiddenDrawer.length).toBe(1);

    $scope.hideDrawer = false;
    $scope.$digest();

    hiddenDrawer = element.find('.drawer-pf.hide');
    expect(hiddenDrawer.length).toBe(0);

    $scope.hideDrawer = true;
    $scope.$digest();

    hiddenDrawer = element.find('.drawer-pf.hide');
    expect(hiddenDrawer.length).toBe(1);
  });

  it ('should toggle showing notifications when the header is clicked', function () {
    var collapseLinks = element.find('.panel-heading .panel-title a');
    expect(collapseLinks.length).toBe(5);

    var collapsedPanels = element.find('.collapse.in');
    expect(collapsedPanels.length).toBe(0);

    eventFire(collapseLinks[2], 'click');
    $scope.$digest();

    collapsedPanels = element.find('.collapse.in');
    expect(collapsedPanels.length).toBe(1);

    eventFire(collapseLinks[2], 'click');
    $scope.$digest();

    collapsedPanels = element.find('.collapse.in');
    expect(collapsedPanels.length).toBe(0);
  });

  it ('should invoke the action button callback when the action button is clicked', function () {
    var collapseElements = element.find('.panel-collapse.collapse');
    expect(collapseElements.length).toBe(5);

    var actionButton = angular.element(collapseElements[1]).find('.drawer-pf-action .btn-link');
    expect(actionButton.length).toBe(1);
    expect($scope.actionButtonClicked).toBe('');

    eventFire(actionButton[0], 'click');
    $scope.$digest();

    expect($scope.actionButtonClicked).toBe('Group 2');
  });

  it ('should perform actions when kebab items are clicked', function () {
    var menuItems = element.find('.panel-collapse.collapse .drawer-pf-notification .dropdown-kebab-pf .secondary-action');
    expect(menuItems.length).toBe(87);

    expect($scope.actionPerformed).toBeUndefined();

    eventFire(menuItems[0], 'click');
    $scope.$digest();

    expect($scope.actionPerformed.name).toBe('Action1');
  });

  it ('should show the expand toggle when allow expand is set to true', function () {
    var expandToggle = element.find('.drawer-pf-toggle-expand');
    expect(expandToggle.length).toBe(0);

    var htmlTmp = '<pf-notification-drawer allow-expand="true" drawer-hidden="hideDrawer" drawer-title="Notifications Drawer"  title-include="test/notification/title.html" ' +
      '     action-button-title="Mark All Read" action-button-callback="actionButtonCB" notification-groups="groups"' +
      '     heading-include="test/notification/heading.html" subheading-include="test/notification/subheading.html" notification-body-include="test/notification/notification-body.html"' +
      '     notification-footer-include="test/notification/notification-footer.html" custom-scope="customScope">' +
      '</pf-notification-drawer>';

    compileHTML(htmlTmp, $scope);

    expandToggle = element.find('.drawer-pf-toggle-expand');
    expect(expandToggle.length).toBe(1);
  });

  it ('should expand the drawer when the expand toggle is clicked', function () {
    var expandedDrawer = element.find('.drawer-pf.drawer-pf-expanded');
    expect(expandedDrawer.length).toBe(0);

    var htmlTmp = '<pf-notification-drawer allow-expand="true" drawer-hidden="hideDrawer" drawer-title="Notifications Drawer"  title-include="test/notification/title.html" ' +
      '     action-button-title="Mark All Read" action-button-callback="actionButtonCB" notification-groups="groups"' +
      '     heading-include="test/notification/heading.html" subheading-include="test/notification/subheading.html" notification-body-include="test/notification/notification-body.html"' +
      '     notification-footer-include="test/notification/notification-footer.html" custom-scope="customScope">' +
      '</pf-notification-drawer>';

    compileHTML(htmlTmp, $scope);

    expandedDrawer = element.find('.drawer-pf.drawer-pf-expanded');
    expect(expandedDrawer.length).toBe(0);
    var expandToggle = element.find('.drawer-pf-toggle-expand');
    expect(expandToggle.length).toBe(1);

    eventFire(expandToggle[0], 'click');
    $scope.$digest();

    expandedDrawer = element.find('.drawer-pf.drawer-pf-expanded');
    expect(expandedDrawer.length).toBe(1);
  });
});
