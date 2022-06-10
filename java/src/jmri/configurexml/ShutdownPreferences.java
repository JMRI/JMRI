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
    public static final String DISPLAY_DIALOG = "displayDialog";

    private boolean _enableStoreCheck = false;
    private DialogDisplayOptions _displayDialog = DialogDisplayOptions.ShowDialog;


    public ShutdownPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _enableStoreCheck = sharedPreferences.getBoolean(ENABLE_STORE_CHECK, true);
        var display = sharedPreferences.get(DISPLAY_DIALOG, "ShowDialog");
        _displayDialog = DialogDisplayOptions.valueOf(display);
        setIsDirty(false);
    }

    public boolean compareValuesDifferent(ShutdownPreferences prefs) {
        if (isStoreCheckEnabled() != prefs.isStoreCheckEnabled()) {
            return true;
        }
        if (!getDisplayDialog().equals(prefs.getDisplayDialog())) {
            return true;
        }
        return false;
    }

    public void apply(ShutdownPreferences prefs) {
        setEnableStoreCheck(prefs.isStoreCheckEnabled());
        setDisplayDialog(prefs.getDisplayDialog());
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(ENABLE_STORE_CHECK, this.isStoreCheckEnabled());
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
