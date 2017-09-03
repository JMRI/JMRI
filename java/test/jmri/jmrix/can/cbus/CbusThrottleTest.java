package jmri.jmrix.can.cbus;

import jmri.DccLocoAddress;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusThrottleTest {

    @Test
    public void testCTor() {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusThrottle t = new CbusThrottle(memo,new DccLocoAddress(100,true),100);
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

    // private final static Logger log = LoggerFactory.getLogger(CbusThrottleTest.class);

}
