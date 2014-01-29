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
            jmri.light = function(name, state, data) {
            };
            jmri.memory = function(name, value, data) {
            };
            jmri.power = function(state) {
            };
            jmri.railroad = function(string) {
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
            jmri.throttle = function(id, data) {
            };
            jmri.time = function(time, data) {
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
            // Getters and Setters
            jmri.getLight = function(name) {
                if (!jmri.setLight(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "lights/" + name, function(json) {
                        jmri.light(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setLight = function(name, state) {
                return jmri.socket.send("light", {name: name, state: state});
            };
            jmri.getMemory = function(name) {
                if (!jmri.socket.send("memory", {name: name})) {
                    $.getJSON(jmri.url + "memories/" + name, function(json) {
                        jmri.memory(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.getPower = function() {
                if (!jmri.setPower(jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "power", function(json) {
                        jmri.power(json.data.state);
                    });
                }
            };
            jmri.setPower = function(state) {
                return jmri.socket.send("power", {state: state});
            };
            jmri.getRosterEntry = function(id) {
                if (!jmri.socket.send("rosterEntry", {name: id})) {
                    $.getJSON(jmri.url + "rosterEntry/" + id, function(json) {
                        jmri.rosterEntry(json.data.name, json.data);
                    });
                }
            };
            jmri.getRoute = function(name) {
                if (!jmri.setSensor(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "routes/" + name, function(json) {
                        jmri.sensor(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setRoute = function(name, state) {
                return jmri.socket.send("route", {name: name, state: state});
            };
            jmri.getSensor = function(name) {
                if (!jmri.setSensor(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "sensors/" + name, function(json) {
                        jmri.sensor(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSensor = function(name, state) {
                return jmri.socket.send("sensor", {name: name, state: state});
            };
            jmri.getSignalHead = function(name) {
                if (!jmri.setSignalHead(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "signalHeads/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalHead = function(name, state) {
                return jmri.socket.send("signalHead", {name: name, state: state});
            };
            jmri.getSignalMast = function(name) {
                if (!jmri.setSignalMast(name, "")) {
                    $.getJSON(jmri.url + "signalMasts/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalMast = function(name, state) {
                return jmri.socket.send("signalHead", {name: name, state: state});
            };
            jmri.getTime = function(name) {
                if (!jmri.socket.send("time", {})) {
                    $.getJSON(jmri.url + "time", function(json) {
                        jmri.time(json.data.time, json.data);
                    });
                }
            };
            jmri.getTurnout = function(name) {
                if (!jmri.setTurnout(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "turnouts/" + name, function(json) {
                        jmri.turnout(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setTurnout = function(name, state) {
                return jmri.socket.send("turnout", {name: name, state: state});
            };
            // Heartbeat
            jmri.heartbeat = function() {
                jmri.socket.send("ping");
            };
            jmri.heartbeatInterval = null;
            // WebSocket
            jmri.socket = $.websocket(jmri.url.replace(/^http/, "ws"), {
                // stop the heartbeat when the socket closes
                close: function() {
                    clearInterval(jmri.heartbeat);
                },
                message: function(e) {
                    jmri.console(e.originalEvent.data);
                },
                events: {
                    // TODO: add constist, programmer, and operations events
                    error: function(e) {
                        jmri.error(e.data);
                    },
                    goodbye: function(e) {
                        jmri.socket.close();
                    },
                    // handle the initial handshake response from the server
                    hello: function(e) {
                        jmri.heartbeatInterval = setInterval(jmri.heartbeat, e.data.heartbeat);
                        jmri.version(e.data.JMRI);
                        jmri.railroad(e.data.railroad);
                    },
                    light: function(e) {
                        jmri.light(e.data.name, e.data.state, e.data);
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
                    turnout: function(e) {
                        jmri.turnout(e.data.name, e.data.state, e.data);
                    }
                }
            });
            return jmri;
        }
    });
})(jQuery);
