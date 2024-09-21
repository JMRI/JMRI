package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SprogCSThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogCSThrottleManagerTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogCSThrottleManager op = null;
    private SprogSystemConnectionMemo m = null;

    @Test
    public void testCtor(){
        Assertions.assertNotNull( op, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface

        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();

        op = new SprogCSThrottleManager(m);
    }

    @AfterEach
    public void tearDown() {
        m.getSlotThread().interrupt();
        m.dispose();
        JUnitUtil.waitThreadTerminated(m.getSlotThread().getName());
        stcs.dispose();
        op = null;
        stcs = null;
        m = null;
        JUnitUtil.tearDown();
    }

}
