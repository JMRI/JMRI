package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnPr2ThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testSetAndGettActiveAddress() { 
        ((LnPr2ThrottleManager)tm).requestThrottleSetup(new jmri.DccLocoAddress(3,false));
        Assert.assertEquals("activeAddress",new jmri.DccLocoAddress(3,false),
                 ((LnPr2ThrottleManager)tm).getActiveAddress());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        tm = new LnPr2ThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnPr2ThrottleManagerTest.class);

}
