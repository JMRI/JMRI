/**
 * @ngdoc directive
 * @name patternfly.form.directive:pfDateTimepicker
 *
 * @description
 *  Angular directive to wrap the bootstrap datetimepicker http://eonasdan.github.io/bootstrap-datetimepicker/
 *
 * @param {object} date date and time moment object
 * @param {string} options the configuration options for the date picker
 *
 * @example
 <example module="patternfly.form">
   <file name="index.html">
     <form class="form" ng-controller="FormDemoCtrl">
       <span>Date and Time: <span ng-bind="date"></span></span>
       <div pf-date-timepicker options="options" date="date"></div>
     </form>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.form' ).controller( 'FormDemoCtrl', function( $scope ) {
       $scope.options = {
         format: 'HH:mm'
       };
       $scope.date = moment();
     });
   </file>
 </example>
 */
angular.module('patternfly.form').directive('pfDateTimepicker', function () {
  'use strict';

  return {
    replace: true,
    restrict: 'A',
    require: '^form',
    templateUrl: 'form/datetimepicker/datetimepicker.html',
    scope: {
      options: '=',
      date: '='
    },
    link: function ($scope, element) {
      //Make sure the date picker is set with the correct options
      element.datetimepicker($scope.options);

      //Set the initial value of the date picker
      element.datetimepicker('date', $scope.date || null);

      //Change happened on the date picker side. Update the underlying date model
      element.on('dp.change', function (elem) {
        $scope.$apply(function () {
          $scope.date = elem.date;
        });
      });
    }
  };
});
