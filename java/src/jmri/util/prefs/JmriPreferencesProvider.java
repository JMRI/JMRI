package jmri.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.Version;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import jmri.util.OrderedProperties;
import jmri.util.node.NodeIdentity;

/**
 * Provides instances of {@link java.util.prefs.Preferences} backed by a
 * JMRI-specific storage implementation based on a Properties file.
 * <p>
 * There are two Properties files per {@link jmri.profile.Profile} and
 * {@link jmri.util.node.NodeIdentity}, both stored in the directory
 * <code>profile:profile</code>:
 * <ul>
 * <li><code>profile.properties</code> preferences that are shared across
 * multiple nodes for a single profile. An example of such a preference would be
 * the Railroad Name preference.</li>
 * <li><code>&lt;node-identity&gt;/profile.properties</code> preferences that
 * are specific to the profile running on a specific host (&lt;node-identity&gt;
 * is the identity returned by
 * {@link jmri.util.node.NodeIdentity#storageIdentity()}). An example of such a
 * preference would be a file location.</li>
 * </ul>
 * <p>
 * Non-profile specific configuration that applies to all profiles is stored in
 * the file <code>settings:preferences/preferences.properties</code>.
 *
 * @author Randall Wood 2015
 */
public final class JmriPreferencesProvider {

    private final JmriPreferences root;
    private final File path;
    private final boolean firstUse;
    private final boolean shared;
    private boolean backedUp = false;

    private static final HashMap<File, JmriPreferencesProvider> SHARED_PROVIDERS = new HashMap<>();
    private static final HashMap<File, JmriPreferencesProvider> PRIVATE_PROVIDERS = new HashMap<>();

