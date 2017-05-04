describe('Directive: pfDatepicker', function() {
  var $scope, $compile, $timeout, element, datepicker, dateInput;

  beforeEach(module('patternfly.form', 'form/datepicker/datepicker.html'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileDatepicker = function(markup, $scope) {
    var el = $compile(markup)($scope);
    $scope.$apply();
    return el;
  };

  it("should set the date picker input", function() {
    $scope.options = {};

    datepicker = compileDatepicker('<form><div pf-datepicker options="options" date="date"></div></form>', $scope);
    dateInput = angular.element(datepicker).find('input');
    expect(dateInput.val()).toBe('');

    $scope.date = new Date("October 13, 2014 11:13:00");
    $scope.$digest();

    expect(dateInput.val()).toBe('10/13/2014');
  });

  it("should set the angular model", function() {
    $scope.options = {};

    datepicker = compileDatepicker('<form><div pf-datepicker options="options" date="date"></div></form>', $scope);
    dateInput = angular.element(datepicker).find('input');
    dateInput.datepicker('update', new Date("October 4, 2015 11:13:00"));

    $scope.$digest();

    expect(dateInput.val()).toBe('10/04/2015');
  });

});
