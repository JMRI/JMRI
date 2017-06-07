/**
 * @ngdoc directive
 * @name patternfly.canvas.directive:pfCanvasEditor
 * @restrict E
 *
 * @description
 * Component for canvas editor which adds a toolbox where items can be dragged and dropped onto canvas, as well as other canvas
 * operations such as: Zoom In, Zoom Out, Hide Connections, Remove Node, and Duplicate Node.  Does not work in IE 11 or lower because they do not support
 * latest svg specification's 'foreignObject' api.  Tested in FireFox, Chrome, and MS-Edge.
 *
 * @param {object} chartDataModel Chart data object which defines the nodes and connections on the canvas. See {@link patternfly.canvas.directive:pfCanvas} for detailed information.
 * @param {object} chartViewModel (Optional) The chartViewModel is initialized from the chartDataModel and contains additional helper methods such as <code>chartViewModel.isOnlyOneNodeSelected()</code> and
 * <code>chartViewModel.getSelectedNodes()</code>.
 * @param {boolean} toolboxTabs An array of Tab objects used in the Toolbox.  Each Tab object many contain 'subtabs' and/or 'items'.  Items may be dragged onto the canvas.
 * <ul style='list-style-type: none'>
 *   <li>.preTitle - (string) (Optional) A small title above the main tab title
 *   <li>.title    - (string) The main title of the tab
 *   <li>.subtabs  - (Array) An array of sub Tab objects. Supports up to three levels of nested sub tabs
 *   <li>.items    - (Array) An array of items which can be dragged and dropped onto the canvas
 *   <ul style='list-style-type: none'>
 *     <li>.name     - (string) The item name/title
 *     <li>.id       - (number) The item id
 *     <li>.image    - (string) (Optional) The url of the item's image.  Ex: "/img/kubernetes.svg"
 *     <li>.icon     - (string) (Optional) The icon class of the item's icon.  Ex: "pf pficon-service"
 *   </ul>
 * </ul>
 * @param {boolean} readOnly (Optional) A flag indicating whether the canvas is in 'read-only' mode.  When in 'read-only' mode nodes cannot be moved, selected, or deleted, and the node action toolbar is hidden.
 * @example
 <example module="patternfly.canvaseditor.demo">
 <file name="index.html">
   <style>
     .canvas {
         background-image: url('/img/canvas-dot-grid.png');
         background-repeat: repeat;
     }
   </style>
   <div ng-controller="CanvasEditorDemoCtrl" class="example-container">
     <pf-canvas-editor chart-data-model="chartDataModel"
                       chart-view-model="chartViewModel"
                       toolbox-tabs="toolboxTabs"
                       read-only="readOnly">
       <span ng-if="!readOnly" class="more-actions">
         <a id="duplicateItem" ng-click="duplicateNode()" ng-class="{'disabled': !chartViewModel.isOnlyOneNodeSelected() || chartViewModel.inConnectingMode}">
           <span class="pficon fa fa-copy"
                 tooltip-append-to-body="true" tooltip-placement="bottom"
                 uib-tooltip="{{'Duplicate Item'}}">
           </span>
         </a>
         <a id="deleteNodes" ng-click="deleteNodes()" ng-class="{'disabled': !chartViewModel.areAnyNodesSelected() || chartViewModel.inConnectingMode}">
           <span class="pficon pficon-delete"
                 tooltip-append-to-body="true" tooltip-placement="bottom"
                 uib-tooltip="{{'Delete Selected Items'}}">
           </span>
         </a>
       </span>
     </pf-canvas-editor>
     <hr>
     <div class="form-group">
       <label class="checkbox-inline">
         <input type="checkbox" ng-model="readOnly">Read Only</input>
       </label>
     </div>
     <div style="padding-top: 12px;">
       <label style="font-weight:normal;vertical-align:middle;">Events: </label>
     </div>
     <div>
       <textarea rows="10" class="col-md-12">{{eventText}}</textarea>
     </div>
   </div>
 </file>

 <file name="modules.js">
   angular.module('patternfly.canvaseditor.demo', ['patternfly.canvas']);
 </file>

 <file name="script.js">
 angular.module( 'patternfly.canvaseditor.demo' ).controller( 'CanvasEditorDemoCtrl', function( $scope, $filter ) {
     $scope.chartDataModel = {
          "nodes": [
            {
              "name": "Nuage",
              "x": 345,
              "y": 67,
              "id": 1,
              "image": "/img/OpenShift-logo.svg",
              "width": 150,
              "bundle": true,
              "backgroundColor": "#fff",
              "inputConnectors": [
                {
                  "name": "Network",
                  "type": "network",
                  "fontFamily": "PatternFlyIcons-webfont",
                  "fontContent": "\ue909"
                },
                {
                  "name": "Container",
                  "type": "container",
                  "fontFamily": "PatternFlyIcons-webfont",
                  "fontContent": "\ue621"
                }
              ],
              "validConnectionTypes": ["network", "container"]
            },
            {
              "name": "Vmware",
              "x": 100,
              "y": 290,
              "id": 2,
              "image": "/img/kubernetes.svg",
              "width": 150,
              "backgroundColor": "#fff",
              "validConnectionTypes": ["storage"],
              "inputConnectors": [
                {
                    "name": "Network",
                    "type": "network",
                    "fontFamily": "PatternFlyIcons-webfont",
                    "fontContent": "\ue909"
                  },
                  {
                    "name": "Storage",
                    "type": "storage",
                    "fontFamily": "PatternFlyIcons-webfont",
                    "fontContent": "\ue90e"
                  },
                  {
                    "name": "Container",
                    "type": "container",
                    "fontFamily": "PatternFlyIcons-webfont",
                    "fontContent": "\ue621"
                  }
                ],
            },
            {
              "name": "NetApp",
              "x": 350,
              "y": 291,
              "id": 3,
              "width": 150,
              "icon": "pf pficon-service",
              "fontSize": "76px",
              "backgroundColor": "#fff",
              "inputConnectors": [
                {
                    "name": "Network",
                    "type": "network",
                    "fontFamily": "PatternFlyIcons-webfont",
                    "fontContent": "\ue909"
                  },
                  {
                    "name": "Container",
                    "type": "container",
                    "fontFamily": "PatternFlyIcons-webfont",
                    "fontContent": "\ue621"
                  }
                ],
              "validConnectionTypes": ["network"]
            },
            {
              "name": "OpenShift",
              "x": 105,
              "y": 67,
              "id": 4,
              "width": 150,
              "fontFamily": "fontawesome",
              "fontContent": "\uf0c2",
              "backgroundColor": "#fff",
              "inputConnectors": [
                {
                  "name": "Storage",
                  "type": "storage",
                  "fontFamily": "PatternFlyIcons-webfont",
                  "fontContent": "\ue90e"
                },
                {
                  "name": "Container",
                  "type": "container",
                  "fontFamily": "PatternFlyIcons-webfont",
                  "fontContent": "\ue621"
                }
              ],
              "validConnectionTypes": ["network", "container", "storage"]
            }
          ],
          "nodeActions" : [
            {
              "id": 1,
              "name": "connect",
              "iconClass": "fa fa-share-alt",
              "action": "nodeActionConnect",
            },
            {
              "id": 2,
              "name": "edit",
              "iconClass": "pf pficon-edit",
              "action": "nodeActionEdit",
            },
            {
              "id": 3,
              "name": "tag",
              "iconClass": "fa fa-tag",
              "action": "nodeActionTag",
            },
          ],
          "connections": [
            {
              "source": {
                "nodeID": 4,
                "connectorIndex": 0
              },
              "dest": {
                "nodeID": 1,
                "connectorIndex": 1
              }
            },
            {
              "source": {
                "nodeID": 4,
                "connectorIndex": 0
              },
              "dest": {
                "nodeID": 3,
                "connectorIndex": 0
              }
            }
          ]
     };

     $scope.toolboxTabs = [
       {
         "preTitle": "Toolbox",
         "title": "Items A",
         "active": true,
         "items": [
           {
             "name": "Nuage",
             "id": 10000000000004,
             "image": "/img/OpenShift-logo.svg"
           },
           {
             "name": "Vmware",
             "id": 10000000000010,
             "image": "/img/kubernetes.svg"
           }
         ]
       },
       {
         "preTitle": "Toolbox",
         "title": "Items B",
         "active": true,
         "items": [
           {
             "name": "NetApp",
             "id": 10000000000014,
             "icon": "pf pficon-service"
           },
           {
             "name": "OpenShift",
             "id": 10000000000021,
             "icon": "fa fa-cloud"
           },
           {
             "name": "OpenStack",
             "id": 10000000000022,
             "icon": "pf pficon-network"
           },
           {
             "name": "Storage",
             "id": 10000000000026,
             "icon": "pf pficon-storage-domain"
           },
           {
             "name": "VM",
             "id": 10000000000023,
             "icon": "pf pficon-virtual-machine"
           },
           {
             "name": "Replicatore",
             "id": 10000000000027,
             "icon": "pf pficon-replicator"
           }
         ]
       }
     ];

     $scope.chartViewModel;
     $scope.readOnly = false;
     $scope.eventText = "";

     $scope.$on('nodeActionClicked', function(evt, args) {
       var action = args.action;
       var node = args.node;
       $scope.eventText = node.name() + ' ' + action + '\r\n' + $scope.eventText;
     });

     $scope.deleteNodes = function() {
       if ($scope.chartViewModel.inConnectingMode) {
         return;
       }

       $scope.chartViewModel.deleteSelected();

       angular.element("#deleteNodes").blur();
     };

     $scope.duplicateNode = function() {
       if ($scope.chartViewModel.inConnectingMode) {
         return;
       }

       var duplicatedNode = $scope.chartViewModel.duplicateSelectedNode();

       // Note: node id will be used in connections to/from this duplicated node
       // If id changes, connections array/obj will need to be updated as well
       duplicatedNode.id = Math.floor((Math.random() * 600) + 1);  // random number between 1 and 600
       duplicatedNode.name = getCopyName(duplicatedNode.name);
       duplicatedNode.backgroundColor = '#fff';

       duplicatedNode.x = duplicatedNode.x + 15 * $scope.chartDataModel.nodes.length;
       duplicatedNode.y = duplicatedNode.y + 15 * $scope.chartDataModel.nodes.length;

       $scope.chartViewModel.addNode(duplicatedNode);

       angular.element("#duplicateItem").blur();
     }

     function getCopyName(baseName) {
       // Test to see if we are duplicating an existing 'Copy'
       var baseNameLength = baseName.indexOf(' Copy');
       if (baseNameLength === -1) {
         baseNameLength = baseName.length;
       }
       baseName = baseName.substr(0, baseNameLength);
       var filteredArray = $filter('filter')($scope.chartDataModel.nodes, {name: baseName}, false);
       var copyName = baseName + " Copy" + ((filteredArray.length === 1) ? "" : " " + filteredArray.length);

       return copyName;
    }
 });
 </file>
 </example>
 */
