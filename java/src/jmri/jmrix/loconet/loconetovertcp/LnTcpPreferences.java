package jmri.jmrix.loconet.loconetovertcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.InstanceManager;
import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences for the LocoNet over TCP server.
 *
 * @author Randall Wood (C) 2017
 */
public class LnTcpPreferences extends PreferencesBean {

    public final static String PORT = jmri.web.server.WebServerPreferences.PORT;
    private static final String PORT_NUMBER_KEY = "PortNumber";
    private static final String SETTINGS_FILE_NAME = "LocoNetOverTcpSettings.ini";

    public static LnTcpPreferences getDefault() {
        return InstanceManager.getOptionalDefault(LnTcpPreferences.class).orElseGet(() -> {
            return InstanceManager.setDefault(LnTcpPreferences.class, new LnTcpPreferences());
        });
    }

    private int port = 1234;
    private final static Logger log = LoggerFactory.getLogger(LnTcpPreferences.class);

    public LnTcpPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        boolean migrate = false;
        try {
            if (sharedPreferences.keys().length == 0) {
                log.debug("No LocoNetOverTCP preferences exist.");
                migrate = true;
            }
        } catch (BackingStoreException ex) {
            log.debug("No preferences file exists.");
            migrate = true;
        }
        if (!migrate) {
            this.port = sharedPreferences.getInt(PORT, this.getPort());
            this.setIsDirty(false);
        } else {
            Properties settings = new Properties();
            File file = new File(FileUtil.getUserFilesPath(), SETTINGS_FILE_NAME);
            log.debug("Opening settings file {}", file);
            try (FileInputStream stream = new FileInputStream(file)) {
                settings.load(stream);
                this.port = Integer.parseInt(settings.getProperty(PORT_NUMBER_KEY, Integer.toString(this.getPort())));
                this.setIsDirty(true);
            } catch (FileNotFoundException ex) {
                log.debug("old preferences file not found");
            } catch (IOException ex) {
                log.debug("exception reading old preferences file", ex);
            }
        }
    }

    public void savePreferences() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putInt(PORT, this.getPort());
        try {
            sharedPreferences.sync();
            setIsDirty(false);  //  Resets only when stored
        } catch (BackingStoreException ex) {
            log.error("Exception while saving web server preferences", ex);
        }
    }

    boolean isPreferencesValid() {
        return this.port > 0 && this.port < 65536;
    }

    /**
     * Get the port used by the LocoNetOverTCP server.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port used by the LocoNetOverTCP server.
     *
     * @param value the port
     */
    public void setPort(int value) {
        int old = this.port;
        if (old != value) {
            this.port = value;
            this.firePropertyChange(PORT, old, value);
            this.setRestartRequired();
        }
    }

}
