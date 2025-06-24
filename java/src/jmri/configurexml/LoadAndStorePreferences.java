package jmri.configurexml;

import java.util.prefs.Preferences;

import jmri.InstanceManagerAutoDefault;
import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;

/**
 * Preferences for Load and store tables and panel files.
 *
 * @author Dave Sand         Copyright 2022
 * @author Daniel Bergqvist  Copyright 2025
 */
public final class LoadAndStorePreferences extends PreferencesBean implements InstanceManagerAutoDefault {

    public static final String EXCLUDE_FILE_HISTORY = "excludeFileHistory";
    public static final String EXCLUDE_MEMORY_IMCURRENTTIME = "excludeMemoryIMCURRENTTIME";
    public static final String EXCLUDE_JMRI_VERSION = "excludeJmriVersion";
    public static final String EXCLUDE_TIMEBASE = "excludeMemoryTimebase";
    public static final String EXCLUDE_FONT_EXTENSIONS = "excludeFontExtensions";

    private boolean _excludeFileHistory = false;
    private boolean _excludeMemoryIMCURRENTTIME = false;
    private boolean _excludeJmriVersion = false;
    private boolean _excludeTimebase = false;
    private boolean _excludeFontExtensions = false;


    public LoadAndStorePreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _excludeFileHistory = sharedPreferences.getBoolean(EXCLUDE_FILE_HISTORY, false);
        _excludeMemoryIMCURRENTTIME = sharedPreferences.getBoolean(EXCLUDE_MEMORY_IMCURRENTTIME, false);
        _excludeJmriVersion = sharedPreferences.getBoolean(EXCLUDE_JMRI_VERSION, false);
        _excludeTimebase = sharedPreferences.getBoolean(EXCLUDE_TIMEBASE, false);
        _excludeFontExtensions = sharedPreferences.getBoolean(EXCLUDE_FONT_EXTENSIONS, false);
        
        setIsDirty(false);
    }

    public boolean compareValuesDifferent(LoadAndStorePreferences prefs) {
        if (isExcludeFileHistory() != prefs.isExcludeFileHistory()) {
            return true;
        }
        if (isExcludeMemoryIMCURRENTTIME() != prefs.isExcludeMemoryIMCURRENTTIME()) {
            return true;
        }
        if (isExcludeJmriVersion() != prefs.isExcludeJmriVersion()) {
            return true;
        }
        if (isExcludeTimebase() != prefs.isExcludeTimebase()) {
            return true;
        }
        if (isExcludeFontExtensions() != prefs.isExcludeFontExtensions()) {
            return true;
        }
        return false;
    }

    public void apply(LoadAndStorePreferences prefs) {
        setExcludeFileHistory(prefs.isExcludeFileHistory());
        setExcludeMemoryIMCURRENTTIME(prefs.isExcludeMemoryIMCURRENTTIME());
        setExcludeJmriVersion(prefs.isExcludeJmriVersion());
        setExcludeTimebase(prefs.isExcludeTimebase());
        setExcludeFontExtensions(prefs.isExcludeFontExtensions());
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(EXCLUDE_FILE_HISTORY, this.isExcludeFileHistory());
        sharedPreferences.putBoolean(EXCLUDE_MEMORY_IMCURRENTTIME, this.isExcludeMemoryIMCURRENTTIME());
        sharedPreferences.putBoolean(EXCLUDE_JMRI_VERSION, this.isExcludeJmriVersion());
        sharedPreferences.putBoolean(EXCLUDE_TIMEBASE, this.isExcludeTimebase());
        sharedPreferences.putBoolean(EXCLUDE_FONT_EXTENSIONS, this.isExcludeFontExtensions());
        setIsDirty(false);
    }

    public void setExcludeFileHistory(boolean value) {
        _excludeFileHistory = value;
        setIsDirty(true);
    }

    public boolean isExcludeFileHistory() {
        return _excludeFileHistory;
    }

    public void setExcludeMemoryIMCURRENTTIME(boolean value) {
        _excludeMemoryIMCURRENTTIME = value;
        setIsDirty(true);
    }

    public boolean isExcludeMemoryIMCURRENTTIME() {
        return _excludeMemoryIMCURRENTTIME;
    }

    public void setExcludeJmriVersion(boolean value) {
        _excludeJmriVersion = value;
        setIsDirty(true);
    }

    public boolean isExcludeJmriVersion() {
        return _excludeJmriVersion;
    }

    public void setExcludeTimebase(boolean value) {
        _excludeTimebase = value;
        setIsDirty(true);
    }

    public boolean isExcludeTimebase() {
        return _excludeTimebase;
    }

    public void setExcludeFontExtensions(boolean value) {
        _excludeFontExtensions = value;
        setIsDirty(true);
    }

    public boolean isExcludeFontExtensions() {
        return _excludeFontExtensions;
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndComparePreferences.class);
}
