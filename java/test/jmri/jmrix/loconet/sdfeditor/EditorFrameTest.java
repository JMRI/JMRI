package jmri.jmrix.loconet.sdfeditor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.sdf.SdfBuffer;
import java.awt.GraphicsEnvironment;


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
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EditorFrameTest.class.getName());

}
