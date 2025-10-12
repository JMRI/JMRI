package jmri.jmrix.debugthrottle;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DebugThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        var memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        tm = new DebugThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DebugThrottleManagerTest.class);

}
