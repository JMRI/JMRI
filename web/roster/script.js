/**
 * Create a roster view.
 *
 * @type undefined
 * @param {scope} $scope the controller's scope
 * @param {loader} $translatePartialLoader I18N support provider
 */
angular.module('jmri.app').controller('RosterCtrl', function RosterCtrl($scope, $translatePartialLoader) {

  $translatePartialLoader.addPart('web/roster');

});
