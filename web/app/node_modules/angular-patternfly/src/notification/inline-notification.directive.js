/**
 * @ngdoc directive
 * @name patternfly.notification.directive:pfInlineNotification
 * @restrict E
 * @scope
 *
 * @param {expression=} pfNotificationType The type of the notification message. Allowed value is one of these: 'success','info','danger', 'warning'.
 * @param {expression=} pfNotificationMessage The main text message of the notification.
 * @param {expression=} pfNotificationHeader The header text of the notification.
 * @param {expression=} pfNotificationPersistent The notification won't disappear after delay timeout, but has to be closed manually with the close button.
 *
 * @description
 * The main visual element of the notification message.
 *
 * @example
 <example module="patternfly.notification">

   <file name="index.html">
     <div ng-controller="NotificationDemoCtrl">

       <pf-inline-notification pf-notification-type="type"
                        pf-notification-header="header"
                        pf-notification-message="message"
                        pf-notification-persistent="isPersistent">
       </pf-inline-notification>

       <form class="form-horizontal">
         <div class="form-group">
           <label class="col-sm-2 control-label" for="header">Header:</label>
           <div class="col-sm-10">
            <input type="text" class="form-control" ng-model="header" id="header"/>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="message">Message:</label>
           <div class="col-sm-10">
            <input type="text" class="form-control" ng-model="message" id="message"/>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="type">Type:</label>
           <div class="col-sm-10">
            <select pf-select ng-model="type" id="type" ng-options="o as o for o in types"></select>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="type">Persistent:</label>
           <div class="col-sm-10">
            <input type="checkbox" ng-model="isPersistent"></input>
           </div>
         </div>
       </form>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.notification' ).controller( 'NotificationDemoCtrl', function( $scope, Notifications ) {
       $scope.types = ['success','info','danger', 'warning'];
       $scope.type = $scope.types[0];
       $scope.isPersistent = false;

       $scope.header = 'Default Header.';
       $scope.message = 'Default Message.';
     });
   </file>

 </example>
 */
angular.module( 'patternfly.notification' ).directive('pfInlineNotification', function () {
  'use strict';

  return {
    scope: {
      'pfNotificationType': '=',
      'pfNotificationMessage': '=',
      'pfNotificationHeader': '=',
      'pfNotificationPersistent': '=',
      'pfNotificationIndex': '='
    },
    restrict: 'E',
    templateUrl: 'notification/inline-notification.html'
  };
});
