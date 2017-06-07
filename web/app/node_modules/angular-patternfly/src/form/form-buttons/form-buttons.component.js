angular.module('patternfly.form').component('pfFormButtons', {

  bindings: {
    pfHandleCancel: '&pfOnCancel',
    pfHandleSave: '&pfOnSave',
    pfWorking: '=',
    pfButtonContainerClass: '@'
  },
  require: {
    form: '^form'
  },
  templateUrl: 'form/form-buttons/form-buttons.html',
  controller: function () {
    'use strict';

    var ctrl = this;

    ctrl.$onInit = function () {
      if (ctrl.pfWorking === undefined) {
        ctrl.pfWorking = false;
      }

      angular.extend(ctrl, {
        isInvalid: isInvalid
      });
    };

    function isInvalid () {
      var invalid = ctrl.form.$invalid;

      if (ctrl.form && ctrl.form.name && ctrl.form.name.$error) {
        if (ctrl.form.name.$error.server) {
          invalid = false;
        }
      }

      return invalid;
    }
  }
});
