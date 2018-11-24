package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.UhlenbrockSlotManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UhlenbrockLnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private UhlenbrockSystemConnectionMemo memo;

    @Test
    @Override
    @Ignore("test requires further setup")
    public void testGetThrottleInfo() {
    }

    // The minimal setup for log4J
    @Before
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
