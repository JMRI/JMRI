package jmri.jmrix.tmcc;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse 2021
 */
public class SerialThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assertions.assertNotNull( tm, "exists");
    }

    private TmccSystemConnectionMemo memo;
    private SerialTrafficController tc;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        memo = new TmccSystemConnectionMemo();
        tc = new SerialTrafficControlScaffold(memo);
        tm = new SerialThrottleManager(memo);
        InstanceManager.setThrottleManager(tm);
    }

    @AfterEach
    public void tearDown() {
        tm.dispose(); // no listeners in TMCC tm
        tm = null;
        memo.dispose();
        memo = null;
        if (tc != null) {
            tc.terminateThreads();
        }
        //log.warn("numListeners()={}", ((SerialTrafficControlScaffold)tc).numListeners());
        tc = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialThrottleManagerTest.class);

}
