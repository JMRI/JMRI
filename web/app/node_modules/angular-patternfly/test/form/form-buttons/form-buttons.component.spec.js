describe('Component: pfFormButtons', function() {
  var $scope, $compile, element, button;

  beforeEach(module(
    'patternfly.form',
    'form/form-buttons/form-buttons.html'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  beforeEach(function() {
    element = '<form name="testForm">' +
                '<input name="name" ng-model="fake.name" required>' +
                '<pf-form-buttons ' +
                    'pf-on-cancel="transitionTo(\'product.index\')" ' +
                    'pf-on-save="save(product)" ' +
                    'pf-working="working"> ' +
               '</pf-form-buttons>' +
             '</form>';

    element = $compile(element)($scope);
    $scope.$digest();
  });

  it("should set create button to disabled if no server validator is set but the form is invalid", function() {
    button = angular.element(element).find('.btn-primary').attr('disabled');
    expect(button).toBe('disabled');
  });

  it("should set create button to enabled if a server validator is set", function() {
    $scope.testForm.name.$error.server = true;
    $scope.$digest();
    button = angular.element(element).find('.btn-primary').attr('disabled');
    expect(button).toBe(undefined);
  });
});
