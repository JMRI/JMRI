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

    public LnTcpPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        this.port = sharedPreferences.getInt(PORT, this.getPort());
        this.setIsDirty(false);
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
