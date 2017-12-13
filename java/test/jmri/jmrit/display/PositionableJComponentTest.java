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
public class PositionableJComponentTest extends PositionableTestBase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists",p);
    }

    @Override
    @Test
    public void testGetAndSetViewCoordinates() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Defalt View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           Editor e = new EditorScaffold();
           p = new PositionableJComponent(e);
        }
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        p = null;
    }

}
