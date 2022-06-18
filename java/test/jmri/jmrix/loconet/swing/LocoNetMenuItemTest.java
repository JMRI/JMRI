package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.locomon.LocoMonPane;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

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
        assertFalse( instance.isInterfaceOnly(), "isInterfaceOnly expect false");
        instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, true, false);
        assertTrue( instance.isInterfaceOnly(), "isInterfaceOnly expect true");
    }

    @Test
    public void testGetName() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        assertEquals("a.b.c.d", instance.getName(), "getName expect 'a.b.c.d'");
    }

    @Test
    public void testGetClassToLoad() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        assertEquals( LocoMonPane.class, instance.getClassToLoad(), "getClassToLoad test");
    }

    @Test
    public void testHasGui() {
        LocoNetMenuItem instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, false, true);
        assertTrue( instance.hasGui(), "hasGui expect true");
        instance = new LocoNetMenuItem("a.b.c.d",
                LocoMonPane.class, true, false);
        assertFalse( instance.hasGui(), "hasGui expect false");
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
