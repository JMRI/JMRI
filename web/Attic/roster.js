function openThrottle(address, id, imageURL) {
    winref = window.open("/web/inControl.html?locoaddress="+address+"&loconame="+id+"&locoimage="+imageURL, id);
    winref.focus();
}