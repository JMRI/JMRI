/**
 * @ngdoc service
 * @name patternfly.notification.Notification
 * @requires $rootScope
 *
 * @description
 * Notification service used to notify user about important events in the application.
 *
 * ## Configuring the service
 *
 * You can configure the service with: setDelay, setVerbose and setPersist.
 *
 * ### Notifications.setDelay
 * Set the delay after which the notification is dismissed. The argument of this method expects miliseconds. Default
 * delay is 5000 ms.
 *
 * ### Notifications.setVerbose
 * Set the verbose mode to on (default) or off. During the verbose mode, each notification is printed in the console,
 * too. This is done using the default angular.js $log service.
 *
 * ### Notifications.setPersist
 * Sets persist option for particular modes. Notification with persistent mode won't be dismissed after delay, but has
 * to be closed manually with the close button. By default, the "error" and "httpError" modes are set to persistent.
 * The input is an object in format {mode: persistValue}.
 *
 * ## Configuration Example
 * ```js
 * angular.module('myApp', []).config(function (NotificationsProvider) {
 *   NotificationsProvider.setDelay(10000).setVerbose(false).setPersist({'error': true, 'httpError': true, 'warn': true});
 * });
 * ```
 * @example
 <example module="patternfly.notification">

   <file name="index.html">
     <div ng-controller="NotificationDemoCtrl">
       <pf-notification-list></pf-notification-list>

       <form class="form-horizontal">
         <div class="form-group">
           <label class="col-sm-2 control-label" for="message">Message:</label>
           <div class="col-sm-10">
            <input type="text" class="form-control" ng-model="message" id="message"/>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="type">Type:</label>
           <div class="col-sm-10">
             <div class="btn-group" uib-dropdown>
               <button type="button" uib-dropdown-toggle class="btn btn-default">
                 {{type}}
                 <span class="caret"></span>
               </button>
               <ul uib-dropdown-menu class="dropdown-menu-right" role="menu">
                 <li ng-repeat="item in types" ng-class="{'selected': item === type}">
                   <a role="menuitem" tabindex="-1" ng-click="updateType(item)">
                     {{item}}
                   </a>
                 </li>
               </ul>
             </div>
           </div>
         </div>
         <div class="form-group">
           <div class="col-sm-12">
            <button ng-click="notify()">Add notification</button>
           </div>
         </div>
       </form>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.notification' ).controller( 'NotificationDemoCtrl', function( $scope, Notifications ) {

       var typeMap = { 'Info': Notifications.info,
                       'Success': Notifications.success,
                       'Warning': Notifications.warn,
                       'Danger': Notifications.error };

       $scope.types = Object.keys(typeMap);

       $scope.type = $scope.types[0];

       $scope.updateType = function(item) {
         $scope.type = item;
       };

       $scope.message = 'Default notification message.';

       $scope.notify = function () {
         typeMap[$scope.type]($scope.message);
       }
     });
   </file>

 </example>
 */
