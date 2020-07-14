package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of DisplayFrame
 *
 * @author Egbert Broerse Copyright (C) 2017
 */
public class DisplayFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testPreviewBg() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DisplayFrame df2 = new DisplayFrame("DisplayFrame Bg Test");
        df2.setPreviewBg(2); // initially 0
        Assert.assertEquals("BgSet", 2, df2.getPreviewBg());
        JUnitUtil.dispose(df2);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new DisplayFrame("DisplayFrame Test");
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
