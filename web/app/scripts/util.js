/**
 * Utilities for the JMRI web app.
 * 
 * @argument {angularService} Notifications angular-patternfly notifications service
 * @argument {angularService} $log AngularJS logging service
 * @type AngularJS Service Factory
 */
angular.module('jmri.app').factory('jmriNotifications', function(Notifications, $log) {

  function closeHandler(data) {
    Notifications.remove(data);
  }
  
    // create a patternfly toast notification with some sane defaults
  function toast(type, message, header = null, persistent = undefined, handleClose = '', primaryAction = '', handleAction, menuActions, data) {
    Notifications.message(
      type,
      header,
      message,
      (typeof persistent === 'undefined' ? persistent : type === 'danger'),
      (handleClose ? handleClose : closeHandler),
      primaryAction,
      handleAction,
      menuActions,
      data
    );
  }

  var methods = {
    success: function(message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data) {
      toast('success', message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data);
    },
    info: function(message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data) {
      toast('info', message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data);
    },
    danger: function(message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data) {
      toast('danger', message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data);
    },
    warning: function(message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data) {
      toast('warning', message, header, persistent, handleClose, primaryAction, handleAction, showMenu, menuActions, data);
    }
  };
  
  return methods;
});
