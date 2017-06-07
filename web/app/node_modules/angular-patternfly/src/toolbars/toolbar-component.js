angular.module('patternfly.toolbars').component('pfToolbar', {
  bindings: {
    config: '='
  },
  transclude: {
    'actions': '?'
  },
  templateUrl: 'toolbars/toolbar.html',
  controller: function () {
    'use strict';

    var ctrl = this;
    var prevConfig;

    ctrl.$onInit = function () {
      if (angular.isDefined(ctrl.config.sortConfig) && angular.isUndefined(ctrl.config.sortConfig.show)) {
        // default to true
        ctrl.config.sortConfig.show = true;
      }

      angular.extend(ctrl, {
        viewSelected: viewSelected,
        isViewSelected: isViewSelected,
        isTableViewSelected: isTableViewSelected,
        checkViewDisabled: checkViewDisabled,
        addFilter: addFilter,
        handleAction: handleAction
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

    function setupConfig () {
      prevConfig = angular.copy(ctrl.config);

      if (ctrl.config && ctrl.config.viewsConfig && ctrl.config.viewsConfig.views) {
        ctrl.config.viewsConfig.viewsList = angular.copy(ctrl.config.viewsConfig.views);

        if (!ctrl.config.viewsConfig.currentView) {
          ctrl.config.viewsConfig.currentView = ctrl.config.viewsConfig.viewsList[0].id;
        }
      }
    }

    function viewSelected (viewId) {
      ctrl.config.viewsConfig.currentView = viewId;
      if (ctrl.config.viewsConfig.onViewSelect && !ctrl.checkViewDisabled(viewId)) {
        ctrl.config.viewsConfig.onViewSelect(viewId);
      }
    }

    function isViewSelected (viewId) {
      return ctrl.config.viewsConfig && (ctrl.config.viewsConfig.currentView === viewId);
    }

    function isTableViewSelected () {
      return ctrl.config.viewsConfig ? (ctrl.config.viewsConfig.currentView === 'tableView') : ctrl.config.isTableView;
    }

    function checkViewDisabled (view) {
      return ctrl.config.viewsConfig.checkViewDisabled && ctrl.config.viewsConfig.checkViewDisabled(view);
    }

    function filterExists (filter) {
      var foundFilter = _.find(ctrl.config.filterConfig.appliedFilters, {title: filter.title, value: filter.value});
      return foundFilter !== undefined;
    }

    function enforceSingleSelect (filter) {
      _.remove(ctrl.config.appliedFilters, {title: filter.title});
    }

    function addFilter (field, value) {
      var newFilter = {
        id: field.id,
        title: field.title,
        value: value
      };
      if (!filterExists(newFilter)) {
        if (newFilter.type === 'select') {
          enforceSingleSelect(newFilter);
        }
        ctrl.config.filterConfig.appliedFilters.push(newFilter);

        if (ctrl.config.filterConfig.onFilterChange) {
          ctrl.config.filterConfig.onFilterChange(ctrl.config.filterConfig.appliedFilters);
        }
      }
    }

    function handleAction (action) {
      if (action && action.actionFn && (action.isDisabled !== true)) {
        action.actionFn(action);
      }
    }
  }
});
