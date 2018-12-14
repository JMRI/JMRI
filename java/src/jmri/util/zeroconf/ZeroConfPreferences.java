package jmri.util.zeroconf;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.beans.PreferencesBean;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences manager for ZeroConf networking. Note that this immediately sets
 * the preference when changed, although that change may not take effect until
 * after JMRI restarts.
 *
 * @author Randall Wood (C) 2018
 */
public class ZeroConfPreferences extends PreferencesBean {

    private boolean useIPv4;
    private boolean useIPv6;
    private boolean useLoopback;
    private boolean useLinkLocal;
    // API constants
    /**
     * Deprecated name in profile.properties.
     *
     * @deprecated since 4.15.1; use {@link #USE_IP_V4} instead
     */
    @Deprecated
    private static final String IPv4 = "IPv4";
    /**
     * Deprecated name in profile.properties.
     *
     * @deprecated since 4.15.1; use {@link #USE_IP_V6} instead
     */
    @Deprecated
    private static final String IPv6 = "IPv6";
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
        Preferences localPreferences = ProfileUtils.getPreferences(profile, this.getClass(), false);
        Preferences sharedPreferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
        this.useIPv4 = localPreferences.getBoolean(USE_IP_V4, sharedPreferences.getBoolean(IPv4, true));
        this.useIPv6 = localPreferences.getBoolean(USE_IP_V6, sharedPreferences.getBoolean(IPv6, true));
        this.useLoopback = localPreferences.getBoolean(USE_LOOPBACK, false);
        this.useLinkLocal = localPreferences.getBoolean(USE_LINK_LOCAL, false);
    }

    public boolean isUseIPv4() {
        return useIPv4;
    }

    public void setUseIPv4(boolean useIPv4) {
        boolean old = this.useIPv4;
        this.useIPv4 = useIPv4;
        savePreferences(getProfile());
        firePropertyChange(IPv4, old, useIPv4);
    }

    public boolean isUseIPv6() {
        return useIPv6;
    }

    public void setUseIPv6(boolean useIPv6) {
        boolean old = this.useIPv6;
        this.useIPv6 = useIPv6;
        savePreferences(getProfile());
        firePropertyChange(IPv6, old, useIPv6);
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
        Preferences localPreferences = ProfileUtils.getPreferences(profile, this.getClass(), false);
        localPreferences.putBoolean(USE_IP_V4, useIPv4);
        localPreferences.putBoolean(USE_IP_V6, useIPv6);
        localPreferences.putBoolean(USE_LOOPBACK, useLoopback);
        localPreferences.putBoolean(USE_LINK_LOCAL, useLinkLocal);
        try {
            localPreferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences", ex);
        }
        try {
            Preferences sharedPreferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
            sharedPreferences.remove(IPv4);
            sharedPreferences.remove(IPv6);
        } catch (IllegalStateException ex) {
            log.error("Unable to remove no-longer-use preferences", ex);
        }
    }

}
