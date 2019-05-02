package jmri.jmrix.loconet.locoio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoIODataTest {

    // infrastructure objects, populated by setUp;
    private jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis;
    // private jmri.jmrix.loconet.SlotManager slotmanager;
    // private jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LocoIOData t = new LocoIOData(1,1,lnis);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
        // slotmanager = new jmri.jmrix.loconet.SlotManager(lnis);
        // memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo(lnis,slotmanager);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoIODataTest.class);

}
