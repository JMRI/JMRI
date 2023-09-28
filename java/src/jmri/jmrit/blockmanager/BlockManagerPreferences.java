package jmri.jmrit.blockmanager;

/**
 * Preferences for BlockManager
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public interface BlockManagerPreferences {

    /**
     * Compare if the values are different from the other preferences.
     * @param prefs the other preferences to check
     * @return true if preferences differ, false otherwise
     */
    boolean compareValuesDifferent(BlockManagerPreferences prefs);

    /**
     * Apply other preferences to this class
     * @param prefs the other preferences
     */
    void apply(BlockManagerPreferences prefs);

    /**
     * Save the preferences
     */
    void save();

    /**
     * Set whenether LogixNG should be started when the program starts or a
     * panel is loaded.
     * @param value true if LogixNG should start on program start or when a
     * panel is loaded, false otherwise
     */
    void setStartLogixNGOnStartup(boolean value);

    /**
     * Get whenether LogixNG should be started when the program starts or a
     * panel is loaded.
     * @return true if LogixNG should start on program start or when a panel
     * is loaded, false otherwise
     */
    boolean getStartLogixNGOnStartup();

    /**
     * Set whenether system names and user names should be visible for actions
     * and expressions.
     * @param value true if names should be visible, false otherwise
     */
    void setShowSystemUserNames(boolean value);

    /**
     * Get whenether system names and user names should be visible for actions
     * and expressions.
     * @return true if names should be visible, false otherwise
     */
    boolean getShowSystemUserNames();

    /**
     * Set whenether the debugger should be installed or nog.
     * @param value true if the debugger should be installed, false otherwise
     */
    void setInstallDebugger(boolean value);

    /**
     * Get whenether the debugger should be installed or nog.
     * @return true if the debugger should be installed, false otherwise
     */
    boolean getInstallDebugger();

    /**
     * Set whether row in tree editor should be highlighted or not.
     * @param value true if the row should be highlighted, false otherwise
     */
    void setTreeEditorHighlightRow(boolean value);

    /**
     * Get whether row in tree editor should be highlighted or not.
     * @return true if the row should be highlighted, false otherwise
     */
    boolean getTreeEditorHighlightRow();

    /**
     * Set whether system names should be shown or not in exceptions.
     * @param value true if system names should be shown, false otherwise
     */
    void setShowSystemNameInException(boolean value);

    /**
     * Get whether system names should be shown or not in exceptions.
     * @return true if the system names should be shown, false otherwise
     */
    boolean getShowSystemNameInException();

}
