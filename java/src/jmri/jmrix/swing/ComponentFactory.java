package jmri.jmrix.swing;

import javax.swing.JMenu;

/**
 * Provide access to Swing components for a jmrix subsystem.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 */
abstract public class ComponentFactory {

    /**
     * Provide a menu with all items attached to this system connection
     */
    abstract public JMenu getMenu();

}
