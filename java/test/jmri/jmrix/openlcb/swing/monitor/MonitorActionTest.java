package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MonitorActionTest {

    private TrafficControllerScaffold tcs = null; 
    private CanSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        MonitorAction t = new MonitorAction();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MonitorActionTest.class.getName());

}
