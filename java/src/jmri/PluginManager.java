package jmri;

import java.util.*;

/**
 * Manages plugins
 *
 * @author Daniel Bergqvist (C) 2023
 */
public interface PluginManager {

    /**
     * Add a plugin.
     * @param p the plugin
     */
    public void addPlugin(Plugin p);

    /**
     * Get a list of the plugins.
     * @return the list
     */
    public List<Plugin> getPlugins();

}
