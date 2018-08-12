package jmri.jmrit.display.layoutEditor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LayoutTrackEditorsTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        LayoutTrackEditors t = new LayoutTrackEditors(e);
        Assert.assertNotNull("exists",t);
        e.dispose();
    }

    @Test
    public void testHasNxSensorPairsNull(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        LayoutTrackEditors t = new LayoutTrackEditors(e);
        Assert.assertFalse("null block NxSensorPairs",t.hasNxSensorPairs(null));
        e.dispose();
    }

    @Test
    public void testHasNxSensorPairsDisconnectedBlock(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        LayoutTrackEditors t = new LayoutTrackEditors(e);
        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertFalse("disconnected block NxSensorPairs",t.hasNxSensorPairs(b));
        e.dispose();
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
