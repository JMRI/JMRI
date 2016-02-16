package jmri.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jmri.profile.AuxiliaryConfiguration;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.node.NodeIdentity;
import jmri.util.xml.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides a general purpose XML element storage mechanism for the storage of
 * configuration and preferences too complex to be handled by
 * {@link jmri.util.prefs.JmriPreferencesProvider}.
 *
 * There are two configuration files per {@link jmri.profile.Profile} and
 * {@link jmri.util.node.NodeIdentity}, both stored in the directory
 * <code>profile:profile</code>:
 * <ul>
 * <li><code>profile.xml</code> preferences that are shared across multiple
 * nodes for a single profile. An example of such a preference would be the
 * Railroad Name preference.</li>
 * <li><code>&lt;node-identity&gt;/profile.xml</code> preferences that are
 * specific to the profile running on a specific host (&lt;node-identity&gt; is
 * the identity returned by {@link jmri.util.node.NodeIdentity#identity()}). An
 * example of such a preference would be a file location.</li>
 * </ul>
 *
 * @author Randall Wood 2015
 */
public final class JmriConfigurationProvider {

    private final JmriConfiguration configuration;
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

    private static final HashMap<Profile, JmriConfigurationProvider> providers = new HashMap<>();

    /**
     * Get the JmriPrefererncesProvider for the specified profile.
     *
     * @param project The profile. This is most often the profile returned by
     *                the {@link jmri.profile.ProfileManager#getActiveProfile()}
     *                method of the ProfileManager returned by
     *                {@link jmri.profile.ProfileManager#getDefault()}
     * @return The shared or private JmriPreferencesProvider for the project.
     */
    static synchronized JmriConfigurationProvider findProvider(Profile project) {
        if (providers.get(project) == null) {
            providers.put(project, new JmriConfigurationProvider(project));
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

    JmriConfigurationProvider(Profile project) {
        this.project = project;
        this.configuration = new JmriConfiguration();
    }

    private File getConfigurationFile(boolean shared) {
        if (JmriConfigurationProvider.this.project == null) {
            return new File(this.getConfigurationDirectory(shared), "preferences.xml");
        } else {
            return new File(this.getConfigurationDirectory(shared), Profile.CONFIG);
        }
    }

    private File getConfigurationDirectory(boolean shared) {
        File dir;
        if (JmriConfigurationProvider.this.project == null) {
            dir = new File(FileUtil.getPreferencesPath(), "preferences");
        } else {
            dir = new File(JmriConfigurationProvider.this.project.getPath(), Profile.PROFILE);
            if (!shared) {
                dir = new File(dir, NodeIdentity.identity());
            }
        }
        FileUtil.createDirectory(dir);
        return dir;
    }

    private class JmriConfiguration implements AuxiliaryConfiguration {

        private final Logger log = LoggerFactory.getLogger(JmriConfiguration.class);

        private JmriConfiguration() {
        }

        @Override
        public Element getConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
            synchronized (this) {
                File file = JmriConfigurationProvider.this.getConfigurationFile(shared);
                if (file != null && file.canRead()) {
                    try {
                        try (InputStream is = new FileInputStream(file)) {
                            InputSource input = new InputSource(is);
                            input.setSystemId(file.toURI().toURL().toString());
                            Element root = XMLUtil.parse(input, false, true, null, null).getDocumentElement();
                            return XMLUtil.findElement(root, elementName, namespace);
                        }
                    } catch (IOException | SAXException | IllegalArgumentException ex) {
                        log.warn("Cannot parse {}", file, ex);
                    }
                }
                return null;
            }
        }

        @Override
        public void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
            synchronized (this) {
                String elementName = fragment.getLocalName();
                String namespace = fragment.getNamespaceURI();
                if (namespace == null) {
                    throw new IllegalArgumentException();
                }
                File file = JmriConfigurationProvider.this.getConfigurationFile(shared);
                Document doc = null;
                if (file != null && file.canRead()) {
                    try {
                        try (InputStream is = new FileInputStream(file)) {
                            InputSource input = new InputSource(is);
                            input.setSystemId(file.toURI().toURL().toString());
                            doc = XMLUtil.parse(input, false, true, null, null);
                        }
                    } catch (IOException | SAXException ex) {
                        log.warn("Cannot parse {}", file, ex);
                    }
                }
                if (doc == null) {
                    doc = XMLUtil.createDocument("auxiliary-configuration", NAMESPACE, null, null);
                }
                Element root = doc.getDocumentElement();
                Element oldFragment = XMLUtil.findElement(root, elementName, namespace);
                if (oldFragment != null) {
                    root.removeChild(oldFragment);
                }
                Node ref = null;
                NodeList list = root.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    int comparison = node.getNodeName().compareTo(elementName);
                    if (comparison == 0) {
                        comparison = node.getNamespaceURI().compareTo(namespace);
                    }
                    if (comparison > 0) {
                        ref = node;
                        break;
                    }
                }
                root.insertBefore(root.getOwnerDocument().importNode(fragment, true), ref);
                try {
                    this.backup(shared);
                    try (OutputStream os = new FileOutputStream(file)) {
                        XMLUtil.write(doc, os, "UTF-8");
                    }
                } catch (IOException ex) {
                    log.error("Cannot write {}", file, ex);
                }
            }
        }

        @Override
        public boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
            synchronized (this) {
                File file = JmriConfigurationProvider.this.getConfigurationFile(shared);
                if (file != null && file.canWrite()) {
                    try {
                        Document doc;
                        try (InputStream is = new FileInputStream(file)) {
                            InputSource input = new InputSource(is);
                            input.setSystemId(file.toURI().toURL().toString());
                            doc = XMLUtil.parse(input, false, true, null, null);
                        }
                        Element root = doc.getDocumentElement();
                        Element toRemove = XMLUtil.findElement(root, elementName, namespace);
                        if (toRemove != null) {
                            root.removeChild(toRemove);
                            this.backup(shared);
                            if (root.getElementsByTagName("*").getLength() > 0) {
                                try (OutputStream os = new FileOutputStream(file)) {
                                    XMLUtil.write(doc, os, "UTF-8");
                                }
                            } else {
                                file.delete();
                            }
                            return true;
                        }
                    } catch (IOException | SAXException | DOMException ex) {
                        log.error("Cannot remove {} from {}", elementName, file, ex);
                    }
                }
                return false;
            }
        }

        private void backup(boolean shared) {
            final File file = JmriConfigurationProvider.this.getConfigurationFile(shared);
            if (!(shared ? JmriConfigurationProvider.this.sharedBackedUp : JmriConfigurationProvider.this.privateBackedUp) && file.exists()) {
                log.debug("Backing up {}", file);
                try {
                    FileUtil.backup(file);
                    if (shared) {
                        JmriConfigurationProvider.this.sharedBackedUp = true;
                    } else {
                        JmriConfigurationProvider.this.privateBackedUp = true;
                    }
                } catch (IOException ex) {
                    log.error("Error backing up {}", file, ex);
                }
            }
        }

    }
}
