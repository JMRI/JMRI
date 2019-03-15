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
 * @author Steve Young Copyright (C) 2019
 */
public class CbusCabSignalTest {

    @Test
    public void testCtor() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        
        CbusCabSignal t = new CbusCabSignal(memo,new DccLocoAddress(1234,true));
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

    // private final static Logger log = LoggerFactory.getLogger(CbusCabSignalTest.class);

}
