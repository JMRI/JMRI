package jmri.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import jmri.profile.AuxiliaryConfiguration;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;
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
 *
 * @author Randall Wood
 */
public abstract class JmriConfiguration implements AuxiliaryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JmriConfiguration.class);

    JmriConfiguration() {
    }

    protected abstract File getConfigurationFile(boolean shared);

    protected abstract boolean isSharedBackedUp();

    protected abstract void setSharedBackedUp(boolean backedUp);

    protected abstract boolean isPrivateBackedUp();

    protected abstract void setPrivateBackedUp(boolean backedUp);

    Document persistentDocument; 
    
    
    Document getDocumentFromFile(final boolean shared) {
        File file = this.getConfigurationFile(shared);
        if (file != null && file.canRead()) {
            try {
                try (final InputStream is = new FileInputStream(file)) {
                    InputSource input = new InputSource(is);
                    input.setSystemId(file.toURI().toURL().toString());
                    
                    log.debug("Start read of user-interface.xml");
                    
                    var document = XMLUtil.parse(input, false, true, null, null);
                    log.debug("End read of user-interface.xml");
                    return document;
                }
            } catch (IOException | SAXException | IllegalArgumentException ex) {
                log.warn("Cannot parse {}", file, ex);
                return null;
            }
        }
        return null;
    }
    
    @Override
    public Element getConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            synchronized (this) {
                if (persistentDocument == null) {
                    persistentDocument = getDocumentFromFile(shared);
                }
                if (persistentDocument == null) {
                    return null;
                }
                Element root = persistentDocument.getDocumentElement();
                return XMLUtil.findElement(root, elementName, namespace);
            }
        });
    }

    @Override
    public void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
        ThreadingUtil.runOnGUI(() -> {
            synchronized (this) {
                String elementName = fragment.getLocalName();
                String namespace = fragment.getNamespaceURI();
                if (namespace == null) {
                    throw new IllegalArgumentException();
                }
                if (persistentDocument == null) {
                    persistentDocument = getDocumentFromFile(shared);
                }
                if (persistentDocument == null) {
                    persistentDocument = XMLUtil.createDocument("auxiliary-configuration", JmriConfigurationProvider.NAMESPACE, null, null); // NOI18N
                }
                Element root = persistentDocument.getDocumentElement();
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
                File file = this.getConfigurationFile(shared);
                try {
                    this.backup(shared);
                    try (final OutputStream os = new FileOutputStream(file)) {
                        log.debug("Start write of user-interface.xml");
                        try {
                            XMLUtil.write(persistentDocument, os, StandardCharsets.UTF_8.name());
                        } catch (IOException ex) {
                            log.error("Cannot write {}", file, ex);
                        }
                        log.debug("End write of user-interface.xml");
                        os.flush();
                    }
                } catch (IOException ex) {
                    log.error("Cannot write {}", file, ex);
                }
            }
        });
    }

    @Override
    public boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            synchronized (this) {
                File file = this.getConfigurationFile(shared);
                if (file.canWrite()) {
                    try {
                        if (persistentDocument == null) {
                            persistentDocument = getDocumentFromFile(shared);
                        }
                        Element root = persistentDocument.getDocumentElement();
                        Element toRemove = XMLUtil.findElement(root, elementName, namespace);
                        if (toRemove != null) {
                            root.removeChild(toRemove);
                            this.backup(shared);
                            if (root.getElementsByTagName("*").getLength() > 0) {
                                // NOI18N
                                try (final OutputStream os = new FileOutputStream(file)) {
                                    log.debug("Start write of user-interface.xml");
                                    XMLUtil.write(persistentDocument, os, StandardCharsets.UTF_8.name());
                                    log.debug("End write of user-interface.xml");
                                }
                            } else if (!file.delete()) {
                                log.warn("Unable to delete {}", file);
                            }
                            return true;
                        }
                    } catch (IOException | DOMException ex) {
                        log.error("Cannot remove {} from {}", elementName, file, ex);
                    }
                }
                return false;
            }
        });
    }

    private void backup(boolean shared) {
        final File file = this.getConfigurationFile(shared);
        if (!(shared ? this.isSharedBackedUp() : this.isPrivateBackedUp()) && file.exists()) {
            log.debug("Backing up {}", file);
            try {
                FileUtil.backup(file);
                if (shared) {
                    this.setSharedBackedUp(true);
                } else {
                    this.setPrivateBackedUp(true);
                }
            } catch (IOException ex) {
                log.error("Error backing up {}", file, ex);
            }
        }
    }

}
