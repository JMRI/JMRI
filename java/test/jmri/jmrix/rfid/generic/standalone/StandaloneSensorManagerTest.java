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

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        StandaloneSystemConnectionMemo memo = new StandaloneSystemConnectionMemo();
        tc = new StandaloneTrafficController(memo);
        memo.setRfidTrafficController(tc);
        memo.setSystemPrefix("R");
        l = new StandaloneSensorManager(tc.getAdapterMemo());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneSensorManagerTest.class);

}
