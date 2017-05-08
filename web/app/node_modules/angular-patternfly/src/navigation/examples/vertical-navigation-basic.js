/**
 * @ngdoc directive
 * @name patternfly.navigation.directive:pfVerticalNavigation - Basic
 *
 * @description
 *   Directive for vertical navigation. This sets up the nav bar header with the collapse button (hamburger) and the
 *   application brand image (or text) as well as the vertical navigation bar containing the navigation items. This
 *   directive supports primary, secondary, and tertiary navigation with options to allow pinning of the secondary and
 *   tertiary navigation menus as well as the option for persistent secondary menus.
 *   <br><br>
 *   The remaining parts of the navbar header can be transcluded.
 *   <br><br>
 *   Tha navigation items are marked active based on the current location and the href value for the item. If not using
 *   href's on the items to navigate, set update-active-items-on-click to "true".
 *   <br><br>
 *   This directive works in conjunction with the main content container if the 'container-pf-nav-pf-vertical' class
 *   selector is added to the main content container.
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
 * </ul>
 * @param {function} navigateCallback function(item) Callback method invoked on a navigation item click (one with no submenus)
 * @param {function} itemClickCallback function(item) Callback method invoked on an item click
 * @param {boolean} updateActiveItemsOnClick Flag if active items should be marked on click rather than on navigation change, default: false
 * @param {boolean} ignoreMobile Flag if mobile state should be ignored (use only if absolutely necessary) default: false
 *
 * @example
 <example module="patternfly.navigation" deps="patternfly.utils, patternfly.filters, patternfly.sort, patternfly.views">
  <file name="index.html">
  <div>
    <button class="btn btn-primary" id="showVerticalNav" onclick="showVerticalNav">Show Vertical Navigation</button>
    <label class="example-info-text">This will display the vertical nav bar and some mock content over the content of this page.</label>
    <label class="example-info-text">Exit the demo to return back to this page.</label>
  </div>
  <div id="verticalNavLayout" class="layout-pf layout-pf-fixed faux-layout hidden" ng-controller="vertNavController">
    <div pf-vertical-navigation items="navigationItems" brand-alt="ANGULAR PATTERNFLY"
         show-badges="true" pinnable-menus="true" update-active-items-on-click="true"
         navigate-callback="handleNavigateClick">
      <div>
        <ul class="nav navbar-nav">
        <li><button id="hideVerticalNav" class="hide-vertical-nav">Exit Vertical Navigation Demo</button></li>
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
      <div id="includedContent"></div>
      </div>
    </div>
  </file>
  <file name="script.js">
  angular.module('patternfly.navigation').controller('vertNavController', ['$scope',
    function ($scope) {
      $scope.navigationItems = [
        {
           title: "Dashboard",
           iconClass: "fa fa-dashboard",
           href: "#/dashboard"
        },
        {
           title: "Dolor",
           iconClass : "fa fa-shield",
           href: "#/dolor",
           badges: [
             {
               count: 1283,
               tooltip: "Total number of items"
             }
           ]
        },
        {
           title: "Ipsum",
           iconClass: "fa fa-space-shuttle",
           children: [
              {
                 title: "Intellegam",
                 children: [
                    {
                       title: "Recteque",
                       href: "#/ipsum/intellegam/recteque",
                       badges: [
                         {
                           count: 6,
                           tooltip: "Total number of error items",
                           badgeClass: 'example-error-background'
                         }
                       ]
                    },
                    {
                       title: "Suavitate",
                       href: "#/ipsum/intellegam/suavitate",
                       badges: [
                         {
                           count: 2,
                           tooltip: "Total number of items"
                         }
                       ]
                    },
                    {
                       title: "Vituperatoribus",
                       href: "#/ipsum/intellegam/vituperatoribus",
                       badges: [
                         {
                           count: 18,
                           tooltip: "Total number of warning items",
                           badgeClass: 'example-warning-background'
                         }
                       ]
                    }
                 ]
              },
              {
                 title: "Copiosae",
                 children: [
                    {
                       title: "Exerci",
                       href: "#/ipsum/copiosae/exerci",
                       badges: [
                         {
                           count: 2,
                           tooltip: "Total number of error items",
                           iconClass: 'pficon pficon-error-circle-o'
                         },
                         {
                           count: 6,
                           tooltip: "Total number warning error items",
                           iconClass: 'pficon pficon-warning-triangle-o'
                         }
                       ]
                    },
                    {
                       title: "Quaeque",
                       href: "#/ipsum/copiosae/quaeque",
                       badges: [
                         {
                           count: 0,
                           tooltip: "Total number of error items",
                           iconClass: 'pficon pficon-error-circle-o'
                         },
                         {
                           count: 4,
                           tooltip: "Total number warning error items",
                           iconClass: 'pficon pficon-warning-triangle-o'
                         }
                       ]
                    },
                    {
                       title: "Utroque",
                       href: "#/ipsum/copiosae/utroque",
                       badges: [
                         {
                           count: 1,
                           tooltip: "Total number of error items",
                           iconClass: 'pficon pficon-error-circle-o'
                         },
                         {
                           count: 2,
                           tooltip: "Total number warning error items",
                           iconClass: 'pficon pficon-warning-triangle-o'
                         }
                       ]
                    }
                 ]
              },
              {
                 title: "Patrioque",
                 children: [
                    {
                       title: "Novum",
                       href: "#/ipsum/patrioque/novum"
                    },
                    {
                       title: "Pericula",
                       href: "#/ipsum/patrioque/pericula"
                    },
                    {
                       title: "Gubergren",
                       href: "#/ipsum/patrioque/gubergren"
                    }
                 ]
              },
              {
                 title: "Accumsan",
                 href: "#/ipsum/Accumsan",
                 badges: [
                   {
                     count: 2,
                     tooltip: "Total number of error items",
                     iconClass: 'pficon pficon-error-circle-o'
                   },
                   {
                     count: 6,
                     tooltip: "Total number warning error items",
                     iconClass: 'pficon pficon-warning-triangle-o'
                   }
                 ]
              }
           ]
        },
        {
           title: "Amet",
           iconClass: "fa fa-paper-plane",
           children: [
              {
                 title: "Detracto",
                 children: [
                    {
                       title: "Delicatissimi",
                       href: "#/amet/detracto/delicatissimi"
                    },
                    {
                       title: "Aliquam",
                       href: "#/amet/detracto/aliquam"
                    },
                    {
                       title: "Principes",
                       href: "#/amet/detracto/principes"
                    }
                 ]
              },
              {
                 title: "Mediocrem",
                 children: [
                    {
                       title: "Convenire",
                       href: "#/amet/mediocrem/convenire"
                    },
                    {
                       title: "Nonumy",
                       href: "#/amet/mediocrem/nonumy"
                    },
                    {
                       title: "Deserunt",
                       href: "#/amet/mediocrem/deserunt"
                    }
                 ]
              },
              {
                 title: "Corrumpit",
                 children: [
                    {
                       title: "Aeque",
                       href: "#/amet/corrumpit/aeque"
                    },
                    {
                       title: "Delenit",
                       href: "#/amet/corrumpit/delenit"
                    },
                    {
                       title: "Qualisque",
                       href: "#/amet/corrumpit/qualisque"
                    }
                 ]
              },
              {
                 title: "urbanitas",
                 href: "#/amet/urbanitas"
              }
           ]
        },
        {
           title: "Adipscing",
           iconClass: "fa fa-graduation-cap",
           href: "#/adipscing"
        },
        {
           title: "Lorem",
           iconClass: "fa fa-gamepad",
           href: "#/lorem"
        },
        {
           title: "Exit Demo"
        }
      ];
      $scope.handleNavigateClick = function (item) {
        if (item.title === "Exit Demo") {
          angular.element(document.querySelector("#verticalNavLayout")).addClass("hidden");
        }
      };
    }
  ]);
  </file>
  <file name="add_content.js">
    $(document).ready(function() {
      $("#includedContent")[0].innerHTML = '\
      <div class="row row-cards-pf"> \
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
            <h2 class="card-pf-title" style="height: 17px;">\
            <span class="fa fa-shield"></span><span class="card-pf-aggregate-status-count">0</span> Ipsum\
            </h2>\
            <div class="card-pf-body" style="height: 50px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <span class="card-pf-aggregate-status-notification"><a href="#" class="add" data-toggle="tooltip" data-placement="top" title="Add Ipsum"><span class="pficon pficon-add-circle-o"></span></a></span>\
              </p>\
            </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
            <h2 class="card-pf-title" style="height: 17px;">\
              <a href="#"><span class="fa fa-shield"></span><span class="card-pf-aggregate-status-count">20</span> Amet</a>\
            </h2>\
            <div class="card-pf-body" style="height: 50px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <span class="card-pf-aggregate-status-notification"><a href="#"><span class="pficon pficon-error-circle-o"></span>4</a></span>\
                <span class="card-pf-aggregate-status-notification"><a href="#"><span class="pficon pficon-warning-triangle-o"></span>1</a></span>\
              </p>\
            </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6 col-md-3">\
            <div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
              <h2 class="card-pf-title" style="height: 17px;">\
                <a href="#"><span class="fa fa-shield"></span><span class="card-pf-aggregate-status-count">9</span> Adipiscing</a>\
              </h2>\
              <div class="card-pf-body" style="height: 50px;">\
                <p class="card-pf-aggregate-status-notifications">\
                  <span class="card-pf-aggregate-status-notification"><span class="pficon pficon-ok"></span></span>\
                </p>\
              </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status" style="height: 89px;">\
            <h2 class="card-pf-title" style="height: 17px;">\
              <a href="#"><span class="fa fa-shield"></span><span class="card-pf-aggregate-status-count">12</span> Lorem</a>\
            </h2>\
            <div class="card-pf-body" style="height: 50px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <a href="#"><span class="card-pf-aggregate-status-notification"><span class="pficon pficon-error-circle-o"></span>1</span></a>\
              </p>\
            </div>\
          </div>\
        </div>\
      </div>\
      <div class="row row-cards-pf">\
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status card-pf-aggregate-status-mini" style="height: 59px;">\
            <h2 class="card-pf-title" style="height: 42px;">\
              <span class="fa fa-rebel"></span>\
              <span class="card-pf-aggregate-status-count">0</span> Ipsum\
            </h2>\
            <div class="card-pf-body" style="height: 24px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <span class="card-pf-aggregate-status-notification"><a href="#" class="add" data-toggle="tooltip" data-placement="top" title="Add Ipsum"><span class="pficon pficon-add-circle-o"></span></a></span>\
              </p>\
            </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status card-pf-aggregate-status-mini" style="height: 59px;">\
            <h2 class="card-pf-title" style="height: 42px;">\
              <a href="#">\
                <span class="fa fa-paper-plane"></span>\
                <span class="card-pf-aggregate-status-count">20</span> Amet\
              </a>\
            </h2>\
            <div class="card-pf-body" style="height: 24px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <span class="card-pf-aggregate-status-notification"><a href="#"><span class="pficon pficon-error-circle-o"></span>4</a></span>\
              </p>\
            </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status card-pf-aggregate-status-mini" style="height: 59px;">\
            <h2 class="card-pf-title" style="height: 42px;">\
              <a href="#">\
                <span class="pficon pficon-cluster"></span>\
                <span class="card-pf-aggregate-status-count">9</span> Adipiscing\
              </a>\
            </h2>\
            <div class="card-pf-body" style="height: 24px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <span class="card-pf-aggregate-status-notification"><span class="pficon pficon-ok"></span></span>\
              </p>\
            </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6 col-md-3">\
          <div class="card-pf card-pf-accented card-pf-aggregate-status card-pf-aggregate-status-mini" style="height: 59px;">\
            <h2 class="card-pf-title" style="height: 42px;">\
              <a href="#">\
                <span class="pficon pficon-image"></span>\
                <span class="card-pf-aggregate-status-count">12</span> Lorem\
              </a>\
            </h2>\
            <div class="card-pf-body" style="height: 24px;">\
              <p class="card-pf-aggregate-status-notifications">\
                <a href="#"><span class="card-pf-aggregate-status-notification"><span class="pficon pficon-error-circle-o"></span>1</span></a>\
              </p>\
            </div>\
          </div>\
        </div>\
      </div>\
      <div class="row row-cards-pf">\
        <div class="col-xs-12 col-sm-6">\
          <div class="card-pf" style="height: 360px;">\
            <div class="card-pf-heading">\
              <h2 class="card-pf-title" style="height: 17px;">\
                Top Utilized\
              </h2>\
            </div>\
            <div class="card-pf-body" style="height: 280px;">\
              <div class="progress-description">\
                Ipsum\
              </div>\
              <div class="progress progress-label-top-right">\
                <div class="progress-bar progress-bar-danger" role="progressbar"style="width: 95%;" data-toggle="tooltip" title="95% Used">\
                  <span><strong>190.0 of 200.0 GB</strong> Used</span>\
                </div>\
                <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 5%;" data-toggle="tooltip" title="5% Available">\
                  <span class="sr-only">5% Available</span>\
                </div>\
              </div>\
              <div class="progress-description">\
                Amet\
              </div>\
              <div class="progress progress-label-top-right">\
                <div class="progress-bar progress-bar-success" role="progressbar" style="width: 50%;" data-toggle="tooltip" title="50% Used">\
                  <span><strong>100.0 of 200.0 GB</strong> Used</span>\
                </div>\
                <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 50%;" data-toggle="tooltip" title="50% Available">\
                  <span class="sr-only">50% Available</span>\
                </div>\
              </div>\
              <div class="progress-description">\
                Adipiscing\
              </div>\
              <div class="progress progress-label-top-right">\
                <div class="progress-bar progress-bar-warning" role="progressbar" style="width: 70%;" data-toggle="tooltip" title="70% Used">\
                  <span><strong>140.0 of 200.0 GB</strong> Used</span>\
                </div>\
                <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 30%;" data-toggle="tooltip" title="30% Available">\
                  <span class="sr-only">30% Available</span>\
                </div>\
              </div>\
              <div class="progress-description">\
                Lorem\
              </div>\
              <div class="progress progress-label-top-right">\
                <div class="progress-bar progress-bar-warning" role="progressbar" style="width: 76.5%;" data-toggle="tooltip" title="76.5% Used">\
                  <span><strong>153.0 of 200.0 GB</strong> Used</span>\
                </div>\
                <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 23.5%;" data-toggle="tooltip" title="23.5% Available">\
                  <span class="sr-only">23.5% Available</span>\
                </div>\
              </div>\
            </div>\
          </div>\
        </div>\
        <div class="col-xs-12 col-sm-6">\
          <div class="card-pf" style="height: 360px;">\
            <div class="card-pf-heading">\
              <h2 class="card-pf-title" style="height: 17px;">\
                Quotas\
              </h2>\
            </div>\
            <div class="card-pf-body" style="height: 280px;">\
              <div class="progress-container progress-description-left progress-label-right">\
                <div class="progress-description">\
                  Ipsum\
                </div>\
                <div class="progress">\
                  <div class="progress-bar" role="progressbar" style="width: 25%;" data-toggle="tooltip" title="25% Used">\
                    <span><strong>115 of 460</strong> MHz</span>\
                  </div>\
                  <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 75%;" data-toggle="tooltip" title="75% Available">\
                    <span class="sr-only">75% Available</span>\
                  </div>\
                </div>\
              </div>\
              <div class="progress-container progress-description-left progress-label-right">\
                <div class="progress-description">\
                  Amet\
                </div>\
                <div class="progress">\
                  <div class="progress-bar" role="progressbar" style="width: 50%;" data-toggle="tooltip" title="8 GB Used">\
                    <span><strong>8 of 16</strong> GB</span>\
                  </div>\
                  <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 50%;" data-toggle="tooltip" title="8 GB Available">\
                    <span class="sr-only">50% Available</span>\
                  </div>\
                </div>\
              </div>\
              <div class="progress-container progress-description-left progress-label-right">\
                <div class="progress-description">\
                  Adipiscing\
                </div>\
                <div class="progress">\
                  <div class="progress-bar" role="progressbar" style="width: 62.5%;" data-toggle="tooltip" title="62.5% Used">\
                    <span><strong>5 of 8</strong> Total</span>\
                  </div>\
                  <div class="progress-bar progress-bar-remaining" role="progressbar" style="width: 37.5%;" data-toggle="tooltip" title="37.5% Available">\
                    <span class="sr-only">37.5% Available</span>\
                  </div>\
                </div>\
              </div>\
              <div class="progress-container progress-description-left progress-label-right">\
                <div class="progress-description">\
                  Lorem\
                </div>\
                <div class="progress">\
                  <div class="progress-bar" role="progressbar" style="width: 100%;" data-toggle="tooltip" title="100% Used">\
                    <span><strong>2 of 2</strong> Total</span>\
                  </div>\
                </div>\
              </div>\
            </div>\
          </div>\
        </div>\
      </div>\
      ';
    });
  </file>
  <file name="hide-show.js">
    $(document).ready(function() {
      $(document).on('click', '#showVerticalNav', function() {
        $(document.getElementById("verticalNavLayout")).removeClass("hidden");
      });
      $(document).on('click', '#hideVerticalNav', function() {
        $(document.getElementById("verticalNavLayout")).addClass("hidden");
      });
    });
  </file>
</example>
*/
