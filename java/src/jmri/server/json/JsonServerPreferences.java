package jmri.server.json;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.InstanceManagerAutoDefault;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import jmri.util.prefs.JmriPreferencesProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonServerPreferences extends Bean implements InstanceManagerAutoDefault {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "JSONServerPreferences"; // NOI18N
    static final String HEARTBEAT_INTERVAL = "heartbeatInterval"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String VALIDATE_CLIENT = "validateClientMessages"; // NOI18N
    static final String VALIDATE_SERVER = "validateServerMessages"; // NOI18N
    // initial defaults if preferences not found
    private int heartbeatInterval = 15000;
    private int portNumber = DEFAULT_PORT;
    private boolean validateClientMessages = false;
    private boolean validateServerMessages = false;
    // as loaded preferences
    private int asLoadedHeartbeatInterval = 15000;
    private int asLoadedPort = DEFAULT_PORT;
    private static final Logger log = LoggerFactory.getLogger(JsonServerPreferences.class);

    public JsonServerPreferences(String fileName) {
        boolean migrate = false;
        Profile activeProfile = ProfileManager.getDefault().getActiveProfile();
        Preferences sharedPreferences = ProfileUtils.getPreferences(activeProfile, getClass(), true);
        migrate = sharedPreferences.get(PORT, null) == null;
        if (migrate) {
            // method is deprecated to discourage use, not for removal
            // using to allow migration from old package to new package
            Preferences oldPreferences = JmriPreferencesProvider.getPreferences(activeProfile, "jmri.jmris.json", true);
            readPreferences(oldPreferences);
        }
        readPreferences(sharedPreferences);
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
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), getClass(), true);
        readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        setHeartbeatInterval(sharedPreferences.getInt(HEARTBEAT_INTERVAL, getHeartbeatInterval()));
        setPort(sharedPreferences.getInt(PORT, getPort()));
        setValidateClientMessages(sharedPreferences.getBoolean(VALIDATE_CLIENT, getValidateClientMessages()));
        setValidateServerMessages(sharedPreferences.getBoolean(VALIDATE_SERVER, getValidateServerMessages()));
        asLoadedHeartbeatInterval = getHeartbeatInterval();
        asLoadedPort = getPort();
    }

    public boolean compareValuesDifferent(JsonServerPreferences prefs) {
        if (getHeartbeatInterval() != prefs.getHeartbeatInterval()) {
            return true;
        }
        return getPort() != prefs.getPort();
    }

    public void apply(JsonServerPreferences prefs) {
        setHeartbeatInterval(prefs.getHeartbeatInterval());
        setPort(prefs.getPort());
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), getClass(), true);
        sharedPreferences.putInt(HEARTBEAT_INTERVAL, heartbeatInterval);
        sharedPreferences.putInt(PORT, portNumber);
        sharedPreferences.putBoolean(VALIDATE_CLIENT, validateClientMessages);
        sharedPreferences.putBoolean(VALIDATE_SERVER, validateServerMessages);
    }

    public boolean isDirty() {
        return (getHeartbeatInterval() != asLoadedHeartbeatInterval
                || getPort() != asLoadedPort);
    }

    public boolean isRestartRequired() {
        // Once the JsonServer heartbeat interval can be updated, return true
        // only if the server port changes.
        return (getHeartbeatInterval() != asLoadedHeartbeatInterval
                || getPort() != asLoadedPort);
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int value) {
        heartbeatInterval = value;
    }

    public int getPort() {
        return portNumber;
    }

    public void setPort(int value) {
        portNumber = value;
    }

    /**
     * Validate that messages from clients are schema valid.
     *
     * @return true if client messages should be validated; false otherwise
     */
    public boolean getValidateClientMessages() {
        return validateClientMessages;
    }

    public void setValidateClientMessages(boolean validate) {
        validateClientMessages = validate;
    }

    /**
     * Validate that messages from the server are schema valid.
     *
     * @return true if server messages should be validated; false otherwise
     */
    public boolean getValidateServerMessages() {
        return validateServerMessages;
    }

    public void setValidateServerMessages(boolean validate) {
        validateServerMessages = validate;
    }
}
