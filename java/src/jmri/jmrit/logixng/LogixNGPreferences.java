package jmri.jmrit.logixng;

/**
 * Preferences for LogixNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogixNGPreferences {

    public boolean compareValuesDifferent(LogixNGPreferences prefs);

    public void apply(LogixNGPreferences prefs);

    public void save();
    
    public void setStartLogixNGOnStartup(boolean value);

    public boolean getStartLogixNGOnStartup();

    public void setUseGenericFemaleSockets(boolean value);

    public boolean getUseGenericFemaleSockets();

    public void setAllowDebugMode(boolean value);

    public boolean getAllowDebugMode();

    public void setLimitRootActions(boolean value);

    public boolean getLimitRootActions();

}
