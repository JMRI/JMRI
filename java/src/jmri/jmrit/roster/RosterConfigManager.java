package jmri.jmrit.roster;

import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import jmri.implementation.FileLocationsPreferences;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.AbstractPreferencesProvider;
import jmri.spi.InitializationException;
import jmri.spi.PreferencesProvider;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load and store the Roster configuration.
 *
 * This only configures the Roster when initialized so that configuration
 * changes made by users do not affect the running instance of JMRI, but only
 * take effect after restarting JMRI.
 *
 * @author Randall Wood (C) 2015
 */
public class RosterConfigManager extends AbstractPreferencesProvider {

    private String directory = FileUtil.PREFERENCES;
    private String defaultOwner = "";

    private static final String DIRECTORY = "directory";
    private static final String DEFAULT_OWNER = "defaultOwner";
    private static final Logger log = LoggerFactory.getLogger(RosterConfigManager.class);

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
            this.setDirectory(preferences.get(DIRECTORY, this.getDirectory()));
            this.setDefaultOwner(preferences.get(DEFAULT_OWNER, this.getDefaultOwner()));
            Roster.getDefault().setRosterLocation(this.getDirectory());
            this.setIsInitialized(profile, true);
        }
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
        preferences.put(DIRECTORY, FileUtil.getPortableFilename(this.getDirectory()));
        preferences.put(DEFAULT_OWNER, this.getDefaultOwner());
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences", ex);
        }
    }

    @Override
    public Set<Class <? extends PreferencesProvider>> getRequires() {
        Set<Class<? extends PreferencesProvider>> requires = super.getRequires();
        requires.add(FileLocationsPreferences.class);
        return requires;
    }
    
    /**
     * @return the defaultOwner
     */
    public @Nonnull String getDefaultOwner() {
        return defaultOwner;
    }

    /**
     * @param defaultOwner the defaultOwner to set
     */
    public void setDefaultOwner(String defaultOwner) {
        if (defaultOwner == null) {
            defaultOwner = "";
        }
        String oldDefaultOwner = this.defaultOwner;
        this.defaultOwner = defaultOwner;
        propertyChangeSupport.firePropertyChange(DEFAULT_OWNER, oldDefaultOwner, defaultOwner);
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(String directory) {
        if (directory == null || directory.isEmpty()) {
            directory = FileUtil.PREFERENCES;
        }
        String oldDirectory = this.directory;
        this.directory = FileUtil.getAbsoluteFilename(directory);
        propertyChangeSupport.firePropertyChange(DIRECTORY, oldDirectory, directory);
    }

}
