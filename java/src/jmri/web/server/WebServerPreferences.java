package jmri.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
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

    // Flag that prefs have not been saved:
    private boolean isDirty = false;
    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean useAjax = true;
    private boolean plain = false;
    private ArrayList<String> disallowedFrames = new ArrayList<>(Arrays.asList(Bundle.getMessage("DefaultDisallowedFrames").split(";")));
    private String railRoadName = Bundle.getMessage("DefaultRailroadName");
    private boolean allowRemoteConfig = false;
    protected boolean readonlyPower = true;
    private int port = 12080;
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
                log.info("Migrating from old Webserver preferences in {} to new format in {}.", fileName, FileUtil.getAbsoluteFilename("profile:preferences"));
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

    private void readPreferences(Preferences sharedPreferences) {
        this.allowRemoteConfig = sharedPreferences.getBoolean(AllowRemoteConfig, this.allowRemoteConfig);
        this.clickDelay = sharedPreferences.getInt(ClickDelay, this.clickDelay);
        this.plain = sharedPreferences.getBoolean(Simple, this.plain);
        this.railRoadName = sharedPreferences.get(RailRoadName, this.railRoadName);
        this.readonlyPower = sharedPreferences.getBoolean(ReadonlyPower, this.readonlyPower);
        this.refreshDelay = sharedPreferences.getInt(RefreshDelay, this.refreshDelay);
        this.useAjax = sharedPreferences.getBoolean(UseAjax, this.useAjax);
        try {
            if (sharedPreferences.nodeExists(DisallowedFrames)) { // throws BackingStoreException
                Preferences frames = sharedPreferences.node(DisallowedFrames);
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
        if ((a = child.getAttribute(ClickDelay)) != null) {
            try {
                setClickDelay(Integer.valueOf(a.getValue()));
            } catch (NumberFormatException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        if ((a = child.getAttribute(RefreshDelay)) != null) {
            try {
                setRefreshDelay(Integer.valueOf(a.getValue()));
            } catch (NumberFormatException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        if ((a = child.getAttribute(UseAjax)) != null) {
            setUseAjax(Boolean.parseBoolean(a.getValue()));
        }
        if ((a = child.getAttribute(Simple)) != null) {
            setPlain(Boolean.parseBoolean(a.getValue()));
        }
        if ((a = child.getAttribute(AllowRemoteConfig)) != null) {
            setAllowRemoteConfig(Boolean.parseBoolean(a.getValue()));
        }
        if ((a = child.getAttribute(ReadonlyPower)) != null) {
            setReadonlyPower(Boolean.parseBoolean(a.getValue()));
        }
        if ((a = child.getAttribute(Port)) != null) {
            try {
                setPort(a.getIntValue());
            } catch (DataConversionException ex) {
                setPort(12080);
                log.error("Unable to read port. Setting to default value.", ex);
            }
        }
        if ((a = child.getAttribute(RailRoadName)) != null) {
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
        setDisallowedFrames((ArrayList<String>) prefs.getDisallowedFrames());
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
            root = null;
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
        Preferences node = sharedPreferences.node(DisallowedFrames);
        this.getDisallowedFrames().stream().forEach((frame) -> {
            node.put(Integer.toString(this.disallowedFrames.indexOf(frame)), frame);
        });
        try {
            sharedPreferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Exception while saving web server preferences", ex);
        }
        sharedPreferences.putInt(Port, this.getPort());
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
        return disallowedFrames;
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

    public void setDisallowedFrames(ArrayList<String> value) {
        disallowedFrames = value;
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

    public static class WebServerPreferencesXml extends XmlFile {
    }
}
