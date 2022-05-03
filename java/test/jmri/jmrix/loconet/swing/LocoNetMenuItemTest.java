package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locomon.LocoMonPane;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for LocoNetMenuItem.
 * <br>
 * @author Bob Milhaupt  Copyright (C) 2022
 */
public class LocoNetMenuItemTest {

    @Test
    public void testIsInterfaceOnly() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        boolean expResult = false;
        boolean result = instance.isInterfaceOnly();
        assertEquals("isInterfaceOnly expect false", expResult, result);
        instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, true, false);
        expResult = true;
        result = instance.isInterfaceOnly();
        assertEquals("isInterfaceOnly expect true", expResult, result);
    }

    @Test
    public void testGetName() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        String expResult = "a.b.c.d";
        String result = instance.getName();
        assertEquals("getName expect 'a.b.c.d'", expResult, result);
    }

    @Test
    public void testGetClassToLoad() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        Class expResult = LocoMonPane.class;
        Class result = instance.getClassToLoad();
        assertEquals("getClassToLoad test", expResult, result);
    }

    @Test
    public void testHasGui() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        boolean expResult = true;
        boolean result = instance.hasGui();
        assertEquals("hasGui expect true", expResult, result);
        instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, true, false);
        expResult = false;
        result = instance.hasGui();
        assertEquals("hasGui expect false", expResult, result);    }

}
