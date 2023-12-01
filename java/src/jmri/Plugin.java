package jmri;

import javax.swing.JMenu;

/**
 * Plugin to JMRI.
 * <P>
 * A class that implements the Plugin interface must have one constructor with
 * no parameters. When the plugin JAR file is loaded, the class which implements
 * the Plugin interface is initialized and the {@link #init()} method is called.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public interface Plugin {

    /**
     * Initialize this plugin.
     */
    public default void init() {
    }

    /**
     * Add menu items to the Tools menu if desired.
     * @param menu the Tools menu
     */
    public default void addToolsMenu(JMenu menu) {
    }

}
