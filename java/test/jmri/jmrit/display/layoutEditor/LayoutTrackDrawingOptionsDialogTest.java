package jmri.jmrit.display.layoutEditor;

import org.junit.*;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutTrackDrawingOptionsDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor("Layout Track Drawing Options Dialog Test Layout");
        LayoutTrackDrawingOptions ltdo = new LayoutTrackDrawingOptions("test");
        LayoutTrackDrawingOptionsDialog t = new LayoutTrackDrawingOptionsDialog(le,false,ltdo);
        Assert.assertNotNull("exists",t);
        le.dispose();
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
