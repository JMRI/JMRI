package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PositionablePopupUtilTest.class);

}
