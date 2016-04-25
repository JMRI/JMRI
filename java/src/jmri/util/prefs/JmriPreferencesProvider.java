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
import jmri.Version;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.OrderedProperties;
import jmri.util.node.NodeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides instances of {@link java.util.prefs.Preferences} backed by a
 * JMRI-specific storage implementation based on a Properties file.
 *
 * There are two Properties files per {@link jmri.profile.Profile} and
 * {@link jmri.util.node.NodeIdentity}, both stored in the directory
 * <code>profile:profile</code>:
 * <ul>
 * <li><code>profile.properties</code> preferences that are shared across
 * multiple nodes for a single profile. An example of such a preference would be
 * the Railroad Name preference.</li>
 * <li><code>&lt;node-identity&gt;/profile.properties</code> preferences that
 * are specific to the profile running on a specific host (&lt;node-identity&gt;
 * is the identity returned by {@link jmri.util.node.NodeIdentity#identity()}).
 * An example of such a preference would be a file location.</li>
 * </ul>
 *
 * @author Randall Wood 2015
 */
public final class JmriPreferencesProvider {

    private final JmriPreferences root;
    private final Profile project;
    private final boolean firstUse;
    private final boolean shared;
    private boolean backedUp = false;

    private static final HashMap<Profile, JmriPreferencesProvider> sharedProviders = new HashMap<>();
    private static final HashMap<Profile, JmriPreferencesProvider> privateProviders = new HashMap<>();
    private static final String INVALID_KEY_CHARACTERS = "_.";
    private static final Logger log = LoggerFactory.getLogger(JmriPreferencesProvider.class);

    /**
     * Get the JmriPrefererncesProvider for the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}
     * @param shared  True if the preferences apply to this profile irregardless
     *                of host. If false, the preferences only apply to this
     *                computer.
     * @return The shared or private JmriPreferencesProvider for the project.
     */
    static synchronized JmriPreferencesProvider findProvider(Profile project, boolean shared) {
        if (shared) {
            if (sharedProviders.get(project) == null) {
                sharedProviders.put(project, new JmriPreferencesProvider(project, shared));
            }
            return sharedProviders.get(project);
        } else {
            if (privateProviders.get(project) == null) {
                privateProviders.put(project, new JmriPreferencesProvider(project, shared));
            }
            return privateProviders.get(project);
        }
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified class in
     * the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}
     * @param clazz   The class requesting preferences. Note that the
     *                preferences returned are for the package containing the
     *                class.
     * @param shared  True if the preferences apply to this profile irregardless
     *                of host. If false, the preferences only apply to this
     *                computer.
     * @return The shared or private Preferences node for the package containing
     *         clazz for project.
     */
    public static Preferences getPreferences(final Profile project, final Class<?> clazz, final boolean shared) {
        return findProvider(project, shared).getPreferences(clazz);
    }

    /**
     * Get the {@link java.util.prefs.Preferences} for the specified class.
     *
     * @param clazz The class requesting preferences. Note that the preferences
     *              returned are for the package containing the class.
     * @return The shared or private Preferences node for the package containing
     *         clazz.
     */
    Preferences getPreferences(final Class<?> clazz) {
        if (clazz == null) {
            return this.root;
        }
        return this.root.node(findCNBForClass(clazz));
    }

    JmriPreferencesProvider(Profile project, boolean shared) {
        this.project = project;
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
     * @return
     */
    public boolean isFirstUse() {
        return this.firstUse;
    }

    private static String encodeString(String s) {
        StringBuilder result = new StringBuilder();

        for (char c : s.toCharArray()) {
            if (INVALID_KEY_CHARACTERS.indexOf(c) == (-1)) {
                result.append(c);
            } else {
                result.append("_");
                result.append(Integer.toHexString(c));
                result.append("_");
            }
        }

        return result.toString();
    }

    /**
     * Returns the name of the package for the class in a format that is treated
     * as a single token.
     *
     * @param cls
     * @return A sanitized package name
     */
    public static String findCNBForClass(@Nonnull Class<?> cls) {
        String absolutePath;
        absolutePath = cls.getName().replaceFirst("(^|\\.)[^.]+$", "");//NOI18N
        return absolutePath.replace('.', '-');
    }

    File getPreferencesFile() {
        if (this.project == null) {
            return new File(this.getPreferencesDirectory(), "preferences.properties");
        } else {
            return new File(this.getPreferencesDirectory(), Profile.PROPERTIES);
        }
    }

    private File getPreferencesDirectory() {
        File dir;
        if (this.project == null) {
            dir = new File(FileUtil.getPreferencesPath(), "preferences");
        } else {
            dir = new File(this.project.getPath(), Profile.PROFILE);
            if (!this.shared) {
                dir = new File(dir, NodeIdentity.identity());
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

        private final Logger log = LoggerFactory.getLogger(JmriPreferences.class);

        private Map<String, String> root;
        private Map<String, JmriPreferences> children;
        private boolean isRemoved = false;

        public JmriPreferences(AbstractPreferences parent, String name) {
            super(parent, name);

            log.trace("Instantiating node \"{}\"", name);

            root = new TreeMap<>();
            children = new TreeMap<>();

            try {
                sync();
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
                    String path = sb.toString();

                    final Enumeration<?> pnen = p.propertyNames();
                    while (pnen.hasMoreElements()) {
                        String propKey = (String) pnen.nextElement();
                        if (propKey.startsWith(path)) {
                            String subKey = propKey.substring(path.length());
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
                    String path = sb.toString();

                    if (file.exists()) {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            p.load(fis);
                        }

                        List<String> toRemove = new ArrayList<>();

                        // Make a list of all direct children of this node to be removed
                        final Enumeration<?> pnen = p.propertyNames();
                        while (pnen.hasMoreElements()) {
                            String propKey = (String) pnen.nextElement();
                            if (propKey.startsWith(path)) {
                                String subKey = propKey.substring(path.length());
                                // Only do immediate descendants
                                if (subKey.indexOf('.') == -1) {
                                    toRemove.add(propKey);
                                }
                            }
                        }

                        // Remove them now that the enumeration is done with
                        toRemove.stream().forEach((propKey) -> {
                            p.remove(propKey);
                        });
                    }

                    // If this node hasn't been removed, add back in any values
                    if (!isRemoved) {
                        root.keySet().stream().forEach((s) -> {
                            p.setProperty(path + s, root.get(s));
                        });
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
    }

}
