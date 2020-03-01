/*
 * Set the Apple Java Extensions preferencesHandler to handler (passed in script context)
 */

var preferencesHandler = new com.apple.eawt.PreferencesHandler() {
    handlePreferences: function(e) {
        handler.handlePreferences(e);
    }
};

com.apple.eawt.Application.getApplication().setPreferencesHandler(preferencesHandler);