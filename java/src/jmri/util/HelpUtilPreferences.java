package jmri.util;

import java.util.prefs.Preferences;

import jmri.InstanceManagerAutoDefault;
import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;

/**
 * Preferences for HelpUtil
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public final class HelpUtilPreferences extends PreferencesBean implements InstanceManagerAutoDefault {

    public static final String OPEN_HELP_ONLINE = "openHelpOnline";
    public static final String OPEN_HELP_ON_FILE = "openHelpOnFile";
    public static final String OPEN_HELP_ON_JMRI_WEB_SERVER = "openHelpOnJMRIWebServer";

    private boolean _openHelpOnline = false;
    private boolean _openHelpOnFile = true;
    private boolean _openHelpOnJMRIWebServer = false;


    public HelpUtilPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _openHelpOnline = sharedPreferences.getBoolean(OPEN_HELP_ONLINE, _openHelpOnline);
        _openHelpOnFile = sharedPreferences.getBoolean(OPEN_HELP_ON_FILE, _openHelpOnFile);
        _openHelpOnJMRIWebServer = sharedPreferences.getBoolean(OPEN_HELP_ON_JMRI_WEB_SERVER, _openHelpOnJMRIWebServer);
        setIsDirty(false);
    }

    public boolean compareValuesDifferent(HelpUtilPreferences prefs) {
        if (getOpenHelpOnline() != prefs.getOpenHelpOnline()) {
            return true;
        }
        if (getOpenHelpOnFile() != prefs.getOpenHelpOnFile()) {
            return true;
        }
        return (getOpenHelpOnJMRIWebServer() != prefs.getOpenHelpOnJMRIWebServer());
    }

    public void apply(HelpUtilPreferences prefs) {
        setOpenHelpOnline(prefs.getOpenHelpOnline());
        setOpenHelpOnFile(prefs.getOpenHelpOnFile());
        setOpenHelpOnJMRIWebServer(prefs.getOpenHelpOnJMRIWebServer());
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(OPEN_HELP_ONLINE, this.getOpenHelpOnline());
        sharedPreferences.putBoolean(OPEN_HELP_ON_FILE, this.getOpenHelpOnFile());
        sharedPreferences.putBoolean(OPEN_HELP_ON_JMRI_WEB_SERVER, this.getOpenHelpOnJMRIWebServer());
        setIsDirty(false);
    }

    public void setOpenHelpOnline(boolean value) {
        _openHelpOnline = value;
        setIsDirty(true);
    }

    public boolean getOpenHelpOnline() {
        return _openHelpOnline;
    }

    public void setOpenHelpOnFile(boolean value) {
        _openHelpOnFile = value;
        setIsDirty(true);
    }

    public boolean getOpenHelpOnFile() {
        return _openHelpOnFile;
    }

    public void setOpenHelpOnJMRIWebServer(boolean value) {
        _openHelpOnJMRIWebServer = value;
        setIsDirty(true);
    }

    public boolean getOpenHelpOnJMRIWebServer() {
        return _openHelpOnJMRIWebServer;
    }
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelpUtilPreferences.class);
}
