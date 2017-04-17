angular.module('jmri.app').factory('JmriJsonSocket', function($websocket) {
  // Open a WebSocket connection
  var parts = document.URL.split('/');
  var socket = $websocket((parts[0] + '//' + parts[2] + '/json/').replace(/^http/, 'ws'));

  var listeners = [];

  socket.onMessage(function(message) {
    var m = JSON.parse(message.data);
    var l;
    while(l in listeners) {
      l.onMessage(m);
    }
  });

  var send = function(type, data, action) {
    var m = {type: type, data: data, action: action};
    socket.send(JSON.stringify(m));
  };

  var heartbeat = function() {
    socket.send(JSON.stringify({type: "ping"}));
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
    getList: function(type) {
      var m = {type: 'list', list: type, action: 'get'};
      socket.send(JSON.stringify(m));  
    },
    register: function(bindings) {
      var listener = JmriSocketListener(this, bindings);
      listeners.push(listener);
      return listener;
    }
  };
    
  return methods;
});

function JmriSocketListener(service, bindings) {
  var jsl = new Object();
  jsl.service = service;
  jsl.socket = service.socket;
  // Default event handlers that do nothing
  jsl.console = function(data) {
  };
  jsl.error = function(error) {
  };
  jsl.open = function() {
  };
  jsl.close = function(event) {
  };
  jsl.willReconnect = function(attempts, milliseconds) {
  };
  jsl.didReconnect = function() {
  };
  jsl.failedReconnect = function() {
  };
  jsl.ping = function() {
  };
  jsl.pong = function() {
  };
  jsl.hello = function(data) {
  };
  jsl.goodbye = function(data) {
  };
  jsl.block = function(name, value, data) {
  };
  jsl.blocks = function(data) {
  };
  jsl.car = function(name, data) {
  };
  jsl.cars = function(data) {
  };
  jsl.configProfile = function(name, data) {
  };
  jsl.configProfiles = function(data) {
  };
  jsl.consist = function(name, data) {
  };
  jsl.consists = function(data) {
  };
  jsl.engine = function(name, data) {
  };
  jsl.engines = function(data) {
  };
  jsl.layoutBlock = function(name, value, data) {
  };
  jsl.layoutBlocks = function(data) {
  };
  jsl.light = function(name, state, data) {
  };
  jsl.lights = function(data) {
  };
  jsl.location = function(name, data) {
  };
  jsl.locations = function(data) {
  };
  jsl.memory = function(name, value, data) {
  };
  jsl.memories = function(data) {
  };
  jsl.metadata = function(data) {
  };
  jsl.networkService = function(name, data) {
  };
  jsl.networkServices = function(data) {
  };
  jsl.power = function(state) {
  };
  jsl.railroad = function(name) {
  };
  jsl.reporter = function(name, value, data) {
  };
  jsl.reporters = function(data) {
  };
  jsl.roster = function(data) {
  };
  jsl.rosterGroups = function(data) {
  };
  jsl.rosterGroup = function(name, data) {
  };
  jsl.rosterEntry = function(name, data) {
  };
  jsl.route = function(name, state, data) {
  };
  jsl.routes = function(data) {
  };
  jsl.sensor = function(name, state, data) {
  };
  jsl.sensors = function(data) {
  };
  jsl.signalHead = function(name, state, data) {
  };
  jsl.signalHeads = function(data) {
  };
  jsl.signalMast = function(name, state, data) {
  };
  jsl.signalMasts = function(data) {
  };
  jsl.throttle = function(throttle, data) {
  };
  jsl.time = function(time, data) {
  };
  jsl.train = function(id, data) {
  };
  jsl.trains = function(data) {
  };
  jsl.turnout = function(name, state, data) {
  };
  jsl.turnouts = function(data) {
  };
  jsl.version = function(string) {
  };
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
  jsl.getPower = function() {
    if (jsl.socket) {
      jsl.socket.get("power", {});
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
  jsl.events = {
    // TODO: add panel and programmer-related events
    error: function(e) {
      jsl.log("Error " + e.data.code + ": " + e.data.message);
      jsl.error(e.data);
    },
    goodbye: function(e) {
      jsl.goodbye(e.data);
    },
    // handle the initial handshake response from the server
    hello: function(e) {
      if (jsl.reconnectAttempts !== 0) {
        jsl.reconnectAttempts = 0;
        jsl.didReconnect();
      }
      jsl.heartbeatInterval = setInterval(jsl.heartbeat, e.data.heartbeat);
      jsl.version(e.data.JMRI);
      jsl.railroad(e.data.railroad);
      jsl.hello(e.data);
    },
    pong: function(e) {
      jsl.pong();
    },
    block: function(e) {
      jsl.block(e.data.name, e.data.value, e.data);
    },
    blocks: function(e) {
      jsl.blocks(e.data);
    },
    car: function(e) {
      jsl.car(e.data.name, e.data);
    },
    cars: function(e) {
      jsl.cars(e.data);
    },
    configProfile: function(e) {
      jsl.configProfile(e.data.name, e.data);
    },
    configProfiles: function(e) {
      jsl.configProfiles(e.data);
    },
    consist: function(e) {
      jsl.consist(e.data.name, e.data);
    },
    consists: function(e) {
      jsl.consists(e.data);
    },
    engine: function(e) {
      jsl.engine(e.data.name, e.data);
    },
    engines: function(e) {
      jsl.engines(e.data);
    },
    layoutBlock: function(e) {
      jsl.layoutBlock(e.data.name, e.data.value, e.data);
    },
    layoutBlocks: function(e) {
      jsl.layoutBlocks(e.data);
    },
    light: function(e) {
      jsl.light(e.data.name, e.data.state, e.data);
    },
    lights: function(e) {
      jsl.lights(e.data);
    },
    location: function(e) {
      jsl.location(e.data.name, e.data);
    },
    locations: function(e) {
      jsl.locations(e.data);
    },
    memory: function(e) {
      jsl.memory(e.data.name, e.data.value, e.data);
    },
    memories: function(e) {
      jsl.memories(e.data);
    },
    metadata: function(e) {
      jsl.metadata(e.data);
    },
    networkService: function(e) {
      jsl.networkService(e.data.name, e.data);
    },
    networkServices: function(e) {
      jsl.networkServices(e.data);
    },
    power: function(e) {
      jsl.power(e.data.state);
    },
    reporter: function(e) {
      jsl.reporter(e.data.name, e.data.value, e.data);
    },
    reporters: function(e) {
      jsl.reporters(e.data);
    },
    roster: function(e) {
      jsl.roster(e.data);
    },
    rosterEntry: function(e) {
      jsl.rosterEntry(e.data.name, e.data);
    },
    rosterGroup: function(e) {
      jsl.rosterGroup(e.data.name, e.data);
    },
    rosterGroups: function(e) {
      jsl.rosterGroups(e.data);
    },
    route: function(e) {
      jsl.route(e.data.name, e.data.state, e.data);
    },
    routes: function(e) {
      jsl.routes(e.data);
    },
    sensor: function(e) {
      jsl.sensor(e.data.name, e.data.state, e.data);
    },
    signalHead: function(e) {
      jsl.signalHead(e.data.name, e.data.state, e.data);
    },
    signalMast: function(e) {
      jsl.signalMast(e.data.name, e.data.state, e.data);
    },
    systemConnection: function(e) {
      jsl.systemConnection(e.data.name, e.data);
    },
    systemConnections: function(e) {
      jsl.systemConnections(e.data);
    },
    throttle: function(e) {
      jsl.throttle(e.data.throttle, e.data);
    },
    time: function(e) {
      jsl.time(e.data.time, e.data);
    },
    train: function(e) {
      jsl.train(e.data.id, e.data);
    },
    trains: function(e) {
      jsl.trains(e.data);
    },
    turnout: function(e) {
      jsl.turnout(e.data.name, e.data.state, e.data);
    },
    turnouts: function(e) {
      jsl.turnouts(e.data);
    }
  };

  /**
   * get the name (type) used for list from the name used for a single item
   *
   * @param {string} name of item
   * @returns {string} name for a list of that item
   */
  jsl.getListType = function(name) {
    var lt = name + "s"; //assume simplest case: "sensor" -> "sensors"
    if (name === "rosterEntry") {  
      lt = "roster";
    } else if (name === "memory") {
      lt = "memories";
    } else if (name === "metadata") { 
      lt = "metadata";
    }
    return lt;
  };

  jsl.reconnect = function() {
    jsl.socket = $.websocket(jsl.url.replace(/^http/, "ws"), {
      open: function() {
        jsl.log("Opened WebSocket");
        jsl.open();
      },
      // stop the heartbeat when the socket closes
      close: function(e) {
        jsl.log("Closed WebSocket " + ((e.wasClean) ? "cleanly" : "unexpectedly") + " (" + e.code + "): " + e.reason);
        clearInterval(jsl.heartbeatInterval);
        jsl.socket.close();
        jsl.socket = null;
        jsl.close(e);
        jsl.attemptReconnection();
      },          
      message: function(e) {
        jsl.console(e.originalEvent.data);
        //determine message type and call appropriate event handler
        var m = JSON.parse(e.originalEvent.data);
         
        //if the message is an array, move array to data and add list type
        if ($.isArray(m)) { 
          if (m.length === 0) {  //cannot determine type of empty array 
            jsl.log("WARN: empty json array received, could not handle");
            return;
          } else { //use type of first entry to determine list type
            var lt = jsl.getListType(m[0].type);
            m = {type: lt, data: m}; //wrap up the message as data for list type
          }
        }
        h = jsl.events[m.type];
        if (h) {
          h.call(this, m);
        }
        if (!m.type) {
          jsl.log("ERROR: 'type' element not found in json message:" + e.originalEvent.data);
        } else if (!h) {
          jsl.log("ERROR: json type '" + m.type +"' received, but not handled");
        }
      }
    });
  };
  jsl.reconnect();
  if (jsl.socket === null) {
    $("#no-websockets").addClass("show").removeClass("hidden");
  }
  $(window).unload(function() {
    jsl.socket.close();
    jsl.socket = null;
    jsl = null;
  });
  return jsl;
}