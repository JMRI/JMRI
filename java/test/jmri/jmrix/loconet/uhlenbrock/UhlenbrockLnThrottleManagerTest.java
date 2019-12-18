package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.UhlenbrockSlotManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UhlenbrockLnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private UhlenbrockSystemConnectionMemo memo;

    @Test
    @Override
    @Ignore("parent class test requires further setup")
    @ToDo("finish initialization and remove this overriden test so that the parent class test can run")
    public void testGetThrottleInfo() {
        Assert.fail("parent class test requires further setup");
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        UhlenbrockSlotManager slotmanager = new UhlenbrockSlotManager(lnis);
        memo = new UhlenbrockSystemConnectionMemo(lnis,slotmanager);
        tm = new UhlenbrockLnThrottleManager(memo);
    }

    @After
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockLnThrottleManagerTest.class);

}
