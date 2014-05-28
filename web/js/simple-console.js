/**
 * JMRI Simple Server Console javascript
 */
if (!window.WebSocket) {
    alert("WebSocket not supported by this browser");
}
var server = {
    connect: function() {
        var location = document.URL.replace('http://',
                'ws://').replace('https://', 'wss://');
        $('serverURL').innerHTML = location;
        this._ws = new WebSocket(location);
        this._ws.onopen = this._onopen;
        this._ws.onmessage = this._onmessage;
        this._ws.onclose = this._onclose;
    },
    _send: function(message) {
        if (this._ws)
            this._ws.send(message);
    },
    send: function(text) {
        if (text != null && text.length > 0)
            server._send(text);
    },
    _onmessage: function(m) {
        if (m.data) {
            var console = $("#console pre");
            console.append(m.data.replace(/(\r\n|\n\n|\n|\r)/gm,"<br>"));
            console.scrollTop(console[0].scrollHeight - console.height());
        }
    },
    _onclose: function(m) {
        this._ws = null;
        $("#alert-websocket-closed");
    },
    _onopen: function() {
        $("#alert-websocket-connecting").addClass("hide").removeClass("show");
    },
    close: function() {
        server._onclose();
    }
};
$(window).unload(function() {
    server.close();
    server = null;
});
$(document).ready(function() {
    $('input#sendCmd').click(function() {
        server.send($('input#command').val());
        return false;
    });
    $('input#command').keypress(function(e) {
        if (e.which === 13) {
            server.send($('input#command').val());
            return false;
        }
    });
    $("#error-alert").on("close.bs.alert", function() {
        $(this).addClass("hidden").removeClass("show");
        return false; //don't remove error-alert from DOM
    });
    server.connect();
});
