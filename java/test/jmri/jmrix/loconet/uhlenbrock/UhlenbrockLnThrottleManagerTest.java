package jmri.jmrix.loconet.uhlenbrock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.UhlenbrockSlotManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UhlenbrockLnThrottleManagerTest {

    @Test
    public void testCTor() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        UhlenbrockSlotManager slotmanager = new UhlenbrockSlotManager(lnis);
        UhlenbrockSystemConnectionMemo memo = new UhlenbrockSystemConnectionMemo(lnis,slotmanager);
        UhlenbrockLnThrottleManager t = new UhlenbrockLnThrottleManager(memo);
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

    private final static Logger log = LoggerFactory.getLogger(UhlenbrockLnThrottleManagerTest.class.getName());

}
