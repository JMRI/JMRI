/**
 * Create a running log notification service.
 *
 * @type type undefined
 */
angular.module('jmri.app').run(function logsNotification($rootScope, $http, $log, Notifications, jmriWebSocket, $translate, $translatePartialLoader) {

  // create an object for configuration and data retention in the rootScope
  // this allows controllers in the jmri.app module to access it
  $rootScope.jmriLogs = {
    entries: [],
    errors: 0,
    infos: 0,
    warnings: 0
  };

  // get the localization; once loaded, will trigger everything else
  $translatePartialLoader.addPart('app/logs');
  
  $rootScope.$on('$translateRefreshEnd', function () {
    $translate('LOGS.ERROR_NOTIFICATION').then(function (translation) {
      // register a listener for logs with the jmriWebSocket service
      // this listener determines if a notification needs to be shown or cleared
      this.webSocket = jmriWebSocket.register({
        logs: function logs(data, method) {
          if (data['@version'] === 1) {
            $rootScope.jmriLogs.entries.push(data);
            if (data.level === "ERROR") {
              $rootScope.jmriLogs.errors++;
              if (data.message !== null) {
                Notifications.message('danger', $translate.instant('LOGS.ERROR_NOTIFICATION', {'dts': data['@timestamp']}), data.message, true);
              }
            } else if (data.level === "WARNING") {
              $rootScope.jmriLogs.warnings++;
            } else if (data.level === "INFO") {
              $rootScope.jmriLogs.infos++;
            }
          }
          $log.debug('Log entries: ' + $rootScope.jmriLogs.errors + ' errors, ' + $rootScope.jmriLogs.warnings + ' warnings, ' + $rootScope.jmriLogs.infos + ' info messages.');
        }
      });
      jmriWebSocket.get('logs', {'state': jmriWebSocket.ACTIVE});
    });
  });

});
