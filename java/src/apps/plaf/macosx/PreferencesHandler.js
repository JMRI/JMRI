/*
 * Set the Java Desktop preferencesHandler to handler (passed in script context)
 */

var preferencesHandler = new java.awt.desktop.PreferencesHandler() {
    handlePreferences: function(e) {
        handler.handlePreferences(e);
    }
};

java.awt.Desktop.getDesktop().setPreferencesHandler(preferencesHandler);