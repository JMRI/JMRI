package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.UhlenbrockSlotManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UhlenbrockLnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        UhlenbrockSlotManager slotmanager = new UhlenbrockSlotManager(lnis);
        UhlenbrockSystemConnectionMemo memo = new UhlenbrockSystemConnectionMemo(lnis,slotmanager);
        tm = new UhlenbrockLnThrottleManager(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockLnThrottleManagerTest.class);

}
