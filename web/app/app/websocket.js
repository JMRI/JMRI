/**
 * Create a WebSocket for handling a JMRI JSON service.
 *
 * TODO:
 * automatic handling of disconnects and reconnects
 *
 * @argument {angularService} $websocket angular-websocket service
 * @argument {angularService} $log AngularJS logging service
 * @type AngularJS Service Factory
 */
angular.module('jmri.app').factory('jmriWebSocket', function($websocket, $log) {
  // Open a WebSocket connection
  var parts = document.URL.split('/');
  var url = parts[0] + '//' + parts[2] + '/json/';
  var socket = $websocket(url.replace(/^http/, 'ws'));
  // control object exposed as config to consumers
  var ctrl = {
    logSent: false,
    logReceived: false
  };

  socket.onMessage(function onMessage(message) {
    if (ctrl.logReceived) {
      $log.debug('Received message ' + message.data);
    }
  });

  var send = function send(type, data, method) {
    var m = {type: type, data: data, method: method};
    if (ctrl.logSent) {
      $log.debug('Sending ' + JSON.stringify(m) + '...');
    }
    socket.send(m);
  };

  var sendDelete = function sendDelete(type, data) {
    send(type, data, 'delete');
  };
  var sendGet = function sendGet(type, data) {
    send(type, data, 'get');
  };
  var sendPost = function sendPost(type, data) {
    send(type, data, 'post');
  };
  var sendPut = function sendPut(type, data) {
    send(type, data, 'put');
  };
  var sendList = function sendList(type) {
    var m = {type: 'list', list: type, method: 'get'};
    $log.debug('Listing ' + type + '...');
    socket.send(m);
  };
  var sendState = function sendState(type, name, state) {
    sendPost(type, {name: name, state: state});
  };

  var methods = {
    // general methods
    socket: socket,
    config: ctrl,
    delete: sendDelete,
    get: sendGet,
    post: sendPost,
    put: sendPut,
    list: sendList,
    setState: sendState,
    ping: function ping() {
      socket.send(JSON.stringify({type: "ping"}));
    },
    /**
     * Register a listener with the passed in array of functions that will be
     * called; each bound function will be called if its name matches the type
     * property of the message with the JSON message contents.
     * @param {type} bindings array of overridden functions
     * @returns {undefined} the new listener
     */
    register: function register(bindings) {
      return JsonSocket(this, bindings);
    },
    /**
     * Push an object onto an array if an object in the array with the same name
     * is not already in the array; the object is merged into the first matching
     * object if one already exists.
     * @param {type} array the target array
     * @param {type} object the object to push or merge
     * @returns {false|websocketL#1.methods.push.array} false if object
     * was pushed onto the array or the merged object
     */
    mergePush: function(array, object) {
      if (array.length > 0) {
        for (var i = 0; i < array.length; i++) {
          if (array[i].name === object.name) {
            var target = array[i];
            $.extend(true, target, array[i], object);
            array[i] = target;
            return target;
          }
        }
      }
      array.push(object);
      return false;
    },
    // Common getters and setters
    getLight: function(name) {
      sendGet("light", {name: name});
    },
    setLight: function(name, state) {
      sendState("light", name, state);
    },
    getMemory: function(name) {
      sendGet("memory", {name: name});
    },
    setMemory: function(name, value) {
      sendPost("memory", {name: name, value: value});
    },
    getBlock: function(name) {
      sendGet("block", {name: name});
    },
    setBlock: function(name, value) {
      sendPost("block", {name: name, value: value});
    },
    getLayoutBlock: function(name) {
      sendGet("layoutBlock", {name: name});
    },
    setLayoutBlock: function(name, value) {
      sendPost("layoutBlock", {name: name, value: value});
    },
    getList: function(name) {
      getList(name);
    },
    getPower: function(name) {
      data = {};
      if (name) {
        data.name = name;
      }
      sendGet("power", data);
    },
    setPower: function(name, state) {
      data = {};
      if (state) {
          data.name = name;
          data.state = state;
      } else {
          data.state = name;
      }
      sendPost("power", data);
    },
    getRailroad: function() {
      sendGet("railroad", {});
    },
    getRosterGroup: function(id) {
      sendGet("rosterGroup", {name: id});
    },
    getRosterEntry: function(id) {
      sendGet("rosterEntry", {name: id});
    },
    getRoute: function(name) {
      sendGet("route", {name: name});
    },
    setRoute: function(name, state) {
      sendState("route", name, state);
    },
    getSensor: function(name) {
      sendGet("sensor", {name: name});
    },
    setSensor: function(name, state) {
      sendState("sensor", name, state);
    },
    getSignalHead: function(name) {
      sendGet("signalHead", {name: name});
    },
    setSignalHead: function(name, state) {
      sendState("signalHead", name, state);
    },
    getSignalMast: function(name) {
      sendGet("signalMast", {name: name});
    },
    setSignalMast: function(name, state) {
      sendState("signalMast", name, state);
    },
    /**
     * Get the current status of the throttle
     *
     * @param {String} throttle identity
     */
    getThrottle: function(throttle) {
      sendGet("throttle", {throttle: throttle, status: true});
    },
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
    setThrottle: function(throttle, data) {
      data.throttle = throttle;
      sendPost("throttle", data);
    },
    getTime: function() {
      sendGet("time", {});
    },
    getTrain: function(id) {
      sendGet("train", {id: id});
    },
    getTurnout: function(name) {
      sendGet("turnout", {name: name});
    },
    setTurnout: function(name, state) {
      sendState("turnout", name, state);
    },
    // Constants
    UNKNOWN: 0,
    POWER_ON: 2,
    POWER_OFF: 4,
    CLOSED: 2,
    THROWN: 4,
    ACTIVE: 2,
    INACTIVE: 4
  };

  return methods;
});

