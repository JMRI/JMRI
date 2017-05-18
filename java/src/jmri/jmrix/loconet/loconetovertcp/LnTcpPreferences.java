package jmri.jmrix.loconet.loconetovertcp;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences for the LocoNet over TCP server.
 *
 * @author Randall Wood (C) 2017
 */
public class LnTcpPreferences extends PreferencesBean {

    public final static String PORT = jmri.web.server.WebServerPreferences.PORT;

    private int port = 1234;
    private final static Logger log = LoggerFactory.getLogger(LnTcpPreferences.class);

    void savePreferences() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
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

    public void setPort(int value) {
        int old = this.port;
        if (old != value) {
            this.port = value;
            this.firePropertyChange(PORT, old, value);
            this.setRestartRequired();
        }
    }

}
