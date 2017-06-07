angular.module('patternfly.sort').component('pfSort', {
  bindings: {
    config: '='
  },
  templateUrl: 'sort/sort.html',
  controller: function () {
    'use strict';

    var ctrl = this;
    var prevConfig;

    ctrl.$onInit = function () {
      if (angular.isDefined(ctrl.config) && angular.isUndefined(ctrl.config.show)) {
        // default to true
        ctrl.config.show = true;
      }

      angular.extend(ctrl, {
        selectField: selectField,
        changeDirection: changeDirection,
        getSortIconClass: getSortIconClass
      });
    };

    ctrl.$onChanges = function () {
      setupConfig();
    };

    ctrl.$doCheck = function () {
      // do a deep compare on config
      if (!angular.equals(ctrl.config, prevConfig)) {
        setupConfig();
      }
    };

    function setupConfig () {
      var updated = false;

      prevConfig = angular.copy(ctrl.config);

      if (ctrl.config.fields === undefined) {
        ctrl.config.fields = [];
      }

      if (ctrl.config.fields.length > 0) {
        if (ctrl.config.currentField === undefined) {
          ctrl.config.currentField = ctrl.config.fields[0];
          updated = true;
        }
        if (ctrl.config.isAscending === undefined) {
          ctrl.config.isAscending = true;
          updated = true;
        }
      }

      if (updated === true && ctrl.config.onSortChange) {
        ctrl.config.onSortChange(ctrl.config.currentField, ctrl.config.isAscending);
      }
    }

    function selectField (field) {
      ctrl.config.currentField = field;

      if (ctrl.config.onSortChange) {
        ctrl.config.onSortChange(ctrl.config.currentField, ctrl.config.isAscending);
      }
    }

    function changeDirection () {
      ctrl.config.isAscending = !ctrl.config.isAscending;

      if (ctrl.config.onSortChange) {
        ctrl.config.onSortChange(ctrl.config.currentField, ctrl.config.isAscending);
      }
    }

    function getSortIconClass () {
      var iconClass;

      if (ctrl.config.currentField.sortType === 'numeric') {
        if (ctrl.config.isAscending) {
          iconClass = 'fa fa-sort-numeric-asc';
        } else {
          iconClass = 'fa fa-sort-numeric-desc';
        }
      } else {
        if (ctrl.config.isAscending) {
          iconClass = 'fa fa-sort-alpha-asc';
        } else {
          iconClass = 'fa fa-sort-alpha-desc';
        }
      }

      return iconClass;
    }
  }
});
