/*
 * Reset the in-memory and on-disk caches for the web app
 * 
 * Works with JMRI 4.7.4 or newer.
 */

// Define Java classes
var ProfileManager = Java.type("jmri.profile.ProfileManager");
var WebAppManager = Java.type("jmri.server.web.app.WebAppManager");
var InstanceManager = Java.type("jmri.InstanceManager");

// Get WebAppManager
var manager = InstanceManager.getDefault(WebAppManager.class);
// Reset manager
manager.savePreferences(ProfileManager.getDefault().getActiveProfile());
