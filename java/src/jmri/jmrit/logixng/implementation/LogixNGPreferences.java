package jmri.jmrit.logixng.implementation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.Preferences;
import jmri.JmriException;
import jmri.beans.PreferencesBean;
import jmri.jmrit.logixng.PluginManager;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;

/**
 * Preferences for LogixNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public final class LogixNGPreferences extends PreferencesBean {

    public static final String START_LOGIXNG_ON_LOAD = "startLogixNGOnStartup";
    public static final String USE_GENERIC_FEMALE_SOCKETS = "useGenericFemaleSockets";
    public static final String ALLOW_DEBUG_MODE = "allowDebugMode";
    
    private boolean _startLogixNGOnLoad = false;
    private boolean _useGenericFemaleSockets = false;
    private boolean _allowDebugMode = false;
    
    private final PluginManager _pluginManager;
    
    public LogixNGPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
//        System.out.format("LogixNG preferences%n");
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        _pluginManager = new PluginManager();
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        this._startLogixNGOnLoad = sharedPreferences.getBoolean(START_LOGIXNG_ON_LOAD, this._startLogixNGOnLoad);
        this._useGenericFemaleSockets = sharedPreferences.getBoolean(USE_GENERIC_FEMALE_SOCKETS, this._useGenericFemaleSockets);
        this._allowDebugMode = sharedPreferences.getBoolean(ALLOW_DEBUG_MODE, this._allowDebugMode);
        
        if (1==0) {
            try {
                String jarFileName = "F:\\Projekt\\Java\\GitHub\\JMRI_LogixNGPlugins\\dist\\JMRI_LogixNGPlugins.jar";
                _pluginManager.addJarFile(jarFileName);
            } catch (IOException | ClassNotFoundException |
                    InstantiationException | IllegalAccessException e) {
                // This needs to be handled in a better way.
                e.printStackTrace();
            }
        }
        
/*        
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
*/
        this.setIsDirty(false);
    }

    public boolean compareValuesDifferent(LogixNGPreferences prefs) {
        if (getStartLogixNGOnStartup() != prefs.getStartLogixNGOnStartup()) {
            return true;
        }
        if (getAllowDebugMode() != prefs.getAllowDebugMode()) {
            return true;
        }
        return (getUseGenericFemaleSockets() != prefs.getUseGenericFemaleSockets());
    }

    public void apply(LogixNGPreferences prefs) {
        setStartLogixNGOnStartup(prefs.getStartLogixNGOnStartup());
        setUseGenericFemaleSockets(prefs.getUseGenericFemaleSockets());
        setAllowDebugMode(prefs.getAllowDebugMode());
    }

    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(START_LOGIXNG_ON_LOAD, this.getStartLogixNGOnStartup());
        sharedPreferences.putBoolean(USE_GENERIC_FEMALE_SOCKETS, this.getUseGenericFemaleSockets());
        sharedPreferences.putBoolean(ALLOW_DEBUG_MODE, this.getAllowDebugMode());
/*        
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
*/
    }
    
    public void setStartLogixNGOnStartup(boolean value) {
        _startLogixNGOnLoad = value;
    }

    public boolean getStartLogixNGOnStartup() {
        return _startLogixNGOnLoad;
    }

    public void setUseGenericFemaleSockets(boolean value) {
        _useGenericFemaleSockets = value;
    }

    public boolean getUseGenericFemaleSockets() {
        return _useGenericFemaleSockets;
    }

    public void setAllowDebugMode(boolean value) {
        _allowDebugMode = value;
    }

    public boolean getAllowDebugMode() {
        return _allowDebugMode;
    }

}
