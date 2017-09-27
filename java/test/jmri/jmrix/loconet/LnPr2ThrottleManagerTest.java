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
public class LnPr2ThrottleManagerTest {

    @Test
    public void testCTor() { 
        LnPr2ThrottleManager t = new LnPr2ThrottleManager(new LocoNetSystemConnectionMemo());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSetAndGettActiveAddress() { 
        LnPr2ThrottleManager t = new LnPr2ThrottleManager(new LocoNetSystemConnectionMemo());
        t.requestThrottleSetup(new jmri.DccLocoAddress(3,false));
        Assert.assertEquals("activeAddress",new jmri.DccLocoAddress(3,false),t.getActiveAddress());
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

    // private final static Logger log = LoggerFactory.getLogger(LnPr2ThrottleManagerTest.class);

}
