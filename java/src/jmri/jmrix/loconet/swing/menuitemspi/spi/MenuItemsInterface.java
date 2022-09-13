package jmri.jmrix.loconet.swing.menuitemspi.spi;

import java.util.ArrayList;
import javax.swing.JMenu;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.swing.WindowInterface;

/**
 * An interface which allows for extension of LocoNet menu items via an
 * implementation of the {@link java.util.spi} mechanism.
 * <br>
 * The {@link jmri.jmrix.loconet.swing.menuitemspi.MenuItemsService} invokes the 
 * {@link jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface#getMenuItems(boolean, jmri.util.swing.WindowInterface, jmri.jmrix.loconet.LocoNetSystemConnectionMemo)} 
 * method to retrieve the service provider's menu items.
 * <br>
 * @author Bob Milhaupt  Copyright (C) 2021
 */

public interface MenuItemsInterface {
    /**
     * An interface for extension of the menu(s) associated with JMRI LocoNet-based
     * connections, the JAVA SPI mechanism.
     * 
     * Implementers of this JAVA SPI interface provide an ArrayList of zero or 
     * more items to be added to JMRI menu(s) for connection(s) of the "LocoNet" type.
     * <br>
     * The returned {@link java.util.ArrayList} may contain zero, one or more of any of the 
     * objects allowed by a {@link javax.swing.JMenu} object.  This includes 
     * objects of the class {@link jmri.jmrix.loconet.swing.LocoNetMenuItem}.
     * <br>
     * @param isLocoNetInterface informs whether the connection
     *      has actual hardware.
     * @param wi allows the extension menu items to be associated with the
     *      {@link jmri.util.swing.WindowInterface} which relates to the connection's menu.
     * @param memo the {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} associated with the menu to
     *      which the extension's MenuItem(s) are to be attached.
     * @return an {@link java.util.ArrayList} of JMenu-compatible objects, as populated from the 
     *      menu items reported by any available SPI extensions.  Implementer may
     *      return an empty ArrayList if it does not implement any menu items.
     */
    public ArrayList<JMenu> getMenuItems(boolean isLocoNetInterface,
            WindowInterface wi, LocoNetSystemConnectionMemo memo);

}
