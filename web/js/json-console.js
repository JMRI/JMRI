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
            $("#activity-alert").addClass("hidden").removeClass("show");
            jmri.getPower();
        },
        console: function(data) {
            if (data !== '{"type":"pong"}') {
                var console = $('#console');
                console.append(data + '<br/>');
                console.scrollTop(console[0].scrollHeight - console.height());
            } else {
                $(".panel-footer form .form-group").addClass("has-success");
                var wait = window.setTimeout(function() {
                    $(".panel-footer form .form-group").removeClass("has-success");
                },
                        1000
                        );
                if (window.console) {
                    window.console.log(data);
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
        jmri.setPower((power == jmri.POWER_ON) ? jmri.POWER_OFF : jmri.POWER_ON);
    });
    $("#error-alert").on("close.bs.alert", function() {
        $(this).addClass("hidden").removeClass("show");
        return false; //don't remove error-alert from DOM
    });
    jmri.connect();
});
