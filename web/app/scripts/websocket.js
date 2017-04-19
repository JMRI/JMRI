angular.module('jmri.app').factory('$jsonSocket', function($websocket, $log) {
  // Open a WebSocket connection
  var parts = document.URL.split('/');
  var socket = $websocket((parts[0] + '//' + parts[2] + '/json/').replace(/^http/, 'ws'));

  socket.onMessage(function(message) {
    $log.info('Received message ' + message.data);
  });

  var send = function(type, data, action) {
    var m = {type: type, data: data, action: action};
//    $log.info('Sending ' + JSON.stringify(m) + '...');
    socket.send(m);
  };

  var methods = {
    socket: socket,
    delete: function(type, data) {
      data.method = 'delete';
      send(type, data, 'delete');
    },
    get: function(type, data) {
      data.method = 'get';
      send(type, data, 'get');
    },
    post: function(type, data) {
      data.method = 'post';
      send(type, data, 'post');
    },
    put: function(type, data) {
      data.method = 'put';
      send(type, data, 'put');
    },
    list: function(type) {
      var m = {type: 'list', list: type, action: 'get'};
      $log.info('Listing ' + type + '...');
      socket.send(m);  
    },
    ping: function() {
      socket.send(JSON.stringify({type: "ping"}));
    },
    register: function(bindings) {
      return JsonSocket(this, bindings);
    }
  };

  return methods;
});

