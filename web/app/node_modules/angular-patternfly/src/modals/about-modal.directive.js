/**
 * @ngdoc directive
 * @name patternfly.modals.directive:pfAboutModal
 *
 * @description
 * Directive for rendering modal windows.
 *
 * @param {string=} additionalInfo Text explaining the version or copyright
 * @param {string=} copyright Product copyright information
 * @param {string=} imgAlt The alt text for the corner grahpic
 * @param {string=} imgSrc The source for the corner grahpic
 * @param {boolean=} isOpen Flag indicating that the modal should be opened
 * @param {function=} onClose Function to call when modal is closed
 * @param {object=} productInfo data for the modal:<br/>
 * <ul style='list-style-type: none'>
 * <li>.product - the product label
 * <li>.version - the product version
 * </ul>
 * @param {string=} title The product title for the modal
 *
 * @example
 <example module="patternfly.modals">
   <file name="index.html">
     <div ng-controller="ModalCtrl">
       <button ng-click="open()" class="btn btn-default">Launch About Modal</button>
       <div pf-about-modal is-open="isOpen" on-close="onClose()" additional-info="additionalInfo"
            product-info="productInfo" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc"></div>
     </div>
   </file>
   <file name="script.js">
     angular.module('patternfly.modals').controller('ModalCtrl', function ($scope) {
       $scope.additionalInfo = "Donec consequat dignissim neque, sed suscipit quam egestas in. Fusce bibendum " +
         "laoreet lectus commodo interdum. Vestibulum odio ipsum, tristique et ante vel, iaculis placerat nulla. " +
         "Suspendisse iaculis urna feugiat lorem semper, ut iaculis risus tempus.";
       $scope.copyright = "Trademark and Copyright Information";
       $scope.imgAlt = "Patternfly Symbol";
       $scope.imgSrc = "img/logo-alt.svg";
       $scope.title = "Product Title";
       $scope.productInfo = [
         { name: 'Version', value: '1.0.0.0.20160819142038_51be77c' },
         { name: 'Server Name', value: 'Localhost' },
         { name: 'User Name', value: 'admin' },
         { name: 'User Role', value: 'Administrator' }];
       $scope.open = function () {
         $scope.isOpen = true;
       }
       $scope.onClose = function() {
         $scope.isOpen = false;
       }
     });
   </file>
 </example>
 */
angular.module('patternfly.modals')

.directive("pfAboutModalTransclude", function ($parse) {
  'use strict';
  return {
    link: function (scope, element, attrs) {
      element.append($parse(attrs.pfAboutModalTransclude)(scope));
    }
  };
})

.directive('pfAboutModal', function () {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      additionalInfo: '=?',
      copyright: '=?',
      close: "&onClose",
      imgAlt: '=?',
      imgSrc: '=?',
      isOpen: '=?',
      productInfo: '=',
      title: '=?'
    },
    templateUrl: 'modals/about-modal.html',
    transclude: true,
    controller: ['$scope', '$uibModal', '$transclude', function ($scope, $uibModal, $transclude) {
      if ($scope.isOpen === undefined) {
        $scope.isOpen = false;
      }

      // The ui-bootstrap modal only supports either template or templateUrl as a way to specify the content.
      // When the content is retrieved, it is compiled and linked against the provided scope by the $uibModal service.
      // Unfortunately, there is no way to provide transclusion there.
      //
      // The solution below embeds a placeholder directive (i.e., pfAboutModalTransclude) to append the transcluded DOM.
      // The transcluded DOM is from a different location than the modal, so it needs to be handed over to the
      // placeholder directive. Thus, we're passing the actual DOM, not the parsed HTML.
      $scope.openModal = function () {
        $uibModal.open({
          controller: ['$scope', '$uibModalInstance', 'content', function ($scope, $uibModalInstance, content) {
            $scope.template = content;
            $scope.close = function () {
              $uibModalInstance.close();
            };
            $scope.$watch(
              function () {
                return $scope.isOpen;
              },
              function (newValue) {
                if (newValue === false) {
                  $uibModalInstance.close();
                }
              }
            );
          }],
          resolve: {
            content: function () {
              var transcludedContent;
              $transclude(function (clone) {
                transcludedContent = clone;
              });
              return transcludedContent;
            }
          },
          scope: $scope,
          templateUrl: "about-modal-template.html"
        })
        .result.then(
          function () {
            $scope.close(); // closed
          },
          function () {
            $scope.close(); // dismissed
          }
        );
      };
    }],
    link: function (scope, element, attrs) {
      // watching isOpen attribute to dispay modal when needed
      var isOpenListener = scope.$watch('isOpen', function (newVal, oldVal) {
        if (newVal === true) {
          scope.openModal();
        }
      });
      scope.$on('$destroy', isOpenListener);
    }
  };
});
