package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
       Assert.assertNotNull("exists",op);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();

        op = new SprogCSThrottleManager(m);
    }

    @After
    public void tearDown() {
        m.getSlotThread().interrupt();
        stcs.dispose();
        op = null;
        stcs = null;
        m = null;
        JUnitUtil.tearDown();
    }

}
