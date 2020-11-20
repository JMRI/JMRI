package jmri.jmrit.logixng;

/**
 * Preferences for LogixNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogixNGPreferences {

    /**
     * Compare if the values are different from the other preferences.
     * @param prefs the other preferences to check
     * @return true if preferences differ, false otherwise
     */
    public boolean compareValuesDifferent(LogixNGPreferences prefs);

    /**
     * Apply other preferences to this class
     * @param prefs the other preferences
     */
    public void apply(LogixNGPreferences prefs);

    /**
     * Save the preferences
     */
    public void save();
    
    /**
     * Set whenether LogixNG should be started when the program starts or a
     * panel is loaded.
     * @param value true if LogixNG should start on program start or when a
     * panel is loaded, false otherwise
     */
    public void setStartLogixNGOnStartup(boolean value);

    /**
     * Get whenether LogixNG should be started when the program starts or a
     * panel is loaded.
     * @return true if LogixNG should start on program start or when a panel
     * is loaded, false otherwise
     */
    public boolean getStartLogixNGOnStartup();

    /**
     * Set whenether generic sockets should be used for expression sockets.
     * @param value true if generic sockets should be used, false otherwise
     */
    public void setUseGenericFemaleSockets(boolean value);

    /**
     * Get whenether generic sockets should be used for expression sockets.
     * @return true if generic sockets should be used, false otherwise
     */
    public boolean getUseGenericFemaleSockets();

    /**
     * Set whenether debug mode is allowed.
     * @param value true if debug mode is allowed, false otherwise
     */
    public void setAllowDebugMode(boolean value);

    /**
     * Get whenether debug mode is allowed.
     * @return true if debug mode is allowed, false otherwise
     */
    public boolean getAllowDebugMode();

    /**
     * Set whenether system names and user names should be visible for actions
     * and expressions.
     * @param value true if names should be visible, false otherwise
     */
    public void setShowSystemUserNames(boolean value);

    /**
     * Get whenether system names and user names should be visible for actions
     * and expressions.
     * @return true if names should be visible, false otherwise
     */
    public boolean getShowSystemUserNames();

}
