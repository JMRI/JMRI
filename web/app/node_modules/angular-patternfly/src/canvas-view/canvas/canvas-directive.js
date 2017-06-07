(function () {
  'use strict';

  angular.module('patternfly.canvas')
  .filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
      return $sce.trustAsResourceUrl(val);
    };
  }])

  //
  // Directive that generates the rendered chart from the data model.
  //
  .directive('pfCanvas', function ($document) {
    return {
      restrict: 'E',
      templateUrl: "canvas-view/canvas/canvas.html",
      replace: true,
      scope: {
        chartDataModel: "=",
        chartViewModel: "=?",
        readOnly: "=?",
        hideConnectors: "=?"
      },
      controller: 'CanvasController',
      link: link
    };
    function link (scope) {
      var deleteKeyCode = 46;
      var ctrlKeyCode = 17;
      var ctrlDown = false;
      var aKeyCode = 65;
      var dKeyCode = 68;
      var escKeyCode = 27;

      $document.find('body').keydown(function (evt) {
        if (evt.keyCode === ctrlKeyCode) {
          ctrlDown = true;
          evt.stopPropagation();
          evt.preventDefault();
        }

        if (evt.keyCode === aKeyCode && ctrlDown) {
          //
          // Ctrl + A
          //
          scope.selectAll();
          scope.$digest();
          evt.stopPropagation();
          evt.preventDefault();
        }
      });

      $document.find('body').keyup(function (evt) {
        if (evt.keyCode === deleteKeyCode) {
          scope.deleteSelected();
          scope.$digest();
        }

        if (evt.keyCode === escKeyCode) {
          scope.deselectAll();
          scope.$digest();
        }

        if (evt.keyCode === ctrlKeyCode) {
          ctrlDown = false;
          evt.stopPropagation();
          evt.preventDefault();
        }
      });
    }
  })
  //
  // Controller for the canvas directive.
  // Having a separate controller is better for unit testing, otherwise
  // it is painful to unit test a directive without instantiating the DOM
  // (which is possible, just not ideal).
  //
  .controller('CanvasController', ['$scope', 'dragging', '$element', '$document', function CanvasController ($scope, dragging, $element, $document) {
    var controller = this;

    $scope.chart = new pfCanvas.ChartViewModel($scope.chartDataModel);
    $scope.chartViewModel = $scope.chart;
    //
    // Reference to the document and jQuery, can be overridden for testting.
    //
    this.document = document;

    //
    // Wrap jQuery so it can easily be  mocked for testing.
    //
    this.jQuery = function (element) {
      return angular.element(element);
    };

    //
    // Init data-model variables.
    //
    $scope.draggingConnection = false;
    $scope.connectorSize = 6;
    $scope.dragSelecting = false;

    //
    // Reference to the connection, connector or node that the mouse is currently over.
    //
    $scope.mouseOverConnector = null;
    $scope.mouseOverConnection = null;
    $scope.mouseOverNode = null;

    //
    // The class for connections and connectors.
    //
    this.connectionClass = 'connection';
    this.connectorClass = 'connector';
    this.nodeClass = 'node';

    //
    // Translate the coordinates so they are relative to the svg element.
    //
    this.translateCoordinates = function (x, y, evt) {
      var svgElem =  $element.get(0);
      var matrix = svgElem.getScreenCTM();
      var point = svgElem.createSVGPoint();
      point.x = (x - evt.view.pageXOffset) / $scope.zoomLevel();
      point.y = (y - evt.view.pageYOffset) / $scope.zoomLevel();

      return point.matrixTransform(matrix.inverse());
    };

    $scope.hideConnectors = $scope.hideConnectors ? $scope.hideConnectors : false;

    $scope.isConnectorConnected = function (connector) {
      return (connector && connector.connected());
    };

    $scope.isConnectorUnconnectedAndValid = function (connector) {
      return (connector && !connector.connected() && !connector.invalid() &&
              connector.parentNode() !== $scope.connectingModeSourceNode);
    };

    // determins if a dest. connector is connected to the source node
    $scope.isConnectedTo = function (connector, node) {
      var i,connection;
      var connections = $scope.chart.connections;
      for (i = 0; i < connections.length; i++) {
        connection = connections[i];
        if (connection.dest === connector && connection.source.parentNode() === node) {
          return true;
        }
      }

      return false;
    };

    $scope.availableConnections = function () {
      return $scope.chart.validConnections;
    };

    $scope.foreignObjectSupported = function () {
      return $document[0].implementation.hasFeature('http://www.w3.org/TR/SVG11/feature#Extensibility', '1.1');
    };

    $scope.addNodeToCanvas = function (newNode) {
      $scope.chart.addNode(newNode);
    };

    $scope.$on('selectAll', function (evt, args) {
      $scope.selectAll();
    });

    $scope.selectAll = function () {
      $scope.chart.selectAll();
    };

    $scope.$on('deselectAll', function (evt, args) {
      $scope.deselectAll();
    });

    $scope.deselectAll = function () {
      $scope.chart.deselectAll();
    };

    $scope.$on('deleteSelected', function (evt, args) {
      $scope.deleteSelected();
    });

    $scope.deleteSelected = function () {
      $scope.chart.deleteSelected();
    };

    //
    // Called on mouse down in the chart.
    //
    $scope.mouseDown = function (evt) {
      if ($scope.readOnly) {
        return;
      }

      if ($scope.chart.inConnectingMode ) {
        // camceling out of connection mode, remove unused output connector
        $scope.cancelConnectingMode();
      }

      $scope.chart.deselectAll();

      $scope.chart.clickedOnChart = true;

      dragging.startDrag(evt, {

        //
        // Commence dragging... setup variables to display the drag selection rect.
        //
        dragStarted: function (x, y) {
          var startPoint;
          $scope.dragSelecting = true;
          startPoint = controller.translateCoordinates(x, y, evt);
          $scope.dragSelectionStartPoint = startPoint;
          $scope.dragSelectionRect = {
            x: startPoint.x,
            y: startPoint.y,
            width: 0,
            height: 0,
          };
        },

        //
        // Update the drag selection rect while dragging continues.
        //
        dragging: function (x, y) {
          var startPoint = $scope.dragSelectionStartPoint;
          var curPoint = controller.translateCoordinates(x, y, evt);

          $scope.dragSelectionRect = {
            x: curPoint.x > startPoint.x ? startPoint.x : curPoint.x,
            y: curPoint.y > startPoint.y ? startPoint.y : curPoint.y,
            width: curPoint.x > startPoint.x ? curPoint.x - startPoint.x : startPoint.x - curPoint.x,
            height: curPoint.y > startPoint.y ? curPoint.y - startPoint.y : startPoint.y - curPoint.y,
          };
        },

        //
        // Dragging has ended... select all that are within the drag selection rect.
        //
        dragEnded: function () {
          $scope.dragSelecting = false;
          $scope.chart.applySelectionRect($scope.dragSelectionRect);
          delete $scope.dragSelectionStartPoint;
          delete $scope.dragSelectionRect;
        },
      });
    };

    //
    // Handle nodeMouseOver on an node.
    //
    $scope.nodeMouseOver = function (evt, node) {
      if (!$scope.readOnly) {
        $scope.mouseOverNode = node;
      }
    };

    //
    // Handle nodeMouseLeave on an node.
    //
    $scope.nodeMouseLeave = function (evt, node) {
      $scope.mouseOverNode = null;
    };

    //
    // Handle mousedown on a node.
    //
    $scope.nodeMouseDown = function (evt, node) {
      var chart = $scope.chart;
      var lastMouseCoords;

      if ($scope.readOnly) {
        return;
      }

      dragging.startDrag(evt, {

        //
        // Node dragging has commenced.
        //
        dragStarted: function (x, y) {
          lastMouseCoords = controller.translateCoordinates(x, y, evt);

          //
          // If nothing is selected when dragging starts,
          // at least select the node we are dragging.
          //
          if (!node.selected()) {
            chart.deselectAll();
            node.select();
          }
        },

        //
        // Dragging selected nodes... update their x,y coordinates.
        //
        dragging: function (x, y) {
          var curCoords = controller.translateCoordinates(x, y, evt);
          var deltaX = curCoords.x - lastMouseCoords.x;
          var deltaY = curCoords.y - lastMouseCoords.y;

          chart.updateSelectedNodesLocation(deltaX, deltaY);

          lastMouseCoords = curCoords;
        },

        //
        // The node wasn't dragged... it was clicked.
        //
        clicked: function () {
          chart.handleNodeClicked(node, evt.ctrlKey);
        },

      });
    };

    //
    // Listen for node action
    //
    $scope.$on('nodeActionClicked', function (evt, args) {
      var action = args.action;
      var node = args.node;

      if (action === 'nodeActionConnect') {
        $scope.startConnectingMode(node);
      }
    });

    $scope.$on('nodeActionClosed', function () {
      $scope.mouseOverNode = null;
    });

    $scope.connectingModeOutputConnector = null;
    $scope.connectingModeSourceNode = null;

    $scope.startConnectingMode = function (node) {
      $scope.chart.inConnectingMode = true;
      $scope.hideConnectors = false;
      $scope.connectingModeSourceNode = node;
      $scope.connectingModeSourceNode.select();
      $scope.connectingModeOutputConnector = node.getOutputConnector();
      $scope.chart.updateValidNodesAndConnectors($scope.connectingModeSourceNode);
    };

    $scope.cancelConnectingMode = function () {
      // if output connector not connected to something, remove it
      if (!$scope.connectingModeOutputConnector.connected()) {
        $scope.chart.removeOutputConnector($scope.connectingModeOutputConnector);
      }
      $scope.stopConnectingMode();
    };

    $scope.stopConnectingMode = function () {
      $scope.chart.inConnectingMode = false;
      $scope.chart.resetValidNodesAndConnectors();
    };

    //
    // Handle connectionMouseOver on an connection.
    //
    $scope.connectionMouseOver = function (evt, connection) {
      if (!$scope.draggingConnection && !$scope.readOnly) {  // Only allow 'connection mouse over' when not dragging out a connection.
        $scope.mouseOverConnection = connection;
      }
    };

    //
    // Handle connectionMouseLeave on an connection.
    //
    $scope.connectionMouseLeave = function (evt, connection) {
      $scope.mouseOverConnection = null;
    };

    //
    // Handle mousedown on a connection.
    //
    $scope.connectionMouseDown = function (evt, connection) {
      var chart = $scope.chart;
      if (!$scope.readOnly) {
        chart.handleConnectionMouseDown(connection, evt.ctrlKey);
      }
      // Don't let the chart handle the mouse down.
      evt.stopPropagation();
      evt.preventDefault();
    };

    //
    // Handle connectorMouseOver on an connector.
    //
    $scope.connectorMouseOver = function (evt, node, connector, connectorIndex, isInputConnector) {
      if (!$scope.readOnly) {
        $scope.mouseOverConnector = connector;
      }
    };

    //
    // Handle connectorMouseLeave on an connector.
    //
    $scope.connectorMouseLeave = function (evt, node, connector, connectorIndex, isInputConnector) {
      $scope.mouseOverConnector = null;
    };

    //
    // Handle mousedown on an input connector.
    //
    $scope.connectorMouseDown = function (evt, node, connector, connectorIndex, isInputConnector) {
      if ($scope.chart.inConnectingMode && node !== $scope.connectingModeSourceNode) {
        $scope.chart.createNewConnection($scope.connectingModeOutputConnector, $scope.mouseOverConnector);
        $scope.stopConnectingMode();
      }
    };

    //
    // zoom.
    //
    $scope.$on('zoomIn', function (evt, args) {
      $scope.chart.zoom.in();
    });

    $scope.$on('zoomOut', function (evt, args) {
      $scope.chart.zoom.out();
    });

    $scope.maxZoom = function () {
      return ($scope.chart.chartViewModel && $scope.chart.chartViewModel.zoom) ? $scope.chart.chartViewModel.zoom.isMax() : false;
    };
    $scope.minZoom = function () {
      return ($scope.chart.chartViewModel && $scope.chart.chartViewModel.zoom) ? $scope.chart.chartViewModel.zoom.isMin() : false;
    };

    $scope.zoomLevel = function () {
      return $scope.chart.zoom.getLevel();
    };
  }
  ]);
})();
