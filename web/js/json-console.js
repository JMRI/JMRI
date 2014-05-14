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
                var console = $("#console pre");
                if (window.localStorage.getItem("jmri.json-console.json.pretty-print") === "true") {
                    console.append(JSON.stringify(JSON.parse(data), null, 2) + "<br>");
                } else {
                    console.append(data + "<br>");
                }
                console.scrollTop(console[0].scrollHeight - console.height());
            } else {
                $("#sendCmd").addClass("btn-success").removeClass("btn-primary");
                window.setTimeout(function() {
                    $("#sendCmd").removeClass("btn-success").addClass("btn-primary");
                },
                        1000);
                if (window.console) {
                    window.console.log("Heartbeat response received.");
                }
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
        }
    });
    $('input#clearConsole').click(function() {
        $('div#console').empty(); //clear the console
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
