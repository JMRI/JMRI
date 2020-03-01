/*
 * Set the Apple Java Extensions quitHandler to handler (passed in script context)
 */

var quitHandler = new com.apple.eawt.QuitHandler() {
    handleQuitRequestWith: function(qe, qr) {
        if (handler.handleQuitRequest(qe)) {
            qr.performQuit();
        } else {
            qr.cancelQuit();
        }
    }
};

com.apple.eawt.Application.getApplication().setQuitHandler(quitHandler);