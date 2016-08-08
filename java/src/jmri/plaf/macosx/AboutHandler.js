/*
 * Set the Java Desktop aboutHandler to handler (passed in script context)
 */

var aboutHandler = new java.awt.desktop.AboutHandler() {
    handleAbout: function(e) {
        handler.handleAbout(e);
    }
};

java.awt.Desktop.getDesktop().setAboutHandler(aboutHandler);