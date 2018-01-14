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
public class SE8cSignalHeadTest {

    @Test
    public void testCTor() {
        SE8cSignalHead t = new SE8cSignalHead(5);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        new LocoNetSystemConnectionMemo(lnis,slotmanager);
        jmri.InstanceManager.setDefault(LnTrafficController.class,lnis);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SE8cSignalHeadTest.class);

}
