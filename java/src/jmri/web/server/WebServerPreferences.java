package jmri.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.beans.PreferencesBean;
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

/**
 * @author Randall Wood Copyright (C) 2012, 2017
 */
public class WebServerPreferences extends PreferencesBean {

    // preferences elements
    public static final String DISALLOWED_FRAMES = "disallowedFrames"; // NOI18N
    public static final String WEB_SERVER_PREFERENCES = "WebServerPreferences"; // NOI18N
    public static final String FRAME = "frame"; // NOI18N
    public static final String PORT = "port"; // NOI18N
    public static final String CLICK_DELAY = "clickDelay"; // NOI18N
    public static final String REFRESH_DELAY = "refreshDelay"; // NOI18N
    public static final String USE_AJAX = "useAjax"; // NOI18N
    public static final String SIMPLE = "simple"; // NOI18N
    public static final String RAILROAD_NAME = "railroadName"; // NOI18N
    public static final String ALLOW_REMOTE_CONFIG = "allowRemoteConfig"; // NOI18N
    public static final String READONLY_POWER = "readonlyPower"; // NOI18N
    public static final String DISABLE_FRAME_SERVER = "disableFrames"; // NOI18N
    public static final String REDIRECT_FRAMES = "redirectFramesToPanels"; // NOI18N
    public static final String USE_ZERO_CONF = "useZeroConf"; // NOI18N

    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean useAjax = true;
    private boolean simple = false;
    private final ArrayList<String> disallowedFrames = new ArrayList<>(Arrays.asList(Bundle.getMessage("DefaultDisallowedFrames").split(";")));
    private String railroadName = Bundle.getMessage("DefaultRailroadName");
    private boolean allowRemoteConfig = false;
    private boolean readonlyPower = true;
    private int port = 12080;
    private boolean disableFrames = true;
    private boolean redirectFramesToPanels = true;
    private final static Logger log = LoggerFactory.getLogger(WebServerPreferences.class);
    private boolean useZeroConf = true;

    public WebServerPreferences(String fileName) {
        super(ProfileManager.getDefault().getActiveProfile());
        boolean migrate = false;
        Preferences sharedPreferences = ProfileUtils.getPreferences(super.getProfile(), this.getClass(), true);
        try {
            if (sharedPreferences.keys().length == 0) {
                log.info("No Webserver preferences exist.");
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
                log.info("Migrating from old Webserver preferences in {} to new format in {}.", fileName, FileUtil.getAbsoluteFilename("profile:profile"));
                sharedPreferences.sync();
            } catch (BackingStoreException ex) {
                log.error("Unable to write WebServer preferences.", ex);
            }
        }
    }

