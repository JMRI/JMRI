describe('Directive: pfDateTimepicker', function() {
  var $scope, $compile, $timeout, element, datepicker, dateInput, dateElement;

  beforeEach(module('patternfly.form', 'form/datetimepicker/datetimepicker.html'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileDateTimepicker = function(markup, $scope) {
    var el = $compile(markup)($scope);
    $scope.$apply();
    return el;
  };

  it("should set the date and time picker input", function() {
    $scope.options = {format: 'HH:mm'};
    $scope.date = moment("2016-01-01 11:31:23");

    datepicker = compileDateTimepicker('<form><div pf-date-timepicker options="options" date="date"></div></form>', $scope);
    dateInput = angular.element(datepicker).find('input');

    expect(dateInput.val()).toBe('11:31');
  });

  it("should set the angular model", function() {
    $scope.options = {format: 'HH:mm'};

    datepicker = compileDateTimepicker('<form><div pf-date-timepicker options="options" date="date"></div></form>', $scope);
    dateInput = angular.element(datepicker).find('input');
    dateElement = angular.element(datepicker).find('div');
    dateElement.datetimepicker('date', moment("2016-01-01 11:31:23"));

    expect(dateInput.val()).toBe('11:31');
  });

});
