angular.module('patternfly.navigation').directive('pfVerticalNavigation', ['$location', '$rootScope', '$window', '$document', '$timeout',  '$injector',
  function (location, rootScope, $window, $document, $timeout, $injector) {
    'use strict';
    var $state;

    // Optional dependency on $state
    if ($injector.has("$state")) {
      $state = $injector.get("$state");
    }

    return {
      restrict: 'A',
      scope: {
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
      replace: true,
      templateUrl: 'navigation/vertical-navigation.html',
      transclude: true,
      controller: function ($scope) {
        var routeChangeListener;

        $scope.showBadges = $scope.showBadges === 'true';
        $scope.persistentSecondary = $scope.persistentSecondary === 'true';
        $scope.pinnableMenus = $scope.pinnableMenus === 'true';
        $scope.hiddenIcons = $scope.hiddenIcons === 'true';
        $scope.updateActiveItemsOnClick = $scope.updateActiveItemsOnClick === 'true';
        $scope.ignoreMobile = $scope.ignoreMobile === 'true';
        $scope.activeSecondary = false;

        $scope.clearActiveItems = function () {
          $scope.items.forEach(function (item) {
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

        $scope.setActiveItems = function () {
          var updatedRoute = "#" + location.path();
          //Setting active state on load
          $scope.items.forEach(function (topLevel) {
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

        if (!$scope.updateActiveItemsOnClick) {
          routeChangeListener = rootScope.$on( "$routeChangeSuccess", function (event, next, current) {
            $scope.clearActiveItems();
            $scope.setActiveItems();
          });

          $scope.$on('$destroy', routeChangeListener);
        }
      },
      link: function ($scope) {
        var breakpoints = {
          'tablet': 768,
          'desktop': 1200
        };

        var getBodyContentElement = function () {
          return angular.element(document.querySelector('.container-pf-nav-pf-vertical'));
        };

        var explicitCollapse = false;
        var hoverDelay = 500;
        var hideDelay = hoverDelay + 200;

        var  initBodyElement = function () {
          var bodyContentElement = getBodyContentElement();
          if ($scope.showBadges) {
            bodyContentElement.addClass('nav-pf-vertical-with-badges');
          }
          if ($scope.persistentSecondary) {
            bodyContentElement.addClass('nav-pf-persistent-secondary');
          }
          if ($scope.hiddenIcons) {
            bodyContentElement.addClass('hidden-icons-pf');
          }
        };

        var updateMobileMenu = function (selected, secondaryItem) {
          $scope.items.forEach(function (item) {
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
              $scope.showMobileSecondary = false;
              $scope.showMobileTertiary = true;
            } else {
              $scope.showMobileSecondary = true;
              $scope.showMobileTertiary = false;
            }
          } else {
            $scope.showMobileSecondary = false;
            $scope.showMobileTertiary = false;
          }
        };

        var checkNavState = function () {
          var width = $window.innerWidth;
          var bodyContentElement = getBodyContentElement();

          // Check to see if we need to enter/exit the mobile state
          if (!$scope.ignoreMobile && width < breakpoints.tablet) {
            if (!$scope.inMobileState) {
              $scope.inMobileState = true;

              //Set the body class to the correct state
              bodyContentElement.removeClass('collapsed-nav');
              bodyContentElement.addClass('hidden-nav');

              // Reset the collapsed states
              updateSecondaryCollapsedState(false);
              updateTertiaryCollapsedState(false);

              explicitCollapse = false;
            }
          } else  {
            $scope.inMobileState = false;
            $scope.showMobileNav = false;

            // Set the body class back to the default
            bodyContentElement.removeClass('hidden-nav');
          }

          if (explicitCollapse) {
            $scope.navCollapsed = true;
            bodyContentElement.addClass('collapsed-nav');
          } else {
            $scope.navCollapsed = false;
            bodyContentElement.removeClass('collapsed-nav');
          }
        };

        var collapseMenu = function () {
          var bodyContentElement = getBodyContentElement();
          $scope.navCollapsed = true;

          //Set the body class to the correct state
          bodyContentElement.addClass('collapsed-nav');

          explicitCollapse = true;
        };

        var expandMenu = function () {
          var bodyContentElement = getBodyContentElement();
          $scope.navCollapsed = false;

          //Set the body class to the correct state
          bodyContentElement.removeClass('collapsed-nav');

          explicitCollapse = false;

          // Dispatch a resize event when showing the expanding then menu to
          // allow content to adjust to the menu sizing
          angular.element($window).triggerHandler('resize');
        };

        var forceHideSecondaryMenu = function () {
          $scope.forceHidden = true;
          $timeout(function () {
            $scope.forceHidden = false;
          }, 500);
        };

        var setParentActive = function (item) {
          $scope.items.forEach(function (topLevel) {
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
          $scope.activeSecondary = false;

          if ($scope.persistentSecondary && !$scope.inMobileState) {
            $scope.items.forEach(function (topLevel) {
              if (topLevel.children) {
                topLevel.children.forEach(function (secondLevel) {
                  if (secondLevel.isActive) {
                    $scope.activeSecondary = true;
                  }
                });
              }
            });
            if ($scope.activeSecondary) {
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
            $scope.showMobileNav = false;
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
                location.path(navTo);
              }
            }
            if ($scope.navigateCallback) {
              $scope.navigateCallback(navItem);
            }
          }

          if ($scope.itemClickCallback) {
            $scope.itemClickCallback(item);
          }

          if ($scope.updateActiveItemsOnClick ) {
            $scope.clearActiveItems();
            navItem.isActive = true;
            setParentActive(navItem);
            setSecondaryItemVisible();
          }
          setSecondaryItemVisible();
        };

        var primaryHover = function () {
          var hover = false;
          $scope.items.forEach(function (item) {
            if (item.isHover) {
              hover = true;
            }
          });
          return hover;
        };

        var secondaryHover = function () {
          var hover = false;
          $scope.items.forEach(function (item) {
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
            $scope.collapsedSecondaryNav = true;

            bodyContentElement.addClass('collapsed-secondary-nav-pf');
          } else {
            // Remove any collapsed secondary menus
            if ($scope.items) {
              $scope.items.forEach(function (item) {
                item.secondaryCollasped = false;
              });
            }
            $scope.collapsedSecondaryNav = false;

            bodyContentElement.removeClass('collapsed-secondary-nav-pf');
          }
        };

        var updateTertiaryCollapsedState = function (setCollapsed, collapsedItem) {
          var bodyContentElement = getBodyContentElement();
          if (collapsedItem) {
            collapsedItem.tertiaryCollapsed = setCollapsed;
          }
          if (setCollapsed) {
            $scope.collapsedTertiaryNav = true;

            bodyContentElement.addClass('collapsed-tertiary-nav-pf');
            updateSecondaryCollapsedState(false);
          } else {
            // Remove any collapsed secondary menus
            if ($scope.items) {
              $scope.items.forEach(function (item) {
                if (item.children && item.children.length > 0) {
                  item.children.forEach(function (secondaryItem) {
                    secondaryItem.tertiaryCollasped = false;
                  });
                }
              });
            }
            $scope.collapsedTertiaryNav = false;

            bodyContentElement.removeClass('collapsed-tertiary-nav-pf');
          }
        };

        $scope.showMobileNav = false;
        $scope.showMobileSecondary = false;
        $scope.showMobileTertiary = false;
        $scope.hoverSecondaryNav = false;
        $scope.hoverTertiaryNav = false;
        $scope.collapsedSecondaryNav = false;
        $scope.collapsedTertiaryNav = false;
        $scope.navCollapsed = false;
        $scope.forceHidden = false;

        $scope.handleNavBarToggleClick = function () {

          if ($scope.inMobileState) {
            // Toggle the mobile nav
            if ($scope.showMobileNav) {
              $scope.showMobileNav = false;
            } else {
              // Always start at the primary menu
              updateMobileMenu();
              $scope.showMobileNav = true;
            }
          } else if ($scope.navCollapsed) {
            expandMenu();
          } else {
            collapseMenu();
          }
        };

        $scope.handlePrimaryClick = function (item, event) {
          if ($scope.inMobileState) {
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

        $scope.handleSecondaryClick = function (primary, secondary, event) {
          if ($scope.inMobileState) {
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

        $scope.handleTertiaryClick = function (primary, secondary, tertiary, event) {
          if ($scope.inMobileState) {
            updateMobileMenu();
          }

          navigateToItem(tertiary);
        };

        // Show secondary nav bar on hover of primary nav items
        $scope.handlePrimaryHover = function (item) {
          if (item.children && item.children.length > 0) {
            if (!$scope.inMobileState) {
              if (item.navUnHoverTimeout !== undefined) {
                $timeout.cancel(item.navUnHoverTimeout);
                item.navUnHoverTimeout = undefined;
              } else if ($scope.navHoverTimeout === undefined && !item.isHover) {
                item.navHoverTimeout = $timeout(function () {
                  $scope.hoverSecondaryNav = true;
                  item.isHover = true;
                  item.navHoverTimeout = undefined;
                }, hoverDelay);
              }
            }
          }
        };

        $scope.handlePrimaryUnHover = function (item) {
          if (item.children && item.children.length > 0) {
            if (item.navHoverTimeout !== undefined) {
              $timeout.cancel(item.navHoverTimeout);
              item.navHoverTimeout = undefined;
            } else if (item.navUnHoverTimeout === undefined && item.isHover) {
              item.navUnHoverTimeout = $timeout(function () {
                item.isHover = false;
                if (!primaryHover()) {
                  $scope.hoverSecondaryNav = false;
                }
                item.navUnHoverTimeout = undefined;
              }, hideDelay);
            }
          }
        };

        // Show tertiary nav bar on hover of secondary nav items
        $scope.handleSecondaryHover = function (item) {
          if (item.children && item.children.length > 0) {
            if (!$scope.inMobileState) {
              if (item.navUnHoverTimeout !== undefined) {
                $timeout.cancel(item.navUnHoverTimeout);
                item.navUnHoverTimeout = undefined;
              } else if ($scope.navHoverTimeout === undefined) {
                item.navHoverTimeout = $timeout(function () {
                  $scope.hoverTertiaryNav = true;
                  item.isHover = true;
                  item.navHoverTimeout = undefined;
                }, hoverDelay);
              }
            }
          }
        };

        $scope.handleSecondaryUnHover = function (item) {
          if (item.children && item.children.length > 0) {
            if (item.navHoverTimeout !== undefined) {
              $timeout.cancel(item.navHoverTimeout);
              item.navHoverTimeout = undefined;
            } else if (item.navUnHoverTimeout === undefined) {
              item.navUnHoverTimeout = $timeout(function () {
                item.isHover = false;
                if (!secondaryHover()) {
                  $scope.hoverTertiaryNav = false;
                }
                item.navUnHoverTimeout = undefined;
              }, hideDelay);
            }
          }
        };

        $scope.collapseSecondaryNav = function (item, event) {
          if ($scope.inMobileState) {
            updateMobileMenu();
          } else {
            if (item.secondaryCollapsed) {
              updateSecondaryCollapsedState(false, item);
              forceHideSecondaryMenu();
            } else {
              updateSecondaryCollapsedState(true, item);
            }
          }

          $scope.hoverSecondaryNav = false;
          event.stopImmediatePropagation();
        };

        $scope.collapseTertiaryNav = function (item, event) {
          if ($scope.inMobileState) {
            $scope.items.forEach(function (primaryItem) {
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

          $scope.hoverSecondaryNav = false;
          $scope.hoverTertiaryNav = false;
          event.stopImmediatePropagation();
        };

        initBodyElement();
        checkNavState();

        angular.element($window).bind('resize', function () {
          checkNavState();
          $timeout(function () {
            try {
              $scope.$apply();
            } catch (e) {
              // Ignore, if we already applied, that is fine.
            }
          });
        });
      }
    };
  }]);

