angular.module('patternfly.charts').component('pfTopology', {
  bindings: {
    items: '<',
    relations: '<',
    kinds: '<',
    icons: '<',
    selection: '<',
    force: '<',
    radius: '<',
    nodes: '<',
    searchText: '<?',
    chartRendered: '&?',
    itemSelected: '&?',
    showLabels: '<?',
    tooltipFunction: '&?'
  },
  controller: function ($element, $attrs) {
    'use strict';
    var options, graph,
      ctrl = this,
      previousItems,
      previousRelations,
      previousKinds,
      contextMenuShowing,
      vs;
    var cache = { };

    ctrl.$onInit = function () {
      $element.css("display", "block");
      options = {"force": ctrl.force, "radius": ctrl.radius};
      ctrl.showLabels = false;

      $element.on("$destroy", function () {
        graph.close();
      });

      d3.select("body").on('click', function () {
        if (contextMenuShowing) {
          removeContextMenu();
        }
      });
    };

    ctrl.$onChanges = function (changesObj) {
      if (changesObj.searchText && graph) {
        search(changesObj.searchText.currentValue);
      }

      if (changesObj.showLabels && vs) {
        toggleLabelVisibility();
      }

      if (changesObj.selection && graph) {
        graph.select(changesObj.selection.currentValue || null);
      }
    };

    ctrl.$doCheck = function () {
      // do a deep compare on data
      if (graph) {
        if (!angular.equals(ctrl.kinds, previousKinds)) {
          previousKinds = angular.copy(ctrl.kinds);
          render(graph.kinds(ctrl.kinds));
        }

        if (!angular.equals(ctrl.items, previousItems) || !angular.equals(ctrl.relations, previousRelations)) {
          previousItems = angular.copy(ctrl.items);
          previousRelations = angular.copy(ctrl.relations);
          render(graph.data(ctrl.items, ctrl.relations));
        }
      }
    };

    ctrl.$postLink = function () {
      options = {"force": ctrl.force, "radius": ctrl.radius};
      graph = topologyGraph($element[0], notify, options);
    };

    function topologyGraph (selector, notify, options) {
      var outer = d3.select(selector);

      /* Kinds of objects to show */
      var kinds = null;

      /* Data we've been fed */
      var items = {};
      var relations = [];

      /* Graph information */
      var width;
      var height;
      var radius = 20;
      var timeout;
      var nodes = [];
      var links = [];
      var lookup = {};
      var selection = null;
      var force = options.force;
      var drag, svg, vertices, edges;

      if (options.radius) {
        radius = options.radius;
      }

      /* Allow the force to be passed in, default if not */
      if (!force) {
        force = d3.layout.force()
          .charge(-800)
          .gravity(0.2)
          .linkDistance(80);
      }

      drag = force.drag();

      svg = outer.append("svg")
        .attr("viewBox", "0 0 1600 1200")
        .attr("preserveAspectRatio", "xMidYMid meet")
        .attr("class", "pf-topology-svg");

      vertices = d3.select();
      edges = d3.select();

      force.on("tick", function () {
        edges.attr("x1", function (d) {
          return d.source.x;
        })
          .attr("y1", function (d) {
            return d.source.y;
          })
          .attr("x2", function (d) {
            return d.target.x;
          })
          .attr("y2", function (d) {
            return d.target.y;
          });

        vertices
          .attr("cx", function (d) {
            d.x = d.fixed ? d.x : Math.max(radius, Math.min(width - radius, d.x));
            return d.x;
          })
          .attr("cy", function (d) {
            d.y = d.fixed ? d.y : Math.max(radius, Math.min(height - radius, d.y));
            return d.y;
          })
          .attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
          });
      });

      drag
        .on("dragstart", function (d) {
          notify(d.item);

          if (d.fixed !== true) {
            d.floatpoint = [d.x, d.y];
          }
          d.fixed = true;
          d3.select(this).classed("fixed", true);
        })
        .on("dragend", function (d) {
          var moved = true;
          if (d.floatpoint) {
            moved = (d.x < d.floatpoint[0] - 5 || d.x > d.floatpoint[0] + 5) ||
              (d.y < d.floatpoint[1] - 5 || d.y > d.floatpoint[1] + 5);
            delete d.floatpoint;
          }
          d.fixed = moved && d.x > 3 && d.x < (width - 3) && d.y >= 3 && d.y < (height - 3);
          d3.select(this).classed("fixed", d.fixed);
        });

      svg
        .on("dblclick", function () {
          svg.selectAll("g")
            .classed("fixed", false)
            .each(function (d) {
              d.fixed = false;
            });
          force.start();
        })
        .on("click", function (ev) {
          if (!d3.select(d3.event.target).datum()) {
            notify(null);
          }
        });

      function select (item) {
        if (item !== undefined) {
          selection = item;
        }
        svg.selectAll("g")
          .classed("selected", function (d) {
            return d.item === selection;
          });
      }

      function adjust () {
        timeout = null;
        width = outer.node().clientWidth;
        height = outer.node().clientHeight;
        force.size([width, height]);
        svg.attr("viewBox", "0 0 " + width + " " + height);
        update();
      }

      function update () {
        var added;

        edges = svg.selectAll("line")
          .data(links);

        edges.exit().remove();
        edges.enter().insert("line", ":first-child");

        edges.attr("class", function (d) {
          return d.kinds;
        });

        vertices = svg.selectAll("g")
          .data(nodes, function (d) {
            return d.id;
          });

        vertices.exit().remove();

        added = vertices.enter().append("g")
          .call(drag);

        select(selection);

        force
          .nodes(nodes)
          .links(links)
          .start();

        return added;
      }

      function digest () {
        var pnodes = nodes;
        var plookup = lookup;
        var item, id, kind, node;
        var i, len, relation, s, t;
        /* The actual data for the graph */
        nodes = [];
        links = [];
        lookup = {};

        for (id in items) {
          if (id) {
            item = items[id];
            kind = item.kind;

            if (kinds && !kinds[kind]) {
              continue;
            }

            /* Prevents flicker */
            node = pnodes[plookup[id]];
            if (!node) {
              node = cache[id];
              delete cache[id];
              if (!node) {
                node = {};
              }
            }

            node.id = id;
            node.item = item;

            lookup[id] = nodes.length;
            nodes.push(node);
          }
        }
        for (i = 0, len = relations.length; i < len; i++) {
          relation = relations[i];

          s = lookup[relation.source];
          t = lookup[relation.target];
          if (s === undefined || t === undefined) {
            continue;
          }

          links.push({source: s, target: t, kinds: nodes[s].item.kind + nodes[t].item.kind});
        }

        if (width && height) {
          return update();
        }
        return d3.select();
      }

      function resized () {
        window.clearTimeout(timeout);
        timeout = window.setTimeout(adjust, 1);
      }

      window.addEventListener('resize', resized);

      adjust();
      resized();

      return {
        select: select,
        kinds: function (value) {
          var added;
          kinds = value;
          added = digest();
          return [vertices, added];
        },
        data: function (newItems, newRelations) {
          var added;
          items = newItems || {};
          relations = newRelations || [];
          added = digest();
          return [vertices, added];
        },
        close: function () {
          var id, node;
          window.removeEventListener('resize', resized);
          window.clearTimeout(timeout);
          /*
           * Keep the positions of these items cached,
           * in case we are asked to make the same graph again.
           */
          cache = {};
          for (id in lookup) {
            if (id) {
              node = nodes[lookup[id]];
              delete node.item;
              cache[id] = node;
            }
          }

          nodes = [];
          lookup = {};
        }
      };
    }

    function search (query) {
      var svg = getSVG();
      var nodes = svg.selectAll("g");
      var selected, links;
      if (query !== "") {
        selected = nodes.filter(function (d) {
          return d.item.name.indexOf(query) === -1;
        });
        selected.style("opacity", "0.2");
        links = svg.selectAll("line");
        links.style("opacity", "0.2");
      }
    }

    function resetSearch (d3) {
      // Display all topology nodes and links
      d3.selectAll("g, line").transition()
        .duration(2000)
        .style("opacity", 1);
    }

    function toggleLabelVisibility () {
      if (ctrl.showLabels) {
        vs.selectAll("text.attached-label")
          .classed("visible", true);
      } else {
        vs.selectAll("text.attached-label")
          .classed("visible", false);
      }
    }

    function getSVG () {
      var graph = d3.select("pf-topology");
      var svg = graph.select('svg');
      return svg;
    }

    function notify (item) {
      ctrl.itemSelected({item: item});
      if ($attrs.selection === undefined) {
        graph.select(item);
      }
    }

    function icon (d) {
      return '#' + d.item.kind;
    }

    function title (d) {
      return d.item.name;
    }

    function render (args) {
      var vertices = args[0];
      var added = args[1];
      var event;

      // allow custom rendering of chart
      if (angular.isFunction(ctrl.chartRendered)) {
        event = ctrl.chartRendered({vertices: vertices, added: added});
      }

      if (!event || !event.defaultPrevented) {
        added.attr("class", function (d) {
          return d.item.kind;
        });

        added.append("circle")
          .attr("r", function (d) {
            return getDimensions(d).r;
          })
          .attr('class', function (d) {
            return getItemStatusClass(d);
          })
          .on("contextmenu", function (d) {
            contextMenu(ctrl, d);
          });

        added.append("title");

        added.on("dblclick", function (d) {
          return dblclick(d);
        });

        added.append("image")
          .attr("xlink:href", function (d) {
            // overwrite this . . .
            var iconInfo = ctrl.icons[d.item.kind];
            switch (iconInfo.type) {
            case 'image':
              return iconInfo.icon;
            case "glyph":
              return null;
            }
          })
          .attr("height", function (d) {
            var iconInfo = ctrl.icons[d.item.kind];
            if (iconInfo.type !== 'image') {
              return 0;
            }
            return 40;
          })
          .attr("width", function (d) {
            var iconInfo = ctrl.icons[d.item.kind];
            if (iconInfo.type !== 'image') {
              return 0;
            }
            return 40;
          })
          .attr("y", function (d) {
            return getDimensions(d).y;
          })
          .attr("x", function (d) {
            return getDimensions(d).x;
          })
          .on("contextmenu", function (d) {
            contextMenu(ctrl, d);
          });

        added.append("text")
          .each(function (d) {
            var iconInfo = ctrl.icons[d.item.kind];
            if (iconInfo.type !== 'glyph') {
              return;
            }
            d3.select(this).text(iconInfo.icon)
              .attr("class", "glyph")
              .attr('font-family', iconInfo.fontfamily);
          })

          .attr("y", function (d) {
            return getDimensions(d).y;
          })
          .attr("x", function (d) {
            return getDimensions(d).x;
          })
          .on("contextmenu", function (d) {
            contextMenu(this, d);
          });


        added.append("text")
          .attr("x", 26)
          .attr("y", 24)
          .text(function (d) {
            return d.item.name;
          })
          .attr('class', function () {
            var className = "attached-label";
            if (ctrl.showLabels) {
              return className + ' visible';
            }
            return className;
          });

        added.selectAll("title").text(function (d) {
          return tooltip(d).join("\n");
        });

        vs = vertices;
      }
      graph.select();
    }

    function tooltip (d) {
      if (ctrl.tooltipFunction) {
        return ctrl.tooltipFunction({node: d});
      }
      return 'Name: ' + d.item.name;
    }

    function removeContextMenu () {
      d3.event.preventDefault();
      d3.select('.popup').remove();
      contextMenuShowing = false;
    }

    function contextMenu (that, data) {
      var canvasSize, popupSize, canvas, mousePosition, popup;

      if (contextMenuShowing) {
        removeContextMenu();
      } else {
        d3.event.preventDefault();

        canvas = d3.select('pf-topology');
        mousePosition = d3.mouse(canvas.node());

        popup = canvas.append('div')
          .attr('class', 'popup')
          .style('left', mousePosition[0] + 'px')
          .style('top', mousePosition[1] + 'px');
        popup.append('h5').text('Actions on ' + data.item.kind);

        buildContextMenuOptions(popup, data);

        canvasSize = [
          canvas.node().offsetWidth,
          canvas.node().offsetHeight
        ];

        popupSize = [
          popup.node().offsetWidth,
          popup.node().offsetHeight
        ];

        if (popupSize[0] + mousePosition[0] > canvasSize[0]) {
          popup.style('left', 'auto');
          popup.style('right', 0);
        }

        if (popupSize[1] + mousePosition[1] > canvasSize[1]) {
          popup.style('top', 'auto');
          popup.style('bottom', 0);
        }
        contextMenuShowing = !contextMenuShowing;
      }
    }

    function buildContextMenuOptions (popup, data) {
      if (data.item.kind === 'Tag') {
        return false;
      }
      addContextMenuOption(popup, 'Go to summary page', data, dblclick);
    }

    function dblclick (d) {
      window.location.assign(d.url);
    }

    function addContextMenuOption (popup, text, data, callback) {
      popup.append('p').text(text).on('click', function () {
        callback(data);
      });
    }

    function getDimensions (d) {
      var nodeEntry = ctrl.nodes[d.item.kind];
      var defaultDimensions = defaultElementDimensions();
      if (nodeEntry) {
        if (nodeEntry.textX) {
          defaultDimensions.x = nodeEntry.textX;
        }
        if (nodeEntry.textY) {
          defaultDimensions.y = nodeEntry.textY;
        }

        if (nodeEntry.radius) {
          defaultDimensions.r = nodeEntry.radius;
        }
      }
      return defaultDimensions;
    }

    function defaultElementDimensions () {
      return { x: 0, y: 9, r: 17 };
    }

    function getItemStatusClass (d) {
      switch (d.item.status.toLowerCase()) {
      case "ok":
      case "active":
      case "available":
      case "on":
      case "ready":
      case "running":
      case "succeeded":
      case "valid":
        return "success";
      case "notready":
      case "failed":
      case "error":
      case "unreachable":
        return "error";
      case 'warning':
      case 'waiting':
      case 'pending':
        return "warning";
      case 'unknown':
      case 'terminated':
        return "unknown";
      }
    }
  }
});
