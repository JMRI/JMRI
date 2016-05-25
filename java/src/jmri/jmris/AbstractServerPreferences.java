package jmri.jmris;

import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.beans.Bean;
import jmri.jmrit.XmlFile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
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
        boolean migrate = false;
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        try {
            if (sharedPreferences.keys().length == 0) {
                log.info("No preferences exist.");
                migrate = true;
            }
        } catch (BackingStoreException ex) {
            log.info("No preferences file exists.");
            migrate = true;
        }
        if (migrate) {
            if (fileName != null) {
                this.openFile(fileName);
            } else {
                migrate = false;
            }
        }
        this.readPreferences(sharedPreferences);
        if (migrate) {
            try {
                log.info("Migrating from old preferences in {} to new format in {}.", fileName, FileUtil.getAbsoluteFilename("profile:profile"));
                sharedPreferences.sync();
            } catch (BackingStoreException ex) {
                log.error("Unable to write preferences.", ex);
            }
        }
    }

    public AbstractServerPreferences() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    protected void readPreferences(Preferences sharedPreferences) {
        this.setPort(sharedPreferences.getInt(PORT, this.getDefaultPort()));
        this.asLoadedPort = this.getPort();
    }

    public void load(Element child) {
        Attribute a;
        a = child.getAttribute(PORT);
        if (a != null) {
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
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        sharedPreferences.putInt(PORT, this.port);
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

    private static class AbstractServerPreferencesXml extends XmlFile {
    }
}
