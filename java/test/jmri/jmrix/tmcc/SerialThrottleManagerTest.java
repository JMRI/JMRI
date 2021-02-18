package jmri.jmrix.tmcc;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", tm);
    }

//    @Test
//    @Override
//    @Disabled("parent class test requires further setup")
//    @ToDo("complete initialization and remove this overridden method so that the parent class test can run")
//    public void testGetThrottleInfo() {
//    }

    private TmccSystemConnectionMemo memo;
    private SerialTrafficController tc;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        memo = new TmccSystemConnectionMemo();
        tc = new SerialTrafficControlScaffold(memo);
        tm = new SerialThrottleManager(memo);
        InstanceManager.setThrottleManager(tm);
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        //SerialThrottleManager dtm = (SerialThrottleManager)tm;
        //dtm.dispose(); // no listeners in TMCC tm
        // tm.releaseThrottle(throttle, a);
        tm = null;
        memo.dispose();
        memo = null;
        if (tc != null) {
            tc.terminateThreads();
        }
        log.warn("numListeners()={}", ((SerialTrafficControlScaffold)tc).numListeners());
        tc = null;
        JUnitUtil.resetWindows(false,false);
        //JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialThrottleManagerTest.class);

}
