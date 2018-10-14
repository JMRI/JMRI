package jmri.jmrix.marklin;

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
public class MarklinThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    @Override
    @Ignore("test requires further setup")
    public void testGetThrottleInfo() {
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        MarklinTrafficController tc = new MarklinTrafficController();
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo(tc);
        tm = new MarklinThrottleManager(c);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinThrottleManagerTest.class);

}
