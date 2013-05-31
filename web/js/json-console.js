/**
 * JMRI JSON Console javascript
 *
 * Demonstration of how the JMRI extension to jQuery can be used.
 * Note that the JMRI extension relies on the jquery-websocket and jquery-json extensions.
 */
var jmri = null;
var power = 0;
$(document).ready(function() {
    jmri = $.JMRI({
        railroad: function(string) {
            document.title = string + " JSON console";
            $('h1 span:first-child').html(document.title);
            jmri.getPower();
        },
        console: function(data) {
            if (data !== '{"type":"pong"}') {
                var console = $('#console');
                console.append(data + '<br/>');
                console.scrollTop(console[0].scrollHeight - console.height());
            }
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
    $('input#sendCmd').click(function() {
        jmri.socket._send($('input#command').val());
        return false;
    });
    $('#footer-menu>li+li+li').before("<li><a href='/help/en/html/web/JsonServlet.shtml'>Json Servlet Help</a></li>");
});
