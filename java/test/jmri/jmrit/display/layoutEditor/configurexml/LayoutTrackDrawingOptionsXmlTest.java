package jmri.jmrit.display.layoutEditor.configurexml;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
