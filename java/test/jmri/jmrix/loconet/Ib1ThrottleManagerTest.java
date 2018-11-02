package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Ib1ThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Test
    @Override
    @Ignore("test requires further setup")
    public void testGetThrottleInfo() {
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        tm = new Ib1ThrottleManager(new LocoNetSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        ((Ib1ThrottleManager)tm).dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Ib1ThrottleManagerTest.class);

}
