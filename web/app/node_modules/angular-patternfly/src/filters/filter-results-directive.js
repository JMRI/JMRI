/**
 * @ngdoc directive
 * @name patternfly.filters.directive:pfFilterResults
 *
 * @description
 *   Directive for the filter results components
 *   <br><br>
 *
 * @param {object} config configuration settings for the filter results:<br/>
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
 * <li>.resultsCount   - (int) The number of results returned after the current applied filters have been applied
 * <li>.onFilterChange - ( function(array of filters) ) Function to call when the applied filters list changes
 * </ul>
 *
 */
angular.module('patternfly.filters').directive('pfFilterResults', function () {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      config: '='
    },
    templateUrl: 'filters/filter-results.html',
    controller: function ($scope) {
      $scope.setupConfig = function () {
        if (!$scope.config.appliedFilters) {
          $scope.config.appliedFilters = [];
        }
        if ($scope.config.resultsCount === undefined) {
          $scope.config.resultsCount = 0;
        }
      };

      $scope.$watch('config', function () {
        $scope.setupConfig();
      }, true);
    },
    link: function (scope, element, attrs) {
      scope.clearFilter = function (item) {
        var newFilters = [];
        scope.config.appliedFilters.forEach(function (filter) {
          if (item.title !== filter.title || item.value !== filter.value) {
            newFilters.push(filter);
          }
        });
        scope.config.appliedFilters = newFilters;

        if (scope.config.onFilterChange) {
          scope.config.onFilterChange(scope.config.appliedFilters);
        }
      };

      scope.clearAllFilters = function () {
        scope.config.appliedFilters = [];

        if (scope.config.onFilterChange) {
          scope.config.onFilterChange(scope.config.appliedFilters);
        }
      };
    }
  };
});
