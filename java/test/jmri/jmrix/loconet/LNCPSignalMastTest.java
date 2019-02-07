package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LNCPSignalMastTest {

    @Test
    public void testCTor() {
        LNCPSignalMast t = new LNCPSignalMast("LF$lncpsm:basic:one-searchlight(123)");
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        jmri.InstanceManager.store(lnis, jmri.jmrix.loconet.LnTrafficController.class);
        SlotManager s = new SlotManager(lnis);
        jmri.InstanceManager.store(s, jmri.CommandStation.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LNCPSignalMastTest.class);

}
