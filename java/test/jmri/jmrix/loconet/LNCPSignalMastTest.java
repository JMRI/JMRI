package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        jmri.InstanceManager.store(lnis, jmri.jmrix.loconet.LnTrafficController.class);
        SlotManager s = new SlotManager(lnis);
        jmri.InstanceManager.store(s, jmri.CommandStation.class);
        s.dispose();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LNCPSignalMastTest.class);

}
