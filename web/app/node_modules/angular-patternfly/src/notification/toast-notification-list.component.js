/**
 * @ngdoc directive
 * @name patternfly.notification.component:pfToastNotificationList
 * @restrict E
 * @scope
 *
 * @param {Array} notifications The list of current notifications to display. Each notification should have the following (see pfToastNotification):
 *           <ul style='list-style-type: none'>
 *             <li>.type - (String) The type of the notification message. Allowed value is one of these: 'success','info','danger', 'warning'
 *             <li>.header - (String) The header to display for the notification (optional)
 *             <li>.message - (String) The main text message of the notification.
 *             <li>.actionTitle Text to show for the primary action, optional.
 *             <li>.actionCallback (function(this notification)) Function to invoke when primary action is selected, optional
 *             <li>.menuActions  Optional list of actions to place in the kebab menu:<br/>
 *               <ul style='list-style-type: none'>
 *                 <li>.name - (String) The name of the action, displayed on the button
 *                 <li>.actionFn - (function(action, this notification)) Function to invoke when the action selected
 *                 <li>.isDisabled - (Boolean) set to true to disable the action
 *                 <li>.isSeparator - (Boolean) set to true if this is a placehodler for a separator rather than an action
 *               </ul>
 *             <li>.isPersistent Flag to show close button for the notification even if showClose is false.
 *           </ul>
 * @param {Boolean} showClose Flag to show the close button on all notifications (not shown if the notification has menu actions)
 * @param {function} closeCallback (function(data)) Function to invoke when closes a toast notification
 * @param {function} updateViewing (function(boolean, data)) Function to invoke when user is viewing/not-viewing (hovering on) a toast notification
 *
 * @description
 * Using this component displayes a list of toast notifications
 *
 * @example
 <example module="patternfly.notification">

   <file name="index.html">
     <div ng-controller="ToastNotificationListDemoCtrl" >
       <pf-toast-notification-list notifications="notifications" show-close="showClose" close-callback="handleClose" update-viewing="updateViewing"></pf-toast-notification-list>
       <div class="row example-container">
         <div class="col-md-12">
           <form class="form-horizontal">
             <div class="form-group">
               <label class="col-sm-3 control-label" for="type">Show Close buttons:</label>
               <div class="col-sm-1">
                 <input type="checkbox" ng-model="showClose"/>
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
               <label class="col-sm-2 control-label" for="message">Primary Action:</label>
               <div class="col-sm-10">
                 <input type="text" class="form-control" ng-model="primaryAction" id="primaryAction"/>
               </div>
             </div>
             <div class="form-group">
               <label class="col-sm-2 control-label" for="type">Persistent:</label>
               <div class="col-sm-1">
                 <input type="checkbox" ng-model="persistent"/>
               </div>
               <label class="col-sm-2 control-label" for="type">Show Menu:</label>
               <div class="col-sm-2">
                 <input type="checkbox" ng-model="showMenu"/>
               </div>
             </div>
             <div class="form-group">
               <div class="col-sm-12">
                 <button ng-click="notify()">Add notification - Click me several times</button>
               </div>
             </div>
           </form>
         </div>
         <div class="col-md-12">
           <label class="actions-label">Actions: </label>
         </div>
         <div class="col-md-12">
           <textarea rows="3" class="col-md-12">{{actionText}}</textarea>
         </div>
       </div>
     </div>
   </file>

   <file name="script.js">
     angular.module('patternfly.notification').controller( 'ToastNotificationListDemoCtrl', function( $scope, $rootScope, Notifications ) {
       $scope.message = 'Default Message.';

       var typeMap = { 'Info': 'info',
                       'Success': 'success',
                       'Warning': 'warning',
                       'Danger': 'danger' };

       $scope.types = Object.keys(typeMap);

       $scope.type = $scope.types[0];
       $scope.header = 'Default header.';
       $scope.message = 'Default notification message.';
       $scope.showClose = false;
       $scope.persistent = false;

       $scope.primaryAction = '';

       $scope.updateType = function(item) {
         $scope.type = item;
       };

       $scope.showMenu = false;
       var performAction = function (menuAction, data) {
         $scope.actionText += menuAction.name +  ": " + data.message + '\n';
       };
       $scope.menuActions = [
          {
            name: 'Action',
            title: 'Perform an action',
            actionFn: performAction
          },
          {
            name: 'Another Action',
            title: 'Do something else',
            actionFn: performAction
          },
          {
            name: 'Disabled Action',
            title: 'Unavailable action',
            actionFn: performAction,
            isDisabled: true
          },
          {
            name: 'Something Else',
            title: '',
            actionFn: performAction
          },
          {
            isSeparator: true
          },
          {
            name: 'Grouped Action 1',
            title: 'Do something',
            actionFn: performAction
          },
          {
            name: 'Grouped Action 2',
            title: 'Do something similar',
            actionFn: performAction
          }
        ];

       $scope.actionText = "";

       $scope.handleAction = function (data) {
         $scope.actionText = $scope.primaryAction + ": " + data.message + '\n' + $scope.actionText;
       };
       $scope.handleClose = function (data) {
         $scope.actionText = "Closed: " + data.message + '\n'+ $scope.actionText;
         Notifications.remove(data);
       };
       $scope.updateViewing = function (viewing, data) {
         Notifications.setViewing(data, viewing);
       };

       $scope.notify = function () {
         Notifications.message (
           typeMap[$scope.type],
           $scope.header,
           $scope.message,
           $scope.persistent,
           $scope.handleClose,
           $scope.primaryAction,
           $scope.handleAction,
           ($scope.showMenu ? $scope.menuActions : undefined)
         );
       }

       $scope.notifications = Notifications.data;
     });
   </file>

 </example>
 */
angular.module('patternfly.notification').component('pfToastNotificationList', {
  bindings: {
    notifications: '=',
    showClose: '=?',
    closeCallback: '=?',
    updateViewing: '=?'
  },
  templateUrl: 'notification/toast-notification-list.html',
  controller: function () {
    'use strict';
    var ctrl = this;

    ctrl.handleClose = function (notification) {
      if (angular.isFunction(ctrl.closeCallback)) {
        ctrl.closeCallback(notification);
      }
    };
    ctrl.handleViewingChange = function (isViewing, notification) {
      if (angular.isFunction(ctrl.updateViewing)) {
        ctrl.updateViewing(isViewing, notification);
      }
    };
  }
});
