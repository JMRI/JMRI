/**
 * @ngdoc directive
 * @name patternfly.utils:pfFixedAccordion
 * @restrict A
 * @element ANY
 * @param {string} scrollSelector specifies the selector to be used to find the element that should scroll (optional, the entire collapse area scrolls by default)
 * @param {string} groupHeight Height to set for uib-accordion group (optional)
 * @param {string} groupClass Class to set for uib-accordion group (optional)
 *
 * @description
 *   Directive for setting a ui-bootstrap uib-accordion to use a fixed height (collapse elements scroll when necessary)
 *
 * @example
 <example module="patternfly.utils" deps="ui.bootstrap">
 <file name="index.html">
 <div class="row example-container">
   <div class="col-md-4">
     <uib-accordion  pf-fixed-accordion  group-height="350px" close-others="true">
       <uib-accordion-group is-open="false" heading="Lorem ipsum">
         Praesent sagittis est et arcu fringilla placerat. Cras erat ante, dapibus non mauris ac, volutpat sollicitudin ligula. Morbi gravida nisl vel risus tempor, sit amet luctus erat tempus. Curabitur blandit sem non pretium bibendum. Donec eleifend non turpis vitae vestibulum. Vestibulum ut sem ac nunc posuere blandit sed porta lorem. Cras rutrum velit vel leo iaculis imperdiet.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Dolor sit amet">
         Donec consequat dignissim neque, sed suscipit quam egestas in. Fusce bibendum laoreet lectus commodo interdum. Vestibulum odio ipsum, tristique et ante vel, iaculis placerat nulla. Suspendisse iaculis urna feugiat lorem semper, ut iaculis risus tempus.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Consectetur">
         Curabitur nisl quam, interdum a venenatis a, consequat a ligula. Nunc nec lorem in erat rhoncus lacinia at ac orci. Sed nec augue congue, vehicula justo quis, venenatis turpis. Nunc quis consectetur purus. Nam vitae viverra lacus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum eu augue felis. Maecenas in dignissim purus, quis pulvinar lectus. Vivamus euismod ultrices diam, in mattis nibh.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Adipisicing elit">
         Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Suspendisse lectus tortor">
         Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede. Praesent blandit odio eu enim. Pellentesque sed dui ut augue blandit sodales. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Aliquam nibh. Mauris ac mauris sed pede pellentesque fermentum. Maecenas adipiscing ante non diam sodales hendrerit.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Velit mauris">
         Ut velit mauris, egestas sed, gravida nec, ornare ut, mi. Aenean ut orci vel massa suscipit pulvinar. Nulla sollicitudin. Fusce varius, ligula non tempus aliquam, nunc turpis ullamcorper nibh, in tempus sapien eros vitae ligula. Pellentesque rhoncus nunc et augue. Integer id felis. Curabitur aliquet pellentesque diam. Integer quis metus vitae elit lobortis egestas. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Morbi vel erat non mauris convallis vehicula. Nulla et sapien. Integer tortor tellus, aliquam faucibus, convallis id, congue eu, quam. Mauris ullamcorper felis vitae erat. Proin feugiat, augue non elementum posuere, metus purus iaculis lectus, et tristique ligula justo vitae magna.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Aliquam convallis">
         Aliquam convallis sollicitudin purus. Praesent aliquam, enim at fermentum mollis, ligula massa adipiscing nisl, ac euismod nibh nisl eu lectus. Fusce vulputate sem at sapien. Vivamus leo. Aliquam euismod libero eu enim. Nulla nec felis sed leo placerat imperdiet. Aenean suscipit nulla in justo. Suspendisse cursus rutrum augue. Nulla tincidunt tincidunt mi. Curabitur iaculis, lorem vel rhoncus faucibus, felis magna fermentum augue, et ultricies lacus lorem varius purus. Curabitur eu amet.
       </uib-accordion-group>
       <uib-accordion-group is-open="false" heading="Vulputate dictum">
         Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed at ante. Mauris eleifend, quam a vulputate dictum, massa quam dapibus leo, eget vulputate orci purus ut lorem. In fringilla mi in ligula. Pellentesque aliquam quam vel dolor. Nunc adipiscing. Sed quam odio, tempus ac, aliquam molestie, varius ac, tellus. Vestibulum ut nulla aliquam risus rutrum interdum. Pellentesque lorem. Curabitur sit amet erat quis risus feugiat viverra. Pellentesque augue justo, sagittis et, lacinia at, venenatis non, arcu. Nunc nec libero. In cursus dictum risus. Etiam tristique nisl a nulla. Ut a orci. Curabitur dolor nunc, egestas at, accumsan at, malesuada nec, magna.
       </uib-accordion-group>
     </uib-accordion>
   </div>
 </div>
 </file>

 <file name="script.js">
 angular.module('patternfly.utils').controller( 'AccordionCntrl', function($scope) {
 });
 </file>
 </example>
 */
