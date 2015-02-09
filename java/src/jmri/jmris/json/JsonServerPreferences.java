package jmri.jmris.json;

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

public class JsonServerPreferences extends Bean {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "JSONServerPreferences"; // NOI18N
    static final String HEARTBEAT_INTERVAL = "heartbeatInterval"; // NOI18N
    static final String PORT = "port"; // NOI18N
    // initial defaults if prefs not found
    private int heartbeatInterval = 15000;
    private int port = DEFAULT_PORT;
    // as loaded prefences
    private int asLoadedHeartbeatInterval = 15000;
    private int asLoadedPort = DEFAULT_PORT;
    private static Logger log = LoggerFactory.getLogger(JsonServerPreferences.class);

    public JsonServerPreferences(String fileName) {
        openFile(fileName);
    }

    public JsonServerPreferences() {
    }

    public void load(Element child) {
        Attribute a;
        if ((a = child.getAttribute(HEARTBEAT_INTERVAL)) != null) {
            try {
                this.setHeartbeatInterval(a.getIntValue());
                this.asLoadedHeartbeatInterval = this.getHeartbeatInterval();
            } catch (DataConversionException e) {
                this.setHeartbeatInterval(15000);
                log.error("Unable to read heartbeat interval. Setting to default value.", e);
            }
        }
        if ((a = child.getAttribute(PORT)) != null) {
            try {
                this.setPort(a.getIntValue());
                this.asLoadedPort = this.getPort();
            } catch (DataConversionException e) {
                this.setPort(2056);
                log.error("Unable to read port. Setting to default value.", e);
            }
        }
    }

    public boolean compareValuesDifferent(JsonServerPreferences prefs) {
        if (this.getHeartbeatInterval() != prefs.getHeartbeatInterval()) {
            return true;
        }
        return this.getPort() != prefs.getPort();
    }

    public void apply(JsonServerPreferences prefs) {
        this.setHeartbeatInterval(prefs.getHeartbeatInterval());
        this.setPort(prefs.getPort());
    }

    public Element store() {
        Element prefs = new Element(XML_PREFS_ELEMENT);
        prefs.setAttribute(HEARTBEAT_INTERVAL, Integer.toString(this.getHeartbeatInterval()));
        prefs.setAttribute(PORT, Integer.toString(this.getPort()));
        // asLoadedHeartbeatInterval should only be reset if the heartbeat
        // interval can be updated without requiring a restart
        // this.asLoadedHeartbeatInterval = this.getHeartbeatInterval();
        return prefs;
    }
    private String fileName;

    public final void openFile(String fileName) {
        this.fileName = fileName;
        JsonServerPreferencesXml prefsXml = new JsonServerPreferences.JsonServerPreferencesXml();
        File file = new File(this.fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (java.io.FileNotFoundException ea) {
            log.info("Could not find JSON Server preferences file.  Normal if preferences have not been saved before.");
            root = null;
        } catch (IOException | JDOMException eb) {
            log.error("Exception while loading JSON server preferences: {}", eb.getLocalizedMessage());
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
                log.debug("Creating new JSON Server prefs file: {}", fileName);
            }
        } catch (IOException ea) {
            log.error("Could not create JSON Server preferences file.");
        }

        try {
            xmlFile.writeXML(file, XmlFile.newDocument(store()));
        } catch (IOException eb) {
            log.warn("Exception in storing JSON Server xml: {}", eb.getLocalizedMessage());
        }
    }

    public boolean isDirty() {
        return (this.getHeartbeatInterval() != this.asLoadedHeartbeatInterval
                || this.getPort() != this.asLoadedPort);
    }

    public boolean isRestartRequired() {
        // Once the JsonServer heartbeat interval can be updated, return true
        // only if the server port changes.
        return (this.getHeartbeatInterval() != this.asLoadedHeartbeatInterval
                || this.getPort() != this.asLoadedPort);
    }

    public int getHeartbeatInterval() {
        return this.heartbeatInterval;
    }

    public void setHeartbeatInterval(int value) {
        this.heartbeatInterval = value;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int value) {
        this.port = value;
    }

    public static class JsonServerPreferencesXml extends XmlFile {
    }
}
