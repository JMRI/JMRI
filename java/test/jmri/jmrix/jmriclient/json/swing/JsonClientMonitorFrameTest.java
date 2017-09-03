package jmri.jmrix.jmriclient.json.swing;

import java.awt.GraphicsEnvironment;
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
public class JsonClientMonitorFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo memo = new jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo();
        JsonClientMonitorFrame t = new JsonClientMonitorFrame(memo);
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(JsonClientMonitorFrameTest.class);

}
