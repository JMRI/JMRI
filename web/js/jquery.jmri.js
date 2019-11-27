/**
 * JMRI JSON protocol abstract client.
 *
 * NOTE this library makes assumptions about JSON arrays returned by JMRI that
 * __do not__ match documented JMRI behavior. It is recommended that no methods
 * expecting a data array of a single type be relied upon.
 * 
 * This library depends on jQuery 1.9 or newer.
 *
 * To be useful, you need to override one or more of the following functions:
 * console(data)
 * error(error)
 * open()
 * close()
 * willReconnect()
 * didReconnect()
 * hello(data)
 * goodbye(data)
 * block(name, value, data)
 * blocks(data array)
 * car(name, data)
 * cars(data array)
 * configProfile(name, data)
 * configProfiles(data array)
 * consist(name, data)
 * consists(data array)
 * engine(name, data)
 * engines(data array)
 * idTag(name, state, data)
 * idTags(data array)
 * layoutBlock(name, value, data)
 * layoutBlocks(data array)
 * light(name, state, data)
 * lights(data array)
 * location(name, data)
 * locations(data array)
 * memory(name, value, data)
 * memories(data array)
 * metadata(data array)
 * networkService(name, data)
 * networkServices(data array)
 * panel(name, value, data)
 * panels(data array)
 * power(state)
 * railroad(name)
 * reporter(name, value, data)
 * reporters(data array)
 * roster(data array)
 * rosterEntry(name, data)
 * rosterGroup(name, data)
 * rosterGroups(data array)
 * route(name, state, data)
 * routes(data array)
 * sensor(name, state, data)
 * sensors(data array)
 * signalHead(name, state, data)
 * signalHeads(data array)
 * signalMast(name, state, data)
 * signalMasts(data array)
 * systemConnection(name, data)
 * systemConnections(data array)
 * throttle(name, data)
 * time(time, data)
 * train(name, data)
 * trains(data array)
 * turnout(name, state, data)
 * turnouts(data array)
 * version(version)
 * as demonstrated in the power.html demonstration web app
 * 
 * @author Copyright (C) Randall Wood 2013, 2014
 * @param {a jQuery object} $
 * @returns {a JMRI object}
 */
