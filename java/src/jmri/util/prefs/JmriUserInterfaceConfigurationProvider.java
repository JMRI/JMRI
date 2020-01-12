package jmri.util.prefs;

import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jmri.profile.AuxiliaryConfiguration;
import jmri.profile.Profile;

/**
 * Provides a general purpose XML element storage mechanism for the storage of
 * user interface configuration.
 * <p>
 * There are two configuration files per {@link jmri.profile.Profile} and
 * {@link jmri.util.node.NodeIdentity}, both stored in the directory
 * <code>profile:profile</code>:
 * <ul>
 * <li><code>{@value jmri.profile.Profile#UI_CONFIG}</code> preferences that are
 * shared across multiple nodes for a single profile. An example of such a
 * preference would be the Railroad Name preference.</li>
 * <li><code>&lt;node-identity&gt;/{@value jmri.profile.Profile#UI_CONFIG}</code>
 * preferences that are specific to the profile running on a specific host
 * (&lt;node-identity&gt; is the identity returned by
 * {@link jmri.util.node.NodeIdentity#networkIdentity()}). An example of such a
 * preference would be a file location.</li>
 * </ul>
 * <p>
 * Non-profile specific configuration that applies to all profiles is stored in
 * the file
 * <code>settings:preferences/{@value jmri.profile.Profile#UI_CONFIG}</code>.
 *
 * @author Randall Wood 2015, 2016
 */
public final class JmriUserInterfaceConfigurationProvider extends AbstractConfigurationProvider {

    private final Configuration configuration;

    public static final String NAMESPACE = "http://www.netbeans.org/ns/auxiliary-configuration/1"; // NOI18N

    static {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    private static final HashMap<Profile, JmriUserInterfaceConfigurationProvider> PROVIDERS = new HashMap<>();

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
        if (PROVIDERS.get(project) == null) {
            PROVIDERS.put(project, new JmriUserInterfaceConfigurationProvider(project));
        }
        return PROVIDERS.get(project);
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
    @Override
    protected AuxiliaryConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    protected File getConfigurationFile(boolean shared) {
        if (this.project == null) {
            return new File(this.getConfigurationDirectory(shared), Profile.UI_CONFIG); // NOI18N
        } else {
            return new File(this.getConfigurationDirectory(shared), Profile.UI_CONFIG); // NOI18N
        }
    }

    JmriUserInterfaceConfigurationProvider(Profile project) {
        super(project);
        this.configuration = new Configuration();
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
            return JmriUserInterfaceConfigurationProvider.this.isSharedBackedUp();
        }

        @Override
        protected void setSharedBackedUp(boolean backedUp) {
            JmriUserInterfaceConfigurationProvider.this.setSharedBackedUp(backedUp);
        }

        @Override
        protected boolean isPrivateBackedUp() {
            return JmriUserInterfaceConfigurationProvider.this.isPrivateBackedUp();
        }

        @Override
        protected void setPrivateBackedUp(boolean backedUp) {
            JmriUserInterfaceConfigurationProvider.this.setPrivateBackedUp(backedUp);
        }

    }
}
