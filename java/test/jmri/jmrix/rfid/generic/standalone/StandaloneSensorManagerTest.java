package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class StandaloneSensorManagerTest {

    private StandaloneTrafficController tc = null;

    @Test
    public void testCTor() {
        StandaloneSensorManager t = new StandaloneSensorManager(tc,"R");
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new StandaloneTrafficController(new StandaloneSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneSensorManagerTest.class);

}
