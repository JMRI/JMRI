package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogThrottle
 * </P>
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogThrottleTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogThrottle op = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",op);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        op = new SprogThrottle(m,new jmri.DccLocoAddress(2,false));
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
