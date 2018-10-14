package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for SprogTurnout.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

   private SprogTrafficControlScaffold stcs = null;

    @Override
    public int numListeners() {
        return stcs.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", stcs.outbound.size() > 0);
        Assert.assertEquals("content", "O 81 FA 7B", stcs.outbound.elementAt(stcs.outbound.size() - 1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", stcs.outbound.size() > 0);
        Assert.assertEquals("content", "O 81 FB 7A", stcs.outbound.elementAt(stcs.outbound.size() - 1).toString());  // CLOSED message
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();

        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        t = new SprogTurnout(2,m);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @After
    public void tearDown() {
        stcs.dispose();
        JUnitUtil.tearDown();
    }

}
