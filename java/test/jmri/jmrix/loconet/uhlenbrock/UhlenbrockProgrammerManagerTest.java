package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.UhlenbrockSlotManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UhlenbrockProgrammerManagerTest {

    @Test
    public void testCTor() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        UhlenbrockSlotManager slotmanager = new UhlenbrockSlotManager(lnis);
        UhlenbrockSystemConnectionMemo memo = new UhlenbrockSystemConnectionMemo(lnis, slotmanager);
        memo.setLnTrafficController(lnis);
        UhlenbrockProgrammerManager t = new UhlenbrockProgrammerManager(memo);
        Assert.assertNotNull("exists",t);
        memo.dispose();
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

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockProgrammerManagerTest.class);

}
