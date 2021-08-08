package jmri.jmrix.loconet.swing;

/**
 * A class to handle Menu Items for LocoNet-based connections.
 * <p>
 * This class was separated out from jmri.jmrix.loconet.swing.LocoNetMenu.java.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * @author B. Milhaupt Copyright (C) 2021
 * @author Bob Jacobsen Copyright 2003, 2010
 */
public class LnMenuItem {

    private final String menuItemName;
    private final String classNameOfItem;
    private final boolean requiresLocoNetAccess;

    /**
     * Describes a Menu Item for inclusion in a LocoNet-based connection's
     * connection-specific menu.
     *
     * @param menuItemName - user name to be displayed in the Menu item
     * @param classToLoad - fully-qualified path to the LnPanel
     * @param requiresAccessToLocoNet - true if the menu item is only to be displayed
     *      for connections which provide access to a physical LocoNet; false
     *      to indicate that the menu item should be shown regardless of
     *      the connection's ability to communicate with physical LocoNet devices.
     *      Note that standalone programmer connections do not provide such
     *      functionality.
     */
    public LnMenuItem(String menuItemName, String classToLoad, boolean requiresAccessToLocoNet) {
        this.menuItemName = menuItemName;
        this.classNameOfItem = classToLoad;
        this.requiresLocoNetAccess = requiresAccessToLocoNet;
    }
    public final String getMenuItemName() {
        return menuItemName;
    }

    public final String getClassToLoad() {
        return classNameOfItem;
    }

    public final boolean getRequiresAccessToLocoNet() {
        return requiresLocoNetAccess;
    }
}