(function ($) {
    $.extend({
        JMRI: function (url, bindings) {
            var jmri = new Object();
            if (typeof (url) === 'string') {
                jmri.url = url;
            } else {
                jmri.url = document.URL.split('/')[0] + "//" + document.URL.split('/')[2] + "/json/";
                bindings = url;
            }
            // Default event handlers that do nothing
            jmri.console = function (data) {
            };
            jmri.error = function (error) {
            };
            jmri.open = function () {
            };
            jmri.close = function (event) {
            };
            jmri.willReconnect = function (attempts, milliseconds) {
            };
            jmri.didReconnect = function () {
            };
            jmri.failedReconnect = function () {
            };
            jmri.ping = function () {
            };
            jmri.pong = function () {
            };
            jmri.hello = function (data) {
            };
            jmri.goodbye = function (data) {
            };
            jmri.block = function (name, value, data) {
            };
            jmri.blocks = function (data) {
            };
            jmri.car = function (name, data) {
            };
            jmri.cars = function (data) {
            };
            jmri.configProfile = function (name, data) {
            };
            jmri.configProfiles = function (data) {
            };
            jmri.consist = function (name, data) {
            };
            jmri.consists = function (data) {
            };
            jmri.engine = function (name, data) {
            };
            jmri.engines = function (data) {
            };
            jmri.idTag = function (name, state, data) {
            };
            jmri.idTags = function (data) {
            };
            jmri.layoutBlock = function (name, value, data) {
            };
            jmri.layoutBlocks = function (data) {
            };
            jmri.light = function (name, state, data) {
            };
            jmri.lights = function (data) {
            };
            jmri.location = function (name, data) {
            };
            jmri.locations = function (data) {
            };
            jmri.memory = function (name, value, data) {
            };
            jmri.memories = function (data) {
            };
            jmri.metadata = function (data) {
            };
            jmri.networkService = function (name, data) {
            };
            jmri.networkServices = function (data) {
            };
            jmri.panel = function (name, value, data) {
            };
            jmri.panels = function (data) {
            };
            jmri.power = function (state) {
            };
            jmri.railroad = function (name) {
            };
            jmri.reporter = function (name, value, data) {
            };
            jmri.reporters = function (data) {
            };
            jmri.roster = function (data) {
            };
            jmri.rosterGroups = function (data) {
            };
            jmri.rosterGroup = function (name, data) {
            };
            jmri.rosterEntry = function (name, data) {
            };
            jmri.route = function (name, state, data) {
            };
            jmri.routes = function (data) {
            };
            jmri.sensor = function (name, state, data) {
            };
            jmri.sensors = function (data) {
            };
            jmri.signalHead = function (name, state, data) {
            };
            jmri.signalHeads = function (data) {
            };
            jmri.signalMast = function (name, state, data) {
            };
            jmri.signalMasts = function (data) {
            };
            jmri.systemConnection = function (name, data) {
            };
            jmri.systemConnections = function (data) {
            };
            jmri.throttle = function (throttle, data) {
            };
            jmri.time = function (time, data) {
            };
            jmri.train = function (name, data) {
            };
            jmri.trains = function (data) {
            };
            jmri.turnout = function (name, state, data) {
            };
            jmri.turnouts = function (data) {
            };
            jmri.version = function (string) {
            };
            // Add user-defined handlers to the settings object
            $.extend(jmri, bindings);
            // Constants
            jmri.UNKNOWN = 0;
            jmri.POWER_ON = 2;
            jmri.POWER_OFF = 4;
            jmri.CLOSED = 2;
            jmri.THROWN = 4;
            jmri.ACTIVE = 2;
            jmri.INACTIVE = 4;
            jmri.INCONSISTENT = 8;
            // Getters and Setters
            jmri.getLight = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("light", { name: name });
                } else {
                    $.getJSON(jmri.url + "light/" + name, function (json) {
                        jmri.light(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setLight = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("light", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "light/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.light(json.data.name, json.data.state, json.data);
                            jmri.getLight(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getMemory = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("memory", { name: name });
                } else {
                    $.getJSON(jmri.url + "memory/" + name, function (json) {
                        jmri.memory(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setMemory = function (name, value) {
                if (jmri.socket) {
                    jmri.socket.send("memory", { name: name, value: value }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "memory/" + name,
                        type: "POST",
                        data: JSON.stringify({ value: value }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.memory(json.data.name, json.data.value, json.data);
                            jmri.getMemory(json.data.name, json.data.value);
                        }
                    });
                }
            };
            jmri.getReporter = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("reporter", { name: name });
                } else {
                    $.getJSON(jmri.url + "reporter/" + name, function (json) {
                        jmri.reporter(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setReporter = function (name, value) {
                if (jmri.socket) {
                    jmri.socket.send("reporter", { name: name, value: value }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "reporter/" + name,
                        type: "POST",
                        data: JSON.stringify({ value: value }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.reporter(json.data.name, json.data.report, json.data);
                            jmri.getReporter(json.data.name, json.data.report);
                        }
                    });
                }
            };
            jmri.getBlock = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("block", { name: name });
                } else {
                    $.getJSON(jmri.url + "block/" + name, function (json) {
                        jmri.block(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setBlock = function (name, value) {
                if (jmri.socket) {
                    jmri.socket.send("block", { name: name, value: value }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "block/" + name,
                        type: "POST",
                        data: JSON.stringify({ value: value }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.block(json.data.name, json.data.value, json.data);
                            jmri.getBlock(json.data.name, json.data.value);
                        }
                    });
                }
            };
            jmri.getIdTag = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("idTag", { name: name });
                } else {
                    $.getJSON(jmri.url + "idTag/" + name, function (json) {
                        jmri.idTag(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setIdTag = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("idTag", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "idTag/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.idTag(json.data.name, json.data.state, json.data);
                            jmri.getIdTag(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getLayoutBlock = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("layoutBlock", { name: name });
                } else {
                    $.getJSON(jmri.url + "layoutBlock/" + name, function (json) {
                        jmri.layoutBlock(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setLayoutBlock = function (name, value) {
                if (jmri.socket) {
                    jmri.socket.send("layoutBlock", { name: name, value: value }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "layoutBlock/" + name,
                        type: "POST",
                        data: JSON.stringify({ value: value }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.layoutBlock(json.data.name, json.data.value, json.data);
                            jmri.getLayoutBlock(json.data.name, json.data.value);
                        }
                    });
                }
            };
            /**
             * Request a json list of the specified list type. Individual
             * listeners for each instance of the type will need to be set
             * up by the consuming client by requesting the state of each
             * item in the returned list individually.
             * @param {String} type of list (e.g. "sensors")
             */
            jmri.getList = function (name) {
                jmri.socket._send(JSON.stringify({ list: name }));
            };

            jmri.getObject = function (type, name) {
                switch (type) {
                    case "light":
                        jmri.getLight(name);
                        break;
                    case "block":
                        jmri.getBlock(name);
                        break;
                    case "idTag":
                        jmri.getIdTag(name);
                        break;
                    case "layoutBlock":
                        jmri.getLayoutBlock(name);
                        break;
                    case "memory":
                        jmri.getMemory(name);
                        break;
                    case "reporter":
                        jmri.getReporter(name);
                        break;
                    case "rosterEntry":
                        jmri.getRosterEntry(name);
                        break;
                    case "rosterGroup":
                        jmri.getRosterGroup(name);
                        break;
                    case "route":
                        jmri.getRoute(name);
                        break;
                    case "sensor":
                        jmri.getSensor(name);
                        break;
                    case "signalHead":
                        jmri.getSignalHead(name);
                        break;
                    case "signalMast":
                        jmri.getSignalMast(name);
                        break;
                    case "turnout":
                        jmri.getTurnout(name);
                        break;
                    default:
                        if (window.console) {
                            console.log("WARN-unknown type of " + type + " encountered by jquery.jmri.js in getObject().");
                        }

                }
            };
            jmri.setObject = function (type, name, state) {
                switch (type) {
                    case "light":
                        jmri.setLight(name, state, 'post');
                        break;
                    case "memory":
                        jmri.setMemory(name, state, 'post');
                        break;
                    case "reporter":
                        jmri.setReporter(name, report, 'post');
                        break;
                    case "block":
                        jmri.setBlock(name, state, 'post');
                        break;
                    case "idTag":
                        jmri.setIdTag(name, state, 'post');
                        break;
                    case "layoutBlock":
                        jmri.setLayoutBlock(name, state, 'post');
                        break;
                    case "rosterEntry":
                        jmri.setRosterEntry(name, state, 'post');
                        break;
                    case "route":
                        jmri.setRoute(name, state, 'post');
                        break;
                    case "sensor":
                        jmri.setSensor(name, state, 'post');
                        break;
                    case "signalHead":
                        jmri.setSignalHead(name, state, 'post');
                        break;
                    case "signalMast":
                        jmri.setSignalMast(name, state, 'post');
                        break;
                    case "turnout":
                        jmri.setTurnout(name, state, 'post');
                        break;
                    default:
                        if (window.console) {
                            console.log("WARN-unknown type of " + type + " encountered by jquery.jmri.js in setObject().");
                        }
                }
            };
            jmri.getPower = function () {
                if (jmri.socket) {
                    jmri.socket.send("power", {});
                } else {
                    $.getJSON(jmri.url + "power", function (json) {
                        jmri.power(json.data.state);
                    });
                }
            };
            jmri.setPower = function (state) {
                if (jmri.socket) {
                    jmri.socket.send("power", { state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "power",
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.power(json.data.state);
                        }
                    });
                }
            };
            jmri.getRosterGroup = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("rosterGroup", { name: name });
                } else {
                    $.getJSON(jmri.url + "rosterGroup/" + name, function (json) {
                        jmri.rosterGroup(json.data.name, json.data);
                    });
                }
            };
            jmri.getRosterEntry = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("rosterEntry", { name: name });
                } else {
                    $.getJSON(jmri.url + "rosterEntry/" + name, function (json) {
                        jmri.rosterEntry(json.data.name, json.data);
                    });
                }
            };
            jmri.getRoute = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("route", { name: name });
                } else {
                    $.getJSON(jmri.url + "route/" + name, function (json) {
                        jmri.route(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setRoute = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("route", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "route/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.route(json.data.name, json.data.state, json.data);
                            jmri.getRoute(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getSensor = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("sensor", { name: name });
                } else {
                    $.getJSON(jmri.url + "sensor/" + name, function (json) {
                        jmri.sensor(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSensor = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("sensor", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "sensor/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.sensor(json.data.name, json.data.state, json.data);
                            jmri.getSensor(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getSignalHead = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("signalHead", { name: name });
                } else {
                    $.getJSON(jmri.url + "signalHead/" + name, function (json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalHead = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalHead", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "signalHead/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.signalHead(json.data.name, json.data.state, json.data);
                            jmri.getSignalHead(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getSignalMast = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", { name: name });
                } else {
                    $.getJSON(jmri.url + "signalMast/" + name, function (json) {
                        jmri.signalMast(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalMast = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "signalMast/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.signalMast(json.data.name, json.data.state, json.data);
                            jmri.getSignalMast(json.data.name, json.data.state);
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
            jmri.getThrottle = function (throttle) {
                if (jmri.socket) {
                    jmri.socket.send("throttle", { throttle: throttle, status: true });
                    return true;
                } else {
                    return false;
                }
            };
            /**
             * Set some aspect of a throttle as defined in data
             *
             * Call this method with the data elements address:[dcc address]
             * or rosterEntry:[roster entry id] to create a JMRI throttle. Include the
             * data element status:true to get the complete throttle status.
             *
             * @param {string} throttle the throttle identity
             * @param {object} data key/value pairs of the throttle properties to change
             * @returns {boolean} false if unable to use throttles
             */
            jmri.setThrottle = function (throttle, data) {
                if (jmri.socket) {
                    data.throttle = throttle;
                    jmri.socket.send("throttle", data, 'post');
                    return true;
                } else {
                    return false;
                }
            };
            jmri.getTime = function () {
                if (jmri.socket) {
                    jmri.socket.send("time", {});
                } else {
                    $.getJSON(jmri.url + "time", function (json) {
                        jmri.time(json.data.time, json.data);
                    });
                }
            };
            jmri.getTrain = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("train", { name: name });
                } else {
                    $.getJSON(jmri.url + "train/" + name, function (json) {
                        jmri.train(json.data.name, json.data);
                    });
                }
            };
            jmri.getTurnout = function (name) {
                if (jmri.socket) {
                    jmri.socket.send("turnout", { name: name });
                } else {
                    $.getJSON(jmri.url + "turnout/" + name, function (json) {
                        jmri.turnout(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setTurnout = function (name, state) {
                if (jmri.socket) {
                    jmri.socket.send("turnout", { name: name, state: state }, 'post');
                } else {
                    $.ajax({
                        url: jmri.url + "turnout/" + name,
                        type: "POST",
                        data: JSON.stringify({ state: state }),
                        contentType: "application/json; charset=utf-8",
                        success: function (json) {
                            jmri.turnout(json.data.name, json.data.state, json.data);
                            jmri.getTurnout(json.data.name, json.data.state);
                        }
                    });
                }
            };
            /**
             * Force the jmri object to begin communicating with the JMRI server
             * even if the WebSocket connection cannot be immediately established
             *
             * @returns {undefined}
             */
            jmri.connect = function () {
                // if the JMRI WebSocket was open before we overloaded the
                // open() method, we call the open() method to ensure it gets
                // called
                if (jmri.socket && jmri.socket.readyState === 1) {
                    jmri.log("Connecting on connect()");
                    jmri.open();
                } else {
                    // if the JMRI WebSocket was not open when the document was
                    // ready, wait one second and call open() if the socket
                    // did not open in the meantime -- with the exception of
                    // throttles, the JMRI object can work around the inability
                    // to use WebSockets
                    setTimeout(function () {
                        if (!jmri.socket || jmri.socket.readyState !== 1) {
                            jmri.log("Connecting on timeout");
                            jmri.open();
                        }
                    }, 1000);
                }
            };
            // Logging
            // Object unique identity - an eight digit hexidecimal number
            jmri.serialNumber = (Math.random().toString(16) + "000000000").substr(2, 8);
            jmri.logWithDateTimeStamp = false;
            jmri.log = function (message) {
                if (window.console) {
                    if (jmri.logWithDateTimeStamp) {
                        window.console.log(new Date().toJSON() + " " + jmri.serialNumber + " " + message);
                    } else {
                        window.console.log(jmri.serialNumber + " " + message);
                    }
                }
            };
            // Heartbeat
            jmri.heartbeat = function () {
                jmri.socket.send("ping");
                jmri.ping();
            };
            jmri.heartbeatInterval = null;
            // WebSocket
            jmri.reconnectAttempts = 0;
            jmri.reconnectPoller = null;
            jmri.reconnectDelay = 0;
            jmri.reconnectPolls = 0;
            jmri.attemptReconnection = function () {
                if (jmri.reconnectAttempts < 20) {
                    jmri.reconnectAttempts++;
                    jmri.reconnectDelay = 15000 * jmri.reconnectAttempts;
                    jmri.willReconnect(jmri.reconnectAttempts, jmri.reconnectDelay);
                    jmri.log("Reconnecting WebSocket (attempt " + jmri.reconnectAttempts + "/20)");
                    setTimeout(
                        function () {
                            if (jmri.reconnectAttempts === 1) {
                                jmri.log("Reconnecting from closed connection.");
                            } else {
                                jmri.log("Reconnecting from failed reconnection attempt.");
                            }
                            jmri.reconnect();
                        }, jmri.reconnectDelay);
                } else {
                    jmri.failedReconnect();
                }
            };
            //set of functions for handling each "type" of json message
            jmri.events = {
                // TODO: add panel and programmer-related events
                error: function (e) {
                    jmri.log("Error " + e.data.code + ": " + e.data.message);
                    jmri.error(e.data);
                },
                goodbye: function (e) {
                    jmri.goodbye(e.data);
                },
                // handle the initial handshake response from the server
                hello: function (e) {
                    if (jmri.reconnectAttempts !== 0) {
                        jmri.reconnectAttempts = 0;
                        jmri.didReconnect();
                    }
                    jmri.heartbeatInterval = setInterval(jmri.heartbeat, e.data.heartbeat);
                    jmri.version(e.data.JMRI);
                    jmri.railroad(e.data.railroad);
                    jmri.hello(e.data);
                },
                pong: function (e) {
                    jmri.pong();
                },
                block: function (e) {
                    jmri.block(e.data.name, e.data.value, e.data);
                },
                blocks: function (e) {
                    jmri.blocks(e.data);
                },
                car: function (e) {
                    jmri.car(e.data.name, e.data);
                },
                cars: function (e) {
                    jmri.cars(e.data);
                },
                configProfile: function (e) {
                    jmri.configProfile(e.data.name, e.data);
                },
                configProfiles: function (e) {
                    jmri.configProfiles(e.data);
                },
                consist: function (e) {
                    jmri.consist(e.data.name, e.data);
                },
                consists: function (e) {
                    jmri.consists(e.data);
                },
                engine: function (e) {
                    jmri.engine(e.data.name, e.data);
                },
                engines: function (e) {
                    jmri.engines(e.data);
                },
                idTag: function (e) {
                    jmri.idTag(e.data.name, e.data.state, e.data);
                },
                idTags: function (e) {
                    jmri.idTags(e.data);
                },
                layoutBlock: function (e) {
                    jmri.layoutBlock(e.data.name, e.data.value, e.data);
                },
                layoutBlocks: function (e) {
                    jmri.layoutBlocks(e.data);
                },
                light: function (e) {
                    jmri.light(e.data.name, e.data.state, e.data);
                },
                lights: function (e) {
                    jmri.lights(e.data);
                },
                location: function (e) {
                    jmri.location(e.data.name, e.data);
                },
                locations: function (e) {
                    jmri.locations(e.data);
                },
                memory: function (e) {
                    jmri.memory(e.data.name, e.data.value, e.data);
                },
                memories: function (e) {
                    jmri.memories(e.data);
                },
                metadata: function (e) {
                    jmri.metadata(e.data);
                },
                networkService: function (e) {
                    jmri.networkService(e.data.name, e.data);
                },
                networkServices: function (e) {
                    jmri.networkServices(e.data);
                },
                panel: function (e) {
                    jmri.panel(e.data.name, e.data.value, e.data);
                },
                panels: function (e) {
                    jmri.panels(e.data);
                },
                power: function (e) {
                    jmri.power(e.data.state);
                },
                reporter: function (e) {
                    jmri.reporter(e.data.name, e.data.value, e.data);
                },
                reporters: function (e) {
                    jmri.reporters(e.data);
                },
                rosterEntry: function (e) {
                    jmri.rosterEntry(e.data.name, e.data);
                },
                roster: function (e) {
                    jmri.roster(e.data);
                },
                rosterGroup: function (e) {
                    jmri.rosterGroup(e.data.name, e.data);
                },
                rosterGroups: function (e) {
                    jmri.rosterGroups(e.data);
                },
                route: function (e) {
                    jmri.route(e.data.name, e.data.state, e.data);
                },
                routes: function (e) {
                    jmri.routes(e.data);
                },
                sensor: function (e) {
                    jmri.sensor(e.data.name, e.data.state, e.data);
                },
                sensors: function (e) {
                    jmri.sensors(e.data);
                },
                signalHead: function (e) {
                    jmri.signalHead(e.data.name, e.data.state, e.data);
                },
                signalHeads: function (e) {
                    jmri.signalHeads(e.data);
                },
                signalMast: function (e) {
                    jmri.signalMast(e.data.name, e.data.state, e.data);
                },
                signalMasts: function (e) {
                    jmri.signalMasts(e.data);
                },
                systemConnection: function (e) {
                    jmri.systemConnection(e.data.name, e.data);
                },
                systemConnections: function (e) {
                    jmri.systemConnections(e.data);
                },
                throttle: function (e) {
                    jmri.throttle(e.data.throttle, e.data);
                },
                time: function (e) {
                    jmri.time(e.data.time, e.data);
                },
                train: function (e) {
                    jmri.train(e.data.name, e.data);
                },
                trains: function (e) {
                    jmri.trains(e.data);
                },
                turnout: function (e) {
                    jmri.turnout(e.data.name, e.data.state, e.data);
                },
                turnouts: function (e) {
                    jmri.turnouts(e.data);
                }
            }

            jmri.reconnect = function () {
                jmri.socket = $.websocket(jmri.url.replace(/^http/, "ws"), {
                    open: function () {
                        jmri.log("Opened WebSocket");
                        jmri.open();
                    },
                    // stop the heartbeat when the socket closes
                    close: function (e) {
                        jmri.log("Closed WebSocket " + ((e.wasClean) ? "cleanly" : "unexpectedly") + " (" + e.code + "): " + e.reason);
                        clearInterval(jmri.heartbeatInterval);
                        jmri.socket.close();
                        jmri.socket = null;
                        jmri.close(e);
                        jmri.attemptReconnection();
                    },
                    message: function (e) {
                        jmri.console(e.originalEvent.data);
                        //determine message type and call appropriate event handler
                        var m = JSON.parse(e.originalEvent.data);

                        if ($.isArray(m)) {
                            m.forEach(o => {
                                h = jmri.events[o.type];
                                if (h) {
                                    h.call(this, o);
                                } else if (!o.type) {
                                    jmri.log("ERROR: missing type property in " + o);
                                } else if (!h) {
                                    jmri.log("Ignoring JSON type ", o.type);
                                }
                            })
                        } else {
                            h = jmri.events[m.type];
                            if (h) {
                                h.call(this, m);
                            } else if (!m.type) {
                                jmri.log("ERROR: missing type property in " + m);
                            } else if (!h) {
                                jmri.log("Ignoring JSON type ", m.type);
                            }
                        }
                    }
                });
            };
            jmri.reconnect();
            if (jmri.socket === null) {
                $("#no-websockets").addClass("show").removeClass("hidden");
            }
            $(window).unload(function () {
                if (jmri.socket != null) {
                    jmri.socket.close();
                }
                jmri.socket = null;
                jmri = null;
            });
            return jmri;
        }
    });
})(jQuery);
