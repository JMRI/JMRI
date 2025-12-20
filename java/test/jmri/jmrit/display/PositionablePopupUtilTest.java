package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PositionablePopupUtilTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        javax.swing.JPanel jp = new javax.swing.JPanel();
        PositionablePopupUtil t = new PositionablePopupUtil(iti,jp);
        assertNotNull( t, "exists");
        JUnitUtil.dispose(ef);
    }

    @Test
    @DisabledIfHeadless
    public void testHasBackground() {
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        javax.swing.JPanel jp = new javax.swing.JPanel();
        PositionablePopupUtil t = new PositionablePopupUtil(iti,jp);
        t.setHasBackground(true);
        assertTrue( t.hasBackground(), "hasBackground");
        t.setHasBackground(false);
        assertFalse( t.hasBackground(), "hasBackground");
        JUnitUtil.dispose(ef);
    }

    @Test
    @DisabledIfHeadless
    public void testSetBackgroundColor() {
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        javax.swing.JPanel jp = new javax.swing.JPanel();
        PositionablePopupUtil t = new PositionablePopupUtil(iti,jp);
        t.setBackgroundColor(new java.awt.Color(0,0,0,255));
        assertEquals( java.awt.Color.black, t.getBackground(), "color set");
        assertTrue( t.hasBackground(), "hasBackground");
        t.setBackgroundColor(new java.awt.Color(255,255,255,0));
        assertEquals( new java.awt.Color(255,255,255,0), t.getBackground(), "color set");
        assertFalse( t.hasBackground(), "hasBackground");
        JUnitUtil.dispose(ef);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PositionablePopupUtilTest.class);

}
