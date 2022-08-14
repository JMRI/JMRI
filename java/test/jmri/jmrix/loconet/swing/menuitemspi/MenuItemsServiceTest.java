package jmri.jmrix.loconet.swing.menuitemspi;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.swing.WindowInterface;
import jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(MenuItemsService.class, result.getClass(),"check class returned");
    }

    @Test
    public void testGetMenuExtensionsItems() {
        boolean isLocoNetInterface = false;
        WindowInterface wi = null;
        LocoNetSystemConnectionMemo memo = null;
        MenuItemsService instance = MenuItemsService.getInstance();
        List<JMenu> result = instance.getMenuExtensionsItems(isLocoNetInterface, wi, memo);
        assertEquals(2 , result.size(), "result number of menus");
        JMenu j1 = result.get(0);
        assertEquals( JMenu.class, j1.getClass(), "result's  first item is a JMenu");
        assertEquals( "A menu", j1.getName(), "result element 0's name");
        assertEquals( 0, j1.getItemCount(), "result menu 1's number of items");
        JMenu j2 = result.get(1);
        assertEquals( JMenu.class, j2.getClass(), "result's  second item is a JMenu");
        assertEquals( "B menu", j2.getName(), "result second item's name");
        assertEquals( 0, j2.getItemCount(), "result second item's number of items");
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