    /**
     * Get the JmriPreferencesProvider for the specified profile path.
     *
     * @param path   The root path of a {@link jmri.profile.Profile}. This is
     *               most frequently the path returned by
     *               {@link jmri.profile.Profile#getPath()}.
     * @param shared True if the preferences apply to the profile at path
     *               regardless of host. If false, the preferences only apply to
     *               this computer.
     * @return The shared or private JmriPreferencesProvider for the project at
     *         path.
     */
    @Nonnull
    static synchronized JmriPreferencesProvider findProvider(@CheckForNull File path, boolean shared) {
        if (shared) {
            return SHARED_PROVIDERS.computeIfAbsent(path, v -> new JmriPreferencesProvider(path, shared));
        } else {
            return PRIVATE_PROVIDERS.computeIfAbsent(path, v -> new JmriPreferencesProvider(path, shared));
        }
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified class in
     * the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}. If null,
     *                preferences apply to all profiles on the computer and the
     *                value of shared is ignored.
     * @param clazz   The class requesting preferences. Note that the
     *                preferences returned are for the package containing the
     *                class.
     * @param shared  True if the preferences apply to this profile regardless
     *                of host. If false, the preferences only apply to this
     *                computer. Ignored if the value of project is null.
     * @return The shared or private Preferences node for the package containing
     *         clazz for project.
     */
    @Nonnull
    public static Preferences getPreferences(@CheckForNull final Profile project, @CheckForNull final Class<?> clazz,
            final boolean shared) {
        return getPreferences(project, clazz != null ? clazz.getPackage() : null, shared);
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified package in
     * the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}. If null,
     *                preferences apply to all profiles on the computer and the
     *                value of shared is ignored.
     * @param pkg     The package requesting preferences.
     * @param shared  True if the preferences apply to this profile regardless
     *                of host. If false, the preferences only apply to this
     *                computer. Ignored if the value of project is null.
     * @return The shared or private Preferences node for the package for
     *         project.
     */
    @Nonnull
    public static Preferences getPreferences(@CheckForNull final Profile project, @CheckForNull final Package pkg,
            final boolean shared) {
        if (project != null) {
            return findProvider(project.getPath(), shared).getPreferences(pkg);
        } else {
            return findProvider(null, shared).getPreferences(pkg);
        }
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified package in
     * the specified profile.
     * <P>
     * Use of
     *  {@link #getPreferences(Profile, Class, boolean)} or
     *  {@link #getPreferences(Profile, Package, boolean)} is
     *   preferred and recommended unless reading preferences for a
     *   non-existent package or class.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}. If null,
     *                preferences apply to all profiles on the computer and the
     *                value of shared is ignored.
     * @param pkg     The package requesting preferences.
     * @param shared  True if the preferences apply to this profile regardless
     *                of host. If false, the preferences only apply to this
     *                computer. Ignored if the value of project is null.
     * @return The shared or private Preferences node for the package.
     */
    @Nonnull
    public static Preferences getPreferences(@CheckForNull final Profile project, @CheckForNull final String pkg,
            final boolean shared) {
        if (project != null) {
            return findProvider(project.getPath(), shared).getPreferences(pkg);
        } else {
            return findProvider(null, shared).getPreferences(pkg);
        }
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified class in
     * the specified path.
     * <P>
     * Use of
     *   {@link #getPreferences(jmri.profile.Profile, java.lang.Class, boolean)}
     *    is preferred and recommended unless being used to during the
     *    construction of a Profile object.
     *
     * @param path   The path to a profile. This is most often the result of
     *               {@link jmri.profile.Profile#getPath()} for a given Profile.
     *               If null, preferences apply to all profiles on the computer
     *               and the value of shared is ignored.
     * @param clazz  The class requesting preferences. Note that the preferences
     *               returned are for the package containing the class.
     * @param shared True if the preferences apply to this profile regardless of
     *               host. If false, the preferences only apply to this
     *               computer. Ignored if the value of path is null.
     * @return The shared or private Preferences node for the package containing
     *         clazz for project.
     */
    public static Preferences getPreferences(@CheckForNull final File path, @CheckForNull final Class<?> clazz,
            final boolean shared) {
        return findProvider(path, shared).getPreferences(clazz);
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified package.
     *
     * @param pkg The package requesting preferences.
     * @return The shared or private Preferences node for the package.
     */
    // package private
    Preferences getPreferences(@CheckForNull final Package pkg) {
        if (pkg == null) {
            return this.root;
        }
        return this.root.node(findCNBForPackage(pkg));
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified class.
     *
     * @param clazz The class requesting preferences. Note that the preferences
     *              returned are for the package containing the class.
     * @return The shared or private Preferences node for the package containing
     *         clazz.
     */
    // package private
    Preferences getPreferences(@CheckForNull final Class<?> clazz) {
        return getPreferences(clazz != null ? clazz.getPackage() : null);
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified package.
     *
     * @param pkg The package for which preferences are needed.
     * @return The shared or private Preferences node for the package.
     */
    // package private
    Preferences getPreferences(@CheckForNull final String pkg) {
        if (pkg == null) {
            return this.root;
        }
        return this.root.node(pkg);
    }

    JmriPreferencesProvider(@CheckForNull File path, boolean shared) {
        this.path = path;
        this.shared = shared;
        this.firstUse = !this.getPreferencesFile().exists();
        this.root = new JmriPreferences(null, "");
        if (!this.firstUse) {
            try {
                this.root.sync();
            } catch (BackingStoreException ex) {
                log.error("Unable to read preferences", ex);
            }
        }
    }

    /**
     * Return true if the properties file had not been written for a JMRI
     * {@link jmri.profile.Profile} before this instance of JMRI was launched.
     * Note that the first use of a node-specific setting can be different than
     * the first use of a multi-node setting.
     *
     * @return true if new or newly migrated profile, false otherwise
     */
    public boolean isFirstUse() {
        return this.firstUse;
    }

    /**
     * Returns the name of the package for the class in a format that is treated
     * as a single token.
     *
     * @param cls The class for which a sanitized package name is needed
     * @return A sanitized package name
     */
    public static String findCNBForClass(@Nonnull Class<?> cls) {
        return findCNBForPackage(cls.getPackage());
    }

    /**
     * Returns the name of the package in a format that is treated as a single
     * token.
     *
     * @param pkg The package for which a sanitized name is needed
     * @return A sanitized package name
     */
    public static String findCNBForPackage(@Nonnull Package pkg) {
        return pkg.getName().replace('.', '-');
    }

    @Nonnull
    File getPreferencesFile() {
        if (this.path == null) {
            return new File(this.getPreferencesDirectory(), "preferences.properties");
        } else {
            return new File(this.getPreferencesDirectory(), Profile.PROPERTIES);
        }
    }

    @Nonnull
    private File getPreferencesDirectory() {
        File dir;
        if (this.path == null) {
            dir = new File(FileUtil.getPreferencesPath(), "preferences");
        } else {
            dir = new File(this.path, Profile.PROFILE);
            if (!this.shared) {
                // protect against testing a new profile
                if (Profile.isProfile(this.path)) {
                    try {
                        Profile profile = new Profile(this.path);
                        File nodeDir = new File(dir, NodeIdentity.storageIdentity(profile));
                        if (!nodeDir.exists() && !ProfileUtils.copyPrivateContentToCurrentIdentity(profile)) {
                            log.debug("Starting profile with new private preferences.");
                        }
                    } catch (IOException ex) {
                        log.debug("Copying existing private configuration failed.");
                    }
                }
                dir = new File(dir, NodeIdentity.storageIdentity());
            }
        }
        FileUtil.createDirectory(dir);
        return dir;
    }

    /**
     * @return the backedUp
     */
    protected boolean isBackedUp() {
        return backedUp;
    }

    /**
     * @param backedUp the backedUp to set
     */
    protected void setBackedUp(boolean backedUp) {
        this.backedUp = backedUp;
    }

    private class JmriPreferences extends AbstractPreferences {

        private final Map<String, String> root;
        private final Map<String, JmriPreferences> children;
        private boolean isRemoved = false;

        public JmriPreferences(AbstractPreferences parent, String name) {
            super(parent, name);

            log.trace("Instantiating node \"{}\"", name);

            root = new TreeMap<>();
            children = new TreeMap<>();

            try {
                super.sync();
            } catch (BackingStoreException e) {
                log.error("Unable to sync on creation of node {}", name, e);
            }
        }

        @Override
        protected void putSpi(String key, String value) {
            root.put(key, value);
            try {
                flush();
            } catch (BackingStoreException e) {
                log.error("Unable to flush after putting {}", key, e);
            }
        }

        @Override
        protected String getSpi(String key) {
            return root.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            root.remove(key);
            try {
                flush();
            } catch (BackingStoreException e) {
                log.error("Unable to flush after removing {}", key, e);
            }
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            isRemoved = true;
            flush();
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            return root.keySet().toArray(new String[root.keySet().size()]);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            return children.keySet().toArray(new String[children.keySet().size()]);
        }

        @Override
        protected JmriPreferences childSpi(String name) {
            JmriPreferences child = children.get(name);
            if (child == null || child.isRemoved()) {
                child = new JmriPreferences(this, name);
                children.put(name, child);
            }
            return child;
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            if (isRemoved()) {
                return;
            }

            final File file = JmriPreferencesProvider.this.getPreferencesFile();

            if (!file.exists()) {
                return;
            }

            synchronized (file) {
                Properties p = new OrderedProperties();
                try {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        p.load(fis);
                    }

                    StringBuilder sb = new StringBuilder();
                    getPath(sb);
                    String pp = sb.toString();

                    final Enumeration<?> pnen = p.propertyNames();
                    while (pnen.hasMoreElements()) {
                        String propKey = (String) pnen.nextElement();
                        if (propKey.startsWith(pp)) {
                            String subKey = propKey.substring(pp.length());
                            // Only load immediate descendants
                            if (subKey.indexOf('.') == -1) {
                                root.put(subKey, p.getProperty(propKey));
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new BackingStoreException(e);
                }
            }
        }

        private void getPath(StringBuilder sb) {
            final JmriPreferences parent = (JmriPreferences) parent();
            if (parent == null) {
                return;
            }

            parent.getPath(sb);
            sb.append(name()).append('.');
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            final File file = JmriPreferencesProvider.this.getPreferencesFile();

            synchronized (file) {
                Properties p = new OrderedProperties();
                try {

                    StringBuilder sb = new StringBuilder();
                    getPath(sb);
                    String pp = sb.toString();

                    if (file.exists()) {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            p.load(fis);
                        }

                        List<String> toRemove = new ArrayList<>();

                        // Make a list of all direct children of this node to be
                        // removed
                        final Enumeration<?> pnen = p.propertyNames();
                        while (pnen.hasMoreElements()) {
                            String propKey = (String) pnen.nextElement();
                            if (propKey.startsWith(pp)) {
                                String subKey = propKey.substring(pp.length());
                                // Only do immediate descendants
                                if (subKey.indexOf('.') == -1) {
                                    toRemove.add(propKey);
                                }
                            }
                        }

                        // Remove them now that the enumeration is done with
                        toRemove.stream().forEach(p::remove);
                    }

                    // If this node hasn't been removed, add back in any values
                    if (!isRemoved) {
                        root.keySet().stream().forEach(s -> p.setProperty(pp + s, root.get(s)));
                    }

                    if (!JmriPreferencesProvider.this.isBackedUp() && file.exists()) {
                        log.debug("Backing up {}", file);
                        FileUtil.backup(file);
                        JmriPreferencesProvider.this.setBackedUp(true);
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        p.store(fos, "JMRI Preferences version " + Version.name());
                    }
                } catch (IOException e) {
                    throw new BackingStoreException(e);
                }
            }
        }

        private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriPreferences.class);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriPreferencesProvider.class);
}
