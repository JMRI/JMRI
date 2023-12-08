/* replace the websocket message if supported */
$(document).ready(function() {
  if (window.WebSocket) {
    $('#websockettest').text("supports");
  }
});