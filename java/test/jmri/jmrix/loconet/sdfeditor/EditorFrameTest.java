package jmri.jmrix.loconet.sdfeditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.loconet.sdf.SdfBuffer;
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
public class EditorFrameTest {

    @Test
    public void testCTor() throws java.io.IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SdfBuffer b = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        EditorFrame t = new EditorFrame(b);
        Assert.assertNotNull("exists", t);
        t.dispose();
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

}
