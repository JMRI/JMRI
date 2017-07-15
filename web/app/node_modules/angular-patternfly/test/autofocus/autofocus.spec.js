describe('pf-autofocus', function () {

  var $scope, $compile, $timeout;

  beforeEach(module('patternfly.autofocus'));
  beforeEach(inject(function (_$rootScope_, _$compile_, _$timeout_) {
    $scope = _$rootScope_;
    $compile = _$compile_;
    $timeout = _$timeout_;
  }));

  describe('Input with pf-focused directive', function () {

    var compileElement = function (markup, scope) {
      var el = $compile(markup)(scope);
      scope.$digest();
      return el;
    };

    it('should be focused when set true', function () {

      $scope.iFocused = true;

      var page = compileElement('<form><input ng-model="m1" id="i1" type="text"/></form><form><input id="i2" ng-model="m2" pf-focused="iFocused" type="text"/></form>', $scope);

      var body = angular.element(document.body);
      body.html(page);

      var eFocused = page.find("#i2")[0];
      var eUnfocused = page.find("#i1")[0];

      $timeout.flush();

      expect(eFocused === document.activeElement).toBe(true);
      expect(eUnfocused === document.activeElement).toBe(false);

    });

    it('should be focused when set false', function () {

      $scope.iFocused1 = false;
      $scope.iFocused2 = false;

      var page = compileElement('<form><input id="i1" pf-focused="iFocused1" type="text"/></form><form><input id="i2" pf-focused="iFocused2" type="text"/></form>', $scope);

      body = angular.element(document.body);
      body.html(page);

      var eUnFocused1 = page.find("#i1")[0];
      var eUnFocused2 = page.find("#i2")[0];

      $scope.$apply(function(){
        $scope.iFocused1 = true;
        $scope.iFocused2 = false;
      });

      $timeout.flush();

      expect(eUnFocused1 === document.activeElement).toBe(true);
      expect(eUnFocused2 === document.activeElement).toBe(false);

      $scope.$apply(function(){
        $scope.iFocused1 = false;
        $scope.iFocused2 = true;
      });

      $timeout.flush();

      expect(eUnFocused1 === document.activeElement).toBe(false);
      expect(eUnFocused2 === document.activeElement).toBe(true);

    });

    it('should respond to its attribute value', function () {

      $scope.iFocused = false;

      var page = compileElement('<form><input id="i1" type="text"/></form><form><input id="i2" pf-focused="iFocused" type="text"/></form>', $scope);

      var body = angular.element(document.body);
      body.html(page);

      var eUnFocused1 = page.find("#i1")[0];
      var eUnfocused2 = page.find("#i2")[0];

      $timeout.flush();

      expect(eUnFocused1 === document.activeElement).toBe(false);
      expect(eUnfocused2 === document.activeElement).toBe(false);

      $scope.$apply(function(){
        $scope.iFocused = true;
      });

      $timeout.flush();

      expect(eUnFocused1 === document.activeElement).toBe(false);
      expect(eUnfocused2 === document.activeElement).toBe(true);

      $scope.$apply(function(){
        $scope.iFocused = false;
      });

      $timeout.flush();

      expect(eUnFocused1 === document.activeElement).toBe(false);

      // The focus set to true doesn't mean the element looses focus, only that the focus is not guaranteed
      expect(eUnfocused2 === document.activeElement).toBe(true);

    });
  });
});