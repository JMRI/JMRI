describe('pf-notification', function () {

  var $scope, $compile, Notifications, $timeout;

  beforeEach(module(
    'patternfly.notification',
    'notification/inline-notification.html',
    'notification/notification-list.html'
  ));

  beforeEach(inject(function (_$rootScope_, _$compile_, _$timeout_, _Notifications_) {
    $scope = _$rootScope_;
    $compile = _$compile_;
    $timeout = _$timeout_;
    Notifications = _Notifications_;
  }));

  describe('The notifications', function () {

    it('should propagate to the $rootScope correctly', function () {

      expect(Notifications.data.length).toBe(0);

      Notifications.info('aloha');

      expect(Notifications.data.length).toBe(1);
      expect(Notifications.data[0].type).toBe('info');
      expect(Notifications.data[0].message).toBe('aloha');

      Notifications.warn('achtung');

      expect(Notifications.data.length).toBe(2);
      expect(Notifications.data[1].type).toBe('warning');
      expect(Notifications.data[1].message).toBe('achtung');

      Notifications.success('allright');

      expect(Notifications.data.length).toBe(3);
      expect(Notifications.data[2].type).toBe('success');
      expect(Notifications.data[2].message).toBe('allright');

      Notifications.error('noway');

      expect(Notifications.data.length).toBe(4);
      expect(Notifications.data[3].type).toBe('danger');
      expect(Notifications.data[3].message).toBe('noway');

      var httpResponse = {data:{message: 'http'}};

      Notifications.httpError('error', httpResponse);

      expect(Notifications.data.length).toBe(5);
      expect(Notifications.data[4].type).toBe('danger');
      expect(Notifications.data[4].message).toBe('error (http)');

      $timeout.flush();
      expect(Notifications.data.length).toBe(2);
    });
  });

  describe('The pf-notifications', function () {

    var compile = function (markup, scope) {
      var el = $compile(markup)(scope);
      scope.$digest();
      return el;
    };

    it('should render correctly the type', function () {

      var types = ['success','info','danger', 'warning'];

      $scope.type = types[0];
      $scope.header = "head1";
      $scope.message = "defaultMes";

      var notifyElement = compile('<pf-inline-notification pf-notification-type="type" pf-notification-header="header" pf-notification-message="message"></pf-inline-notification>', $scope);

      var alertElement = angular.element(notifyElement.children().get(0));
      var closeButton = angular.element(alertElement.find('span.pficon-close'));
      var iconOk = angular.element(alertElement.find('span.pficon-ok'));
      var iconInfo = angular.element(alertElement.find('span.pficon-info'));
      var iconError = angular.element(alertElement.find('span.pficon-error-circle-o'));
      var iconWarning = angular.element(alertElement.find('span.pficon-warning-triangle-o'));

      expect(closeButton).toBeDefined();
      expect(alertElement.text()).toContain('head1 defaultMes');
      expect(alertElement).toHaveClass('alert');
      expect(alertElement).toHaveClass('alert-success');
      expect(iconOk).not.toHaveClass('ng-hide');
      expect(iconInfo).toHaveClass('ng-hide');
      expect(iconError).toHaveClass('ng-hide');
      expect(iconWarning).toHaveClass('ng-hide');

      $scope.$apply(function(){
        $scope.type = types[1];
      });

      expect(iconOk).toHaveClass('ng-hide');
      expect(iconInfo).not.toHaveClass('ng-hide');
      expect(iconError).toHaveClass('ng-hide');
      expect(iconWarning).toHaveClass('ng-hide');

      $scope.$apply(function(){
        $scope.type = types[2];
      });

      expect(iconOk).toHaveClass('ng-hide');
      expect(iconInfo).toHaveClass('ng-hide');
      expect(iconError).not.toHaveClass('ng-hide');
      expect(iconWarning).toHaveClass('ng-hide');

      $scope.$apply(function(){
        $scope.type = types[3];
      });

      expect(iconOk).toHaveClass('ng-hide');
      expect(iconInfo).toHaveClass('ng-hide');
      expect(iconError).toHaveClass('ng-hide');
      expect(iconWarning).not.toHaveClass('ng-hide');

    });

    it('should render correctly the message and header', function () {

      var types = ['success','info','danger', 'warning'];

      $scope.type = types[0];
      $scope.header = "head1";
      $scope.message = "defaultMes";

      var notifyElement = compile('<pf-inline-notification pf-notification-type="type" pf-notification-header="header" pf-notification-message="message"></pf-inline-notification>', $scope);

      var alertElement = angular.element(notifyElement.children().get(0));
      var closeButton = angular.element(alertElement.find('span.pficon-close'));

      expect(closeButton).toBeDefined();
      expect(alertElement.text()).toContain('head1 defaultMes');

      $scope.$apply(function(){
        $scope.header = "head2";
      });

      closeButton = angular.element(alertElement.find('span.pficon-close'));
      expect(closeButton).toBeDefined();
      expect(alertElement.text()).toContain('head2 defaultMes');

      $scope.$apply(function(){
        $scope.message = "defaultMes2";
      });

      closeButton = angular.element(alertElement.find('span.pficon-close'));
      expect(closeButton).toBeDefined();
      expect(alertElement.text()).toContain('head2 defaultMes2');

    });
  });

  describe('The pf-notifications-list', function () {

    var compile = function (markup, scope) {
      var el = $compile(markup)(scope);
      scope.$digest();
      return el;
    };

    it('should have the right number of alerts', function () {

      var types = ['success','info','danger', 'warning'];

      $scope.type = types[0];
      $scope.header = "head1";
      $scope.message = "defaultMes";

      var element = compile('<pf-notification-list></pf-notification-list>', $scope);

      var notifyList = angular.element(element.children().get(0));

      expect(notifyList.find('div.alert').length).toBe(0);

      $scope.$apply(function(){
        Notifications.info('aloha');
      });

      expect(notifyList.find('div.alert').length).toBe(1);

      $scope.$apply(function(){
        Notifications.info('ahoj');
      });

      expect(notifyList.find('div.alert').length).toBe(2);

      $timeout.flush();

      expect(notifyList.find('div.alert').length).toBe(0);
    });
  });
});
