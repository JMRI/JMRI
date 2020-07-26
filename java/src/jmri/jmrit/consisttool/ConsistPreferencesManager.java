package jmri.jmrit.consisttool;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender (C) 2019
 */
@ServiceProvider(service = PreferencesManager.class)
public class ConsistPreferencesManager extends AbstractPreferencesManager {

    private static final Logger log = LoggerFactory.getLogger(ConsistPreferencesManager.class);
    public static final String UPDATE_CV19 = "updateCV19";
    private boolean updateCV19 = false;

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
            this.setUpdateCV19(preferences.getBoolean(UPDATE_CV19, this.isUpdateCV19()));
            this.setInitialized(profile, true);
        }
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
        preferences.putBoolean(UPDATE_CV19, this.updateCV19);
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences.", ex);
        }
    }

    /**
     * @return updateCV19
     */
    public boolean isUpdateCV19() {
        return updateCV19;
    }

    /**
     * @param update the value to set updateCV19 to.
     */
    public void setUpdateCV19(boolean update) {
        boolean oldUpdateCV19 = this.updateCV19;
        updateCV19 = update;
        firePropertyChange(UPDATE_CV19, oldUpdateCV19, updateCV19);
    }

}
