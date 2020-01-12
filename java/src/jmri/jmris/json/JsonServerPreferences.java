package jmri.jmris.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.InstanceInitializer;
import jmri.beans.Bean;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.XmlFile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonServerPreferences extends Bean {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "JSONServerPreferences"; // NOI18N
    static final String HEARTBEAT_INTERVAL = "heartbeatInterval"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String VALIDATE_CLIENT = "validateClientMessages"; // NOI18N
    static final String VALIDATE_SERVER = "validateServerMessages"; // NOI18N
    // initial defaults if prefs not found
    private int heartbeatInterval = 15000;
    private int port = DEFAULT_PORT;
    private boolean validateClientMessages = false;
    private boolean validateServerMessages = false;
    // as loaded prefences
    private int asLoadedHeartbeatInterval = 15000;
    private int asLoadedPort = DEFAULT_PORT;
    private final static Logger log = LoggerFactory.getLogger(JsonServerPreferences.class);

    public JsonServerPreferences(String fileName) {
        boolean migrate = false;
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        try {
            if (sharedPreferences.keys().length == 0) {
                log.info("No JsonServer preferences exist.");
                migrate = true;
            }
        } catch (BackingStoreException ex) {
            log.info("No preferences file exists.");
            migrate = true;
        }
        if (migrate) {
            if (fileName != null) {
                try {
                    this.openFile(fileName);
                } catch (FileNotFoundException ex) {
                    migrate = false;
                }
            } else {
                migrate = false;
            }
        }
        this.readPreferences(sharedPreferences);
        if (migrate) {
            try {
                log.info("Migrating from old JsonServer preferences in {} to new format in {}.", fileName, FileUtil.getAbsoluteFilename("profile:profile"));
                sharedPreferences.sync();
            } catch (BackingStoreException ex) {
                log.error("Unable to write JsonServer preferences.", ex);
            }
        }
    }

    public JsonServerPreferences() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        this.setHeartbeatInterval(sharedPreferences.getInt(HEARTBEAT_INTERVAL, this.getHeartbeatInterval()));
        this.setPort(sharedPreferences.getInt(PORT, this.getPort()));
        this.setValidateClientMessages(sharedPreferences.getBoolean(VALIDATE_CLIENT, this.getValidateClientMessages()));
        this.setValidateServerMessages(sharedPreferences.getBoolean(VALIDATE_SERVER, this.getValidateServerMessages()));
        this.asLoadedHeartbeatInterval = this.getHeartbeatInterval();
        this.asLoadedPort = this.getPort();
    }

    public void load(Element child) {
        Attribute a;
        a = child.getAttribute(HEARTBEAT_INTERVAL);
        if (a != null) {
            try {
                this.setHeartbeatInterval(a.getIntValue());
                this.asLoadedHeartbeatInterval = this.getHeartbeatInterval();
            } catch (DataConversionException e) {
                this.setHeartbeatInterval(15000);
                log.error("Unable to read heartbeat interval. Setting to default value.", e);
            }
        }
        a = child.getAttribute(PORT);
        if (a != null) {
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

    public final void openFile(String fileName) throws FileNotFoundException {
        JsonServerPreferencesXml prefsXml = new JsonServerPreferences.JsonServerPreferencesXml();
        File file = new File(fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (FileNotFoundException ea) {
            log.info("Could not find JSON Server preferences file.  Normal if preferences have not been saved before.");
            throw ea;
        } catch (IOException | JDOMException eb) {
            log.error("Exception while loading JSON server preferences: {}", eb.getLocalizedMessage());
            root = null;
        }
        if (root != null) {
            this.load(root);
        }
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        sharedPreferences.putInt(HEARTBEAT_INTERVAL, this.heartbeatInterval);
        sharedPreferences.putInt(PORT, this.port);
        sharedPreferences.putBoolean(VALIDATE_CLIENT, this.validateClientMessages);
        sharedPreferences.putBoolean(VALIDATE_SERVER, this.validateServerMessages);
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

    /**
     * Validate that messages from clients are schema valid.
     *
     * @return true if client messages should be validated; false otherwise
     */
    public boolean getValidateClientMessages() {
        return this.validateClientMessages;
    }

    public void setValidateClientMessages(boolean validate) {
        this.validateClientMessages = validate;
    }

    /**
     * Validate that messages from the server are schema valid.
     *
     * @return true if server messages should be validated; false otherwise
     */
    public boolean getValidateServerMessages() {
        return this.validateServerMessages;
    }

    public void setValidateServerMessages(boolean validate) {
        this.validateServerMessages = validate;
    }

    private static class JsonServerPreferencesXml extends XmlFile {
    }

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(JsonServerPreferences.class)) {
                String fileName = FileUtil.getUserFilesPath() + "networkServices" + File.separator + "JsonServerPreferences.xml"; // NOI18N
                if ((new File(fileName)).exists()) {
                    return new JsonServerPreferences(fileName);
                } else {
                    return new JsonServerPreferences();
                }
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(JsonServerPreferences.class);
            return set;
        }
    }
}
