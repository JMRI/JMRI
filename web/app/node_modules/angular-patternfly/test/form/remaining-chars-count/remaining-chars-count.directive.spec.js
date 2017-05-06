describe('Directive: pfRemainingCharsCount', function() {
  var $scope, $compile, isoScope, element;

  beforeEach(module(
    'patternfly.form'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileRemainingCharsCount = function(markup, $scope) {
    var el = $compile(markup)($scope);
    $scope.$apply();
    isoScope = el.isolateScope();
    return el;
  };

  it("should count remaining characters", function() {
    $scope.messageAreaText = "initial Text";

    element = compileRemainingCharsCount('<textarea pf-remaining-chars-count ng-model="messageAreaText" ' +
      'chars-max-limit="20" chars-warn-remaining="2" count-fld="charRemainingCntFld"></textarea>' +
      '<span id="charRemainingCntFld"></span>', $scope);

    expect(isoScope.ngModel).toBe('initial Text');
    expect(isoScope.remainingChars).toBe(8);
    expect(isoScope.remainingCharsWarning).toBeFalsy();
  });

  it("should warn when remaining characters threshold met", function() {
    $scope.messageAreaText = "initial Text";

    element = compileRemainingCharsCount('<textarea pf-remaining-chars-count ng-model="messageAreaText" ' +
      'chars-max-limit="20" chars-warn-remaining="10" count-fld="charRemainingCntFld"></textarea>'+
      '<span id="charRemainingCntFld"></span>', $scope);

    expect(isoScope.ngModel).toBe('initial Text');
    expect(isoScope.remainingChars).toBe(8);
    expect(isoScope.remainingCharsWarning).toBeTruthy();
  });

  it("should allow negative remaining characters by default", function() {
    $scope.messageAreaText = "initial Text";

    element = compileRemainingCharsCount('<textarea pf-remaining-chars-count ng-model="messageAreaText" ' +
      'chars-max-limit="5" chars-warn-remaining="2" count-fld="charRemainingCntFld"></textarea>' +
      '<span id="charRemainingCntFld"></span>', $scope);

    expect(isoScope.ngModel).toBe('initial Text');
    expect(isoScope.remainingChars).toBe(-7);
    expect(isoScope.remainingCharsWarning).toBeTruthy();
  });

  it("should not allow negative remaining characters when blockInputAtMaxLimit is true", function() {
    $scope.messageAreaText = "initial Text";

    element = compileRemainingCharsCount('<textarea pf-remaining-chars-count ng-model="messageAreaText" ' +
      'chars-max-limit="5" chars-warn-remaining="2" count-fld="charRemainingCntFld"' +
      'block-input-at-max-limit="true"></textarea><span id="charRemainingCntFld"></span>', $scope);

    expect(isoScope.ngModel).toBe('initi');
    expect(isoScope.remainingChars).toBe(0);
    expect(isoScope.remainingCharsWarning).toBeTruthy();
  });

});
