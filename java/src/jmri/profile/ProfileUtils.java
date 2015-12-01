package jmri.profile;

import java.util.prefs.Preferences;
import jmri.util.prefs.JmriConfigurationProvider;
import jmri.util.prefs.JmriPreferencesProvider;

/**
 * Utility methods to get information about {@link jmri.profile.Profile}s.
 *
 * @author Randall Wood 2015
 */
public class ProfileUtils {

    public static AuxiliaryConfiguration getAuxiliaryConfiguration(Profile project) {
        return JmriConfigurationProvider.getConfiguration(project);
    }

    public static Preferences getPreferences(Profile project, Class clazz, boolean shared) {
        return JmriPreferencesProvider.getPreferences(project, clazz, shared);
    }

}
