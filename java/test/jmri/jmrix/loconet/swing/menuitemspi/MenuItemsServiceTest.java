package jmri.jmrix.loconet.swing.menuitemspi;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.swing.WindowInterface;
import jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface;

import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Tests for MenuItemsService.
 * <br>
 * @author Bob Milhaupt  Copyright (C) 2022
 */

@ServiceProvider(service = jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface.class)
public class MenuItemsServiceTest implements MenuItemsInterface {

    @Test
    public void testGetInstance() {
        MenuItemsService result = MenuItemsService.getInstance();
        assertNotNull( result);
        assertEquals("check class returned", MenuItemsService.class, result.getClass());
    }

    @Test
    public void testGetMenuExtensionsItems() {
        boolean isLocoNetInterface = false;
        WindowInterface wi = null;
        LocoNetSystemConnectionMemo memo = null;
        MenuItemsService instance = MenuItemsService.getInstance();
        List<JMenu> result = instance.getMenuExtensionsItems(isLocoNetInterface, wi, memo);
        assertEquals("result number of menus", 2, result.size());
        JMenu j1 = result.get(0);
        assertEquals("result's  first item is a JMenu", JMenu.class,
                j1.getClass());
        assertEquals("result element 0's name", "A menu", j1.getName());
        assertEquals("result menu 1's number of items",
                0, j1.getItemCount());
        JMenu j2 = result.get(1);
        assertEquals("result's  second item is a JMenu", JMenu.class,
                j2.getClass());
        assertEquals("result second item's name", "B menu", j2.getName());
        assertEquals("result second item's number of items",
                0, j2.getItemCount());
    }

    @Override
    public ArrayList<JMenu> getMenuItems(boolean isLocoNetInterface,
            WindowInterface wi, LocoNetSystemConnectionMemo memo) {
        ArrayList<JMenu> j = new ArrayList<>();
        JMenu jm = new JMenu();
        jm.setName("A menu");
        j.add(jm);
        jm = new JMenu();
        jm.setName("B menu");
        j.add(jm); // add another menu so there's something more to check on.
        return j;
    }
}
