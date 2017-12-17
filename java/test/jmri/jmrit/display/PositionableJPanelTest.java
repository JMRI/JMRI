package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PositionableJPanelTest extends PositionableTestBase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",p);
    }

    @Test
    @Override
    public void testGetAndSetRotationDegrees(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        p.rotate(50);
        // setting rotation is currently ignored by PositionableJPanel 
        // and it's sub classes.
        Assert.assertEquals("Degrees",0,p.getDegrees());
    }

    @Test
    @Override
    public void testGetAndSetViewCoordinates() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Default View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           editor = new EditorScaffold();
           p = new PositionableJPanel(editor);
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(PositionableJPanelTest.class);

}
