/*
 * jQuery Web Sockets Plugin v0.0.4
 * https://github.com/dchelimsky/jquery-websocket
 * http://code.google.com/p/jquery-websocket/
 *
 * This document is licensed as free software under the terms of the
 * MIT License: http://www.opensource.org/licenses/mit-license.php
 *
 * Copyright (c) 2010 by shootaroo (Shotaro Tsubouchi).
 */
(function($){
    $.extend({
        websocket: function(url, s, protocols) {
            var ws;
            if ( protocols ) {
                ws = window['MozWebSocket'] ? new MozWebSocket(url, protocols) : window['WebSocket'] ? new WebSocket(url, protocols) : null;
            } else {
                ws = window['MozWebSocket'] ? new MozWebSocket(url) : window['WebSocket'] ? new WebSocket(url) : null;
            }

            var settings = {
                open: function(){},
                close: function(){},
                message: function(){},
                options: {},
                events: {}
            };
            $.extend(settings, $.websocketSettings, s);

            if (ws) {
                $(ws)
                    .bind('open', settings.open)
                    .bind('close', settings.close)
                    .bind('message', settings.message)
                    .bind('message', function(e) {
                        var m = JSON.parse(e.originalEvent.data);
                        var h = settings.events[m.type];
                        if (h) h.call(this, m);
                    });
                ws._send = ws.send;
                ws.send = function(type, data) {
                    var m = {type: type};
                    m = $.extend(true, m, $.extend(true, {}, settings.options, m));
                    if (data) m['data'] = data;
                    return this._send(JSON.stringify(m));
                };
                $(window).unload(function(){ ws.close(); ws = null; });
            }

            return ws;
        }
    });
})(jQuery);
