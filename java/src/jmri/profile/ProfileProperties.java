package jmri.profile;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import jmri.util.prefs.JmriPreferencesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileProperties implements AuxiliaryProperties {

    private final File path;

    private final static Logger log = LoggerFactory.getLogger(ProfileProperties.class);

    public ProfileProperties(Profile project) {
        this.path = project.getPath();
    }

    /**
     * Package protected constructor used in
     * {@link jmri.profile.Profile#Profile(java.io.File, boolean)}.
     *
     * @param path Path to a partially constructed Profile
     */
    ProfileProperties(File path) {
        this.path = path;
    }

    @Override
    @SuppressFBWarnings(value = "deprecation", justification = "Avoids errors passing partly constructed object.")
    @SuppressWarnings("deprecation") // silence deprecation notice in IDEs
    public String get(String key, boolean shared) {
        return JmriPreferencesProvider.getPreferences(path, null, shared).node(Profile.PROFILE).get(key, null);
    }

    @Override
    @SuppressFBWarnings(value = "deprecation", justification = "Avoids errors passing partly constructed object.")
    @SuppressWarnings("deprecation")
    public Iterable<String> listKeys(boolean shared) {
        try {
            String[] keys = JmriPreferencesProvider.getPreferences(path, null, shared).node(Profile.PROFILE).keys();
            return new ArrayList<>(Arrays.asList(keys));
        } catch (BackingStoreException ex) {
            log.error("Unable to read properties.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    @SuppressFBWarnings(value = "deprecation", justification = "Avoids errors passing partly constructed object.")
    @SuppressWarnings("deprecation")
    public void put(String key, String value, boolean shared) {
        JmriPreferencesProvider.getPreferences(path, null, shared).node(Profile.PROFILE).put(key, value);
    }

}
