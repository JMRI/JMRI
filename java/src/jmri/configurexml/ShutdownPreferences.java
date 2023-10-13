package jmri.configurexml;

import java.util.prefs.Preferences;

import jmri.InstanceManagerAutoDefault;
import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;

/**
 * Preferences for Shutdown used by StoreAndCompare
 *
 * @author Dave Sand Copyright 2022
 */
public final class ShutdownPreferences extends PreferencesBean implements InstanceManagerAutoDefault {

    public static final String ENABLE_STORE_CHECK = "enableStoreCheck";
    public static final String IGNORE_TIMEBASE = "ignoreTimebase";
    public static final String IGNORE_SENSORCOLORS = "ignoreSensorColors";
    public static final String DISPLAY_DIALOG = "displayDialog";

    private boolean _enableStoreCheck = false;
    private boolean _ignoreTimebase = false;
    private boolean _ignoreSensorColors = false;
    private DialogDisplayOptions _displayDialog = DialogDisplayOptions.ShowDialog;


    public ShutdownPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _enableStoreCheck = sharedPreferences.getBoolean(ENABLE_STORE_CHECK, true);
        _ignoreTimebase = sharedPreferences.getBoolean(IGNORE_TIMEBASE, false);
        _ignoreSensorColors = sharedPreferences.getBoolean(IGNORE_SENSORCOLORS, false);
        var display = sharedPreferences.get(DISPLAY_DIALOG, "ShowDialog");
        _displayDialog = DialogDisplayOptions.valueOf(display);
        setIsDirty(false);
    }

    public boolean compareValuesDifferent(ShutdownPreferences prefs) {
        if (isStoreCheckEnabled() != prefs.isStoreCheckEnabled()) {
            return true;
        }
        if (isIgnoreTimebaseEnabled() != prefs.isIgnoreTimebaseEnabled()) {
            return true;
        }
        if (isIgnoreSensorColorsEnabled() != prefs.isIgnoreSensorColorsEnabled()) {
            return true;
        }
        if (!getDisplayDialog().equals(prefs.getDisplayDialog())) {
            return true;
        }
        return false;
    }

    public void apply(ShutdownPreferences prefs) {
        setEnableStoreCheck(prefs.isStoreCheckEnabled());
        setIgnoreTimebase(prefs.isIgnoreTimebaseEnabled());
        setIgnoreSensorColors(prefs.isIgnoreSensorColorsEnabled());
        setDisplayDialog(prefs.getDisplayDialog());
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(ENABLE_STORE_CHECK, this.isStoreCheckEnabled());
        sharedPreferences.putBoolean(IGNORE_TIMEBASE, this.isIgnoreTimebaseEnabled());
        sharedPreferences.putBoolean(IGNORE_SENSORCOLORS, this.isIgnoreSensorColorsEnabled());
        sharedPreferences.put(DISPLAY_DIALOG, this.getDisplayDialog().name());
        setIsDirty(false);
    }

    public void setEnableStoreCheck(boolean value) {
        _enableStoreCheck = value;
        setIsDirty(true);
    }

    public boolean isStoreCheckEnabled() {
        return _enableStoreCheck;
    }

    public void setIgnoreTimebase(boolean value) {
        _ignoreTimebase = value;
        setIsDirty(true);
    }

    public boolean isIgnoreTimebaseEnabled() {
        return _ignoreTimebase;
    }

    public void setIgnoreSensorColors(boolean value) {
        _ignoreSensorColors = value;
        setIsDirty(true);
    }

    public boolean isIgnoreSensorColorsEnabled() {
        return _ignoreSensorColors;
    }

    public void setDisplayDialog(DialogDisplayOptions value) {
        _displayDialog = value;
        setIsDirty(true);
    }

    public DialogDisplayOptions getDisplayDialog() {
        return _displayDialog;
    }

    public enum DialogDisplayOptions {

        ShowDialog(Bundle.getMessage("OptionDisplay")),
        SkipDialog(Bundle.getMessage("OptionSkip"));

        private final String _description;

        private DialogDisplayOptions(String description) {
            _description = description;
        }

        @Override
        public String toString() {
            return _description;
        }
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndComparePreferences.class);
}