angular.module('patternfly.utils').directive('pfFixedAccordion', function ($window, $timeout) {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      scrollSelector: '@',
      groupHeight: '@',
      groupClass: '@'
    },
    link: function ($scope, $element, $attrs) {
      var setCollapseHeights = function () {
        var componentSelector, height, openPanel, contentHeight, bodyHeight, overflowY = 'hidden', parentElement = $element.find('.panel-group');

        height = parentElement.height();

        // Close any open panel
        openPanel = parentElement.find('.collapse.in');
        if (openPanel && openPanel.length > 0) {
          openPanel.removeClass('in');
        }

        // Determine the necessary height for the closed content
        contentHeight = 0;
        parentElement.children().each(function (index, groupHeading) {
          var headingElement = angular.element(groupHeading);
          contentHeight += headingElement.prop('offsetHeight');
          contentHeight += parseInt(headingElement.css('margin-top'));
          contentHeight += parseInt(headingElement.css('margin-bottom'));
        });

        // Determine the height remaining for opened collapse panels
        bodyHeight = height - contentHeight;

        // Make sure we have enough height to be able to scroll the contents if necessary
        if (bodyHeight < 25) {
          bodyHeight = 25;

          // Allow the parent to scroll so the child elements are accessible
          overflowY = 'auto';
        }

        // Reopen the initially opened panel
        if (openPanel && openPanel.length > 0) {
          openPanel.addClass("in");
        }

        $timeout(function () {
          // Set the max-height for the fixed height components
          parentElement.find('.panel-collapse').each(function (index, collapsePanel) {
            var $panel = angular.element(collapsePanel);
            var scrollElement = $panel;
            var innerHeight = 0;
            var selected;
            var $sibling;

            if (angular.isDefined($scope.scrollSelector)) {
              selected = angular.element($panel.find($scope.scrollSelector));
              if (selected.length === 1) {
                scrollElement = angular.element(selected[0]);
                $panel.children().each(function (j, sibling) {
                  if (sibling !== scrollElement[0]) {
                    $sibling = angular.element(sibling);
                    innerHeight += $sibling.prop('offsetHeight');
                    innerHeight += parseInt($sibling.css('margin-top'));
                    innerHeight += parseInt($sibling.css('margin-bottom'));
                  }
                });
              }
            }

            // set the max-height
            angular.element(scrollElement).css('max-height', (bodyHeight - innerHeight) + 'px');
            angular.element(scrollElement).css('overflow-y', 'auto');
          });
        });

        angular.element(parentElement).css('overflow-y', overflowY);
      };

      if ($scope.groupHeight) {
        $element.find('.panel-group').css("height", $scope.groupHeight);
      }
      if ($scope.groupClass) {
        $element.find('.panel-group').addClass($scope.groupClass);
      }

      $timeout(function () {
        setCollapseHeights();
      }, 100);

      // Update on window resizing
      angular.element($window).bind('resize', function () {
        setCollapseHeights();
      });
    }
  };
});
