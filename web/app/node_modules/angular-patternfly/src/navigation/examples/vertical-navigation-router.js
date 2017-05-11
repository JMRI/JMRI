/**
 * @ngdoc directive
 * @name patternfly.navigation.directive:pfVerticalNavigation - Router
 *
 * @description
 *   This example shows how to use pfVerticalNavigation with angular-ui-router's $states and uiSrefs.
 *
 * @param {string} brandSrc src for brand image
 * @param {string} brandAlt  Text for product name when brand image is not available
 * @param {boolean} showBadges Flag if badges are used on navigation items, default: false
 * @param {boolean} persistentSecondary Flag to use persistent secondary menus, default: false
 * @param {boolean} hiddenIcons Flag to not show icons on the primary menu, default: false
 * @param {array} items List of navigation items
 * <ul style='list-style-type: none'>
 * <li>.title          - (string) Name of item to be displayed on the menu
 * <li>.iconClass      - (string) Classes for icon to be shown on the menu (ex. "fa fa-dashboard")
 * <li>.href           - (string) href link to navigate to on click
 * <li>.children       - (array) Submenu items (same structure as top level items)
 * <li>.badges         -  (array) Badges to display for the item, badges with a zero count are not displayed.
 *   <ul style='list-style-type: none'>
 *   <li>.count        - (number) Count to display in the badge
 *   <li>.iconClass    - (string) Class to use for showing an icon before the count
 *   <li>.tooltip      - (string) Tooltip to display for the badge
 *   <li>.badgeClass:  - (string) Additional class(es) to add to the badge container
 *   </ul>
 * <li>.uiSref         - (string) Optional Angular UI Router state name. If specified, href must be not defined, and vice versa.
 * <li>.uiSrefOptions  - (object) Optional object to be passed to Angular UI Router $state.go() function
 * </ul>
 * @param {function} navigateCallback function(item) Callback method invoked on a navigation item click (one with no submenus)
 * @param {function} itemClickCallback function(item) Callback method invoked on an item click
 * @param {boolean} updateActiveItemsOnClick Flag if active items should be marked on click rather than on navigation change, default: false
 * @param {boolean} ignoreMobile Flag if mobile state should be ignored (use only if absolutely necessary) default: false
 *
 * @example
 <example module="myApp" deps="patternfly.utils, patternfly.filters, patternfly.sort, patternfly.views">
  <file name="index.html">
    <div>
      <button class="btn btn-primary" id="showVerticalNavWithRouter" onclick="showVerticalNavWithRouter">Show Vertical Navigation with UIRouter</button>
      <label class="example-info-text">This will display the vertical nav bar and some mock content over the content of this page.</label>
      <label class="example-info-text">Exit the demo to return back to this page.</label>
    </div>
    <div id="verticalNavWithRouterLayout" class="layout-pf layout-pf-fixed faux-layout hidden" ng-controller="vertNavWithRouterController">
      <div pf-vertical-navigation items="navigationItems" brand-alt="ANGULAR PATTERNFLY"
          show-badges="true" pinnable-menus="true" update-active-items-on-click="true"
          navigate-callback="handleNavigateClickRouter">
        <div>
          <ul class="nav navbar-nav">
          <li><button id="hideVerticalNavWithRouter" class="hide-vertical-nav">Exit Vertical Navigation Demo</button></li>
          </ul>
          <ul class="nav navbar-nav navbar-right navbar-iconic">
            <li class="dropdown">
            </li>
            <li class="dropdown">
              <a class="dropdown-toggle nav-item-iconic" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                <span title="Help" class="fa pficon-help"></span>
                <span class="caret"></span>
              </a>
              <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
                <li><a href="#">Help</a></li>
                <li><a href="#">About</a></li>
              </ul>
            </li>
            <li class="dropdown">
              <a class="dropdown-toggle nav-item-iconic" id="dropdownMenu2" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                <span title="Username" class="fa pficon-user"></span>
                <span class="caret"></span>
              </a>
              <ul class="dropdown-menu" aria-labelledby="dropdownMenu2">
                <li><a href="#">Preferences</a></li>
                <li><a href="#">Logout</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
      <div id="contentContainer" class="container-fluid container-cards-pf container-pf-nav-pf-vertical example-page-container">
        <ui-view>
          <!-- Content will be added here -->
        </ui-view>
      </div>
    </div>
  </file>
  <file name="script.js">
    angular.module('myApp',['patternfly.navigation', 'ui.router'])
      .config(function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise('dashboard');

        $stateProvider
            .state('dashboard', {
                url: '/dashboard',
                template: '<div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
                              <div class="card-pf-body" style="height: 50px;">\
                                <p class="card-pf-aggregate-status-notifications">\
                                  State: Dashboard\
                                </p>\
                              </div>\
                            </div>'
            })
            .state('dolor', {
                url: '/dolor',
                template: '<div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
                              <div class="card-pf-body" style="height: 50px;">\
                                <p class="card-pf-aggregate-status-notifications">\
                                  State: Dolor\
                                </p>\
                              </div>\
                            </div>'
            })
            .state('ipsum', {
                url: '/ipsum',
                template: '<div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
                              <div class="card-pf-body" style="height: 50px;">\
                                <p class="card-pf-aggregate-status-notifications">\
                                  State: Ipsum\
                                </p>\
                              </div>\
                            </div>'
            });
    })
      .controller('vertNavWithRouterController', ['$scope',
        function ($scope) {
          $scope.navigationItems = [
            {
              title: "Dashboard",
              iconClass: "fa fa-dashboard",
              uiSref: "dashboard",
              uiSrefOptions: { someKey: 'SomeValue' }
            },
            {
              title: "Dolor",
              iconClass : "fa fa-shield",
              uiSref: "dolor"
            },
            {
              title: "Ipsum",
              iconClass : "fa fa-space-shuttle",
              uiSref: "ipsum"
            },
            {
              title: "Exit Demo"
            }
          ];
          $scope.handleNavigateClickRouter = function (item) {
            if (item.title === "Exit Demo") {
              angular.element(document.querySelector("#verticalNavWithRouterLayout")).addClass("hidden");
            }
          };
        }
      ]);
  </file>
  <file name="hide-show.js">
    $(document).ready(function() {
      $(document).on('click', '#showVerticalNavWithRouter', function() {
        $(document.getElementById("verticalNavWithRouterLayout")).removeClass("hidden");
      });
      $(document).on('click', '#hideVerticalNavWithRouter', function() {
        $(document.getElementById("verticalNavWithRouterLayout")).addClass("hidden");
      });
    });
  </file>
</example>
*/
