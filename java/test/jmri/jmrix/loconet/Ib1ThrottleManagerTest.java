package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Ib1ThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Test
    @Override
    @Disabled("parent class test requires further setup")
    @ToDo("complete initialization and remove this overriden method so that the parent class test can run")
    public void testGetThrottleInfo() {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        tm = new Ib1ThrottleManager(new LocoNetSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        ((Ib1ThrottleManager)tm).dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Ib1ThrottleManagerTest.class);

}
