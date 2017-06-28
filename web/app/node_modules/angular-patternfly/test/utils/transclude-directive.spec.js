angular.module('patternfly.pfTranscludeTestHelper',['patternfly.utils'])
  .controller( 'TestUtilCtrl', function($scope) {
  })
  .config(function($provide){
    $provide.decorator('ngTranscludeDirective', ['$delegate', function($delegate) {
      // Remove the original directive
      $delegate.shift();
      return $delegate;
    }]);
  })
  .directive( 'transcludeSibling', function() {
    return {
      restrict: 'EAC',
      transclude: true,
      scope: {},
      template:
      '<div>' +
      '  <input id="directiveId" value="{{$id}}">' +
      '  <div pf-transclude></div>' +
      '</div>'
    }
  })
  .directive( 'transcludeParent', function() {
    return {
      restrict: 'EAC',
      transclude: true,
      scope: {},
      template:
      '<div>' +
      '  <input id="directiveId" value="{{$id}}">' +
      '  <div pf-transclude="parent"></div>' +
      '</div>'
    }
  })
  .directive( 'transcludeChild', function() {
    return {
      restrict: 'EAC',
      transclude: true,
      scope: {},
      template:
      '<div>' +
      '  <input id="directiveId" value="{{$id}}">' +
      '  <div pf-transclude="child"></div>' +
      '</div>'
    }
  })
;

describe('Directive:  pfTransclude', function () {
  var $scope;
  var $compile;
  var outerId;
  var directiveId;
  var childId;
  var parentId;

  // load the controller's module
  beforeEach(function () {
    module('patternfly.utils');
    module('patternfly.pfTranscludeTestHelper');
  });

  beforeEach(inject(function (_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  var compileHTML = function (transludeDirective, scope) {
    var markup = '<div ng-controller="TestUtilCtrl" >' +
      '  <input id="outerId" value="{{$id}}">{{$id}}</input>' +
      '  <div ' + transludeDirective + ' class="pf-transclude-example">' +
      '    <input id="childId" value="{{$id}}">' +
      '    <input id="parentId" value="{{$parent.$id}}">' +
      '  </div>' +
      '</div';
    var element = $compile(angular.element(markup))(scope);
    scope.$digest();

    outerId = parseInt(element.find('#outerId')[0].value);
    directiveId = parseInt(element.find('#directiveId')[0].value);
    childId = parseInt(element.find('#childId')[0].value);
    parentId = parseInt(element.find('#parentId')[0].value);
  };

  it('should have correct sibling scope', function () {
    compileHTML('transclude-sibling', $scope);

    expect(directiveId).toBe(outerId + 1);
    expect(childId).toBe(directiveId + 1);
    expect(parentId).toBe(outerId + 1);
  });

  it('should have correct parent scope', function () {
    compileHTML('transclude-parent', $scope);

    expect(directiveId).toBe(outerId + 1);
    expect(childId).toBe(directiveId);
    expect(parentId).toBe(outerId);
  });

  it('should have correct child scope', function () {
    compileHTML('transclude-child', $scope);

    expect(directiveId).toBe(outerId + 1);
    expect(childId).toBe(directiveId + 1);
    expect(parentId).toBe(directiveId);
  });
});
