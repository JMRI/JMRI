package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusOpCodesTest {

    @Test
    public void testCTor() {
        CbusOpCodes t = new CbusOpCodes();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDecode() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 ); // request e stop
        Assert.assertTrue(CbusOpCodes.decode(m).contains("Request Emergency Stop All"));
        Assert.assertTrue(CbusOpCodes.decodeopc(m).contains("RESTP"));
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

    // private final static Logger log = LoggerFactory.getLogger(CbusOpCodesTest.class);

}
