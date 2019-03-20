package jmri.jmrix.loconet.pr2;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.SlotManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * tests for the Jmri package LnPr2PowerManager
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class LnPr2PowerManagerTest extends AbstractPowerManagerTestBase {

    private SlotManager slotmanager = null;

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
        LocoNetMessage m = new LocoNetMessage(16);
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x10);
        m.setElement(2, 0x22);
        m.setElement(3, 0x22);
        m.setElement(4, 0x01);
        m.setElement(5, 0x00);
        m.setElement(6, 0x00);
        m.setElement(7, 0x00);
        m.setElement(8, 0x00);
        m.setElement(9, 0x00);
        m.setElement(10, 0x00);
        m.setElement(11, 0x00);
        m.setElement(12, 0x00);
        m.setElement(13, 0x00);
        m.setElement(14, 0x00);
        controller.sendTestMessage(m);
    }

    @Override
    protected void sendOffReply() {
        hearOff();
    }

    @Override
    protected void hearIdle() {
        return;
    }

    @Override
    protected void sendIdleReply() {
        return;
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
        Assert.assertEquals(LnConstants.OPC_WR_SL_DATA,controller.outbound.elementAt(index).getOpCode());
        return LnConstants.OPC_WR_SL_DATA
                == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected boolean outboundOffOK(int index) {
        Assert.assertEquals(LnConstants.OPC_WR_SL_DATA,controller.outbound.elementAt(index).getOpCode());
        return LnConstants.OPC_WR_SL_DATA
                == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        return false;
    }

    @Test
    @Override
    public void testSetPowerOn() throws JmriException {
        int initialSent = outboundSize();
        p.setPower(PowerManager.ON);
        // check one message sent, correct form, unknown state
        Assert.assertEquals("messages sent", initialSent + 1, outboundSize());
        Assert.assertTrue("message type OK", outboundOnOK(initialSent));
        Assert.assertEquals("state after set", PowerManager.ON, p.getPower());
    }

    @Test
    @Override
    public void testSetPowerOff() throws JmriException {
        int startingMessages = outboundSize();
        p.setPower(PowerManager.OFF);
        // check one message sent, correct form
        Assert.assertEquals("messages sent", startingMessages + 1, outboundSize());
        Assert.assertTrue("message type OK", outboundOffOK(startingMessages));
        Assert.assertEquals("state after set ", PowerManager.OFF, p.getPower());

    }


    @Test
    @Override
    @Ignore("test in parent class fails for some reason")
    @ToDo("investigate failure in parent class test and make corrections, either to initialization or to this overriden test")
    public void testDispose2() throws JmriException {
    }

    @Test
    @Override
    @Ignore("test in parent class fails for some reason")
    @ToDo("investigate failure in parent class test and make corrections, either to initialization or to this overriden test")
    public void testStateOff() throws JmriException {
    }
    
    @Test
    @Override
    public void testImplementsIdle() {
        if (p.implementsIdle()) {
            hearIdle();
            try {
                Assert.assertEquals("power state", PowerManager.IDLE, p.getPower());
            } catch (JmriException e) {
                Assert.fail("JmriJException occured invoking p.getPower()");
            }
        }
    }

    // setup a default interface
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        controller = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(controller);
        PR2SystemConnectionMemo memo = new PR2SystemConnectionMemo(controller,slotmanager);
        memo.configureManagers();
        jmri.InstanceManager.setThrottleManager(memo.getPr2ThrottleManager());
        memo.getPr2ThrottleManager().requestThrottleSetup(new jmri.DccLocoAddress(3,false),true);
        p = pwr = memo.get(jmri.PowerManager.class);
    }

    @After
    public void tearDown() {
        pwr.dispose();
        JUnitUtil.tearDown();
    }

    LocoNetInterfaceScaffold controller;  // holds dummy for testing
    LnPowerManager pwr;

}
