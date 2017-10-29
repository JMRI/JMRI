package jmri.profile;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import jmri.util.FileUtil;
import jmri.util.FileUtilSupport;
import jmri.util.prefs.JmriConfigurationProvider;
import jmri.util.prefs.JmriPreferencesProvider;
import jmri.util.prefs.JmriUserInterfaceConfigurationProvider;

/**
 * Utility methods to get information about {@link jmri.profile.Profile}s.
 *
 * @author Randall Wood 2015
 */
public class ProfileUtils {

    /**
     * Get the XMl configuration container for a given configuration profile.
     *
     * @param project The configuration profile.
     * @return An XML configuration container, possibly empty.
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(Profile project) {
        return JmriConfigurationProvider.getConfiguration(project);
    }

    /**
     * Get the preferences needed by a class for a given configuration profile.
     *
     * @param project The configuration profile.
     * @param clazz   The class requesting preferences.
     * @param shared  True if the preferences are for all nodes (computers) this
     *                profile may run on, false if the preferences are only for
     *                this node.
     * @return The preferences.
     */
    public static Preferences getPreferences(Profile project, Class<?> clazz, boolean shared) {
        return JmriPreferencesProvider.getPreferences(project, clazz, shared);
    }

    /**
     * Get the XMl configuration container for a given configuration profile's
     * user interface state.
     *
     * @param project the configuration profile
     * @return an XML configuration container, possibly empty
     */
    public static AuxiliaryConfiguration getUserInterfaceConfiguration(Profile project) {
        return JmriUserInterfaceConfigurationProvider.getConfiguration(project);
    }

    /**
     * Get the local cache directory for the given profile.
     *
     * This cache is outside the profile for which the cache exists to prevent
     * the possibility that different JMRI installations have different contents
     * that would invalidate the cache if copied from one computer to another.
     *
     * @param project the project to get the cache directory for, or null to get
     *                the cache directory for all projects on this computer
     * @param owner   The class owning the cached information, or null to get
     *                the cache directory for the project
     * @return a directory in which data can be cached
     */
    public static File getCacheDirectory(Profile project, Class<?> owner) {
        File cache = FileUtilSupport.getDefault().getCacheDirectory();
        if (project != null) {
            cache = new File(cache, project.getId());
        }
        if (owner != null) {
            cache = new File(cache, JmriPreferencesProvider.findCNBForClass(owner));
        }
        FileUtil.createDirectory(cache);
        return cache;
    }

    /**
     * Copy one profile configuration to another profile.
     *
     * @param source      The source profile.
     * @param destination The destination profile.
     * @throws IllegalArgumentException If the destination profile is the active
     *                                  profile.
     * @throws IOException              If the copy cannot be completed.
     */
    public static void copy(Profile source, Profile destination) throws IllegalArgumentException, IOException {
        if (destination.equals(ProfileManager.getDefault().getActiveProfile())) {
            throw new IllegalArgumentException("Target profile cannot be active profile.");
        }
        FileUtil.copy(source.getPath(), destination.getPath());
        File profile = new File(destination.getPath(), Profile.PROFILE);
        File[] files = profile.listFiles((File pathname) -> (pathname.getName().endsWith(source.getUniqueId())));
        if (files != null) {
            for (File file : files) {
                if (!file.renameTo(new File(profile, file.getName().replace(source.getUniqueId(), destination.getUniqueId())))) {
                    throw new IOException("Unable to rename " + file + " to use new profile ID");
                }
            }
        }
        destination.save();
    }

}
