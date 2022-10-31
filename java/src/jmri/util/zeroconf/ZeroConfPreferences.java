package jmri.util.zeroconf;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.beans.PreferencesBean;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences manager for ZeroConf networking.
 * <p>
 * <strong>NOTE:</strong> preferences are immediately changed and stored when
 * set, although not all code that reads these preferences responds to changes
 * in the preferences immediately.
 * <p>
 * <strong>NOTE:</strong> these preferences apply to all JMRI applications and
 * all profiles on the computer on which they are set.
 *
 * @author Randall Wood (C) 2018
 */
public class ZeroConfPreferences extends PreferencesBean {

    // Setting and default values
    private boolean useIPv4 = true;
    private boolean useIPv6 = true;
    private boolean useLoopback = false;
    private boolean useLinkLocal = true;

    // API constants
    /**
     * Preferences name in profile.properties and property to subscribe to
     * notification changes for.
     * <p>
     * {@value #USE_IP_V4}
     */
    public static final String USE_IP_V4 = "useIPv4";
    /**
     * Preferences name in profile.properties and property to subscribe to
     * notification changes for.
     * <p>
     * {@value #USE_IP_V6}
     */
    public static final String USE_IP_V6 = "useIPv6";
    /**
     * Preferences name in profile.properties and property to subscribe to
     * notification changes for.
     * <p>
     * {@value #USE_LOOPBACK}
     */
    public static final String USE_LOOPBACK = "useLoopback";
    /**
     * Preferences name in profile.properties and property to subscribe to
     * notification changes for.
     * <p>
     * {@value #USE_LINK_LOCAL}
     */
    public static final String USE_LINK_LOCAL = "useLinkLocal";

    private final static Logger log = LoggerFactory.getLogger(ZeroConfPreferences.class);

    public ZeroConfPreferences(Profile profile) {
        super(profile);
        Preferences localPreferences = ProfileUtils.getPreferences(null, this.getClass(), false);

        // search all-profile local for IPv4 and IPv6 use control
        this.useIPv4 = localPreferences.getBoolean(USE_IP_V4, this.useIPv4);
        this.useIPv6 = localPreferences.getBoolean(USE_IP_V6, this.useIPv6);

        this.useLinkLocal = localPreferences.getBoolean(USE_LINK_LOCAL, this.useLinkLocal);
        this.useLoopback = localPreferences.getBoolean(USE_LOOPBACK, this.useLoopback);
    }

    public boolean isUseIPv4() {
        return useIPv4;
    }

    public void setUseIPv4(boolean useIPv4) {
        boolean old = this.useIPv4;
        this.useIPv4 = useIPv4;
        savePreferences(getProfile());
        firePropertyChange(USE_IP_V4, old, useIPv4);
    }

    public boolean isUseIPv6() {
        return useIPv6;
    }

    public void setUseIPv6(boolean useIPv6) {
        boolean old = this.useIPv6;
        this.useIPv6 = useIPv6;
        savePreferences(getProfile());
        firePropertyChange(USE_IP_V6, old, useIPv6);
    }

    public boolean isUseLoopback() {
        return useLoopback;
    }

    public void setUseLoopback(boolean useLoopback) {
        boolean old = this.useLoopback;
        this.useLoopback = useLoopback;
        savePreferences(getProfile());
        firePropertyChange(USE_LOOPBACK, old, useIPv6);
    }

    public boolean isUseLinkLocal() {
        return useLinkLocal;
    }

    public void setUseLinkLocal(boolean useLinkLocal) {
        boolean old = this.useLinkLocal;
        this.useLinkLocal = useLinkLocal;
        savePreferences(getProfile());
        firePropertyChange(USE_LINK_LOCAL, old, useLinkLocal);
    }

    public void savePreferences(Profile profile) {
        Preferences localPreferences = ProfileUtils.getPreferences(null, this.getClass(), false);
        localPreferences.putBoolean(USE_IP_V4, useIPv4);
        localPreferences.putBoolean(USE_IP_V6, useIPv6);
        localPreferences.putBoolean(USE_LINK_LOCAL, useLinkLocal);
        localPreferences.putBoolean(USE_LOOPBACK, useLoopback);
        try {
            localPreferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences", ex);
        }
    }

}
