package jmri.jmrix.loconet.locoio;

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
public class LocoIODataTest {

    // infrastructure objects, populated by setUp;
    private jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis;
    private jmri.jmrix.loconet.SlotManager slotmanager;
    private jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LocoIOData t = new LocoIOData(1,1,lnis);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface
        lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
        slotmanager = new jmri.jmrix.loconet.SlotManager(lnis);
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo(lnis,slotmanager);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoIODataTest.class.getName());

}