function JsonSocket(service, bindings) {
  var jsl = {
    // Default event handlers that do nothing
    console: function(data) {
    },
    onError: function(error) {
    },
    onOpen: function() {
    },
    onClose: function(event) {
    },
    willReconnect: function(attempts, milliseconds) {
    },
    didReconnect: function() {
    },
    failedReconnect: function() {
    },
    ping: function() {
    },
    pong: function() {
    },
    hello: function(data) {
    },
    goodbye: function(data) {
    },
    block: function(data) {
    },
    blocks: function(data) {
    },
    car: function(data) {
    },
    cars: function(data) {
    },
    configProfile: function(data) {
    },
    configProfiles: function(data) {
    },
    consist: function(data) {
    },
    consists: function(data) {
    },
    engine: function(data) {
    },
    engines: function(data) {
    },
    layoutBlock: function(data) {
    },
    layoutBlocks: function(data) {
    },
    light: function(data) {
    },
    lights: function(data) {
    },
    location: function(data) {
    },
    locations: function(data) {
    },
    memory: function(data) {
    },
    memories: function(data) {
    },
    metadata: function(data) {
    },
    networkService: function(data) {
    },
    networkServices: function(data) {
    },
    power: function(data) {
    },
    railroad: function(name) {
    },
    reporter: function(data) {
    },
    reporters: function(data) {
    },
    roster: function(data) {
    },
    rosterGroups: function(data) {
    },
    rosterGroup: function(data) {
    },
    rosterEntry: function(data) {
    },
    route: function(data) {
    },
    routes: function(data) {
    },
    sensor: function(data) {
    },
    sensors: function(data) {
    },
    signalHead: function(data) {
    },
    signalHeads: function(data) {
    },
    signalMast: function(data) {
    },
    signalMasts: function(data) {
    },
    throttle: function(data) {
    },
    time: function(data) {
    },
    train: function(data) {
    },
    trains: function(data) {
    },
    turnout: function(data) {
    },
    turnouts: function(data) {
    },
    version: function(string) {
    }
  };
  jsl.service = service;
  jsl.socket = service.socket;
  jsl.socket.onMessage(function(message) {
    jsl.onMessage(JSON.parse(message.data));
  });
  jsl.socket.onOpen(function() {
    jsl.onOpen();
  });
  jsl.socket.onClose(function() {
    jsl.onClose();
  });
  jsl.socket.onError(function() {
    jsl.onError();
  });
  // Add user-defined handlers to the settings object
  $.extend(jsl, bindings);
  // Constants
  jsl.UNKNOWN = 0;
  jsl.POWER_ON = 2;
  jsl.POWER_OFF = 4;
  jsl.CLOSED = 2;
  jsl.THROWN = 4;
  jsl.ACTIVE = 2;
  jsl.INACTIVE = 4;
  // Getters and Setters
  jsl.getLight = function(name) {
    if (jsl.socket) {
      jsl.socket.get("light", {name: name});
    } else {
      $.getJSON(jsl.url + "light/" + name, function(json) {
        jsl.light(json.data.name, json.data.state, json.data);
      });
    }
  };
  jsl.setLight = function(name, state) {
    if (jsl.socket) {
      jsl.socket.post("light", {name: name, state: state});
    } else {
      $.ajax({
        url: jsl.url + "light/" + name,
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.light(json.data.name, json.data.state, json.data);
          jsl.getLight(json.data.name, json.data.state);
        }
      });
    }
  };
  jsl.getMemory = function(name) {
    if (jsl.socket) {
      jsl.socket.get("memory", {name: name});
    } else {
      $.getJSON(jsl.url + "memory/" + name, function(json) {
        jsl.memory(json.data.name, json.data.value, json.data);
      });
    }
  };
  jsl.setMemory = function(name, value) {
    if (jsl.socket) {
      jsl.socket.post("memory", {name: name, value: value});
    } else {
      $.ajax({
        url: jsl.url + "memory/" + name,
        type: "POST",
        data: JSON.stringify({value: value}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.memory(json.data.name, json.data.value, json.data);
          jsl.getMemory(json.data.name, json.data.value);
        }
      });
    }
  };
  jsl.getBlock = function(name) {
    if (jsl.socket) {
      jsl.socket.get("block", {name: name});
    } else {
      $.getJSON(jsl.url + "block/" + name, function(json) {
        jsl.block(json.data.name, json.data.value, json.data);
      });
    }
  };
  jsl.setBlock = function(name, value) {
    if (jsl.socket) {
      jsl.socket.post("block", {name: name, value: value});
    } else {
      $.ajax({
        url: jsl.url + "block/" + name,
        type: "POST",
        data: JSON.stringify({value: value}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.block(json.data.name, json.data.value, json.data);
          jsl.getBlock(json.data.name, json.data.value);
        }
      });
    }
  };
  jsl.getLayoutBlock = function(name) {
    if (jsl.socket) {
      jsl.socket.get("layoutBlock", {name: name});
    } else {
      $.getJSON(jsl.url + "layoutBlock/" + name, function(json) {
        jsl.layoutBlock(json.data.name, json.data.value, json.data);
      });
    }
  };
  jsl.setLayoutBlock = function(name, value) {
    if (jsl.socket) {
      jsl.socket.post("layoutBlock", {name: name, value: value});
    } else {
      $.ajax({
        url: jsl.url + "layoutBlock/" + name,
        type: "POST",
        data: JSON.stringify({value: value}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.layoutBlock(json.data.name, json.data.value, json.data);
          jsl.getLayoutBlock(json.data.name, json.data.value);
        }
      });
    }
  };
  jsl.getList = function(name) {
    jsl.service.getList(name);
  };

  jsl.getObject = function(type, name) {
    switch (type) {
      case "light":
        jsl.getLight(name);
        break;
      case "block":
        jsl.getBlock(name);
        break;
      case "layoutBlock":
        jsl.getLayoutBlock(name);
        break;
      case "memory":
        jsl.getMemory(name);
        break;
      case "rosterEntry":
        jsl.getRosterEntry(name);
        break;
      case "rosterGroup":
        jsl.getRosterGroup(name);
        break;
      case "route":
        jsl.getRoute(name);
        break;
      case "sensor":
        jsl.getSensor(name);
        break;
      case "signalHead":
        jsl.getSignalHead(name);
        break;
      case "signalMast":
        jsl.getSignalMast(name);
        break;
      case "turnout":
        jsl.getTurnout(name);
        break;
      default:
        if (window.console) {
          console.log("WARN-unknown type of " + type + " encountered by jquery.jsl.js in getObject().");
        }

    }
  };
  jsl.setObject = function(type, name, state) {
    switch (type) {
      case "light":
        jsl.setLight(name, state);
        break;
      case "memory":
        jsl.setMemory(name, state);
        break;
      case "block":
        jsl.setBlock(name, state);
        break;
      case "layoutBlock":
        jsl.setLayoutBlock(name, state);
        break;
      case "rosterEntry":
        jsl.setRosterEntry(name, state);
        break;
      case "route":
        jsl.setRoute(name, state);
        break;
      case "sensor":
        jsl.setSensor(name, state);
        break;
      case "signalHead":
        jsl.setSignalHead(name, state);
        break;
      case "signalMast":
        jsl.setSignalMast(name, state);
        break;
      case "turnout":
        jsl.setTurnout(name, state);
        break;
      default:
        if (window.console) {
          console.log("WARN-unknown type of " + type + " encountered by jquery.jsl.js in setObject().");
        }
    }
  };
  jsl.getPower = function(name) {
    data = {};
    if (name) {
      data.name = name;
    }
    if (jsl.socket) {
      jsl.socket.get("power", data);
    } else {
      $.getJSON(jsl.url + "power", function(json) {
        jsl.power(json.data.state);
      });
    }
  };
  jsl.setPower = function(state) {
    if (jsl.socket) {
      jsl.socket.post("power", {state: state});
    } else {
      $.ajax({
        url: jsl.url + "power",
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.power(json.data.state);
        }
      });
    }
  };
  jsl.getRosterGroup = function(id) {
    if (jsl.socket) {
      jsl.socket.get("rosterGroup", {name: id});
    } else {
      $.getJSON(jsl.url + "rosterGroup/" + id, function(json) {
        jsl.rosterGroup(json.data.name, json.data);
      });
    }
  };
  jsl.getRosterEntry = function(id) {
    if (jsl.socket) {
      jsl.socket.get("rosterEntry", {name: id});
    } else {
      $.getJSON(jsl.url + "rosterEntry/" + id, function(json) {
        jsl.rosterEntry(json.data.name, json.data);
      });
    }
  };
  jsl.getRoute = function(name) {
    if (jsl.socket) {
      jsl.socket.get("route", {name: name});
    } else {
      $.getJSON(jsl.url + "route/" + name, function(json) {
        jsl.route(json.data.name, json.data.state, json.data);
      });
    }
  };
  jsl.setRoute = function(name, state) {
    if (jsl.socket) {
      jsl.socket.post("route", {name: name, state: state});
    } else {
      $.ajax({
        url: jsl.url + "route/" + name,
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.route(json.data.name, json.data.state, json.data);
          jsl.getRoute(json.data.name, json.data.state);
        }
      });
    }
  };
  jsl.getSensor = function(name) {
    if (jsl.socket) {
      jsl.socket.get("sensor", {name: name});
    } else {
      $.getJSON(jsl.url + "sensor/" + name, function(json) {
        jsl.sensor(json.data.name, json.data.state, json.data);
      });
    }
  };
  jsl.setSensor = function(name, state) {
    if (jsl.socket) {
      jsl.socket.post("sensor", {name: name, state: state});
    } else {
      $.ajax({
        url: jsl.url + "sensor/" + name,
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.sensor(json.data.name, json.data.state, json.data);
          jsl.getSensor(json.data.name, json.data.state);
        }
      });
    }
  };
  jsl.getSignalHead = function(name) {
    if (jsl.socket) {
      jsl.socket.get("signalHead", {name: name});
    } else {
      $.getJSON(jsl.url + "signalHead/" + name, function(json) {
        jsl.signalHead(json.data.name, json.data.state, json.data);
      });
    }
  };
  jsl.setSignalHead = function(name, state) {
    if (jsl.socket) {
      jsl.socket.post("signalHead", {name: name, state: state});
    } else {
      $.ajax({
        url: jsl.url + "signalHead/" + name,
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.signalHead(json.data.name, json.data.state, json.data);
          jsl.getSignalHead(json.data.name, json.data.state);
        }
      });
    }
  };
  jsl.getSignalMast = function(name) {
    if (jsl.socket) {
      jsl.socket.get("signalMast", {name: name});
    } else {
      $.getJSON(jsl.url + "signalMast/" + name, function(json) {
        jsl.signalMast(json.data.name, json.data.state, json.data);
      });
    }
  };
  jsl.setSignalMast = function(name, state) {
    if (jsl.socket) {
      jsl.socket.post("signalMast", {name: name, state: state});
    } else {
      $.ajax({
        url: jsl.url + "signalMast/" + name,
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.signalMast(json.data.name, json.data.state, json.data);
          jsl.getSignalMast(json.data.name, json.data.state);
        }
      });
    }
  };
  /**
   * Get the current status of the throttle
   *
   * @param {String} throttle identity
   * @returns {Boolean} false if unable to use throttles
   */
  jsl.getThrottle = function(throttle) {
    if (jsl.socket) {
      jsl.socket.get("throttle", {throttle: throttle, status: true});
      return true;
    } else {
      return false;
    }
  };
  /**
   * Set some aspect of a throttle as defined in data
   *
   * Call this method with the data elements address:[dcc address]
   * or id:[roster entry id] to create a JMRI throttle. Include the
   * data element status:true to get the complete throttle status.
   *
   * @param {string} throttle the throttle identity
   * @param {object} data key/value pairs of the throttle properties to change
   * @returns {boolean} false if unable to use throttles
   */
  jsl.setThrottle = function(throttle, data) {
    if (jsl.socket) {
      data.throttle = throttle;
      jsl.socket.post("throttle", data);
      return true;
    } else {
      return false;
    }
  };
  jsl.getTime = function() {
    if (jsl.socket) {
      jsl.socket.get("time", {});
    } else {
      $.getJSON(jsl.url + "time", function(json) {
        jsl.time(json.data.time, json.data);
      });
    }
  };
  jsl.getTrain = function(id) {
    if (jsl.socket) {
      jsl.socket.get("train", {id: id});
    } else {
      $.getJSON(jsl.url + "train/" + id, function(json) {
        jsl.train(json.data.id, json.data);
      });
    }
  };
  jsl.getTurnout = function(name) {
    if (jsl.socket) {
      jsl.socket.get("turnout", {name: name});
    } else {
      $.getJSON(jsl.url + "turnout/" + name, function(json) {
        jsl.turnout(json.data.name, json.data.state, json.data);
      });
    }
  };
  jsl.setTurnout = function(name, state) {
    if (jsl.socket) {
      jsl.socket.post("turnout", {name: name, state: state});
    } else {
      $.ajax({
        url: jsl.url + "turnout/" + name,
        type: "POST",
        data: JSON.stringify({state: state}),
        contentType: "application/json; charset=utf-8",
        success: function(json) {
          jsl.turnout(json.data.name, json.data.state, json.data);
          jsl.getTurnout(json.data.name, json.data.state);
        }
      });
    }
  };
  /**
   * Force the jsl object to begin communicating with the JMRI server
   * even if the WebSocket connection cannot be immediately established
   *
   * @returns {undefined}
   */
  jsl.connect = function() {
    // if the JMRI WebSocket was open before we overloaded the
    // open() method, we call the open() method to ensure it gets
    // called
    if (jsl.socket && jsl.socket.readyState === 1) {
      jsl.log("Connecting on connect()");
      jsl.open();
    } else {
      // if the JMRI WebSocket was not open when the document was
      // ready, wait one second and call open() if the socket
      // did not open in the meantime -- with the exception of
      // throttles, the JMRI object can work around the inability
      // to use WebSockets
      setTimeout(function() {
        if (!jsl.socket || jsl.socket.readyState !== 1) {
          jsl.log("Connecting on timeout");
          jsl.open();
        }
      }, 1000);
    }
  };
  // Logging
  // Object unique identity - an eight digit hexidecimal number
  jsl.serialNumber = (Math.random().toString(16) + "000000000").substr(2, 8);
  jsl.logWithDateTimeStamp = false;
  jsl.log = function(message) {
    if (window.console) {
      if (jsl.logWithDateTimeStamp) {
        window.console.log(new Date().toJSON() + " " + jsl.serialNumber + " " + message);
      } else {
        window.console.log(jsl.serialNumber + " " + message);
      }
    }
  };
  // Heartbeat
  jsl.heartbeat = function() {
    jsl.socket.send("ping");
    jsl.ping();
  };
  jsl.heartbeatInterval = null;
  // WebSocket
  jsl.reconnectAttempts = 0;
  jsl.reconnectPoller = null;
  jsl.reconnectDelay = 0;
  jsl.reconnectPolls = 0;
  jsl.attemptReconnection = function() {
    if (jsl.reconnectAttempts < 20) {
      jsl.reconnectAttempts++;
      jsl.reconnectDelay = 15000 * jsl.reconnectAttempts;
      jsl.willReconnect(jsl.reconnectAttempts, jsl.reconnectDelay);
      jsl.log("Reconnecting WebSocket (attempt " + jsl.reconnectAttempts + "/20)");
      setTimeout(
        function() {
          if (jsl.reconnectAttempts === 1) {
            jsl.log("Reconnecting from closed connection.");
          } else {
            jsl.log("Reconnecting from failed reconnection attempt.");
          }
          jsl.reconnect();
        }, jsl.reconnectDelay);
    } else {
      jsl.failedReconnect();
    }
  };
  //set of functions for handling each "type" of json message
  jsl.onMessage = function(m) {
    // if message is an array, call handler for each object in array
    if ($.isArray(m)) {
      for (var i in m) {
        var o = m[i];
        h = jsl[o.type];
        if (h) {
          h.call(this, o);
        }
      }
      return;
    }
    h = jsl[m.type];
    if (h) {
      h.call(this, m);
    }
    // note that h being null is not an error!
    if (!m.type) {
      jsl.log("ERROR: 'type' element not found in json message:" + m);
    }
  };
}
