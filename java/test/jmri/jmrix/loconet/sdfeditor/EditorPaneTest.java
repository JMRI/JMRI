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
 * Tests for the jmri.jmrix.loconet.sdfeditor.EditorPane class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class EditorPaneTest {

    @Test
    public void testShowPane() throws java.io.IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SdfBuffer buff = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        Assert.assertNotNull(buff);
        EditorFrame f = new EditorFrame(buff);
        f.setVisible(true);
        f.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
