package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Ib2ThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Test
    @Override
    @Disabled("parent class test requires further setup")
    @ToDo("finish initialization and remove this overriden test so that the parent class test can run")
    public void testGetThrottleInfo() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        tm = new Ib2ThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        ((Ib2ThrottleManager)tm).dispose();
        memo.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Ib2ThrottleManagerTest.class);

}
