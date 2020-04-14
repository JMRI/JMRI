package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MarklinThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    @Override
    @Ignore("test requires further setup")
    @ToDo("finish test setup, then remove overriden test so that test in parent class can run.")
    public void testGetThrottleInfo() {
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        MarklinTrafficController tc = new MarklinTrafficController();
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo(tc);
        tm = new MarklinThrottleManager(c);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinThrottleManagerTest.class);

}
