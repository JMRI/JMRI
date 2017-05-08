(function () {
  'use strict';

  angular.module('patternfly.views').constant('pfViewUtils', {
    getDashboardView: function (title) {
      return {
        id: 'dashboardView',
        title: title || 'Dashboard View',
        iconClass: 'fa fa-dashboard'
      };
    },
    getCardView: function (title) {
      return {
        id: 'cardView',
        title: title || 'Card View',
        iconClass: 'fa fa-th'
      };
    },
    getListView: function (title) {
      return {
        id: 'listView',
        title: title || 'List View',
        iconClass: 'fa fa-th-list'
      };
    },
    getTableView: function (title) {
      return {
        id: 'tableView',
        title: title || 'Table View',
        iconClass: 'fa fa-table'
      };
    },
    getTopologyView: function (title) {
      return {
        id: 'topologyView',
        title: title || 'Topology View',
        iconClass: 'fa fa-sitemap'
      };
    }
  });
})();
