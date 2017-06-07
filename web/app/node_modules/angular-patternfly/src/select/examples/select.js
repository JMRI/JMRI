/**
 * @ngdoc directive
 * @name patternfly.select.component:pfSelect
 * @restrict E
 *
 * @param {object} selected Curently selected value
 * @param {object} options Array of valid selections
 * @param {string} displayField Field from the object in the array to display for selection (optional)
 * @param {string} emptyValue value to display when nothing is selected
 * @param {function(item)} onSelect Function to call upon user selection of an item.
 *
 * @description
 * The pfSelect component provides a wrapper for the angular ui bootstrap dropdown container allowing for use of ng-model and ng-options
 *
 * @example
 <example module="patternfly.select">
 <file name="index.html">
   <div ng-controller="SelectDemoCtrl">
     <form class="form-horizontal">
       <div class="form-group">
         <label class="col-sm-2 control-label">Preferred pet:</label>
         <div class="col-sm-10">
           <pf-select selected="pet" empty-value="{{noPet}}" options="pets"></pf-select>
         </div>
       </div>
       <div class="form-group">
         <label class="col-sm-2 control-label">Preferred fruit:</label>
         <div class="col-sm-10">
           <pf-select selected="fruit" options="fruits" display-field="title"></pf-select>
         </div>
       </div>
       <div class="form-group">
         <label class="col-sm-2 control-label">Preferred drink:</label>
         <div class="col-sm-10">
           <pf-select selected="drink" empty-value="{{noDrink}}" options="drinks" display-field="name"></pf-select>
         </div>
       </div>
     </form>
     <p>Your preferred pet is {{pet || noPet}}.</p>
     <p>Your preferred drink is {{fruit.name}}.</p>
     <p>Your preferred drink is {{drink ? drink.name : noDrink}}.</p>
   </div>
   </file>
 <file name="script.js">
   angular.module( 'patternfly.select' ).controller( 'SelectDemoCtrl', function( $scope ) {
         $scope.pets = ['Dog', 'Cat', 'Chicken'];
         $scope.noPet = "No pet selected";

         $scope.fruits = [
           { id: 1, name:'orange', title: 'Oranges - fresh from Florida'},
           { id: 2, name:'apple', title: 'Apples - Macintosh, great for pies.'},
           { id: 3, name:'banana', title: 'Bananas - you will go ape for them!' }
         ];
         $scope.fruit = $scope.fruits[0];

         $scope.drinks = [
           { id: 1, name:'tea'},
           { id: 2, name:'coffee'},
           { id: 3, name:'water'},
           { id: 4, name:'wine'},
           { id: 5, name:'beer'}
         ];
         $scope.drink = $scope.drinks[0];
         $scope.noDrink = "No drink selected";
       });
   </file>
 </example>
 */
