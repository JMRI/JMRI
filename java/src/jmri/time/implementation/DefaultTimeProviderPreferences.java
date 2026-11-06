package jmri.time.implementation;

import java.util.prefs.Preferences;

import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.time.TimeProviderPreferences;

/**
 * Default implementation of TimeProvider preferences.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class DefaultTimeProviderPreferences extends PreferencesBean implements TimeProviderPreferences {

    private static final String ENABLE_TIMEPROVIDER = "enableTimeProvider";

    private boolean _enableTimeProvider = false;


    public DefaultTimeProviderPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _enableTimeProvider = sharedPreferences.getBoolean(ENABLE_TIMEPROVIDER, _enableTimeProvider);
        setIsDirty(false);
    }

    @Override
    public boolean compareValuesDifferent(TimeProviderPreferences prefs) {
//        if (getEnableTimeProvider() != prefs.getEnableTimeProvider()) {
//            return true;
//        }
        return (getEnableTimeProvider() != prefs.getEnableTimeProvider());
    }

    @Override
    public void apply(TimeProviderPreferences prefs) {
        setEnableTimeProvider(prefs.getEnableTimeProvider());
    }

    @Override
    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(ENABLE_TIMEPROVIDER, this.getEnableTimeProvider());
        setIsDirty(false);
    }

    @Override
    public void setEnableTimeProvider(boolean value) {
        _enableTimeProvider = value;
        setIsDirty(true);
    }

    @Override
    public boolean getEnableTimeProvider() {
        return _enableTimeProvider;
    }

}
