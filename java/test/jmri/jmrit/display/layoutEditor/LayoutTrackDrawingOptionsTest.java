package jmri.jmrit.display.layoutEditor;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutTrackDrawingOptionsTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackDrawingOptions t = new LayoutTrackDrawingOptions("test");
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
