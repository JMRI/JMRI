/**
 * @ngdoc directive
 * @name patternfly.form.directive:pfFormGroup
 * @restrict E
 *
 * @description
 *  Encapsulates the structure and styling for a label + input used within a
 *  Bootstrap3 based form.
 *
 *  This directive creates new scope.
 *
 * @param {string} pfLabel the text for the <label> element.
 * @param {string} pfFieldId the id of the form field. Default value is id of the form field element.
 * @param {string} pfLabelClass the class of the label element. Default value is "col-sm-2".
 * @param {string} pfInputClass the class of the input element. Default value is "col-sm-5".
 *
 * @example
 <example module="patternfly.form">

   <file name="index.html">
     <div ng-controller="FormDemoCtrl">
       <p>Name: {{ item.name }}</p>
       <p>Description: {{ item.description }}</p>
       <form>
         <pf-form-group pf-label="Name" required>
           <input id="name" name="name" ng-model="item.name" type="text" required/>
         </pf-form-group>
         <pf-form-group pf-label="Description">
           <textarea id="description" name="description" ng-model="item.description">
             {{ item.description }}
           </textarea>
         </pf-form-group>
       </form>
       <p>Horizontal Form</p>
       <form class="form-horizontal">
         <pf-form-group pf-label="Name" required pf-label-class="col-sm-2" pf-input-class="col-sm-5">
           <input id="name" name="name" ng-model="item.name" type="text" required/>
         </pf-form-group>
         <pf-form-group pf-label="Description" pf-label-class="col-sm-2" pf-input-class="col-sm-5">
           <textarea id="description" name="description" ng-model="item.description">
             {{ item.description }}
           </textarea>
         </pf-form-group>
       </form>
     </div>
   </file>

   <file name="script.js">
     angular.module( 'patternfly.form' ).controller( 'FormDemoCtrl', function( $scope ) {
       $scope.item = {
         name: 'Homer Simpson',
         description: 'I like donuts and Duff.  Doh!'
       };
     });
   </file>
 </example>
*/
