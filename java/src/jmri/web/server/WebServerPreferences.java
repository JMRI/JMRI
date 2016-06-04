package jmri.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.InstanceManager;
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

/**
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
public class WebServerPreferences extends Bean {

    // preferences elements
    public static final String DisallowedFrames = "disallowedFrames"; // NOI18N
    public static final String WebServerPreferences = "WebServerPreferences"; // NOI18N
    public static final String Frame = "frame"; // NOI18N
    public static final String Port = "port"; // NOI18N
    public static final String ClickDelay = "clickDelay"; // NOI18N
    public static final String RefreshDelay = "refreshDelay"; // NOI18N
    public static final String UseAjax = "useAjax"; // NOI18N
    public static final String Simple = "simple"; // NOI18N
    public static final String RailRoadName = "railRoadName"; // NOI18N
    public static final String AllowRemoteConfig = "allowRemoteConfig"; // NOI18N
    public static final String ReadonlyPower = "readonlyPower"; // NOI18N
    public static final String DISABLE_FRAME_SERVER = "disableFrames"; // NOI18N
    public static final String REDIRECT_FRAMES = "redirectFramesToPanels"; // NOI18N
    // properties
    public static final String DIRTY = "dirty"; // NOI18N
    public static final String RESTART_REQUIRED = "restartRequired"; // NOI18N

    // Flag that prefs have not been saved
    private boolean isDirty = false;
    // flag that changed prefs cannot be applied before restarting
    private boolean restartRequired = false;
    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean useAjax = true;
    private boolean simple = false;
    private final ArrayList<String> disallowedFrames = new ArrayList<>(Arrays.asList(Bundle.getMessage("DefaultDisallowedFrames").split(";")));
    private String railRoadName = Bundle.getMessage("DefaultRailroadName");
    private boolean allowRemoteConfig = false;
    private boolean readonlyPower = true;
    private int port = 12080;
    private boolean disableFrames = false;
    private boolean redirectFramesToPanels = true;
    private static Logger log = LoggerFactory.getLogger(WebServerPreferences.class.getName());

    public WebServerPreferences(String fileName) {
        boolean migrate = false;
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
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
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    public static WebServerPreferences getDefault() {
        if (InstanceManager.getDefault(WebServerPreferences.class) == null) {
            InstanceManager.setDefault(WebServerPreferences.class, new WebServerPreferences());
        }
        return InstanceManager.getDefault(WebServerPreferences.class);
    }

    private void readPreferences(Preferences sharedPreferences) {
        this.allowRemoteConfig = sharedPreferences.getBoolean(AllowRemoteConfig, this.allowRemoteConfig);
        this.clickDelay = sharedPreferences.getInt(ClickDelay, this.clickDelay);
        this.simple = sharedPreferences.getBoolean(Simple, this.simple);
        this.railRoadName = sharedPreferences.get(RailRoadName, this.railRoadName);
        this.readonlyPower = sharedPreferences.getBoolean(ReadonlyPower, this.readonlyPower);
        this.refreshDelay = sharedPreferences.getInt(RefreshDelay, this.refreshDelay);
        this.useAjax = sharedPreferences.getBoolean(UseAjax, this.useAjax);
        this.disableFrames = sharedPreferences.getBoolean(DISABLE_FRAME_SERVER, this.disableFrames);
        this.redirectFramesToPanels = sharedPreferences.getBoolean(REDIRECT_FRAMES, this.redirectFramesToPanels);
        try {
            Preferences frames = sharedPreferences.node(DisallowedFrames);
            if (frames.keys().length != 0) {
                this.disallowedFrames.clear();
                for (String key : frames.keys()) { // throws BackingStoreException
                    this.disallowedFrames.add(frames.get(key, null));
                }
            }
        } catch (BackingStoreException ex) {
            // this is expected if sharedPreferences have not been written previously,
            // so do nothing.
        }
        this.port = sharedPreferences.getInt(Port, this.port);
    }

    public void load(Element child) {
        Attribute a;
        a = child.getAttribute(ClickDelay);
        if (a != null) {
            try {
                setClickDelay(a.getIntValue());
            } catch (DataConversionException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        a = child.getAttribute(RefreshDelay);
        if (a != null) {
            try {
                setRefreshDelay(a.getIntValue());
            } catch (DataConversionException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        a = child.getAttribute(UseAjax);
        if (a != null) {
            setUseAjax(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(Simple);
        if (a != null) {
            setSimple(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(AllowRemoteConfig);
        if (a != null) {
            setAllowRemoteConfig(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(ReadonlyPower);
        if (a != null) {
            setReadonlyPower(Boolean.parseBoolean(a.getValue()));
        }
        a = child.getAttribute(Port);
        if (a != null) {
            try {
                setPort(a.getIntValue());
            } catch (DataConversionException ex) {
                setPort(12080);
                log.error("Unable to read port. Setting to default value.", ex);
            }
        }
        a = child.getAttribute(RailRoadName);
        if (a != null) {
            setRailRoadName(a.getValue());
        }
        Element df = child.getChild(DisallowedFrames);
        if (df != null) {
            this.disallowedFrames.clear();
            df.getChildren(Frame).stream().forEach((f) -> {
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
        return !getRailRoadName().equals(prefs.getRailRoadName());
    }

    public void apply(WebServerPreferences prefs) {
        setClickDelay(prefs.getClickDelay());
        setRefreshDelay(prefs.getRefreshDelay());
        setUseAjax(prefs.isUseAjax());
        this.setAllowRemoteConfig(prefs.allowRemoteConfig());
        this.setReadonlyPower(prefs.isReadonlyPower());
        setDisallowedFrames(prefs.getDisallowedFrames());
        setPort(prefs.getPort());
        setRailRoadName(prefs.getRailRoadName());
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
        Preferences sharedPreferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(), this.getClass(), true);
        sharedPreferences.putInt(ClickDelay, this.getClickDelay());
        sharedPreferences.putInt(RefreshDelay, this.getRefreshDelay());
        sharedPreferences.putBoolean(UseAjax, this.isUseAjax());
        sharedPreferences.putBoolean(Simple, this.isSimple());
        sharedPreferences.putBoolean(AllowRemoteConfig, this.allowRemoteConfig());
        sharedPreferences.putBoolean(ReadonlyPower, this.isReadonlyPower());
        sharedPreferences.put(RailRoadName, getRailRoadName());
        sharedPreferences.putBoolean(DISABLE_FRAME_SERVER, this.isDisableFrames());
        sharedPreferences.putBoolean(REDIRECT_FRAMES, this.redirectFramesToPanels);
        Preferences node = sharedPreferences.node(DisallowedFrames);
        this.disallowedFrames.stream().forEach((frame) -> {
            node.put(Integer.toString(this.disallowedFrames.indexOf(frame)), frame);
        });
        sharedPreferences.putInt(Port, this.getPort());
        try {
            sharedPreferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Exception while saving web server preferences", ex);
        }
        setIsDirty(false);  //  Resets only when stored
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setIsDirty(boolean value) {
        boolean old = this.isDirty;
        if (old != value) {
            this.isDirty = value;
            this.firePropertyChange(DIRTY, old, value);
        }
    }

    public int getClickDelay() {
        return clickDelay;
    }

    public void setClickDelay(int value) {
        int old = this.clickDelay;
        if (old != value) {
            this.clickDelay = value;
            this.firePropertyChange(ClickDelay, old, value);
        }
    }

    public int getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(int value) {
        int old = this.refreshDelay;
        if (old != value) {
            this.refreshDelay = value;
            this.firePropertyChange(RefreshDelay, old, value);
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
            this.firePropertyChange(UseAjax, old, useAjax);
        }
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean value) {
        boolean old = this.simple;
        if (old != value) {
            this.simple = value;
            this.firePropertyChange(Simple, old, value);
        }
    }

    public boolean allowRemoteConfig() {
        return this.allowRemoteConfig;
    }

    public void setAllowRemoteConfig(boolean value) {
        boolean old = this.allowRemoteConfig;
        if (old != value) {
            this.allowRemoteConfig = value;
            this.firePropertyChange(AllowRemoteConfig, old, value);
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

    public void setDisallowedFrames(String[] value) {
        if (!Arrays.equals(this.getDisallowedFrames(), value)) {
            this.disallowedFrames.clear();
            this.disallowedFrames.addAll(Arrays.asList(value));
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        int old = this.port;
        if (old != value) {
            this.port = value;
            this.firePropertyChange(Port, old, value);
            this.setRestartRequired();
        }
    }

    /**
     * @return the railRoadName
     */
    public String getRailRoadName() {
        return railRoadName;
    }

    /**
     * @param railRoadName the railRoadName to set
     */
    public void setRailRoadName(String railRoadName) {
        String old = this.railRoadName;
        if ((old != null && !old.equals(railRoadName)) || railRoadName != null) {
            if (railRoadName != null) {
                this.railRoadName = railRoadName;
            } else {
                this.railRoadName = Bundle.getMessage("DefaultRailroadName");
            }
            this.firePropertyChange(RailRoadName, old, this.railRoadName);
        }
    }

    /**
     * Test if the railroad name has been set by user.
     *
     * @return true if user has not set the railroad name.
     */
    public boolean isDefaultRailroadName() {
        return this.getRailRoadName().equals(Bundle.getMessage("DefaultRailroadName"));
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

    /**
     * Check if some preferences cannot be applied without restarting JMRI.
     *
     * @return true if JMRI needs to be restarted, false otherwise.
     */
    public boolean isRestartRequired() {
        return this.restartRequired;
    }

    /**
     * Set if restart needs to be required for some preferences to take effect.
     *
     * @param restartRequired true if JMRI needs to be restarted.
     */
    private void setRestartRequired() {
        if (!this.restartRequired) {
            this.restartRequired = true;
            this.firePropertyChange(RESTART_REQUIRED, false, true);
        }
    }

    private static class WebServerPreferencesXml extends XmlFile {
    }
}
