package jmri.jmrix.loconet.swing.spi;

import java.util.List;
import java.util.Locale;
import jmri.spi.JmriServiceProviderInterface;

/**
 * Provide for including menu items from "extensions" which implement LnPanel
 * objects and which require inclusion on the LocoNet connection's menu.
 * <p>
 * This interface provides a JAVA SPI-based mechanism to allow an extension to
 * add items to the LocoNet connection's menu.
 * <p>
 * When the jmri.jrmix.loconet.swing.LocoNetMenu object prepares its menu, it
 * adds all of the JMRI-native menu items, then invokes
 * jmri.jmrix.loconet.swing.LnMenuItemExtensionSerivce.getExtensionLnMenuItems
 * to retrieve any menu items from "extensions".  If any are found, those menu
 * items are added to the bottom of the menu.
 * <p>
 * Note that any extension's .jar file _must_ be included in the JAVA "classpath"
 * in order to be seen by the SPI's loader.
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
 * @author B. Milhaupt  Copyright 2021
 */
import jmri.jmrix.loconet.swing.LnMenuItem;

public interface LnMenuItemExtension extends JmriServiceProviderInterface {
    /**
     * Provide the LocoNet menu item information for the extension.
     * <p>
     * Provides the information needed by jmri.jmrix.loconet.swing.LocoNetMenu
     * to include an extension's menu items on JMRI LocoNet connection's menu.
     * <p>
     * Developers are encouraged to support internationalization of their menuItem's
     * name (and other GUI functionality).  For convenience, JMRI's active Locale
     * is provided as a parameter.
     * <p>
     * @param locale to be used, where supported, to internationalize the menu
     *          item text
     * @return the list of LnMenuItems to be added to the JMRI LocoNet
     *          connection's menu.
     */

    public List<LnMenuItem> getLocoNetMenuInfo(Locale locale);
}
