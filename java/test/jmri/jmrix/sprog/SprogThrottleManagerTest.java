package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private SprogTrafficControlScaffold stcs = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        tm = new SprogThrottleManager(m);
    }

    @AfterEach
    public void tearDown() {
        stcs.dispose();
        JUnitUtil.tearDown();
    }

}
