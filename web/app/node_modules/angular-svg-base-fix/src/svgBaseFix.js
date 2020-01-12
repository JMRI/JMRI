(function (root, factory) {
  'use strict';
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['angular'], factory);
  } else if (typeof module !== 'undefined' && typeof module.exports === 'object') {
    // CommonJS support (for us webpack/browserify/ComponentJS folks)
    module.exports = factory(require('angular'));
  } else {
    // in the case of no module loading system
    // then don't worry about creating a global
    // variable like you would in normal UMD.
    // It's not really helpful... Just call your factory
    return factory(root.angular);
  }
}(this, function (angular) {
  'use strict';

  var moduleName = 'svgBaseFix';
  var attr = 'xlinkHref';

  angular.module(moduleName, [])

    .directive(attr, ['$rootScope', function ($rootScope) {
      return {
        restrict: 'A',
        link: function (scope, element, attrs) {
          var initialHref = attrs[attr];
          var parsingNode;

          if (!initialHref || initialHref.charAt(0) !== '#') {
            return;
          }

          parsingNode = document.createElement('a');

          attrs.$observe(attr, updateValue);
          $rootScope.$on('$locationChangeSuccess', updateValue);

          function updateValue() {
            var newVal;
            parsingNode.setAttribute(
              'href',
              location.pathname + location.search + initialHref
            );
            newVal = parsingNode.toString();
            if (newVal && attrs[attr] !== newVal) {
              attrs.$set(attr, newVal);
            }
          }
        }
      };
    }]);

  return moduleName;
}));
