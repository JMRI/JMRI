describe('pf-validation', function () {

  var $scope, $compile;

  beforeEach(module('patternfly.validation'));

  beforeEach(inject(function (_$rootScope_, _$compile_) {
    $scope = _$rootScope_;
    $compile = _$compile_;
  }));

  describe('Input with pf-validation directive', function () {

    var compileInput = function (markup, scope) {
      var pre = '<form class="form-horizontal" id="myForm" name="myForm"><div class="form-group">' +
        '<label class="col-sm-2 control-label" for="foo">Number:</label><div class="col-sm-10">';
      var post = '<span class="help-block">The value you typed is not a number.</span>' +
        '</div></div></form>';
      var el = $compile(pre + markup + post)(scope);
      scope.$digest();
      return el;
    };

    var validationFunc = function(input){
      if (input === 'abc'){
        return true;
      }
      return false;
    }

    it('should show a validation message when the ng-model is initially invalid', function () {

      $scope.bar = 'abcd';

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" ng-model="bar" id="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).toHaveClass('has-error');
      expect(inputPage.find('span')).not.toHaveClass('ng-hide');
    });

    it('should not show a validation message when the ng-model is initially valid', function () {

      $scope.bar = 'abc';

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" ng-model="bar" id="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');
    });


    it('should respond to ng-model change', function () {

      $scope.bar = 'abc';

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" ng-model="bar" id="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');

      $scope.$apply(function() {
        $scope.bar = 'abcd';
      });

      expect(inputPage.find('div.col-sm-10')).toHaveClass('has-error');
      expect(inputPage.find('span')).not.toHaveClass('ng-hide');

      $scope.$apply(function() {
        $scope.bar = 'abc';
      });

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');
    });

    it('should set the $valid property properly', function () {

      $scope.bar = 'abc';

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" ng-model="bar" id="foo" name="foo"/>', $scope);

      expect($scope.myForm.foo.$valid).toBe(true);

      $scope.$apply(function() {
        $scope.bar = 'abcd';
      });

      expect($scope.myForm.foo.$valid).toBe(false);

      $scope.$apply(function() {
        $scope.bar = 'abc';
      });

      expect($scope.myForm.foo.$valid).toBe(true);

    });

    it('should evaluate ng-model when enabled initially', function () {
      $scope.bar = 'abcd';
      $scope.switch = false;

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" pf-validation-disabled="switch" ng-model="bar" id="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).toHaveClass('has-error');
      expect(inputPage.find('span')).not.toHaveClass('ng-hide');
    });

    it('should not evaluate ng-model when disabled initially', function () {
      $scope.bar = 'abcd';
      $scope.switch = true;

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" pf-validation-disabled="switch" ng-model="bar" id="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');
    });

    it('should not evaluate ng-model when disabled', function () {
      $scope.bar = 'abcd';
      $scope.switch = true;

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation="foo(input)" pf-validation-disabled="switch" ng-model="bar" id="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');

      $scope.$apply(function() {
        $scope.switch = false;
      });

      expect(inputPage.find('div.col-sm-10')).toHaveClass('has-error');
      expect(inputPage.find('span')).not.toHaveClass('ng-hide');

      $scope.$apply(function() {
        $scope.switch = true;
      });

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');
    });

    it('should work without validation function', function () {
      $scope.bar = 'abcd';
      $scope.switch = true;

      $scope.foo = validationFunc;

      var inputPage = compileInput('<input type="text" pf-validation ng-model="bar" id="foo" name="foo"/>', $scope);

      expect(inputPage.find('div.col-sm-10')).not.toHaveClass('has-error');
      expect(inputPage.find('span')).toHaveClass('ng-hide');

      $scope.$apply(function() {
        $scope.myForm.foo.$setValidity('foo', false);
      });

      expect(inputPage.find('div.col-sm-10')).toHaveClass('has-error');
      expect(inputPage.find('span')).not.toHaveClass('ng-hide');
    });
  });
});