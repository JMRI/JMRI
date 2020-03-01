/*
 * Set the Apple Java Extensions aboutHandler to handler (passed in script context)
 */

var aboutHandler = new com.apple.eawt.AboutHandler() {
    handleAbout: function(e) {
        handler.handleAbout(e);
    }
};

com.apple.eawt.Application.getApplication().setAboutHandler(aboutHandler);