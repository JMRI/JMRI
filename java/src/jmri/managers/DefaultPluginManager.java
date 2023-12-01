package jmri.managers;

import java.util.*;

import jmri.Plugin;
import jmri.PluginManager;

/**
 * Default implementation of PluginManager.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class DefaultPluginManager implements PluginManager {

    private final List<Plugin> _plugins = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void addPlugin(Plugin p) {
        _plugins.add(p);
    }

    /** {@inheritDoc} */
    @Override
    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(_plugins);
    }
}
