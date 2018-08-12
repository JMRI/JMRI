package jmri.jmrix.cmri.serial.serialmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;


/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialFilterFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialFilterFrame t = new SerialFilterFrame(new CMRISystemConnectionMemo());
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

    // private final static Logger log = LoggerFactory.getLogger(SerialFilterFrameTest.class);

}
