package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusNodeEditEventFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Paul Bender Copyright (C) 2019
 */
public class CbusNodeEditNVarFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNodeEditNVarFrame t = new CbusNodeEditNVarFrame(null);
        Assert.assertNotNull("exists",t);
        t = null;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarFrameTest.class);

}
