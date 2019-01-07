package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    @Override
    @Ignore("test requires further setup")
    @ToDo("finish test setup and remove this overriden test so that  the parent class test can run")
    public void testGetThrottleInfo() {
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        tm = new CbusThrottleManager(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusThrottleManagerTest.class);

}
