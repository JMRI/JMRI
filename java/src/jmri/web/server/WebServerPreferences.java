package jmri.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    // XML elements
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

    // Flag that prefs have not been saved:
    private boolean isDirty = false;
    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean useAjax = true;
    private boolean plain = false;
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
        this.plain = sharedPreferences.getBoolean(Simple, this.plain);
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
            setPlain(Boolean.parseBoolean(a.getValue()));
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
                this.addDisallowedFrame(f.getText().trim());
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
        if (useAjax() != prefs.useAjax()) {
            return true;
        }
        if (this.allowRemoteConfig() != prefs.allowRemoteConfig()) {
            return true;
        }
        if (this.isReadonlyPower() != prefs.isReadonlyPower()) {
            return true;
        }
        if (!(getDisallowedFrames().equals(prefs.getDisallowedFrames()))) {
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
        setUseAjax(prefs.useAjax());
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
        sharedPreferences.putBoolean(UseAjax, this.useAjax());
        sharedPreferences.putBoolean(Simple, this.isPlain());
        sharedPreferences.putBoolean(AllowRemoteConfig, this.allowRemoteConfig());
        sharedPreferences.putBoolean(ReadonlyPower, this.isReadonlyPower());
        sharedPreferences.put(RailRoadName, getRailRoadName());
        sharedPreferences.putBoolean(DISABLE_FRAME_SERVER, this.isDisableFrames());
        sharedPreferences.putBoolean(REDIRECT_FRAMES, this.redirectFramesToPanels);
        Preferences node = sharedPreferences.node(DisallowedFrames);
        this.getDisallowedFrames().stream().forEach((frame) -> {
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
        isDirty = value;
    }

    public int getClickDelay() {
        return clickDelay;
    }

    public void setClickDelay(int value) {
        clickDelay = value;
    }

    public int getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(int value) {
        refreshDelay = value;
    }

    public List<String> getDisallowedFrames() {
        return Collections.unmodifiableList(disallowedFrames);
    }

    public boolean useAjax() {
        return useAjax;
    }

    public void setUseAjax(boolean value) {
        useAjax = value;
    }

    public boolean isPlain() {
        return plain;
    }

    public void setPlain(boolean value) {
        plain = value;
    }

    public boolean allowRemoteConfig() {
        return this.allowRemoteConfig;
    }

    public void setAllowRemoteConfig(boolean value) {
        this.allowRemoteConfig = value;
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

    public void setDisallowedFrames(List<String> value) {
        this.disallowedFrames.clear();
        this.disallowedFrames.addAll(value);
    }

    public void addDisallowedFrame(String frame) {
        disallowedFrames.add(frame);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
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
        if (railRoadName != null) {
            this.railRoadName = railRoadName;
        } else {
            this.railRoadName = Bundle.getMessage("DefaultRailroadName");
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
     * @return the disableFrames
     */
    public boolean isDisableFrames() {
        return disableFrames;
    }

    /**
     * @param disableFrames the disableFrames to set
     */
    public void setDisableFrames(boolean disableFrames) {
        this.disableFrames = disableFrames;
    }

    /**
     * @return the redirectFramesToPanels
     */
    public boolean isRedirectFramesToPanels() {
        return redirectFramesToPanels;
    }

    /**
     * @param redirectFramesToPanels the redirectFramesToPanels to set
     */
    public void setRedirectFramesToPanels(boolean redirectFramesToPanels) {
        this.redirectFramesToPanels = redirectFramesToPanels;
    }

    private static class WebServerPreferencesXml extends XmlFile {
    }
}
