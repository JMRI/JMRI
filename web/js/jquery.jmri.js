(function($) {
    $.extend({
        JMRI: function(url, bindings) {
            var jmri = new Object();
            if (typeof(url) === 'string') {
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
            jmri.light = function(name, state) {
            };
            jmri.power = function(state) {
            };
            jmri.railroad = function(string) {
            };
            jmri.reporter = function(name, value) {
            };
            jmri.sensor = function(name, state) {
            };
            jmri.turnout = function(name, state) {
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
                    $.getJSON(jmri.url + "light/" + name, function(json) {
                        jmri.light(json.data.name, json.data.state);
                    });
                }
            };
            jmri.setLight = function(name, state) {
                return jmri.socket.send("light", {name: name, state: state});
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
            jmri.getSensor = function(name) {
                if (!jmri.setSensor(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "sensor/" + name, function(json) {
                        jmri.sensor(json.data.name, json.data.state);
                    });
                }
            };
            jmri.setSensor = function(name, state) {
                return jmri.socket.send("sensor", {name: name, state: state});
            };
            jmri.getTurnout = function(name) {
                if (!jmri.setTurnout(name, jmri.UNKNOWN)) {
                    $.getJSON(jmri.url + "turnout/" + name, function(json) {
                        jmri.turnout(json.data.name, json.data.state);
                    });
                }
            };
            jmri.setTurnout = function(name, state) {
                return jmri.socket.send("turnout", {name: name, state: state});
            };
            // Heartbeat
            jmri.heartbeat = function() {
            };
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
                    error: function(e) {
                        jmri.error(e.data);
                    },
                    goodbye: function(e) {
                        jmri.socket.close();
                    },
                    // handle the initial handshake response from the server
                    hello: function(e) {
                        jmri.heartbeat = setInterval(function() {
                            jmri.socket._send("*");
                        }, e.data.heartbeat);
                        jmri.version(e.data.JMRI);
                        jmri.railroad(e.data.railroad);
                    },
                    light: function(e) {
                        jmri.light(e.data.name, e.data.state);
                    },
                    // most events just call the user-defined handler
                    power: function(e) {
                        jmri.power(e.data.state);
                    }
                }
            });
            return jmri;
        }
    });
})(jQuery);
