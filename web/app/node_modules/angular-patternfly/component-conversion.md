# Converting Angular 1.4 style directives to Angular 1.5

## Directive Conversion
1. Rename file from name-directive.js to name.component.js
2.  In the directive - modify the following:
  ```
  angular.module('patternfly.notification').directive('pfNotificationDrawer', function ($window, $timeout) {
    'use strict';
    return {
    restrict: 'A',
    scope: {
      scrollSelector: '@',
      groupHeight: '@',
      groupClass: '@'
    },
    controller: function($window, $timeout) {
      $scope.toggle = function () {
  <!------------->
  <!-- becomes -->
  <!------------->
  angular.module('patternfly.notification').component('pfNotificationDrawer', {
    bindings: {
      scrollSelector: '@',
      groupHeight: '@',
      groupClass: '@'
    },
    controller: function ($window, $timeout) {
      'use strict';
      var ctrl = this;
  
      ctrl.toggle = function () {
  ```
3. Any initialization logic should be moved out of link functions and into $onInit functions
4. Any event listeners that are added for $window or $timeout events should be cleaned up in $onDestroy
5. $scope watchers should be moved to $onChanges for bound properties (defined in bindings object)
6. If DOM manipulation still must happen, there is a $postLink function.  A bit more investigation will be necessary to see if these components can be upgraded to Angular 2.

## View Conversion
In the template referenced by the templateUrl in the component, some changes have to be made.  Anywhere a former $scope variable is referenced, you'll need to prepend $ctrl
  ```
  <li ng-repeat="item in items" class="list-group-item" ng-class="{'active': item.isActive}">
    <div ng-if="showBadges && tertiaryItem.badges" class="badge-container-pf">
  ```
  Becomes:
  ```
  <!-- item is in an ng-repeat so no $ctrl is needed -->
  <li ng-repeat="item in $ctrl.items" class="list-group-item" ng-class="{'active': item.isActive}">
    <div ng-if="$ctrl.showBadges && tertiaryItem.badges" class="badge-container-pf">
  ```
      
## Unit tests
1. Modify test to move attribute directives to component in any html code used in the $compile step.  
 ```
 <div pf-directive></div>
 <!--  becomes -->
 <pf-directive></pf-directive>
 ```
2. Make sure all unit tests pass

## NgDoc changes
1. Replace the word .directive. with .component. in the ngdoc @name
2. Add @restrict E under the @name
3. In example html, move any attribute directives to component.  <div pf-directive></div> becomes <pf-directive></pf-directive>

## Helpful Links
- https://docs.angularjs.org/guide/component
- https://gist.github.com/toddmotto/5b4de6c777d3e446e6410fdadb824522
