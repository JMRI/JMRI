/**
 * @ngdoc directive
 * @name patternfly.filters.component:pfFilterFields
 * @restrict E
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
angular.module('patternfly.filters').component('pfFilterFields', {
  bindings: {
    config: '=',
    addFilterFn: '<'
  },
  templateUrl: 'filters/filter-fields.html',
  controller: function () {
    'use strict';

    var ctrl = this;
    var prevConfig;

    ctrl.$onInit = function () {
      angular.extend(ctrl, {
        selectField: selectField,
        selectValue: selectValue,
        onValueKeyPress: onValueKeyPress
      });
    };

    ctrl.$onChanges = function () {
      setupConfig ();
    };

    ctrl.$doCheck = function () {
      // do a deep compare on config
      if (!angular.equals(ctrl.config, prevConfig)) {
        setupConfig();
      }
    };

    function selectField (item) {
      ctrl.currentField = item;
      ctrl.currentValue = null;
    }

    function selectValue (filterValue) {
      if (angular.isDefined(filterValue)) {
        ctrl.addFilterFn(ctrl.currentField, filterValue);
        ctrl.currentValue = null;
      }
    }

    function onValueKeyPress (keyEvent) {
      if (keyEvent.which === 13) {
        ctrl.addFilterFn(ctrl.currentField, ctrl.currentValue);
        ctrl.currentValue = undefined;
      }
    }

    function setupConfig () {
      var fieldFound = false;

      prevConfig = angular.copy(ctrl.config);

      if (ctrl.config.fields === undefined) {
        ctrl.config.fields = [];
      }

      if (ctrl.currentField) {
        fieldFound = _.find(ctrl.config.fields, function (nextField) {
          return nextField.id === ctrl.currentField.id;
        });
      }

      if (!fieldFound) {
        ctrl.currentField = ctrl.config.fields[0];
        ctrl.currentValue = null;
      }

      if (ctrl.currentValue === undefined) {
        ctrl.currentValue = null;
      }
    }
  }
});
