/**
 * Create a running short detection service.
 *
 * @type type undefined
 */
angular.module('jmri.app').run(function shortDetector($rootScope, $http, $log, Notifications, jmriWebSocket, $translate, $translatePartialLoader) {

  // create an object for configuration and data retention in the rootScope
  // this allows controllers in the jmri.app module to access it
  $rootScope.shortDetector = {
    active: [],
    detectors: [],
    regex: undefined,
    detected: 'Short Detected'
  };

  $rootScope.$on('$translateRefreshEnd', function () {
    if (typeof($rootScope.shortDetector.detectors.regex) !== 'undefined') {
      $translate('SHORT-DETECTOR.DETECTED').then(function(translated) {
        $rootScope.shortDetector.detected = translated;
      });
    }
  });

  // register a listener for sensors with the jmriWebSocket service
  // this listener determines if a notification needs to be shown or cleared
  this.webSocket = jmriWebSocket.register({
    sensor: function sensor(data, method) {
      // when a sensor changes, see if we are interested in it
      if (method !== 'delete' && $rootScope.shortDetector.regex.test(data.userName)) {
        // post a notification if the sensor went active
        if (data.state === jmriWebSocket.ACTIVE) {
          if (data.comment) {
            Notifications.message('danger', $rootScope.shortDetector.detected, data.comment, true);
          } else {
            $log.error('Short detection sensor "' + data.userName + '" is missing comment.');
          }
          $rootScope.shortDetector.active.push(data.name);
        // clear any existing notifications is the sensor went any state other
        // than active
        } else {
          Notifications.data.forEach(function(notification) {
            if (notification.message === data.comment) {
                Notifications.remove(notification);
            }
          });
          $log.info('Short cleared at ' + data.userName);
          $rootScope.shortDetector.active.pop(data.name);
        }
      }
    }
  });

  // when run, check for the IMSHORTDETECTION memory object; if present
  // get a list of sensors and listen to sensors of interest
  $http.get('/json/memory/IMSHORTDETECTION').then(
    // handle a successful get for the memory
    function(response) {
      // localize, but only if needed
      $translatePartialLoader.addPart('app/short-detector');
      // set the regex
      $rootScope.shortDetector.regex = new RegExp(response.data.data.value);
      // get a list of sensors
      $http.get('/json/sensors').then(
        // handle a successful get for the list of sensors
        function(response) {
          // iterate over the list
          if (Array.isArray(response.data)) {
            response.data.forEach(function onSensor(sensor) {
              if (sensor.type === 'sensor'
                  && $.inArray(sensor.data.name, $rootScope.shortDetector.detectors) === -1
                  && $rootScope.shortDetector.regex.test(sensor.data.userName)) {
                // if the sensors username matches, get the sensor and listen to it
                jmriWebSocket.getSensor(sensor.data.name);
                $rootScope.shortDetector.detectors.push(sensor.data.name);
              }
            });
          } else {
            $log.debug('Single sensor (' + response.data.data.name + ') returned, not checking for short detectors');
          }
        },
        // handle a failed get for the list of sensors
        function(response) {
          // something went wrong
          $log.error('Unable to retrieve list of sensors');
        }
      );
    },
    // handle a failed get for the memory
    function(response) {
      // could merely be unused and unconfigured on JMRI, so not an error
      $log.info('Short detection not configured, and not running');
    }
  );
});
