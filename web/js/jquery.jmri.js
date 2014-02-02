(function($) {
    $.extend({
        JMRI: function(url, bindings) {
            var jmri = new Object();
            // monitoring is a list of open get* requests for JMRI layout objects
            // monitoring[name] is true if long polling request is open, and is
            // false if no request is open, or if WebSockets are in use
            jmri.monitoring = new Object();
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
            jmri.getLight = function(name, state) {
                if (jmri.monitoring[name]) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("light", {name: name});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring[name] = true;
                    $.getJSON(jmri.url + "light/" + name + "?state=" + state, function(json) {
                        jmri.monitoring[json.data.name] = false;
                        jmri.light(json.data.name, json.data.state, json.data);
                        jmri.getLight(json.data.name, json.data.state);
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
            jmri.getObject = function(type, name) {
                switch (type) {
                    case "light":
                        jmri.getLight(name);
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
            jmri.getPower = function(state) {
                if (jmri.monitoring.power) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("power", {});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring.power = true;
                    $.getJSON(jmri.url + "power?state=" + state, function(json) {
                        jmri.monitoring.power = false;
                        jmri.power(json.data.state);
                        jmri.getPower(json.data.state);
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
                            jmri.getPower(json.data.state);
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
            jmri.getRoute = function(name, state) {
                if (jmri.monitoring[name]) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("route", {name: name});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring[name] = true;
                    $.getJSON(jmri.url + "route/" + name + "?state=" + state, function(json) {
                        jmri.monitoring[json.data.name] = false;
                        jmri.route(json.data.name, json.data.state, json.data);
                        jmri.getRoute(json.data.name, json.data.state);
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
            jmri.getSensor = function(name, state) {
                if (jmri.monitoring[name]) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("sensor", {name: name});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring[name] = true;
                    $.getJSON(jmri.url + "sensor/" + name + "?state=" + state, function(json) {
                        jmri.monitoring[json.data.name] = false;
                        jmri.sensor(json.data.name, json.data.state, json.data);
                        jmri.getSensor(json.data.name, json.data.state);
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
            jmri.getSignalHead = function(name, state) {
                if (jmri.monitoring[name]) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("signalHead", {name: name});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring[name] = true;
                    $.getJSON(jmri.url + "signalHead/" + name + "?state=" + state, function(json) {
                        jmri.monitoring[json.data.name] = false;
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                        jmri.getSignalHead(json.data.name, json.data.state);
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
            jmri.getSignalMast = function(name, state) {
                if (jmri.monitoring[name]) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("signalMast", {name: name});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring[name] = true;
                    $.getJSON(jmri.url + "signalMast/" + name + "?state=" + state, function(json) {
                        jmri.monitoring[json.data.name] = false;
                        jmri.signalMast(json.data.name, json.data.state, json.data);
                        jmri.getSignalMast(json.data.name, json.data.state);
                    });
                }
            };
            jmri.setSignalMast = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", {name: name, aspect: state});
                } else {
                    $.ajax({
                        url: jmri.url + "signalMast/" + name,
                        type: "POST",
                        data: JSON.stringify({aspect: state}),
                        contentType: "application/json; charset=utf-8",
                        success: function(json) {
                            jmri.signalMast(json.data.name, json.data.state, json.data);
                            jmri.getSignalMast(json.data.name, json.data.state);
                        }
                    });
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
            jmri.getTurnout = function(name, state) {
                if (jmri.monitoring[name]) {
                    return;
                }
                if (jmri.socket) {
                    jmri.socket.send("turnout", {name: name});
                } else {
                    state = state || jmri.UNKNOWN;
                    jmri.monitoring[name] = true;
                    $.getJSON(jmri.url + "turnout/" + name + "?state=" + state, function(json) {
                        jmri.monitoring[json.data.name] = false;
                        jmri.turnout(json.data.name, json.data.state, json.data);
                        jmri.getTurnout(json.data.name, json.data.state);
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
            if (jmri.socket === null) {
                $("#no-websockets").addClass("show").removeClass("hidden");
            }
            return jmri;
        }
    });
})(jQuery);
