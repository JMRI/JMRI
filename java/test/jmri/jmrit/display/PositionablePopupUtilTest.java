package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PositionablePopupUtilTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        javax.swing.JPanel jp = new javax.swing.JPanel();
        PositionablePopupUtil t = new PositionablePopupUtil(iti,jp);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testHasBackground() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        javax.swing.JPanel jp = new javax.swing.JPanel();
        PositionablePopupUtil t = new PositionablePopupUtil(iti,jp);
        t.setHasBackground(true);
        Assert.assertTrue("hasBackground",t.hasBackground());
        t.setHasBackground(false);
        Assert.assertFalse("hasBackground",t.hasBackground());
    }

    @Test
    public void testSetBackgroundColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        PositionableIcon iti = new PositionableIcon(ef);
        javax.swing.JPanel jp = new javax.swing.JPanel();
        PositionablePopupUtil t = new PositionablePopupUtil(iti,jp);
        t.setBackgroundColor(new java.awt.Color(0,0,0,255));
        Assert.assertEquals("color set",java.awt.Color.black, t.getBackground());
        Assert.assertTrue("hasBackground",t.hasBackground());
        t.setBackgroundColor(new java.awt.Color(255,255,255,0));
        Assert.assertEquals("color set",new java.awt.Color(255,255,255,0), t.getBackground());
        Assert.assertFalse("hasBackground",t.hasBackground());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PositionablePopupUtilTest.class);

}
