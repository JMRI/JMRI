package jmri.jmrit.display.layoutEditor.configurexml;

import org.junit.*;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LayoutTrackDrawingOptionsXmlTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackDrawingOptionsXml t = new LayoutTrackDrawingOptionsXml();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
