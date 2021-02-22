package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Test for the jmri.jmrix.lenz.XNetThrottleManager class
 *
 * @author Paul Bender
 * @author Egbert Broerse 2021
 */
public class XNetThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    XNetInterfaceScaffold tc;
    XNetSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        memo = new XNetSystemConnectionMemo(tc);
        tm = new XNetThrottleManager(memo);
    }

    @AfterEach
    public  void tearDown() {
        tm.dispose(); // no listeners in XNetThrottleManager
        tm = null;
        memo.dispose();
        memo = null;
        if (tc != null) {
            tc.terminateThreads();
            //log.warn("numListeners()={}", ((XNetInterfaceScaffold)tc).numListeners());
        }
        tc = null;
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(XNetThrottleManager.class);

}
