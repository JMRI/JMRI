
describe('Component: pfTopology', function() {
  var $scope, $compile, $timeout;

  beforeEach(module(
    'patternfly.charts'
  ));

  beforeEach(inject(function(_$compile_, _$rootScope_, _$timeout_) {
    $compile = _$compile_;
    $scope = _$rootScope_;
    $timeout = _$timeout_;
  }));

  var compileTopology = function (markup, scope) {
    var el = $compile(markup)(scope);
    scope.$digest();
    return angular.element(el);
  };

  beforeEach(function() {
    var index = 0;
    var datasets = [];

    function sink(dataset) {
      datasets.push(dataset);
    }

    sink({
      "items": {
        "ContainerManager10r20": {
          "name": "ocp-master.example.com",
          "kind": "ContainerManager",
          "miq_id": 10000000000020,
          "status": "Valid",
          "display_kind": "OpenshiftEnterprise"
        },
        "ContainerNode10r14": {
          "name": "ocp-master.example.com",
          "kind": "ContainerNode",
          "miq_id": 10000000000014,
          "status": "Ready",
          "display_kind": "Node"
        },
        "ContainerGroup10r240": {
          "name": "docker-registry-2-vrguw",
          "kind": "ContainerGroup",
          "miq_id": 10000000000240,
          "status": "Running",
          "display_kind": "Pod"
        },
        "Container10r235": {
          "name": "registry",
          "kind": "Container",
          "miq_id": 10000000000235,
          "status": "Running",
          "display_kind": "Container"
        },
        "ContainerReplicator10r56": {
          "name": "docker-registry-2",
          "kind": "ContainerReplicator",
          "miq_id": 10000000000056,
          "status": "OK",
          "display_kind": "Replicator"
        },
        "ContainerService10r61": {
          "name": "docker-registry",
          "kind": "ContainerService",
          "miq_id": 10000000000061,
          "status": "Unknown",
          "display_kind": "Service"
        },
      },
      "relations": [
        {
          "source": "ContainerManager10r20",
          "target": "ContainerNode10r14"
        }, {
          "source": "ContainerNode10r14",
          "target": "ContainerGroup10r240"
        }, {
          "source": "ContainerGroup10r240",
          "target": "Container10r235"
        }, {
          "source": "ContainerGroup10r240",
          "target": "ContainerReplicator10r56"
        }, {
          "source": "ContainerGroup10r240",
          "target": "ContainerService10r61"
        }, {
          "source": "ContainerNode10r14",
          "target": "ContainerGroup10r241"
        }
      ],
      "icons": {
        "AvailabilityZone": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "ContainerReplicator": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "ContainerGroup": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "FontAwesome"
        },
        "ContainerNode": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "ContainerService": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "ContainerRoute": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "Container": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "FontAwesome"
        },
        "Host": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "Vm": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        },
        "ContainerManager": {
          "type": "glyph",
          "icon": "",
          "fontfamily": "PatternFlyIcons-webfont"
        }
      }
    });

    $scope.data = datasets[index];

    var nodeKinds = {
      "ContainerReplicator": true,
      "ContainerGroup": true,
      "Container": true,
      "ContainerNode": true,
      "ContainerService": true,
      "Host": true,
      "Vm": true,
      "ContainerRoute": true,
      "ContainerManager": true
    };

    $scope.kinds = nodeKinds;

    var icons = $scope.data.icons;
    $scope.nodes = {};
    for(var kind in nodeKinds) {
      var icon = icons[kind];
      $scope.nodes[kind] = {
        "name": kind,
        "enabled": nodeKinds[kind],
        "radius": 16,
        "textX": 0,
        "textY": 5,
        "height": 18,
        "width": 18,
        "icon": icon.icon,
        "fontFamily": icon.fontfamily
      };
    }

    // Individual values can also be set for specific icons
    $scope.nodes.ContainerService.textY = 9;
    $scope.nodes.ContainerService.textX = -1;

    $scope.itemSelected = function (item) {
      var text = "";
      if (item) {
        text = "Selected: " + item.name;
      }
      angular.element(document.getElementById("selected")).text(text);
    };

    $scope.tooltip = function (node) {
      var status = [
        'Name: ' + node.item.name,
        'Type: ' + node.item.kind,
        'Status: ' + node.item.status
      ];
      return status;
    };
  });

  afterEach(function() {
    d3.selectAll('pf-topology').remove();
  });

  it("should create an svg internally", function() {
    var element = compileTopology('<div><pf-topology items="data.items" relations="data.relations" kinds="kinds" icons="data.icons" nodes="nodes" item-selected="itemSelected(item)" search-text="searchText" show-labels="showLabels" tooltip-function="tooltip(node)"></pf-topology></div>', $scope);
    expect(element.find('svg')).not.toBe(undefined);
  });

  it("should generate 6 nodes", function () {
    var elementDiv = '<pf-topology style="display: block; width: 200px; height: 200px;" items="data.items" relations="data.relations" kinds="kinds" icons="data.icons" nodes="nodes" item-selected="itemSelected(item)" search-text="searchText" show-labels="showLabels" tooltip-function="tooltip(node)"></pf-topology>';
    var body = angular.element(document.body);
    body.append(elementDiv);
    compileTopology(body, $scope);
    var topologyElement = body.find('pf-topology svg g');
    expect(topologyElement.length).toBe(6);
  });

  it("should generate 5 lines", function () {
    var elementDiv = '<pf-topology style="display: block; width: 200px; height: 200px;" items="data.items" relations="data.relations" kinds="kinds" icons="data.icons" nodes="nodes" item-selected="itemSelected(item)" search-text="searchText" show-labels="showLabels" tooltip-function="tooltip(node)"></pf-topology>';
    var body = angular.element(document.body);
    body.append(elementDiv);
    compileTopology(body, $scope);
    var topologyElement = body.find('pf-topology svg line');
    expect(topologyElement.length).toBe(5);
  });

  it("should hide/show the text labels", function () {
    var elementDiv = '<pf-topology style="display: block; width: 200px; height: 200px;" items="data.items" relations="data.relations" kinds="kinds" icons="data.icons" nodes="nodes" item-selected="itemSelected(item)" search-text="searchText" show-labels="showLabels" tooltip-function="tooltip(node)"></pf-topology>';
    var body = angular.element(document.body);
    body.append(elementDiv);
    compileTopology(body, $scope);
    var topologyElement = body.find('pf-topology svg');
    expect(topologyElement.find('text.visible').length).toBe(0);
    $scope.showLabels = true;
    $scope.$digest();
    expect(topologyElement.find('text.visible').length).toBe(6);
  });

  it("should update view on search", function () {
    var elementDiv = '<pf-topology style="display: block; width: 200px; height: 200px;" items="data.items" relations="data.relations" kinds="kinds" icons="data.icons" nodes="nodes" item-selected="itemSelected(item)" search-text="searchText" show-labels="showLabels" tooltip-function="tooltip(node)"></pf-topology>';
    var body = angular.element(document.body);
    body.append(elementDiv);
    compileTopology(body, $scope);
    var disabledNodes = body.find('g[style="opacity: 0.2;"]')
    expect(disabledNodes.length).toBe(0);
    $scope.searchText = 'vrguw';
    $scope.$digest();
    disabledNodes = body.find('g[style="opacity: 0.2;"]')
    expect(disabledNodes.length).toBe(5);
  });
});
