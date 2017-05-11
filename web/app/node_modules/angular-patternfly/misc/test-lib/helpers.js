// jasmine matcher for expecting an element to have a css class
// https://github.com/angular/angular.js/blob/master/test/matchers.js
beforeEach(function() {
  jasmine.addMatchers({
    toHaveClass: function() {
      return {
        compare: function(actual, expected) {
          var message = 'Expected "' + actual + '"' + (this.isNot ? ' not ' : ' ') + 'to have class "' + expected + '".';

          return {
            pass: actual.hasClass(expected),
            message: message
          }
        }
      };
    },

    toBeHidden: function() {
      return {
        compare: function(actual) {
          var element = angular.element(actual);

          var message = 'Expected "' + actual + '"' + (this.isNot ? ' not ' : ' ') + 'to be hidden.'

          return {
            pass: element.hasClass('ng-hide') || element.css('display') == 'none',
            message: message
          }
        }
      };
    },

    toEqualSelect: function() {
      return {
        compare: function (actual, expected) {
          var actualValues = [],
            expectedValues = [].slice.call(expected);

          angular.forEach(actual.find('option'), function(option){
            actualValues.push(option.selected ? [option.text] : option.text);
          });

          var message = 'Expected ' + angular.toJson(actualValues) + ' to equal ' + angular.toJson(expectedValues) + '.';

          return {
            pass: angular.equals(expectedValues, actualValues),
            message: message
          };

        }
      }
    }
  })
});