    public WebServerPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        this.allowRemoteConfig = sharedPreferences.getBoolean(ALLOW_REMOTE_CONFIG, this.allowRemoteConfig);
        this.clickDelay = sharedPreferences.getInt(CLICK_DELAY, this.clickDelay);
        this.simple = sharedPreferences.getBoolean(SIMPLE, this.simple);
        this.railroadName = sharedPreferences.get(RAILROAD_NAME, this.railroadName);
        this.readonlyPower = sharedPreferences.getBoolean(READONLY_POWER, this.readonlyPower);
        this.refreshDelay = sharedPreferences.getInt(REFRESH_DELAY, this.refreshDelay);
        this.useAjax = sharedPreferences.getBoolean(USE_AJAX, this.useAjax);
        this.disableFrames = sharedPreferences.getBoolean(DISABLE_FRAME_SERVER, this.disableFrames);
        this.redirectFramesToPanels = sharedPreferences.getBoolean(REDIRECT_FRAMES, this.redirectFramesToPanels);
        try {
            Preferences frames = sharedPreferences.node(DISALLOWED_FRAMES);
            if (frames.keys().length != 0) {
                this.disallowedFrames.clear();
                for (String key : frames.keys()) { // throws BackingStoreException
                    String frame = frames.get(key, null);
                    if (frame != null && !frame.trim().isEmpty()) {
                        this.disallowedFrames.add(frame);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            // this is expected if sharedPreferences have not been written previously,
            // so do nothing.
        }
        this.port = sharedPreferences.getInt(PORT, this.port);
        this.useZeroConf = sharedPreferences.getBoolean(USE_ZERO_CONF, this.useZeroConf);
        this.setIsDirty(false);
    }

    public void load(Element child) {
        Attribute a;
        a = child.getAttribute(CLICK_DELAY);
        if (a != null) {
            try {
                setClickDelay(a.getIntValue());
            } catch (DataConversionException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        a = child.getAttribute(REFRESH_DELAY);
        if (a != null) {
            try {
                setRefreshDelay(a.getIntValue());
            } catch (DataConversionException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        a = child.getAttribute(USE_AJAX);
        if (a != null) {
            setUseAjax(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(SIMPLE);
        if (a != null) {
            setSimple(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(ALLOW_REMOTE_CONFIG);
        if (a != null) {
            setAllowRemoteConfig(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(READONLY_POWER);
        if (a != null) {
            setReadonlyPower(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(PORT);
        if (a != null) {
            try {
                setPort(a.getIntValue());
            } catch (DataConversionException ex) {
                setPort(12080);
                log.error("Unable to read port. Setting to default value.", ex);
            }
        }
        a = child.getAttribute(RAILROAD_NAME);
        if (a != null) {
            setRailroadName(a.getValue());
        }
        Element df = child.getChild(DISALLOWED_FRAMES);
        if (df != null) {
            this.disallowedFrames.clear();
            df.getChildren(FRAME).stream().forEach((f) -> {
                this.disallowedFrames.add(f.getText().trim());
            });
        }
    }

    public boolean compareValuesDifferent(WebServerPreferences prefs) {
        if (getClickDelay() != prefs.getClickDelay()) {
            return true;
        }
        if (getRefreshDelay() != prefs.getRefreshDelay()) {
            return true;
        }
        if (isUseAjax() != prefs.isUseAjax()) {
            return true;
        }
        if (this.allowRemoteConfig() != prefs.allowRemoteConfig()) {
            return true;
        }
        if (this.isReadonlyPower() != prefs.isReadonlyPower()) {
            return true;
        }
        if (!(Arrays.equals(getDisallowedFrames(), prefs.getDisallowedFrames()))) {
            return true;
        }
        if (getPort() != prefs.getPort()) {
            return true;
        }
        return !getRailroadName().equals(prefs.getRailroadName());
    }

    public void apply(WebServerPreferences prefs) {
        setClickDelay(prefs.getClickDelay());
        setRefreshDelay(prefs.getRefreshDelay());
        setUseAjax(prefs.isUseAjax());
        this.setAllowRemoteConfig(prefs.allowRemoteConfig());
        this.setReadonlyPower(prefs.isReadonlyPower());
        setDisallowedFrames(prefs.getDisallowedFrames());
        setPort(prefs.getPort());
        setRailroadName(prefs.getRailroadName());
    }

    public final void openFile(String fileName) throws FileNotFoundException {
        WebServerPreferencesXml prefsXml = new WebServerPreferencesXml();
        File file = new File(fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (FileNotFoundException ex) {
            log.debug("Could not find Web Server preferences file. Normal if preferences have not been saved before.");
            throw ex;
        } catch (IOException | JDOMException ex) {
            log.error("Exception while loading web server preferences: " + ex);
            root = null;
        }
        if (root != null) {
            load(root);
        }
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putInt(PORT, this.getPort());
        sharedPreferences.putBoolean(USE_ZERO_CONF, this.isUseZeroConf());
        sharedPreferences.putInt(CLICK_DELAY, this.getClickDelay());
        sharedPreferences.putInt(REFRESH_DELAY, this.getRefreshDelay());
        sharedPreferences.putBoolean(USE_AJAX, this.isUseAjax());
        sharedPreferences.putBoolean(SIMPLE, this.isSimple());
        sharedPreferences.putBoolean(ALLOW_REMOTE_CONFIG, this.allowRemoteConfig());
        sharedPreferences.putBoolean(READONLY_POWER, this.isReadonlyPower());
        sharedPreferences.put(RAILROAD_NAME, getRailroadName());
        sharedPreferences.putBoolean(DISABLE_FRAME_SERVER, this.isDisableFrames());
        sharedPreferences.putBoolean(REDIRECT_FRAMES, this.redirectFramesToPanels);
        try {
            Preferences node = sharedPreferences.node(DISALLOWED_FRAMES);
            this.disallowedFrames.stream().forEach((frame) -> {
                node.put(Integer.toString(this.disallowedFrames.indexOf(frame)), frame);
            });
            if (this.disallowedFrames.size() < node.keys().length) {
                for (int i = node.keys().length - 1; i >= this.disallowedFrames.size(); i--) {
                    node.remove(Integer.toString(i));
                }
            }
            sharedPreferences.sync();
            setIsDirty(false);  //  Resets only when stored
        } catch (BackingStoreException ex) {
            log.error("Exception while saving web server preferences", ex);
        }
    }

    public int getClickDelay() {
        return clickDelay;
    }

    public void setClickDelay(int value) {
        int old = this.clickDelay;
        if (old != value) {
            this.clickDelay = value;
            this.firePropertyChange(CLICK_DELAY, old, value);
        }
    }

    public int getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(int value) {
        int old = this.refreshDelay;
        if (old != value) {
            this.refreshDelay = value;
            this.firePropertyChange(REFRESH_DELAY, old, value);
        }
    }

    public String[] getDisallowedFrames() {
        return this.disallowedFrames.toArray(new String[this.disallowedFrames.size()]);
    }

    public boolean isUseAjax() {
        return useAjax;
    }

    public void setUseAjax(boolean useAjax) {
        boolean old = this.useAjax;
        if (old != useAjax) {
            this.useAjax = useAjax;
            this.firePropertyChange(USE_AJAX, old, useAjax);
        }
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean value) {
        boolean old = this.simple;
        if (old != value) {
            this.simple = value;
            this.firePropertyChange(SIMPLE, old, value);
        }
    }

    public boolean isUseZeroConf() {
        return useZeroConf;
    }

    public void setUseZeroConf(boolean value) {
        boolean old = this.useZeroConf;
        if (old != value) {
            this.useZeroConf = value;
            this.firePropertyChange(USE_ZERO_CONF, old, value);
        }
    }
    public boolean allowRemoteConfig() {
        return this.allowRemoteConfig;
    }

    public void setAllowRemoteConfig(boolean value) {
        boolean old = this.allowRemoteConfig;
        if (old != value) {
            this.allowRemoteConfig = value;
            this.firePropertyChange(ALLOW_REMOTE_CONFIG, old, value);
        }
    }

    /**
     * @return the readonlyPower
     */
    public boolean isReadonlyPower() {
        return readonlyPower;
    }

    /**
     * @param readonlyPower the readonlyPower to set
     */
    public void setReadonlyPower(boolean readonlyPower) {
        this.readonlyPower = readonlyPower;
    }

    public void setDisallowedFrames(String[] disallowedFrames) {
        String[] old = this.getDisallowedFrames();
        if (!Arrays.equals(old, disallowedFrames)) {
            this.disallowedFrames.clear();
            this.disallowedFrames.addAll(Arrays.asList(disallowedFrames));
            this.firePropertyChange(DISALLOWED_FRAMES, old, disallowedFrames);
        }
    }

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

    /**
     * Get the name of the railroad.
     *
     * @return the railroad name
     */
    public String getRailroadName() {
        return railroadName;
    }

    /**
     * Set the railroad name.
     *
     * @param railroadName the railroadName to set
     */
    public void setRailroadName(String railroadName) {
        String old = this.railroadName;
        if ((old != null && !old.equals(railroadName)) || railroadName != null) {
            if (railroadName != null) {
                this.railroadName = railroadName;
            } else {
                this.railroadName = Bundle.getMessage("DefaultRailroadName");
            }
            this.firePropertyChange(RAILROAD_NAME, old, this.railroadName);
        }
    }

    /**
     * Test if the railroad name has been set by user.
     *
     * @return true if user has not set the railroad name.
     */
    public boolean isDefaultRailroadName() {
        return this.getRailroadName().equals(Bundle.getMessage("DefaultRailroadName"));
    }

    /**
     * Get the default railroad name. This method exists solely to support unit
     * testing.
     *
     * @return The default railroad name
     */
    public String getDefaultRailroadName() {
        return Bundle.getMessage("DefaultRailroadName");
    }

    /**
     * @return true if displaying frames in web pages is disabled, false
     *         otherwise
     */
    public boolean isDisableFrames() {
        return disableFrames;
    }

    /**
     * Set whether or not frames are returned when requests for frames are made
     * from web pages.
     *
     * @param disableFrames true to prevent frames from being displayed in web
     *                      pages
     */
    public void setDisableFrames(boolean disableFrames) {
        boolean old = this.disableFrames;
        if (old != disableFrames) {
            this.disableFrames = disableFrames;
            this.firePropertyChange(DISABLE_FRAME_SERVER, old, disableFrames);
        }
    }

    /**
     * Are requests for frames redirected to panels when frames are disabled?
     *
     * @return true if frames should be redirected to panels, false otherwise
     */
    public boolean isRedirectFramesToPanels() {
        return redirectFramesToPanels;
    }

    /**
     * Set whether or not requests for frames should be redirected to panels
     * when frames are disabled.
     *
     * @param redirectFramesToPanels true if frames should be redirected to
     *                               panels, false otherwise
     */
    public void setRedirectFramesToPanels(boolean redirectFramesToPanels) {
        boolean old = this.redirectFramesToPanels;
        if (old != redirectFramesToPanels) {
            this.redirectFramesToPanels = redirectFramesToPanels;
            this.firePropertyChange(REDIRECT_FRAMES, old, this.redirectFramesToPanels);
        }
    }

    private static class WebServerPreferencesXml extends XmlFile {
    }
}
