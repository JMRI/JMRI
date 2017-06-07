angular.module('patternfly.select').component('pfSelect', {

  bindings: {
    selected: '=',
    options: '<',
    displayField: '@',
    emptyValue: '@',
    onSelect: '<'
  },
  templateUrl: 'select/select.html',
  controller: function () {
    'use strict';

    var ctrl = this;

    ctrl.$onInit = function () {
      angular.extend(ctrl, {
        showEmpty: angular.isDefined(ctrl.emptyValue),
        getDisplayValue: getDisplayValue,
        selectItem: selectItem
      });
    };

    function getDisplayValue (item) {
      var value;

      if (item !== ctrl.emptyValue && angular.isString(ctrl.displayField)) {
        value = item[ctrl.displayField];
      } else {
        value = item;
      }

      return value;
    }

    function selectItem (item) {
      ctrl.selected = item;
      if (angular.isFunction(ctrl.onSelect)) {
        ctrl.onSelect(item);
      }
    }
  }
});
