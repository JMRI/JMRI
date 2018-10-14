/**
 * JMRI JSON Console javascript
 *
 * Demonstration of how the JMRI extension to jQuery can be used.
 * Note that the JMRI extension relies on the jquery-websocket and jquery2 extensions.
 */
var jmri = null;
var power = 0;

$(document).ready(function() {
    jmri = $.JMRI({
        railroad: function(string) {
            $("#alert-websocket-connecting").addClass("hidden").removeClass("show");
            jmri.getPower();
        },
        console: function(data) {
            if (data !== '{"type":"pong"}') {
                $("#error-alert").addClass("hidden").removeClass("show");
                var console = $("#console pre");
                if (window.localStorage.getItem("jmri.json-console.json.pretty-print") === "true") {
                    console.append(JSON.stringify(JSON.parse(data), null, 2) + "<br>");
                } else {
                    console.append(data + "<br>");
                }
                console.scrollTop(console[0].scrollHeight - console.height());
            }
        },
        error: function(error) {
            $("#error-alert div").text(error.message);
            $("#error-alert").addClass("show").removeClass("hidden");
        },
        power: function(state) {
            power = state;
            switch (power) {
                case jmri.UNKNOWN:
                    $('#powerImg').prop('src', "/images/PowerGrey.png");
                    break;
                case jmri.POWER_ON:
                    $('#powerImg').prop('src', "/images/PowerGreen.png");
                    break;
                case jmri.POWER_OFF:
                    $('#powerImg').prop('src', "/images/PowerRed.png");
                    break;
            }
        },
        ping: function() {
            $("#sendCmd").addClass("btn-info").removeClass("btn-primary");
        },
        pong: function() {
            $("#sendCmd").addClass("btn-success").removeClass("btn-info");
            window.setTimeout(
                    function() {
                        $("#sendCmd").removeClass("btn-success").addClass("btn-primary");
                    },
                    1000);
            jmri.log("Heartbeat response received.");
        },
        /*
         * Hide the reconnecting dialog on a successful automatic reconnection.
         */
        didReconnect: function() {
            $('#modal-websocket-reconnecting').modal("hide");
        },
        /*
         * Show a reconnecting dialog if a websocket fails. This dialog, defined
         * in NavBar.html, will be maintained until a successful reconnection or
         * user's manual action to dismiss.
         */
        willReconnect: function(attempts, milliseconds) {
            $('#modal-websocket-reconnecting').modal("show");
            $('#modal-websocket-reconnecting-attempts').text(attempts);
            trigger = milliseconds;
            if (milliseconds >= 120000) {
                $('#modal-websocket-reconnecting-next').text("in " + parseInt(milliseconds / 60000) + " minutes");
                milliseconds = milliseconds - 60000;
                trigger = 60000;
            } else if (milliseconds >= 70000) {
                $('#modal-websocket-reconnecting-next').text("in 1 minute");
                milliseconds = milliseconds - 10000;
                trigger = 10000;
            } else if (milliseconds >= 60000) {
                $('#modal-websocket-reconnecting-next').text("in 1 minute");
                milliseconds = milliseconds - 10000;
                trigger = 1000;
            } else if (milliseconds >= 2000) {
                $('#modal-websocket-reconnecting-next').text("in " + parseInt(milliseconds / 1000) + " seconds");
                milliseconds = milliseconds - 1000;
                trigger = 1000;
            } else if (milliseconds >= 1000) {
                $('#modal-websocket-reconnecting-next').text("in 1 second");
                milliseconds = milliseconds - 1000;
                trigger = 1000;
            } else {
                $('#modal-websocket-reconnecting-next').text("now");
                trigger = -1;
            }
            if (trigger > 0) {
                setTimeout(
                        function() {
                            /* Change "jmri" to the name of your JMRI object */
                            jmri.willReconnect(attempts, milliseconds);
                        }, trigger);
            }
        },
        /*
         * Modify the reconnecting dialog if a successful reconnection did not
         * happen within an hour of the initial failure.
         */
        failedReconnect: function() {
            $('#modal-websocket-reconnecting-attempt').removeClass("show").addClass("hide");
            $('#modal-websocket-reconnecting-failed').removeClass("hide").addClass("show");
            $('#modal-websocket-reconnecting-now').removeClass("show").addClass("hide");
            $('#modal-websocket-reconnecting-reload').removeClass("btn-danger").addClass("btn-primary");
        }
    });
    /*
     * Enable the reconnect now button in the reconnecting dialog.
     */
    $('#modal-websocket-reconnecting-now').click(function(event) {
        nbJmri.reconnect();
        $('#modal-websocket-reconnecting').addClass('hide').removeClass('show');
    });
    /*
     * Enable the reload page button in the reconnecting dialog.
     */
    $('#modal-websocket-reconnecting-reload').click(function(event) {
        location.reload(false);
        $('#modal-websocket-reconnecting').addClass('hide').removeClass('show');
    });
    $('input#clearConsole').click(function() {
        $("#error-alert").addClass("hidden").removeClass("show");
        $('div#console pre').empty(); //clear the console
        return false;
    });
    $('input#disconnect').click(function() {
        jmri.socket.send("goodbye", {});
        return false;
    });
    $('input#sendCmd').click(function() {
        jmri.socket._send($('input#command').val());
        return false;
    });
    $('input#command').keypress(function(e) {
        if (e.which === 13) {
            jmri.socket._send($('input#command').val());
            return false;
        }
    });
    $("power a").click(function() {
        jmri.setPower((power === jmri.POWER_ON) ? jmri.POWER_OFF : jmri.POWER_ON);
    });
    $("#error-alert").on("close.bs.alert", function() {
        $(this).addClass("hidden").removeClass("show");
        return false; //don't remove error-alert from DOM
    });
    if (window.localStorage.getItem("jmri.json-console.json.pretty-print") === null) {
        window.localStorage.setItem("jmri.json-console.json.pretty-print", "true");
    }
    $("#pretty-print").parent().tooltip();
    $("#pretty-print").prop("checked", (window.localStorage.getItem("jmri.json-console.json.pretty-print") === "true"));
    $("#pretty-print").change(function() {
        window.localStorage.setItem("jmri.json-console.json.pretty-print", $(this).prop("checked") ? "true" : "false");
    });
    jmri.connect();
});