angular.module('patternfly.notification').provider('Notifications', function () {
  'use strict';

  // time (in ms) the notifications are shown
  this.delay = 8000;
  this.verbose = true;
  this.notifications = {};
  this.notifications.data = [];
  this.persist = {'error': true, 'httpError': true};

  this.setDelay = function (delay) {
    this.delay = delay;
    return this;
  };

  this.setVerbose = function (verbose) {
    this.verbose = verbose;
    return this;
  };

  this.setPersist = function (persist) {
    this.persist = persist;
  };

  this.$get = ['$timeout', '$log', function ($timeout, $log) {
    var delay = this.delay;
    var notifications = this.notifications;
    var verbose = this.verbose;
    var persist = this.persist;

    var modes = {
      info: { type: 'info', header: 'Info!', log: 'info'},
      success: { type: 'success', header: 'Success!', log: 'info'},
      error: { type: 'danger', header: 'Error!', log: 'error'},
      warn: { type: 'warning', header: 'Warning!', log: 'warn'}
    };

    if (!notifications) {
      notifications.data = [];
    }

    notifications.message = function (type, header, message, isPersistent, closeCallback, actionTitle, actionCallback, menuActions) {
      var notification = {
        type : type,
        header: header,
        message : message,
        isPersistent: isPersistent,
        closeCallback: closeCallback,
        actionTitle: actionTitle,
        actionCallback: actionCallback,
        menuActions: menuActions
      };

      notification.show = true;
      notifications.data.push(notification);

      if (!notification.isPersistent) {
        notification.viewing = false;
        $timeout(function () {
          notification.show = false;
          if (!notification.viewing) {
            notifications.remove(notification);
          }
        }, delay);
      }
    };

    function createNotifyMethod (mode) {
      return function (message, header, persistent, closeCallback, actionTitle, actionCallback, menuActions) {
        if (angular.isUndefined(header)) {
          header = modes[mode].header;
        }
        if (angular.isUndefined(persistent)) {
          persistent = persist[mode];
        }
        notifications.message(modes[mode].type, header, message, persistent, closeCallback, actionTitle, actionCallback, menuActions);
        if (verbose) {
          $log[modes[mode].log](message);
        }
      };
    }

    angular.forEach(modes, function (mode, index) {
      notifications[index] = createNotifyMethod(index);
    });


    notifications.httpError = function (message, httpResponse) {
      message += ' (' + (httpResponse.data.message || httpResponse.data.cause || httpResponse.data.cause || httpResponse.data.errorMessage) + ')';
      notifications.message('danger', 'Error!', message, persist.httpError);
      if (verbose) {
        $log.error(message);
      }
    };

    notifications.remove = function (notification) {
      var index = notifications.data.indexOf(notification);
      if (index !== -1) {
        notifications.removeIndex(index);
      }
    };

    notifications.removeIndex = function (index) {
      //notifications.remove(index);
      notifications.data.splice(index, 1);
    };

    notifications.setViewing = function (notification, viewing) {
      notification.viewing = viewing;
      if (!viewing && !notification.show) {
        notifications.remove(notification);
      }
    };

    return notifications;
  }];

});

/**
 * @ngdoc directive
 * @name patternfly.notification.component:pfNotificationList
 * @restrict E
 *
 * @description
 * Using this component automatically creates a list of notifications generated by the {@link api/patternfly.notification.Notification notification} service.
 *
 * @example
 <example module="patternfly.notification">

   <file name="index.html">
     <div ng-controller="NotificationDemoCtrl">

       <pf-notification-list></pf-notification-list>

       <form class="form-horizontal">
         <div class="form-group">
           <label class="col-sm-2 control-label" for="type">Type:</label>
           <div class="col-sm-10">
             <div class="btn-group" uib-dropdown>
               <button type="button" uib-dropdown-toggle class="btn btn-default">
                 {{type}}
                 <span class="caret"></span>
               </button>
               <ul uib-dropdown-menu class="dropdown-menu-right" role="menu">
                 <li ng-repeat="item in types" ng-class="{'selected': item === type}">
                   <a role="menuitem" tabindex="-1" ng-click="updateType(item)">
                     {{item}}
                   </a>
                 </li>
               </ul>
             </div>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="message">Message:</label>
           <div class="col-sm-10">
            <input type="text" class="form-control" ng-model="message" id="message"/>
           </div>
         </div>
         <div class="form-group">
           <div class="col-sm-12">
            <button ng-click="notify()">Add notification - Click me several times</button>
           </div>
         </div>
       </form>
     </div>
   </file>

   <file name="script.js">
     angular.module('patternfly.notification').controller( 'NotificationDemoCtrl', function( $scope, Notifications ) {
       $scope.message = 'Default Message.';

       var typeMap = { 'Info': Notifications.info,
                       'Success': Notifications.success,
                       'Warning': Notifications.warn,
                       'Danger': Notifications.error };

       $scope.types = Object.keys(typeMap);

       $scope.type = $scope.types[0];
       $scope.message = 'Default notification message.';

       $scope.updateType = function(item) {
         $scope.type = item;
       };

       $scope.notify = function () {
         typeMap[$scope.type]($scope.message);
       }
     });
   </file>

 </example>
 */
angular.module('patternfly.notification').component('pfNotificationList', {
  templateUrl: 'notification/notification-list.html',
  controller: function (Notifications) {
    'use strict';
    var ctrl = this;

    ctrl.$onInit = function () {
      ctrl.notifications = Notifications;
    };
  }
});
