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
public class StandaloneSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private StandaloneTrafficController tc = null;

    @Override
    public String getSystemName(int i) {
        return "RS" + i;
    }

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new StandaloneTrafficController(new StandaloneSystemConnectionMemo());
        l = new StandaloneSensorManager(tc,"R");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneSensorManagerTest.class);

}
