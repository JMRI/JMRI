/*
 * Set the Java Desktop quitHandler to handler (passed in script context)
 */

var quitHandler = new java.awt.desktop.QuitHandler() {
    handleQuitRequestWith: function(qe, qr) {
        if (handler.handleQuitRequest(qe)) {
            qr.performQuit();
        } else {
            qr.cancelQuit();
        }
    }
};

java.awt.Desktop.getDesktop().setQuitHandler(quitHandler);