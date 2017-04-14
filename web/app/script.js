var jmriApp = angular.module('jmri.app', [
  // dependencies %1$s
]).config(['$routeProvider',
  function ($routeProvider) {
    'use strict';

    $routeProvider
    // routes %2$s
    // Default
    .otherwise({redirectTo: '/'});
  }
]).controller('NavigationCtrl', ['$scope', '$http',
  function ($scope, $http) {
    // navigation items %3$s

    $http.get('/app/about').then(function (response) {
      $scope.additionalInfo = response.data.additionalInfo;
      $scope.copyright = response.data.copyright;
      $scope.imgAlt = response.data.imgAlt;
      $scope.imgSrc = response.data.imgSrc;
      $scope.title = response.data.title;
      $scope.productInfo = response.data.productInfo;
    });
    $scope.openAboutModal = function () {
      $scope.isAboutModalOpen = true;
    };
    $scope.onAboutModalClose = function() {
      $scope.isAboutModalOpen = false;
    };
  }
]);
