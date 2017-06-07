/**
 * @ngdoc directive
 * @name patternfly.notification.component:pfInlineNotification
 * @restrict E
 * @scope
 *
 * @param {expression=} pfNotificationType The type of the notification message. Allowed value is one of these: 'success','info','danger', 'warning'.
 * @param {expression=} pfNotificationMessage The main text message of the notification.
 * @param {expression=} pfNotificationHeader The header text of the notification.
 * @param {expression=} pfNotificationPersistent The notification won't disappear after delay timeout, but has to be closed manually with the close button.
 * @param {expression=} pfNotificationRemove The function to remove the notification (called by the close button when clicked).
 *
 * @description
 * The main visual element of the notification message.
 *
 * @example
 <example module="patternfly.notification">

   <file name="index.html">
     <div ng-controller="NotificationDemoCtrl">

       <pf-inline-notification pf-notification-type="notification.type"
                        pf-notification-header="notification.header"
                        pf-notification-message="notification.message"
                        pf-notification-persistent="notification.isPersistent"
                        pf-notification-remove="removeNotification()">
       </pf-inline-notification>

       <form class="form-horizontal">
         <div class="form-group">
           <label class="col-sm-2 control-label" for="header">Header:</label>
           <div class="col-sm-10">
            <input type="text" class="form-control" ng-model="notification.header" id="header"/>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="message">Message:</label>
           <div class="col-sm-10">
            <input type="text" class="form-control" ng-model="notification.message" id="message"/>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="type">Type:</label>
           <div class="col-sm-10">
             <div class="btn-group" uib-dropdown>
               <button type="button" uib-dropdown-toggle class="btn btn-default">
                 {{notification.type}}
                 <span class="caret"></span>
               </button>
               <ul uib-dropdown-menu class="dropdown-menu-right" role="menu">
                 <li ng-repeat="item in types" ng-class="{'selected': item === notification.type}">
                 <a role="menuitem" tabindex="-1" ng-click="updateType(item)">
                   {{item}}
                 </a>
                 </li>
               </ul>
             </div>
           </div>
         </div>
         <div class="form-group">
           <label class="col-sm-2 control-label" for="type">Persistent:</label>
           <div class="col-sm-10">
            <input type="checkbox" ng-model="notification.isPersistent"></input>
           </div>
         </div>
       </form>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.notification' ).controller( 'NotificationDemoCtrl', function( $scope, $timeout ) {
       $scope.types = ['success','info','danger', 'warning'];

       $scope.updateType = function(item) {
         $scope.notification.type = item;
       };

       $scope.removeNotification = function () {
         $scope.notification = undefined;
         // Add notification back for demo purposes
         $timeout(function() {
           createNotification();
         }, 1000);
       };

       var createNotification = function () {
         $scope.notification = {
           type: $scope.types[0],
           isPersistent: false,
           header: 'Default Header.',
           message: 'Default Message.'
         };
       };
       createNotification();
     });
   </file>

 </example>
 */
angular.module( 'patternfly.notification' ).component('pfInlineNotification', {
  bindings: {
    'pfNotificationType': '=',
    'pfNotificationMessage': '=',
    'pfNotificationHeader': '=',
    'pfNotificationPersistent': '=',
    'pfNotificationIndex': '=',
    'pfNotificationRemove': '&?'
  },
  templateUrl: 'notification/inline-notification.html'
});
