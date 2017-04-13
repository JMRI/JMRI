angular.module ('jmri.app', [
  // dependencies %1$s
]).config(['$routeProvider',
  function ($routeProvider) {
    'use strict';

    $routeProvider
      // routes %2$s
      // Default
      .otherwise({redirectTo:'/'});
  }
]).controller('vertNavController', ['$scope',
    function ($scope) {
        // navigation items %3$s
    }
]);
