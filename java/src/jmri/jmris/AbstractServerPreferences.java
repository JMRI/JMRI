package jmri.jmris;

import java.io.File;
import java.io.IOException;
import jmri.beans.Bean;
import jmri.jmrit.XmlFile;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractServerPreferences extends Bean {

    static final String XML_PREFS_ELEMENT = "AbstractServerPreferences"; // NOI18N
    static final String PORT = "port"; // NOI18N
    private int port;
    // as loaded prefences
    private int asLoadedPort;
    private final static Logger log = LoggerFactory.getLogger(AbstractServerPreferences.class);

    public AbstractServerPreferences(String fileName) {
        port=asLoadedPort=getDefaultPort();
        openFile(fileName);
    }

    public AbstractServerPreferences() {
        port=asLoadedPort=getDefaultPort();
    }

    public void load(Element child) {
        Attribute a;
        if ((a = child.getAttribute(PORT)) != null) {
            try {
                this.setPort(a.getIntValue());
                this.asLoadedPort = this.getPort();
            } catch (DataConversionException e) {
                this.setPort(getDefaultPort());
                log.error("Unable to read port. Setting to default value.", e);
            }
        }
    }

    public boolean compareValuesDifferent(AbstractServerPreferences prefs) {
        return this.getPort() != prefs.getPort();
    }

    public void apply(AbstractServerPreferences prefs) {
        this.setPort(prefs.getPort());
    }

    public Element store() {
        Element prefs = new Element(XML_PREFS_ELEMENT);
        prefs.setAttribute(PORT, Integer.toString(this.getPort()));
        return prefs;
    }
    private String fileName;

    public final void openFile(String fileName) {
        this.fileName = fileName;
        AbstractServerPreferencesXml prefsXml = new AbstractServerPreferences.AbstractServerPreferencesXml();
        File file = new File(this.fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (java.io.FileNotFoundException ea) {
            log.info("Could not find Server preferences file.  Normal if preferences have not been saved before.");
            root = null;
        } catch (IOException | JDOMException eb) {
            log.error("Exception while loading server preferences: {}", eb.getLocalizedMessage());
            root = null;
        }
        if (root != null) {
            this.load(root);
        }
    }

    public void save() {
        if (this.fileName == null) {
            return;
        }

        XmlFile xmlFile = new XmlFile() {
        };
        xmlFile.makeBackupFile(this.fileName);
        File file = new File(this.fileName);
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) {
                    log.warn("Could not create parent directory for prefs file :{}", fileName);
                    return;
                }
            }
            if (file.createNewFile()) {
                log.debug("Creating new Server prefs file: {}", fileName);
            }
        } catch (IOException ea) {
            log.error("Could not create Server preferences file.");
        }

        try {
            xmlFile.writeXML(file, XmlFile.newDocument(store()));
        } catch (IOException eb) {
            log.warn("Exception in storing Server xml: {}", eb.getLocalizedMessage());
        }
    }

    public boolean isDirty() {
        return (this.getPort() != this.asLoadedPort);
    }

    public boolean isRestartRequired() {
        // return true only if the server port changes.
        return (this.getPort() != this.asLoadedPort);
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int value) {
        this.port = value;
    }

    abstract public int getDefaultPort();

    public static class AbstractServerPreferencesXml extends XmlFile {
    }
}
