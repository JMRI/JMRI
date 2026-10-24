package jmri.jmrit.entryexit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that each EntryExitPairs has its own glass pane, so that the mouse
 * listener one adds does not keep it reachable through another.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class EntryExitPairsGlassPaneTest {

    @Test
    public void testEachHasItsOwnGlassPane() {
        EntryExitPairs first = new EntryExitPairs();
        EntryExitPairs second = new EntryExitPairs();
        assertNotSame(first.getGlassPane(), second.getGlassPane(), "separate glass panes");
        assertEquals(1, first.getGlassPane().getMouseListeners().length, "one mouse listener on the first");
        assertEquals(1, second.getGlassPane().getMouseListeners().length, "one mouse listener on the second");
        first.dispose();
        second.dispose();
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
