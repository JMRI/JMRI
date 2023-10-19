package jmri.jmrix.loconet.swing.menuitemspi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;

import jmri.util.swing.WindowInterface;

import javax.swing.JMenu;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAVA SPI Service to retrieve, from providers of the {@link 
 * jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface} service, lists 
 * of items to be added to the JMRI menu(s) associated with LocoNet connections.
 * <br>
 * @author Bob M.  Copyright (C) 2021
 */
public final class MenuItemsService {
    private static MenuItemsService service; // (singleton pattern)
    private final ServiceLoader <MenuItemsInterface> loader;

    private MenuItemsService () {
        loader = ServiceLoader.load(MenuItemsInterface.class);
    }

    /**
     * Method for getting the SPI service "singleton"
     * @return the singleton instance of this class
     */
    public static synchronized MenuItemsService getInstance() {
        if (service == null) {
            service = new MenuItemsService();
        }
        return service;
    }

    /**
     * Return menu items from all LocoNet Menu Extension SPI providers.
     * <br>
     * @param isLocoNetInterface informs whether the connection
     *      has actual hardware
     * @param wi allows the extension menu items to be associated with the
     *      JAVA WindowInterface which relates to the connection's menu
     * @param memo the LocoNetSystemConnectionMemo associated with the menu to
     *      which the extension's MenuItem(s) are to be attached.
     * @return an ArrayList of JMenu objects, as populated from the menu items
     *      reported by any available SPI extensions.  May be an empty ArrayList
     *      if none of the SPI extensions provide menu items for this menu.
     */
    public List<JMenu> getMenuExtensionsItems(boolean isLocoNetInterface,
            WindowInterface wi, LocoNetSystemConnectionMemo memo) {
        ArrayList <JMenu> menus = new java.util.ArrayList<>();

        try {
            java.util.Iterator<MenuItemsInterface> miIterator = loader.iterator();
            while (miIterator.hasNext()) {
                MenuItemsInterface mii = miIterator.next();
                log.debug("adding menu items for extension {}", mii.getClass());
                ArrayList <JMenu> me = mii.getMenuItems(isLocoNetInterface, wi, memo);
                menus.addAll(me);
            }
        } catch (ServiceConfigurationError serviceError) {
            // ignore the exception
        }
        return menus;
    }
    private final static Logger log = LoggerFactory.getLogger(MenuItemsService.class);
}
