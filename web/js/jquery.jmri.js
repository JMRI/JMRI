/**
 * JMRI JSON protocol abstract client.
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
 * light(name, state, data)
 * memory(name, value, data)
 * block(name, value, data)
 * layoutBlock(name, value, data)
 * power(state)
 * railroad(name)
 * reporter(name, value, data)
 * rosterEntry(id, data)
 * route(name, state, data)
 * sensor(name, state, data)
 * signalHead(name, state, data)
 * signalMast(name, state, data)
 * throttle(id, data)
 * time(time, data)
 * train(id, data)
 * turnout(name, state, data)
 * version(version)
 * as demonstrated in the power.html demonstration web app
 * 
 * @author Copyright (C) Randall Wood 2013, 2014
 * @param {a jQuery object} $
 * @returns {a JMRI object}
 */
(function($) {
    $.extend({
        JMRI: function(url, bindings) {
            var jmri = new Object();
            if (typeof (url) === 'string') {
                jmri.url = url;
            } else {
                jmri.url = document.URL.split('/')[0] + "//" + document.URL.split('/')[2] + "/json/";
                bindings = url;
            }
            // Default event handlers that do nothing
            jmri.console = function(data) {
            };
            jmri.error = function(error) {
            };
            jmri.open = function() {
            };
            jmri.close = function(event) {
            };
            jmri.willReconnect = function(attempts, milliseconds) {
            };
            jmri.didReconnect = function() {
            };
            jmri.failedReconnect = function() {
            };
            jmri.ping = function() {
            };
            jmri.pong = function() {
            };
            jmri.hello = function(data) {
            };
            jmri.goodbye = function(data) {
            };
            jmri.light = function(name, state, data) {
            };
            jmri.memory = function(name, value, data) {
            };
            jmri.block = function(name, value, data) {
            };
            jmri.layoutBlock = function(name, value, data) {
            };
            jmri.power = function(state) {
            };
            jmri.railroad = function(name) {
            };
            jmri.reporter = function(name, value, data) {
            };
            jmri.rosterEntry = function(id, data) {
            };
            jmri.route = function(name, state, data) {
            };
            jmri.sensor = function(name, state, data) {
            };
            jmri.signalHead = function(name, state, data) {
            };
            jmri.signalMast = function(name, state, data) {
            };
            jmri.throttle = function(throttle, data) {
            };
            jmri.time = function(time, data) {
            };
            jmri.train = function(id, data) {
            };
            jmri.turnout = function(name, state, data) {
            };
            jmri.version = function(string) {
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
            // Getters and Setters
            jmri.getLight = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("light", {name: name});
                } else {
                    $.getJSON(jmri.url + "light/" + name, function(json) {
                        jmri.light(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setLight = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("light", {name: name, state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "light/" + name,
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.light(json.data.name, json.data.state, json.data);
                            jmri.getLight(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getMemory = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("memory", {name: name});
                } else {
                    $.getJSON(jmri.url + "memory/" + name, function(json) {
                        jmri.memory(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setMemory = function(name, value) {
                if (jmri.socket) {
                    jmri.socket.send("memory", {name: name, value: value});
                } else {
                    $.ajax({
                        url: jmri.url + "memory/" + name,
                        type: "POST",
                        data: JSON.stringify({value: value}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.memory(json.data.name, json.data.value, json.data);
                            jmri.getMemory(json.data.name, json.data.value);
                        }
                    });
                }
            };
            jmri.getBlock = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("block", {name: name});
                } else {
                    $.getJSON(jmri.url + "block/" + name, function(json) {
                        jmri.block(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setBlock = function(name, value) {
                if (jmri.socket) {
                    jmri.socket.send("block", {name: name, value: value});
                } else {
                    $.ajax({
                        url: jmri.url + "block/" + name,
                        type: "POST",
                        data: JSON.stringify({value: value}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.block(json.data.name, json.data.value, json.data);
                            jmri.getBlock(json.data.name, json.data.value);
                        }
                    });
                }
            };
            jmri.getLayoutBlock = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("layoutBlock", {name: name});
                } else {
                    $.getJSON(jmri.url + "layoutBlock/" + name, function(json) {
                        jmri.layoutBlock(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.setLayoutBlock = function(name, value) {
                if (jmri.socket) {
                    jmri.socket.send("layoutBlock", {name: name, value: value});
                } else {
                    $.ajax({
                        url: jmri.url + "layoutBlock/" + name,
                        type: "POST",
                        data: JSON.stringify({value: value}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.layoutBlock(json.data.name, json.data.value, json.data);
                            jmri.getLayoutBlock(json.data.name, json.data.value);
                        }
                    });
                }
            };
            jmri.getObject = function(type, name) {
                switch (type) {
                    case "light":
                        jmri.getLight(name);
                        break;
                    case "block":
                        jmri.getBlock(name);
                        break;
                    case "layoutBlock":
                        jmri.getLayoutBlock(name);
                        break;
                    case "memory":
                        jmri.getMemory(name);
                        break;
                    case "rosterEntry":
                        jmri.getRosterEntry(name);
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
                }
            };
            jmri.setObject = function(type, name, state) {
                switch (type) {
                    case "light":
                        jmri.setLight(name, state);
                        break;
                    case "memory":
                        jmri.setMemory(name, state);
                        break;
                    case "block":
                        jmri.setBlock(name, state);
                        break;
                    case "layoutBlock":
                        jmri.setLayoutBlock(name, state);
                        break;
                    case "rosterEntry":
                        jmri.setRosterEntry(name, state);
                        break;
                    case "route":
                        jmri.setRoute(name, state);
                        break;
                    case "sensor":
                        jmri.setSensor(name, state);
                        break;
                    case "signalHead":
                        jmri.setSignalHead(name, state);
                        break;
                    case "signalMast":
                        jmri.setSignalMast(name, state);
                        break;
                    case "turnout":
                        jmri.setTurnout(name, state);
                        break;
                }
            };
            jmri.getPower = function() {
                if (jmri.socket) {
                    jmri.socket.send("power", {});
                } else {
                    $.getJSON(jmri.url + "power", function(json) {
                        jmri.power(json.data.state);
                    });
                }
            };
            jmri.setPower = function(state) {
                if (jmri.socket) {
                    jmri.socket.send("power", {state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "power",
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.power(json.data.state);
                        }
                    });
                }
            };
            jmri.getRosterEntry = function(id) {
                if (jmri.socket) {
                    jmri.socket.send("rosterEntry", {name: id});
                } else {
                    $.getJSON(jmri.url + "rosterEntry/" + id, function(json) {
                        jmri.rosterEntry(json.data.name, json.data);
                    });
                }
            };
            jmri.getRoute = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("route", {name: name});
                } else {
                    $.getJSON(jmri.url + "route/" + name, function(json) {
                        jmri.route(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setRoute = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("route", {name: name, state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "route/" + name,
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.route(json.data.name, json.data.state, json.data);
                            jmri.getRoute(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getSensor = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("sensor", {name: name});
                } else {
                    $.getJSON(jmri.url + "sensor/" + name, function(json) {
                        jmri.sensor(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSensor = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("sensor", {name: name, state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "sensor/" + name,
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.sensor(json.data.name, json.data.state, json.data);
                            jmri.getSensor(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getSignalHead = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("signalHead", {name: name});
                } else {
                    $.getJSON(jmri.url + "signalHead/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalHead = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalHead", {name: name, state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "signalHead/" + name,
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.signalHead(json.data.name, json.data.state, json.data);
                            jmri.getSignalHead(json.data.name, json.data.state);
                        }
                    });
                }
            };
            jmri.getSignalMast = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", {name: name});
                } else {
                    $.getJSON(jmri.url + "signalMast/" + name, function(json) {
                        jmri.signalMast(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalMast = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", {name: name, state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "signalMast/" + name,
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
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
            jmri.getThrottle = function(throttle) {
                if (jmri.socket) {
                    jmri.socket.send("throttle", {throttle: throttle, status: true});
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
            jmri.setThrottle = function(throttle, data) {
                if (jmri.socket) {
                    data.throttle = throttle;
                    jmri.socket.send("throttle", data);
                    return true;
                } else {
                    return false;
                }
            };
            jmri.getTime = function() {
                if (jmri.socket) {
                    jmri.socket.send("time", {});
                } else {
                    $.getJSON(jmri.url + "time", function(json) {
                        jmri.time(json.data.time, json.data);
                    });
                }
            };
            jmri.getTrain = function(id) {
                if (jmri.socket) {
                    jmri.socket.send("train", {id: id});
                } else {
                    $.getJSON(jmri.url + "train/" + id, function(json) {
                        jmri.train(json.data.id, json.data);
                    });
                }
            };
            jmri.getTurnout = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("turnout", {name: name});
                } else {
                    $.getJSON(jmri.url + "turnout/" + name, function(json) {
                        jmri.turnout(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setTurnout = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("turnout", {name: name, state: state});
                } else {
                    $.ajax({
                        url: jmri.url + "turnout/" + name,
                        type: "POST",
                        data: JSON.stringify({state: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
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
            jmri.connect = function() {
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
                    setTimeout(function() {
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
            jmri.log = function(message) {
                if (window.console) {
                    if (jmri.logWithDateTimeStamp) {
                        window.console.log(new Date().toJSON() + " " + jmri.serialNumber + " " + message);
                    } else {
                        window.console.log(jmri.serialNumber + " " + message);
                    }
                }
            };
            // Heartbeat
            jmri.heartbeat = function() {
                jmri.socket.send("ping");
                jmri.ping();
            };
            jmri.heartbeatInterval = null;
            // WebSocket
            jmri.reconnectAttempts = 0;
            jmri.reconnectPoller = null;
            jmri.reconnectDelay = 0;
            jmri.reconnectPolls = 0;
            jmri.attemptReconnection = function() {
                if (jmri.reconnectAttempts < 20) {
                    jmri.reconnectAttempts++;
                    jmri.reconnectDelay = 15000 * jmri.reconnectAttempts;
                    jmri.willReconnect(jmri.reconnectAttempts, jmri.reconnectDelay);
                    jmri.log("Reconnecting WebSocket (attempt " + jmri.reconnectAttempts + "/20)");
                    setTimeout(
                            function() {
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
            jmri.reconnect = function() {
                jmri.socket = $.websocket(jmri.url.replace(/^http/, "ws"), {
                    open: function() {
                        jmri.log("Opened WebSocket");
                        jmri.open();
                    },
                    // stop the heartbeat when the socket closes
                    close: function(e) {
                        jmri.log("Closed WebSocket " + ((e.wasClean) ? "cleanly" : "unexpectedly") + " (" + e.code + "): " + e.reason);
                        clearInterval(jmri.heartbeatInterval);
                        jmri.socket.close();
                        jmri.socket = null;
                        jmri.close(e);
                        jmri.attemptReconnection();
                    },
                    message: function(e) {
                        jmri.console(e.originalEvent.data);
                    },
                    events: {
                        // TODO: add consist, programmer, and operations-related events
                        error: function(e) {
                            jmri.log("Error " + e.data.code + ": " + e.data.message);
                            jmri.error(e.data);
                        },
                        goodbye: function(e) {
                            jmri.goodbye(e.data);
                        },
                        // handle the initial handshake response from the server
                        hello: function(e) {
                            if (jmri.reconnectAttempts !== 0) {
                                jmri.reconnectAttempts = 0;
                                jmri.didReconnect();
                            }
                            jmri.heartbeatInterval = setInterval(jmri.heartbeat, e.data.heartbeat);
                            jmri.version(e.data.JMRI);
                            jmri.railroad(e.data.railroad);
                            jmri.hello(e.data);
                        },
                        light: function(e) {
                            jmri.light(e.data.name, e.data.state, e.data);
                        },
                        block: function(e) {
                            jmri.block(e.data.name, e.data.value, e.data);
                        },
                        layoutBlock: function(e) {
                            jmri.layoutBlock(e.data.name, e.data.value, e.data);
                        },
                        memory: function(e) {
                            jmri.memory(e.data.name, e.data.value, e.data);
                        },
                        power: function(e) {
                            jmri.power(e.data.state);
                        },
                        reporter: function(e) {
                            jmri.reporter(e.data.name, e.data.value, e.data);
                        },
                        route: function(e) {
                            jmri.route(e.data.name, e.data.state, e.data);
                        },
                        sensor: function(e) {
                            jmri.sensor(e.data.name, e.data.state, e.data);
                        },
                        signalHead: function(e) {
                            jmri.signalHead(e.data.name, e.data.state, e.data);
                        },
                        signalMast: function(e) {
                            jmri.signalMast(e.data.name, e.data.state, e.data);
                        },
                        throttle: function(e) {
                            jmri.throttle(e.data.throttle, e.data);
                        },
                        time: function(e) {
                            jmri.time(e.data.time, e.data);
                        },
                        train: function(e) {
                            jmri.train(e.data.id, e.data);
                        },
                        turnout: function(e) {
                            jmri.turnout(e.data.name, e.data.state, e.data);
                        },
                        pong: function(e) {
                            jmri.pong();
                        }
                    }
                });
            };
            jmri.reconnect();
            if (jmri.socket === null) {
                $("#no-websockets").addClass("show").removeClass("hidden");
            }
            $(window).unload(function() {
                jmri.socket.close();
                jmri.socket = null;
                jmri = null;
            });
            return jmri;
        }
    });
})(jQuery);
