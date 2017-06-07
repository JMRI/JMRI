angular.module('patternfly.wizard').controller('DetailsGeneralController', ['$rootScope', '$scope',
  function ($rootScope, $scope) {
    'use strict';

    $scope.reviewTemplate = "test/wizard/review-template.html";

    $scope.onShow = function() {
      $scope.detailsGeneralComplete = false;
    };

    $scope.updateName = function() {
      $scope.detailsGeneralComplete = angular.isDefined($scope.data.name) && $scope.data.name.length > 0;
    };
  }
]);

angular.module('patternfly.wizard').controller('DetailsReviewController', ['$rootScope', '$scope',
  function ($rootScope, $scope) {
    'use strict';

    // Find the data!
    var next = $scope;
    while (angular.isUndefined($scope.data)) {
      next = next.$parent;
      if (angular.isUndefined(next)) {
        $scope.data = {};
      } else {
        $scope.data = next.$ctrl.wizardData;
      }
    }
  }
]);

angular.module('patternfly.wizard').controller('SummaryController', ['$rootScope', '$scope', '$timeout',
  function ($rootScope, $scope, $timeout) {
    'use strict';
    $scope.pageShown = false;

    $scope.onShow = function () {
      $scope.pageShown = true;
      $timeout(function () {
        $scope.pageShown = false;  // done so the next time the page is shown it updates
      });
    }
  }
]);

angular.module('patternfly.wizard').controller('DeploymentController', ['$rootScope', '$scope', '$timeout',
  function ($rootScope, $scope, $timeout) {
    'use strict';

    $scope.onShow = function() {
      $scope.deploymentComplete = false;
      $timeout(function() {
        $scope.deploymentComplete = true;
      }, 2500);
    };
  }
]);
