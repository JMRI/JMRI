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

    private static final String START_LOGIXNG_ON_LOAD = "startLogixNGOnStartup";
    private static final String INSTALL_DEBUGGER = "installDebugger";
    private static final String SHOW_SYSTEM_USER_NAMES = "showSystemUserNames";
    private static final String ERROR_HANDLING_TYPE = "errorHandlingType";
    private static final String TREE_EDITOR_HIGHLIGHT_ROW = "treeEditorHighlightRow";
    private static final String SHOW_SYSTEM_NAME_IN_EXCEPTION = "showSystemNameInExceptions";

    private boolean _startLogixNGOnLoad = true;
    private boolean _showSystemUserNames = false;
    private boolean _installDebugger = true;
    private ErrorHandlingType _errorHandlingType = ErrorHandlingType.ShowDialogBox;
    private boolean _treeEditorHighlightRow = false;
    private boolean _showSystemNameInException = false;


    public DefaultLogixNGPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
//        System.out.format("LogixNG preferences%n");
        Preferences sharedPreferences = ProfileUtils.getPreferences(
                super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {
        _startLogixNGOnLoad = sharedPreferences.getBoolean(START_LOGIXNG_ON_LOAD, _startLogixNGOnLoad);
        _installDebugger = sharedPreferences.getBoolean(INSTALL_DEBUGGER, _installDebugger);
        _showSystemUserNames = sharedPreferences.getBoolean(SHOW_SYSTEM_USER_NAMES, _showSystemUserNames);
        _errorHandlingType = ErrorHandlingType.valueOf(
                sharedPreferences.get(ERROR_HANDLING_TYPE, _errorHandlingType.name()));
        _treeEditorHighlightRow = sharedPreferences.getBoolean(TREE_EDITOR_HIGHLIGHT_ROW, _treeEditorHighlightRow);
        _showSystemNameInException = sharedPreferences.getBoolean(SHOW_SYSTEM_NAME_IN_EXCEPTION, _showSystemNameInException);

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
        if (getTreeEditorHighlightRow() != prefs.getTreeEditorHighlightRow()) {
            return true;
        }
        if (getShowSystemNameInException() != prefs.getShowSystemNameInException()) {
            return true;
        }
        return (getErrorHandlingType() != prefs.getErrorHandlingType());
    }

    @Override
    public void apply(LogixNGPreferences prefs) {
        setStartLogixNGOnStartup(prefs.getStartLogixNGOnStartup());
        setInstallDebugger(prefs.getInstallDebugger());
        setShowSystemUserNames(prefs.getShowSystemUserNames());
        this.setErrorHandlingType(prefs.getErrorHandlingType());
        setTreeEditorHighlightRow(prefs.getTreeEditorHighlightRow());
        setShowSystemNameInException(prefs.getShowSystemNameInException());
    }

    @Override
    public void save() {
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putBoolean(START_LOGIXNG_ON_LOAD, this.getStartLogixNGOnStartup());
        sharedPreferences.putBoolean(INSTALL_DEBUGGER, this.getInstallDebugger());
        sharedPreferences.putBoolean(SHOW_SYSTEM_USER_NAMES, this.getShowSystemUserNames());
        sharedPreferences.put(ERROR_HANDLING_TYPE, this.getErrorHandlingType().name());
        sharedPreferences.putBoolean(TREE_EDITOR_HIGHLIGHT_ROW, this.getTreeEditorHighlightRow());
        sharedPreferences.putBoolean(SHOW_SYSTEM_NAME_IN_EXCEPTION, this.getShowSystemNameInException());
        setIsDirty(false);
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

    @Override
    public void setTreeEditorHighlightRow(boolean value) {
        _treeEditorHighlightRow = value;
        setIsDirty(true);
    }

    @Override
    public boolean getTreeEditorHighlightRow() {
        return _treeEditorHighlightRow;
    }

    @Override
    public void setShowSystemNameInException(boolean value) {
        _showSystemNameInException = value;
        setIsDirty(true);
    }

    @Override
    public boolean getShowSystemNameInException() {
        return _showSystemNameInException;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGPreferences.class);
}
