package jmri.managers;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultPowerManagerTest {

    @Test
    public void testCTor() {
        DefaultPowerManager t = new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testTimeSinceLastPowerOn() throws jmri.JmriException {
        DefaultPowerManager t = new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        Assertions.assertEquals(Long.MAX_VALUE, t.timeSinceLastPowerOn(), "never on");
        t.setPower(jmri.PowerManager.OFF);
        Assertions.assertEquals(Long.MAX_VALUE, t.timeSinceLastPowerOn(), "still never on");
        t.setPower(jmri.PowerManager.ON);
        long since = t.timeSinceLastPowerOn();
        Assertions.assertTrue(since >= 0 && since < 60_000, "just turned on: " + since);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(DefaultPowerManagerTest.class);

}
