/**
 * @ngdoc directive
 * @name patternfly.filters.directive:pfFilterFields
 *
 * @description
 *   Directive for the filter bar's filter entry components
 *   <br><br>
 *
 * @param {object} config configuration settings for the filters:<br/>
 * <ul style='list-style-type: none'>
 * <li>.fields          - (Array) List of filterable fields containing:
 * <ul style='list-style-type: none'>
 * <li>.id          - (String) Optional unique Id for the filter field, useful for comparisons
 * <li>.title       - (String) The title to display for the filter field
 * <li>.placeholder - (String) Text to display when no filter value has been entered
 * <li>.filterType  - (String) The filter input field type (any html input type, or 'select' for a select box)
 * <li>.filterValues - (Array) List of valid select values used when filterType is 'select'
 * </ul>
 * <li>.appliedFilters - (Array) List of the currently applied filters
 * </ul>
 *
 */
angular.module('patternfly.filters').directive('pfFilterFields', function () {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      config: '=',
      addFilterFn: '='
    },
    templateUrl: 'filters/filter-fields.html',
    controller: function ($scope) {
      $scope.setupConfig = function () {
        if ($scope.fields === undefined) {
          $scope.fields = [];
        }
        if (!$scope.currentField) {
          $scope.currentField = $scope.config.fields[0];
          $scope.config.currentValue = null;
        }

        if ($scope.config.currentValue === undefined) {
          $scope.config.currentValue = null;
        }
      };

      $scope.$watch('config', function () {
        $scope.setupConfig();
      }, true);
    },

    link: function (scope, element, attrs) {
      scope.selectField = function (item) {
        scope.currentField = item;
        scope.config.currentValue = null;
      };

      scope.selectValue = function (filterValue) {
        scope.addFilterFn(scope.currentField, filterValue);
        scope.config.currentValue = null;
      };

      scope.onValueKeyPress = function (keyEvent) {
        if (keyEvent.which === 13) {
          keyEvent.preventDefault();
          scope.addFilterFn(scope.currentField, scope.config.currentValue);
          scope.config.currentValue = undefined;
        }
      };
    }
  };
});
