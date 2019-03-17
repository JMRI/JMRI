package jmri.profile;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import jmri.util.FileUtil;
import jmri.util.FileUtilSupport;
import jmri.util.node.NodeIdentity;
import jmri.util.prefs.JmriConfigurationProvider;
import jmri.util.prefs.JmriPreferencesProvider;
import jmri.util.prefs.JmriUserInterfaceConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to get information about {@link jmri.profile.Profile}s.
 *
 * @author Randall Wood 2015
 */
public class ProfileUtils {

    private final static Logger log = LoggerFactory.getLogger(ProfileUtils.class);

    /**
     * Get the XMl configuration container for a given configuration profile.
     *
     * @param project The project to get the configuration container for, or
     *                null to get a configuration container that can apply to
     *                all projects on this computer
     * @return An XML configuration container, possibly empty
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(Profile project) {
        return JmriConfigurationProvider.getConfiguration(project);
    }

    /**
     * Get the preferences needed by a class for a given configuration profile.
     *
     * @param project The project to get the configuration for, or null to get a
     *                preferences object that can apply to all projects on this
     *                computer
     * @param clazz   The class requesting preferences
     * @param shared  True if the preferences are for all nodes (computers) this
     *                project may run on, false if the preferences are only for
     *                this node; ignored if the value of project is null
     * @return The preferences
     */
    public static Preferences getPreferences(Profile project, Class<?> clazz, boolean shared) {
        return JmriPreferencesProvider.getPreferences(project, clazz, shared);
    }

    /**
     * Get the XMl configuration container for a given configuration profile's
     * user interface state.
     *
     * @param project The project to get the configuration container for, or
     *                null to get a configuration container that can apply to
     *                all projects on this computer
     * @return An XML configuration container, possibly empty
     */
    public static AuxiliaryConfiguration getUserInterfaceConfiguration(Profile project) {
        return JmriUserInterfaceConfigurationProvider.getConfiguration(project);
    }

    /**
     * Get the local cache directory for the given profile.
     * <p>
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
    public static void copy(@Nonnull Profile source, @Nonnull Profile destination) throws IllegalArgumentException, IOException {
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

    /**
     * Copy the most recently modified former identity, if any, for the current computer
     * in the given profile to the current storage identity of the current computer for
     * the given profile.
     *
     * @param profile the profile containing identities to copy
     * @return true if an existing identity is copied, false otherwise
     * @throws IOException if unable to a copy an existing identity
     */
    public static boolean copyPrivateContentToCurrentIdentity(@Nonnull Profile profile) throws IOException {
        String uniqueId = "-" + profile.getUniqueId();
        File newPath = new File(new File(profile.getPath(), Profile.PROFILE), NodeIdentity.storageIdentity(profile));
        if (!newPath.exists()) {
            File oldPath = null;
            for (String identity : NodeIdentity.formerIdentities()) {
                if (oldPath == null) {
                    File path = new File(new File(profile.getPath(), Profile.PROFILE), identity + uniqueId);
                    if (path.exists()) {
                        oldPath = path;
                    }
                } else {
                    File path = new File(new File(profile.getPath(), Profile.PROFILE), identity + uniqueId);
                    if (path.exists() && path.lastModified() > oldPath.lastModified()) {
                        oldPath = path;
                    }
                }
            }
            if (oldPath != null && oldPath.exists()) {
                try {
                    log.info("Copying from old node \"{}\" to new node \"{}\"", oldPath, newPath);
                    FileUtil.copy(oldPath, newPath);
                    return true;
                } catch (IOException ex) {
                    log.warn("Failed copying \"{}\" to \"{}\"", oldPath, newPath);
                }
            }
        }
        return false;
    }
}
