package jmri.jmrit.logixng.implementation;

import java.util.prefs.Preferences;

import jmri.beans.PreferencesBean;
import jmri.jmrit.logixng.LogixNGPreferences;
import jmri.jmrit.logixng.MaleSocket.ErrorHandlingType;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;

/**
 * Preferences for LogixNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public final class DefaultLogixNGPreferences extends PreferencesBean implements LogixNGPreferences {

    public static final String START_LOGIXNG_ON_LOAD = "startLogixNGOnStartup";
    public static final String USE_GENERIC_FEMALE_SOCKETS = "useGenericFemaleSockets";
    public static final String INSTALL_DEBUGGER = "installDebugger";
    public static final String SHOW_SYSTEM_USER_NAMES = "showSystemUserNames";
    public static final String ERROR_HANDLING_TYPE = "errorHandlingType";
    
    private boolean _startLogixNGOnLoad = true;
    private boolean _useGenericFemaleSockets = false;
    private boolean _showSystemUserNames = false;
    private boolean _installDebugger = true;
    private ErrorHandlingType _errorHandlingType = ErrorHandlingType.ShowDialogBox;
    
    
    public DefaultLogixNGPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
//        System.out.format("LogixNG preferences%n");
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _startLogixNGOnLoad = sharedPreferences.getBoolean(START_LOGIXNG_ON_LOAD, _startLogixNGOnLoad);
        _useGenericFemaleSockets = sharedPreferences.getBoolean(USE_GENERIC_FEMALE_SOCKETS, _useGenericFemaleSockets);
        _installDebugger = sharedPreferences.getBoolean(INSTALL_DEBUGGER, _installDebugger);
        _showSystemUserNames = sharedPreferences.getBoolean(SHOW_SYSTEM_USER_NAMES, _showSystemUserNames);
        _errorHandlingType = ErrorHandlingType.valueOf(
                sharedPreferences.get(ERROR_HANDLING_TYPE, _errorHandlingType.name()));
        
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
        setIsDirty(false);
    }

    @Override
    public boolean compareValuesDifferent(LogixNGPreferences prefs) {
        if (getStartLogixNGOnStartup() != prefs.getStartLogixNGOnStartup()) {
            return true;
        }
        if (getInstallDebugger() != prefs.getInstallDebugger()) {
            return true;
        }
        if (getShowSystemUserNames() != prefs.getShowSystemUserNames()) {
            return true;
        }
        if (getUseGenericFemaleSockets() != prefs.getUseGenericFemaleSockets()) {
            return true;
        }
        return (getErrorHandlingType() != prefs.getErrorHandlingType());
    }

    @Override
    public void apply(LogixNGPreferences prefs) {
        setStartLogixNGOnStartup(prefs.getStartLogixNGOnStartup());
        setUseGenericFemaleSockets(prefs.getUseGenericFemaleSockets());
        setInstallDebugger(prefs.getInstallDebugger());
        setShowSystemUserNames(prefs.getShowSystemUserNames());
        this.setErrorHandlingType(prefs.getErrorHandlingType());
    }

    @Override
    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(START_LOGIXNG_ON_LOAD, this.getStartLogixNGOnStartup());
        sharedPreferences.putBoolean(USE_GENERIC_FEMALE_SOCKETS, this.getUseGenericFemaleSockets());
        sharedPreferences.putBoolean(INSTALL_DEBUGGER, this.getInstallDebugger());
        sharedPreferences.putBoolean(SHOW_SYSTEM_USER_NAMES, this.getShowSystemUserNames());
        sharedPreferences.put(ERROR_HANDLING_TYPE, this.getErrorHandlingType().name());
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
    
    @Override
    public void setStartLogixNGOnStartup(boolean value) {
        _startLogixNGOnLoad = value;
        setIsDirty(true);
    }

    @Override
    public boolean getStartLogixNGOnStartup() {
        return _startLogixNGOnLoad;
    }

    @Override
    public void setUseGenericFemaleSockets(boolean value) {
        _useGenericFemaleSockets = value;
        setIsDirty(true);
    }

    @Override
    public boolean getUseGenericFemaleSockets() {
        return _useGenericFemaleSockets;
    }

    @Override
    public void setShowSystemUserNames(boolean value) {
        _showSystemUserNames = value;
        setIsDirty(true);
    }

    @Override
    public boolean getShowSystemUserNames() {
        return _showSystemUserNames;
    }

    @Override
    public void setInstallDebugger(boolean value) {
        _installDebugger = value;
        setIsDirty(true);
    }

    @Override
    public boolean getInstallDebugger() {
        return _installDebugger;
    }

    @Override
    public void setErrorHandlingType(ErrorHandlingType type) {
        _errorHandlingType = type;
        setIsDirty(true);
    }

    @Override
    public ErrorHandlingType getErrorHandlingType() {
        return _errorHandlingType;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGPreferences.class);
}
