package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DisplayFrame
 *
 * @author Egbert Broerse Copyright (C) 2017
 */
public class DisplayFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DisplayFrame df = new DisplayFrame("DisplayFrame Test");
        Assert.assertNotNull("exists", df );
        JUnitUtil.dispose(df);
    }

    @Test
    public void testPreviewBg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DisplayFrame df2 = new DisplayFrame("DisplayFrame Bg Test");
        df2.setPreviewBg(2); // initially 0
        Assert.assertEquals("BgSet", 2, df2.getPreviewBg());
        JUnitUtil.dispose(df2);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() { JUnitUtil.tearDown(); }

}
