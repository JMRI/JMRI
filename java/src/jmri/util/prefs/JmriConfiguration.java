package jmri.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jmri.profile.AuxiliaryConfiguration;
import jmri.util.FileUtil;
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

    private final static Logger log = LoggerFactory.getLogger(JmriConfiguration.class);

    JmriConfiguration() {
    }

    protected abstract File getConfigurationFile(boolean shared);

    protected abstract boolean isSharedBackedUp();

    protected abstract void setSharedBackedUp(boolean backedUp);

    protected abstract boolean isPrivateBackedUp();

    protected abstract void setPrivateBackedUp(boolean backedUp);

    @Override
    public Element getConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
        synchronized (this) {
            File file = this.getConfigurationFile(shared);
            if (file != null && file.canRead()) {
                try {
                    try (final InputStream is = new FileInputStream(file)) {
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
            File file = this.getConfigurationFile(shared);
            Document doc = null;
            if (file != null && file.canRead()) {
                try {
                    try (final InputStream is = new FileInputStream(file)) {
                        InputSource input = new InputSource(is);
                        input.setSystemId(file.toURI().toURL().toString());
                        doc = XMLUtil.parse(input, false, true, null, null);
                    }
                } catch (IOException | SAXException ex) {
                    log.warn("Cannot parse {}", file, ex);
                }
            }
            if (doc == null) {
                doc = XMLUtil.createDocument("auxiliary-configuration", JmriConfigurationProvider.NAMESPACE, null, null); // NOI18N
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
                try (final OutputStream os = new FileOutputStream(file)) {
                    XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                }
            } catch (IOException ex) {
                log.error("Cannot write {}", file, ex);
            }
        }
    }

    @Override
    public boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        synchronized (this) {
            File file = this.getConfigurationFile(shared);
            if (file.canWrite()) {
                try {
                    Document doc;
                    try (final InputStream is = new FileInputStream(file)) {
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
                            // NOI18N
                            try (final OutputStream os = new FileOutputStream(file)) {
                                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                            }
                        } else if (!file.delete()) {
                            log.debug("Unable to delete {}", file);
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
