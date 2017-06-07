/**
 * @ngdoc directive
 * @name patternfly.modals.component:pfAboutModal
 * @restrict E
 *
 * @description
 * Component for rendering modal windows.
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
       <pf-about-modal is-open="isOpen" on-close="onClose()" additional-info="additionalInfo"
            product-info="productInfo" title="title" copyright="copyright" img-alt="imgAlt" img-src="imgSrc"></pf-about-modal>
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
.component('pfModalContent', {
  templateUrl: 'about-modal-template.html',
  bindings: {
    resolve: '<',
    close: '&',
    dismiss: '&'
  },
  controller: function () {
    'use strict';
    var $ctrl = this;

    $ctrl.$onInit = function () {
      $ctrl.additionalInfo = $ctrl.resolve.additionalInfo;
      $ctrl.copyright = $ctrl.resolve.copyright;
      $ctrl.imgAlt = $ctrl.resolve.imgAlt;
      $ctrl.imgSrc = $ctrl.resolve.imgSrc;
      $ctrl.isOpen = $ctrl.resolve.isOpen;
      $ctrl.productInfo = $ctrl.resolve.productInfo;
      $ctrl.title = $ctrl.resolve.title;
      $ctrl.template = $ctrl.resolve.content;
    };
  }
})
.component('pfAboutModal', {
  bindings: {
    additionalInfo: '=?',
    copyright: '=?',
    close: "&onClose",
    imgAlt: '=?',
    imgSrc: '=?',
    isOpen: '<?',
    productInfo: '=',
    title: '=?'
  },
  templateUrl: 'modals/about-modal.html',
  transclude: true,
  controller: function ($uibModal, $transclude) { //$uibModal, $transclude, $window
    'use strict';
    var ctrl = this;

    // The ui-bootstrap modal only supports either template or templateUrl as a way to specify the content.
    // When the content is retrieved, it is compiled and linked against the provided scope by the $uibModal service.
    // Unfortunately, there is no way to provide transclusion there.
    //
    // The solution below embeds a placeholder directive (i.e., pfAboutModalTransclude) to append the transcluded DOM.
    // The transcluded DOM is from a different location than the modal, so it needs to be handed over to the
    // placeholder directive. Thus, we're passing the actual DOM, not the parsed HTML.
    ctrl.openModal = function () {
      //$window.console.log('hi mom');
      $uibModal.open({
        component: 'pfModalContent',
        resolve: {
          content: function () {
            var transcludedContent;
            $transclude(function (clone) {
              transcludedContent = clone;
            });
            return transcludedContent;
          },
          additionalInfo: function () {
            return ctrl.additionalInfo;
          },
          copyright: function () {
            return ctrl.copyright;
          },
          close: function () {
            return ctrl.close;
          },
          imgAlt: function () {
            return ctrl.imgAlt;
          },
          imgSrc: function () {
            return ctrl.imgSrc;
          },
          isOpen: function () {
            return ctrl.isOpen;
          },
          productInfo: function () {
            return ctrl.productInfo;
          },
          title: function () {
            return ctrl.title;
          }
        }
      })
        .result.then(
        function () {
          ctrl.close(); // closed
        },
        function () {
          ctrl.close(); // dismissed
        }
      );
    };
    ctrl.$onInit = function () {
      if (ctrl.isOpen === undefined) {
        ctrl.isOpen = false;
      }
    };

    ctrl.$onChanges = function (changesObj) {
      if (changesObj.isOpen && changesObj.isOpen.currentValue === true) {
        ctrl.openModal();
      }
    };
  }
});
