angular.module('patternfly.navigation').component('pfVerticalNavigation', {
  bindings: {
    brandSrc: '@',
    brandAlt: '@',
    showBadges: '@',
    persistentSecondary: '@',
    pinnableMenus: '@',
    hiddenIcons: '@',
    items: '=',
    navigateCallback: '=?',
    itemClickCallback: '=?',
    updateActiveItemsOnClick: '@',
    ignoreMobile: '@'
  },
  //replace: true,
  templateUrl: 'navigation/vertical-navigation.html',
  transclude: true,
  controller: function ($window, $timeout, $injector, $location, $rootScope) {
    'use strict';
    var routeChangeListener,
      ctrl = this,
      $state;

    // Private internal functions
    var breakpoints = {
      'tablet': 768,
      'desktop': 1200
    };
    var explicitCollapse = false;
    var hoverDelay = 500;
    var hideDelay = hoverDelay + 200;

    var getBodyContentElement = function () {
      return angular.element(document.querySelector('.container-pf-nav-pf-vertical'));
    };

    var initBodyElement = function () {
      var bodyContentElement = getBodyContentElement();
      if (ctrl.showBadges) {
        bodyContentElement.addClass('nav-pf-vertical-with-badges');
      }
      if (ctrl.persistentSecondary) {
        bodyContentElement.addClass('nav-pf-persistent-secondary');
      }
      if (ctrl.hiddenIcons) {
        bodyContentElement.addClass('hidden-icons-pf');
      }
    };

    var updateMobileMenu = function (selected, secondaryItem) {
      ctrl.items.forEach(function (item) {
        item.isMobileItem = false;
        if (item.children) {
          item.children.forEach(function (nextSecondary) {
            nextSecondary.isMobileItem = false;
          });
        }
      });

      if (selected) {
        selected.isMobileItem = true;
        if (secondaryItem) {
          secondaryItem.isMobileItem = true;
          ctrl.showMobileSecondary = false;
          ctrl.showMobileTertiary = true;
        } else {
          ctrl.showMobileSecondary = true;
          ctrl.showMobileTertiary = false;
        }
      } else {
        ctrl.showMobileSecondary = false;
        ctrl.showMobileTertiary = false;
      }
    };


    var checkNavState = function () {
      var width = $window.innerWidth;
      var bodyContentElement = getBodyContentElement();

      // Check to see if we need to enter/exit the mobile state
      if (!ctrl.ignoreMobile && width < patternfly.pfBreakpoints.tablet) {
        if (!ctrl.inMobileState) {
          ctrl.inMobileState = true;

          //Set the body class to the correct state
          bodyContentElement.removeClass('collapsed-nav');
          bodyContentElement.addClass('hidden-nav');

          // Reset the collapsed states
          updateSecondaryCollapsedState(false);
          updateTertiaryCollapsedState(false);

          explicitCollapse = false;
        }
      } else  {
        ctrl.inMobileState = false;
        ctrl.showMobileNav = false;

        // Set the body class back to the default
        bodyContentElement.removeClass('hidden-nav');
      }

      if (explicitCollapse) {
        ctrl.navCollapsed = true;
        bodyContentElement.addClass('collapsed-nav');
      } else {
        ctrl.navCollapsed = false;
        bodyContentElement.removeClass('collapsed-nav');
      }
    };

    var collapseMenu = function () {
      var bodyContentElement = getBodyContentElement();
      ctrl.navCollapsed = true;

      //Set the body class to the correct state
      bodyContentElement.addClass('collapsed-nav');

      explicitCollapse = true;
    };

    var expandMenu = function () {
      var bodyContentElement = getBodyContentElement();
      ctrl.navCollapsed = false;

      //Set the body class to the correct state
      bodyContentElement.removeClass('collapsed-nav');

      explicitCollapse = false;

      // Dispatch a resize event when showing the expanding then menu to
      // allow content to adjust to the menu sizing
      angular.element($window).triggerHandler('resize');
    };

    var forceHideSecondaryMenu = function () {
      ctrl.forceHidden = true;
      $timeout(function () {
        ctrl.forceHidden = false;
      }, 500);
    };

    var setParentActive = function (item) {
      ctrl.items.forEach(function (topLevel) {
        if (topLevel.children) {
          topLevel.children.forEach(function (secondLevel) {
            if (secondLevel === item) {
              topLevel.isActive = true;
            }
            if (secondLevel.children) {
              secondLevel.children.forEach(function (thirdLevel) {
                if (thirdLevel === item) {
                  topLevel.isActive = true;
                  secondLevel.isActive = true;
                }
              });
            }
          });
        }
      });
    };

    var getFirstNavigateChild = function (item) {
      var firstChild;
      if (!item.children || item.children.length < 1) {
        firstChild = item;
      } else {
        firstChild = getFirstNavigateChild(item.children[0]);
      }
      return firstChild;
    };

    var setSecondaryItemVisible = function () {
      var bodyContentElement = getBodyContentElement();
      ctrl.activeSecondary = false;

      if (ctrl.persistentSecondary && !ctrl.inMobileState) {
        ctrl.items.forEach(function (topLevel) {
          if (topLevel.children) {
            topLevel.children.forEach(function (secondLevel) {
              if (secondLevel.isActive) {
                ctrl.activeSecondary = true;
              }
            });
          }
        });
        if (ctrl.activeSecondary) {
          bodyContentElement.addClass('secondary-visible-pf');
        } else {
          bodyContentElement.removeClass('secondary-visible-pf');
        }
      }
    };

    var navigateToItem = function (item) {
      var navItem = getFirstNavigateChild(item);
      var navTo;
      if (navItem) {
        ctrl.showMobileNav = false;
        if (navItem.uiSref && navItem.href) {
          throw new Error('Using both uiSref and href on an item is not supported.');
        }
        if (navItem.uiSref) {
          if ($state === undefined) {
            throw new Error('uiSref is defined on item, but no $state has been injected. ' +
              'Did you declare a dependency on "ui.router" module in your app?');
          }
          $state.go(navItem.uiSref, navItem.uiSrefOptions);
        } else {
          navTo = navItem.href;
          if (navTo) {
            if (navTo.startsWith('#/')) {
              navTo = navTo.substring(2);
            }
            $location.path(navTo);
          }
        }
        if (ctrl.navigateCallback) {
          ctrl.navigateCallback(navItem);
        }
      }

      if (ctrl.itemClickCallback) {
        ctrl.itemClickCallback(item);
      }

      if (ctrl.updateActiveItemsOnClick ) {
        ctrl.clearActiveItems();
        navItem.isActive = true;
        setParentActive(navItem);
        setSecondaryItemVisible();
      }
      setSecondaryItemVisible();
    };

    var primaryHover = function () {
      var hover = false;
      ctrl.items.forEach(function (item) {
        if (item.isHover) {
          hover = true;
        }
      });
      return hover;
    };

    var secondaryHover = function () {
      var hover = false;
      ctrl.items.forEach(function (item) {
        if (item.children && item.children.length > 0) {
          item.children.forEach(function (secondaryItem) {
            if (secondaryItem.isHover) {
              hover = true;
            }
          });
        }
      });
      return hover;
    };

    var updateSecondaryCollapsedState = function (setCollapsed, collapsedItem) {
      var bodyContentElement = getBodyContentElement();
      if (collapsedItem) {
        collapsedItem.secondaryCollapsed = setCollapsed;
      }
      if (setCollapsed) {
        ctrl.collapsedSecondaryNav = true;

        bodyContentElement.addClass('collapsed-secondary-nav-pf');
      } else {
        // Remove any collapsed secondary menus
        if (ctrl.items) {
          ctrl.items.forEach(function (item) {
            item.secondaryCollasped = false;
          });
        }
        ctrl.collapsedSecondaryNav = false;

        bodyContentElement.removeClass('collapsed-secondary-nav-pf');
      }
    };

    var updateTertiaryCollapsedState = function (setCollapsed, collapsedItem) {
      var bodyContentElement = getBodyContentElement();
      if (collapsedItem) {
        collapsedItem.tertiaryCollapsed = setCollapsed;
      }
      if (setCollapsed) {
        ctrl.collapsedTertiaryNav = true;

        bodyContentElement.addClass('collapsed-tertiary-nav-pf');
        updateSecondaryCollapsedState(false);
      } else {
        // Remove any collapsed secondary menus
        if (ctrl.items) {
          ctrl.items.forEach(function (item) {
            if (item.children && item.children.length > 0) {
              item.children.forEach(function (secondaryItem) {
                secondaryItem.tertiaryCollasped = false;
              });
            }
          });
        }
        ctrl.collapsedTertiaryNav = false;

        bodyContentElement.removeClass('collapsed-tertiary-nav-pf');
      }
    };

    ctrl.showBadges = ctrl.showBadges === 'true';
    ctrl.persistentSecondary = ctrl.persistentSecondary === 'true';
    ctrl.pinnableMenus = ctrl.pinnableMenus === 'true';
    ctrl.hiddenIcons = ctrl.hiddenIcons === 'true';
    ctrl.updateActiveItemsOnClick = ctrl.updateActiveItemsOnClick === 'true';
    ctrl.ignoreMobile = ctrl.ignoreMobile === 'true';
    ctrl.activeSecondary = false;
    ctrl.showMobileNav = false;
    ctrl.showMobileSecondary = false;
    ctrl.showMobileTertiary = false;
    ctrl.hoverSecondaryNav = false;
    ctrl.hoverTertiaryNav = false;
    ctrl.collapsedSecondaryNav = false;
    ctrl.collapsedTertiaryNav = false;
    ctrl.navCollapsed = false;
    ctrl.forceHidden = false;

    ctrl.clearActiveItems = function () {
      ctrl.items.forEach(function (item) {
        item.isActive = false;
        if (item.children) {
          item.children.forEach(function (secondary) {
            secondary.isActive = false;
            if (secondary.children) {
              secondary.children.forEach(function (tertiary) {
                tertiary.isActive = false;
              });
            }
          });
        }
      });
    };

    ctrl.setActiveItems = function () {
      var updatedRoute = "#" + $location.path();
      //Setting active state on load
      ctrl.items.forEach(function (topLevel) {
        if (updatedRoute.indexOf(topLevel.href) > -1) {
          topLevel.isActive = true;
        }
        if (topLevel.children) {
          topLevel.children.forEach(function (secondLevel) {
            if (updatedRoute.indexOf(secondLevel.href) > -1) {
              secondLevel.isActive = true;
              topLevel.isActive = true;
            }
            if (secondLevel.children) {
              secondLevel.children.forEach(function (thirdLevel) {
                if (updatedRoute.indexOf(thirdLevel.href) > -1) {
                  thirdLevel.isActive = true;
                  secondLevel.isActive = true;
                  topLevel.isActive = true;
                }
              });
            }
          });
        }
      });
    };

    ctrl.handleNavBarToggleClick = function () {

      if (ctrl.inMobileState) {
        // Toggle the mobile nav
        if (ctrl.showMobileNav) {
          ctrl.showMobileNav = false;
        } else {
          // Always start at the primary menu
          updateMobileMenu();
          ctrl.showMobileNav = true;
        }
      } else if (ctrl.navCollapsed) {
        expandMenu();
      } else {
        collapseMenu();
      }
    };

    ctrl.handlePrimaryClick = function (item, event) {
      if (ctrl.inMobileState) {
        if (item.children && item.children.length > 0) {
          updateMobileMenu(item);
        } else {
          updateMobileMenu();
          navigateToItem(item);
        }
      } else {
        navigateToItem(item);
      }
    };

    ctrl.handleSecondaryClick = function (primary, secondary, event) {
      if (ctrl.inMobileState) {
        if (secondary.children && secondary.children.length > 0) {
          updateMobileMenu(primary, secondary);
        } else {
          updateMobileMenu();
          navigateToItem(secondary);
        }
      } else {
        navigateToItem(secondary);
      }
    };

    ctrl.handleTertiaryClick = function (primary, secondary, tertiary, event) {
      if (ctrl.inMobileState) {
        updateMobileMenu();
      }

      navigateToItem(tertiary);
    };

    // Show secondary nav bar on hover of primary nav items
    ctrl.handlePrimaryHover = function (item) {
      if (item.children && item.children.length > 0) {
        if (!ctrl.inMobileState) {
          if (item.navUnHoverTimeout !== undefined) {
            $timeout.cancel(item.navUnHoverTimeout);
            item.navUnHoverTimeout = undefined;
          } else if (ctrl.navHoverTimeout === undefined && !item.isHover) {
            item.navHoverTimeout = $timeout(function () {
              ctrl.hoverSecondaryNav = true;
              item.isHover = true;
              item.navHoverTimeout = undefined;
            }, hoverDelay);
          }
        }
      }
    };

    ctrl.handlePrimaryUnHover = function (item) {
      if (item.children && item.children.length > 0) {
        if (item.navHoverTimeout !== undefined) {
          $timeout.cancel(item.navHoverTimeout);
          item.navHoverTimeout = undefined;
        } else if (item.navUnHoverTimeout === undefined && item.isHover) {
          item.navUnHoverTimeout = $timeout(function () {
            item.isHover = false;
            if (!primaryHover()) {
              ctrl.hoverSecondaryNav = false;
            }
            item.navUnHoverTimeout = undefined;
          }, hideDelay);
        }
      }
    };

    // Show tertiary nav bar on hover of secondary nav items
    ctrl.handleSecondaryHover = function (item) {
      if (item.children && item.children.length > 0) {
        if (!ctrl.inMobileState) {
          if (item.navUnHoverTimeout !== undefined) {
            $timeout.cancel(item.navUnHoverTimeout);
            item.navUnHoverTimeout = undefined;
          } else if (ctrl.navHoverTimeout === undefined) {
            item.navHoverTimeout = $timeout(function () {
              ctrl.hoverTertiaryNav = true;
              item.isHover = true;
              item.navHoverTimeout = undefined;
            }, hoverDelay);
          }
        }
      }
    };

    ctrl.handleSecondaryUnHover = function (item) {
      if (item.children && item.children.length > 0) {
        if (item.navHoverTimeout !== undefined) {
          $timeout.cancel(item.navHoverTimeout);
          item.navHoverTimeout = undefined;
        } else if (item.navUnHoverTimeout === undefined) {
          item.navUnHoverTimeout = $timeout(function () {
            item.isHover = false;
            if (!secondaryHover()) {
              ctrl.hoverTertiaryNav = false;
            }
            item.navUnHoverTimeout = undefined;
          }, hideDelay);
        }
      }
    };

    ctrl.collapseSecondaryNav = function (item, event) {
      if (ctrl.inMobileState) {
        updateMobileMenu();
      } else {
        if (item.secondaryCollapsed) {
          updateSecondaryCollapsedState(false, item);
          forceHideSecondaryMenu();
        } else {
          updateSecondaryCollapsedState(true, item);
        }
      }

      ctrl.hoverSecondaryNav = false;
      event.stopImmediatePropagation();
    };

    ctrl.collapseTertiaryNav = function (item, event) {
      if (ctrl.inMobileState) {
        ctrl.items.forEach(function (primaryItem) {
          if (primaryItem.children) {
            primaryItem.children.forEach(function (secondaryItem) {
              if (secondaryItem === item) {
                updateMobileMenu(primaryItem);
              }
            });
          }
        });
      } else {
        if (item.tertiaryCollapsed) {
          updateTertiaryCollapsedState(false, item);
          forceHideSecondaryMenu();
        } else {
          updateTertiaryCollapsedState(true, item);
        }
      }

      ctrl.hoverSecondaryNav = false;
      ctrl.hoverTertiaryNav = false;
      event.stopImmediatePropagation();
    };


    ctrl.$onInit = function () {
      // Optional dependency on $state
      if ($injector.has("$state")) {
        $state = $injector.get("$state");
      }

      if (!ctrl.updateActiveItemsOnClick) {
        if ($rootScope) {
          routeChangeListener = $rootScope.$on("$routeChangeSuccess", function (event, next, current) {
            ctrl.clearActiveItems();
            ctrl.setActiveItems();
          });
        }
      }

      initBodyElement();
      checkNavState();

      // Need to bind to resize event
      angular.element($window).on('resize', function () {
        checkNavState();
      });
    };

    ctrl.$onDestroy = function () {
      if (_.isFunction(routeChangeListener)) {
        routeChangeListener();
      }
    };
  },
});
