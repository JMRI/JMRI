/**
 * @ngdoc directive
 * @name patternfly.navigation.component:pfApplicationLauncher
 * @restrict E
 *
 * @description
 * Component for rendering application launcher dropdown.
 *
 * @param {string=} label Use a custom label for the launcher, default: Application Launcher
 * @param {boolean=} isDisabled Disable the application launcher button, default: false
 * @param {boolean=} isList Display items as a list instead of a grid, default: false
 * @param {boolean=} hiddenIcons Flag to not show icons on the launcher, default: false
 * @param {array} items List of navigation items
 * <ul style='list-style-type: none'>
 * <li>.title        - (string) Name of item to be displayed on the menu
 * <li>.iconClass    - (string) Classes for icon to be shown on the menu (ex. "fa fa-dashboard")
 * <li>.href         - (string) href link to navigate to on click
 * <li>.tooltip      - (string) Tooltip to display for the badge
 * </ul>
 * @example
 <example module="patternfly.navigation">
 <file name="index.html">
   <div ng-controller="applicationLauncherController" class="row">
     <div class="col-xs-12 pre-demo-text">
       <label>Click the launcher indicator to show the Application Launcher Dropdown:</label>
     </div>
     <nav class="navbar navbar-pf navbar-collapse">
       <ul class="nav navbar-left">
         <li>
           <pf-application-launcher items="navigationItems" label="{{label}}" is-disabled="isDisabled" is-list="isList" hidden-icons="hiddenIcons"></pf-application-launcher>
         </li>
       </ul>
     </nav>
   </div>
 </file>
 <file name="script.js">
   angular.module('patternfly.navigation').controller('applicationLauncherController', ['$scope',
     function ($scope) {
       $scope.navigationItems = [
         {
           title: "Recteque",
           href: "#/ipsum/intellegam/recteque",
           tooltip: "Launch the Function User Interface",
           iconClass: "pficon-storage-domain"
         },
         {
           title: "Suavitate",
           href: "#/ipsum/intellegam/suavitate",
           tooltip: "Launch the Function User Interface",
           iconClass: "pficon-build"
         },
         {
           title: "Lorem",
           href: "#/ipsum/intellegam/lorem",
           tooltip: "Launch the Function User Interface",
           iconClass: "pficon-domain"
         },
         {
           title: "Home",
           href: "#/ipsum/intellegam/home",
           tooltip: "Launch the Function User Interface",
           iconClass: "pficon-home"
         }
       ];

       $scope.label = 'Application Launcher';
       $scope.isDisabled = false;
       $scope.isList = false;
       $scope.hiddenIcons = false;
     }]);
 </file>
 </example>
 */
angular.module('patternfly.navigation').component('pfApplicationLauncher', {
  bindings: {
    items: '<',
    label: '@?',
    isDisabled: '<?',
    isList: '<?',
    hiddenIcons: '<?'
  },
  templateUrl: 'navigation/application-launcher.html',
  controller: function ($scope) {
    'use strict';
    var ctrl = this;

    ctrl.$id = $scope.$id;
  }
});

