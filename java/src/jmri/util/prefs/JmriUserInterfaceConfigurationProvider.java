package jmri.util.prefs;

import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jmri.profile.AuxiliaryConfiguration;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.node.NodeIdentity;

/**
 * Provides a general purpose XML element storage mechanism for the storage of
 * user interface configuration.
 *
 * There are two configuration files per {@link jmri.profile.Profile} and
 * {@link jmri.util.node.NodeIdentity}, both stored in the directory
 * <code>profile:profile</code>:
 * <ul>
 * <li><code>user-interface.xml</code> preferences that are shared across
 * multiple nodes for a single profile. An example of such a preference would be
 * the Railroad Name preference.</li>
 * <li><code>&lt;node-identity&gt;/user-interface.xml</code> preferences that
 * are specific to the profile running on a specific host (&lt;node-identity&gt;
 * is the identity returned by {@link jmri.util.node.NodeIdentity#identity()}).
 * An example of such a preference would be a file location.</li>
 * </ul>
 *
 * @author Randall Wood 2015, 2016
 */
/*
Need to reduce duplication of code between this and the JmriConfigurationProvider.
 */
public final class JmriUserInterfaceConfigurationProvider {

    private final Configuration configuration;
    private final Profile project;
    private boolean privateBackedUp = false;
    private boolean sharedBackedUp = false;

    public static final String NAMESPACE = "http://www.netbeans.org/ns/auxiliary-configuration/1"; // NOI18N

    static {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    private static final HashMap<Profile, JmriUserInterfaceConfigurationProvider> providers = new HashMap<>();

    /**
     * Get the JmriPrefererncesProvider for the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}
     * @return The shared or private JmriPreferencesProvider for the project.
     */
    static synchronized JmriUserInterfaceConfigurationProvider findProvider(Profile project) {
        if (providers.get(project) == null) {
            providers.put(project, new JmriUserInterfaceConfigurationProvider(project));
        }
        return providers.get(project);
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified class in
     * the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}
     * @return The shared or private AuxiliaryConfiguration for project.
     */
    public static AuxiliaryConfiguration getConfiguration(final Profile project) {
        return findProvider(project).getConfiguration();
    }

    /**
     * Get the {@link jmri.profile.AuxiliaryConfiguration}.
     *
     * @return The AuxiliaryConfiguration.
     */
    private AuxiliaryConfiguration getConfiguration() {
        return this.configuration;
    }

    JmriUserInterfaceConfigurationProvider(Profile project) {
        this.project = project;
        this.configuration = new Configuration();
    }

    private File getConfigurationFile(boolean shared) {
        if (JmriUserInterfaceConfigurationProvider.this.project == null) {
            return new File(this.getConfigurationDirectory(shared), Profile.UI_CONFIG); // NOI18N
        } else {
            return new File(this.getConfigurationDirectory(shared), Profile.UI_CONFIG); // NOI18N
        }
    }

    public File getConfigurationDirectory(boolean shared) {
        File dir;
        if (JmriUserInterfaceConfigurationProvider.this.project == null) {
            dir = new File(FileUtil.getPreferencesPath(), "preferences"); // NOI18N
        } else {
            dir = new File(JmriUserInterfaceConfigurationProvider.this.project.getPath(), Profile.PROFILE);
            if (!shared) {
                dir = new File(dir, NodeIdentity.identity());
            }
        }
        FileUtil.createDirectory(dir);
        return dir;
    }

    private class Configuration extends JmriConfiguration {

        private Configuration() {
            super();
        }

        @Override
        protected File getConfigurationFile(boolean shared) {
            return JmriUserInterfaceConfigurationProvider.this.getConfigurationFile(shared);
        }

        @Override
        protected boolean isSharedBackedUp() {
            return JmriUserInterfaceConfigurationProvider.this.sharedBackedUp;
        }

        @Override
        protected void setSharedBackedUp(boolean backedUp) {
            JmriUserInterfaceConfigurationProvider.this.sharedBackedUp = backedUp;
        }

        @Override
        protected boolean isPrivateBackedUp() {
            return JmriUserInterfaceConfigurationProvider.this.privateBackedUp;
        }

        @Override
        protected void setPrivateBackedUp(boolean backedUp) {
            JmriUserInterfaceConfigurationProvider.this.privateBackedUp = backedUp;
        }

    }
}
