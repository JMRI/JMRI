package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private MarklinTrafficController tc;
    private MarklinSystemConnectionMemo memo;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficController(){
            @Override
            public void sendMarklinMessage(MarklinMessage m, MarklinListener reply) {
            } // prevent requestThrottle to actually send a MarklinMessage
        };
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
        tm = memo.getThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        //if (tm != null) {
        //    tm.dispose();
        //}
        tc.terminateThreads();
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinThrottleManagerTest.class);

}