function JsonSocket(service, bindings) {
  var jsl = {
    // Default event handlers that do nothing
    console: function(data, method) {
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
    hello: function(data, method) {
    },
    goodbye: function(data, method) {
    },
    block: function(data, method) {
    },
    blocks: function(data, method) {
    },
    car: function(data, method) {
    },
    cars: function(data, method) {
    },
    configProfile: function(data, method) {
    },
    configProfiles: function(data, method) {
    },
    consist: function(data, method) {
    },
    consists: function(data, method) {
    },
    engine: function(data, method) {
    },
    engines: function(data, method) {
    },
    layoutBlock: function(data, method) {
    },
    layoutBlocks: function(data, method) {
    },
    light: function(data, method) {
    },
    lights: function(data, method) {
    },
    location: function(data, method) {
    },
    locations: function(data, method) {
    },
    memory: function(data, method) {
    },
    memories: function(data, method) {
    },
    metadata: function(data, method) {
    },
    networkService: function(data, method) {
    },
    networkServices: function(data, method) {
    },
    power: function(data, method) {
    },
    railroad: function(data, method) {
    },
    reporter: function(data, method) {
    },
    reporters: function(data, method) {
    },
    roster: function(data, method) {
    },
    rosterGroups: function(data, method) {
    },
    rosterGroup: function(data, method) {
    },
    rosterEntry: function(data, method) {
    },
    route: function(data, method) {
    },
    routes: function(data, method) {
    },
    sensor: function(data, method) {
    },
    sensors: function(data, method) {
    },
    signalHead: function(data, method) {
    },
    signalHeads: function(data, method) {
    },
    signalMast: function(data, method) {
    },
    signalMasts: function(data, method) {
    },
    throttle: function(data, method) {
    },
    time: function(data, method) {
    },
    train: function(data, method) {
    },
    trains: function(data, method) {
    },
    turnout: function(data, method) {
    },
    turnouts: function(data, method) {
    },
    version: function(data, method) {
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
    if (jsl.socket && jsl.service.readyState === 1) {
      jsl.log("Connecting on connect()");
      jsl.open();
    } else {
      // if the JMRI WebSocket was not open when the document was
      // ready, wait one second and call open() if the socket
      // did not open in the meantime -- with the exception of
      // throttles, the JMRI object can work around the inability
      // to use WebSockets
      setTimeout(function() {
        if (!jsl.socket || jsl.service.readyState !== 1) {
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
    jsl.service.send("ping");
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
          h.call(this, o.data, o.method);
        }
      }
      return;
    }
    h = jsl[m.type];
    if (h) {
      h.call(this, m.data, m.method);
    }
    // note that h being null is not an error!
    if (!m.type) {
      jsl.log("ERROR: 'type' element not found in json message:" + m);
    }
  };
}
