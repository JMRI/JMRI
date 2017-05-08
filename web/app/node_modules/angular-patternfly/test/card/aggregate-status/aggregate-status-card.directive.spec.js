describe('Directive: pfAggregateStatusCard', function() {
  var $scope, $compile, element, cardClass, notifications;

  beforeEach(module('patternfly.card', 'card/aggregate-status/aggregate-status-card.html'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
  }));

  describe('Page with pf-aggregate-status-card directive', function () {

    var compileCard = function (markup, scope) {
      var el = $compile(markup)(scope);
      scope.$digest();
      return el;
    };

    it("should set the title link, count, and icons class", function() {

      $scope.status = {
        "title":"Nodes",
        "count":793,
        "href":"#",
        "iconClass": "fa fa-shield",
      };

      element = compileCard('<div pf-aggregate-status-card status="status"></div>', $scope);

      //Make sure the count is getting set properly in the title
      expect(angular.element(element).find('.card-pf-aggregate-status-count').html()).toBe("793");

      //Make sure a link renders in the title
      expect(angular.element(element).find('.card-pf-title').find('a').size()).toBe(1);

      //Make sure the class is getting set for the title icon
      expect(angular.element(element).find('.card-pf-title').find('.fa').hasClass('fa-shield')).toBeTruthy();

      // By default, showTopBorder if not defined, should be false, resulting in hiding the top
      // border, ie. having a .card-pf class
      cardClass = angular.element(element).find('.card-pf').hasClass('card-pf-accented');
      expect(cardClass).toBeFalsy();
    });

    it("No link should be present in the title", function() {

      $scope.status = {
        "title":"Nodes",
        "count":793,
        "iconClass": "fa fa-shield"
      };

      element = compileCard('<div pf-aggregate-status-card status="status"></div>', $scope);

      //Make sure a link renders in the title
      expect(angular.element(element).find('.card-pf-title').find('a').size()).toBe(0);
    });

    it("should set the notifications", function() {

      $scope.status = {
        "title":"Nodes",
        "count":793,
        "href":"#",
        "iconClass": "fa fa-shield",
        "notifications":[
         {
           "iconClass":"pficon pficon-error-circle-o",
           "count":4,
           "href":"#"
         },
         {
           "iconClass":"pficon pficon-warning-triangle-o",
           "count":1
         }
       ]
      };

      element = compileCard('<div pf-aggregate-status-card status="status"></div>', $scope);

      notifications = angular.element(element).find('.card-pf-aggregate-status-notification');

      //Make sure two notifications render
      expect(notifications.size()).toBe(2);

      //First notification should have a link
      expect(notifications.eq(0).find('a').size()).toBe(1);

      //Second notification should not have a link
      expect(notifications.eq(1).find('a').size()).toBe(0);

      //first notification should have the following class
      expect(notifications.eq(0).find('span')).toHaveClass('pficon pficon-error-circle-o');

      //second notification should have the following class
      expect(notifications.eq(1).find('span')).toHaveClass('pficon pficon-warning-triangle-o');
    });

    it("should show the top border", function() {
      element = compileCard('<div pf-aggregate-status-card show-top-border="true"></div>', $scope);

      // showTopBorder set to true, results in having the .card-pf-accented class
      cardClass = angular.element(element).find('.card-pf').hasClass('card-pf-accented');
      expect(cardClass).toBeTruthy();

    });

    it("should hide the top border", function() {
      element = compileCard('<div pf-aggregate-status-card show-top-border="false"></div>', $scope);

      // showTopBorder set to false, results in not having the .card-pf-accented class
      cardClass = angular.element(element).find('.card-pf').hasClass('card-pf-accented');
      expect(cardClass).toBeFalsy();
    });

    it("should show mini layout", function() {

      $scope.status = {
        "title":"Nodes",
        "count":793,
        "href":"#",
        "iconClass": "fa fa-shield",
        "notification": {
          "iconClass": "pficon pficon-error-circle-o",
          "count": 4,
          "href": "#"
        }
      };

      element = compileCard('<div pf-aggregate-status-card status="status" layout="mini"></div>', $scope);

      // should have the mini layout class
      cardClass = angular.element(element).find('.card-pf').hasClass('card-pf-aggregate-status-mini');
      expect(cardClass).toBeTruthy();

      // should show the main icon
      cardClass = angular.element(element).find('.fa-shield');
      expect(cardClass.size()).toBe(1);

      notifications = angular.element(element).find('.card-pf-aggregate-status-notification');

      //notification should have an icon
      expect(notifications.eq(0).find('span').hasClass('pficon-error-circle-o')).toBeTruthy();

      //notification should have a count
      expect(notifications.eq(0).find('span').eq(1).html()).toBe('4');

    });

    it("should show mini layout, and hide optional items", function() {

      $scope.status = {
        "title":"Nodes",
        "count":793,
        "notification":
          {
            "count":6
          }
      };

      element = compileCard('<div pf-aggregate-status-card status="status" layout="mini"></div>', $scope);

      // should have the mini layout class
      cardClass = angular.element(element).find('.card-pf').hasClass('card-pf-aggregate-status-mini');
      expect(cardClass).toBeTruthy();

      // should not show the main icon
      cardClass = angular.element(element).find('.fa-shield');
      expect(cardClass.size()).toBe(0);

      notifications = angular.element(element).find('.card-pf-aggregate-status-notification');

      //notification should not have an icon
      expect(notifications.eq(0).find('span').hasClass('pficon-error-circle-o')).toBeFalsy();

      //notification should have a count
      expect(notifications.eq(0).find('span').eq(1).html()).toBe('6');


      $scope.status = {
        "title":"Nodes",
        "count":793,
        "notification":
          {
            "iconClass":"pficon pficon-error-circle-o"
          }
      };

      element = compileCard('<div pf-aggregate-status-card status="status" layout="mini"></div>', $scope);

      notifications = angular.element(element).find('.card-pf-aggregate-status-notification');

      //notification should have an icon
      expect(notifications.eq(0).find('span').hasClass('pficon-error-circle-o')).toBeTruthy();

      //notification should not have a count
      expect(notifications.eq(0).find('span').eq(1).html()).not.toBe('6');
    });

    it("should set of the iconImage value", function() {

      $scope.aggStatusAlt = {
        "title":"Providers",
        "count":3,
        "notifications":[
          {
            "iconImage":"img/kubernetes.svg",
            "count":1,
            "href":"#"
          },
          {
            "iconImage":"img/OpenShift-logo.svg",
            "count":2
          }
        ]
      };

      element = compileCard('<div pf-aggregate-status-card status="aggStatusAlt" layout="tall"></div>', $scope);

      // should have the images
      imageElements = angular.element(element).find('.card-pf-icon-image');
      expect(imageElements.length).toBe(2);
      expect(angular.element(imageElements[0]).attr('src')).toBe('img/kubernetes.svg');
      expect(angular.element(imageElements[1]).attr('src')).toBe('img/OpenShift-logo.svg');
    });
  });

});
