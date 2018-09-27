package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Jmri package LnPowerManager.
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class LnPowerManagerTest extends AbstractPowerManagerTestBase {

    private LocoNetSystemConnectionMemo memo;

    /**
     * service routines to simulate receiving on, off from interface
     */
    @Override
    protected void hearOn() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_GPON);
        controller.sendTestMessage(l);
    }

    @Override
    protected void sendOnReply() {
        hearOn();
    }

    @Override
    protected void hearOff() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_GPOFF);
        controller.sendTestMessage(l);
    }

    @Override
    protected void sendOffReply() {
        hearOff();
    }

    @Override
    protected void hearIdle() {
        LocoNetMessage l = new LocoNetMessage(2);
        l.setOpCode(LnConstants.OPC_IDLE);
        controller.sendTestMessage(l);
    }

    @Override
    protected void sendIdleReply() {
        hearIdle();
    }

    @Override
    protected int numListeners() {
        return controller.numListeners();
    }

    @Override
    protected int outboundSize() {
        return controller.outbound.size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        return LnConstants.OPC_GPON
                == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return LnConstants.OPC_GPOFF
                == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        return LnConstants.OPC_IDLE
                == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    @Test
    public void testImplementsIdle() {
        // LocoNet implements IDLE power state
        Assert.assertTrue(p.implementsIdle());
    }

    // setup a default interface
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        controller = new LocoNetInterfaceScaffold();
        memo = new LocoNetSystemConnectionMemo(controller, null);
        p = pwr = new LnPowerManager(memo);
    }

    @After
    public void tearDown() {
        pwr.dispose();
        memo.dispose();
        pwr = null;
        memo = null;
        controller = null;
        JUnitUtil.tearDown();
    }

    LocoNetInterfaceScaffold controller;  // holds dummy for testing
    LnPowerManager pwr;

}
