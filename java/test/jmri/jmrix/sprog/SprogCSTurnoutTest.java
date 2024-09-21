package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogCSTurnout.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogCSTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @Override
    public int numListeners() {
        return stcs.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertArrayEquals("slot contents",jmri.NmraPacket.accDecoderPkt(2,false),m.getCommandStation().slot(0).getPayload());
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertArrayEquals("slot contents",jmri.NmraPacket.accDecoderPkt(2,true),m.getCommandStation().slot(0).getPayload());
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController
        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();

        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();

        t = new SprogCSTurnout(2,m);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);

    }

    @Override
    @AfterEach
    public void tearDown() {
        m.getSlotThread().interrupt();
        m.dispose();
        JUnitUtil.waitThreadTerminated(m.getSlotThread().getName());
        stcs.dispose();
        JUnitUtil.tearDown();
    }

}
