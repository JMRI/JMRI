package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.PowerManager;
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
        if (p.implementsIdle()) {
            LocoNetMessage l = new LocoNetMessage(2);
            l.setOpCode(LnConstants.OPC_IDLE);
            controller.sendTestMessage(l);
        }
    }

    @Override
    protected void sendIdleReply() {
        if (p.implementsIdle()) {
            hearIdle();
        }
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
        if (p.implementsIdle()) {
            return LnConstants.OPC_IDLE
                    == controller.outbound.elementAt(index).getOpCode();
        } else {
            return false;
        }
    }

    @Override
    @Test
    public void testImplementsIdle() {

        // DB150 implements IDLE power state
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DB150, false, false, false);
        Assert.assertTrue(p.implementsIdle());
        // DCS100 implements IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS100);
        Assert.assertTrue(p.implementsIdle());
        // DCS200 implements IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS200);
        Assert.assertTrue(p.implementsIdle());
        // DCS240 implements IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS240);
        Assert.assertTrue(p.implementsIdle());
        // DCS100 implements IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS210);
        Assert.assertTrue(p.implementsIdle());
        // DCS50 does not implement IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS050);
        Assert.assertFalse(p.implementsIdle());
        // DCS51 does not implement IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS051);
        Assert.assertFalse(p.implementsIdle());
        // PR2 does not implement IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_PR2_ALONE);
        Assert.assertFalse(p.implementsIdle());
        // PR3 does not implement IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_PR3_ALONE);
        Assert.assertFalse(p.implementsIdle());
        // Standalone LocoNet does not implement IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_STANDALONE);
        Assert.assertFalse(p.implementsIdle());

    }

    @Override
    @Test
    public void testStateIdle() throws JmriException {

        hearOn();  // set up an initial state
        // DCS51 does not implement IDLE power state
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS200, false, false, false);
        Assert.assertTrue(p.implementsIdle());
        hearIdle();
        Assert.assertEquals("power state", PowerManager.IDLE, p.getPower());

        hearOn(); // set up an initial state
        // PR2 does not implement IDLE power state
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_PR2_ALONE);
        Assert.assertFalse(p.implementsIdle());
        hearIdle();
        Assert.assertEquals("power state", PowerManager.ON, p.getPower());
    }

    @Test
    @Override
    public void testSetPowerIdle() throws JmriException {
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS200, false, false, false);
        Assert.assertTrue("LocoNet implements IDLE", p.implementsIdle());
        int initialSent = outboundSize();
        p.setPower(PowerManager.IDLE);
        // check one message sent, correct form, Idle state
        Assert.assertEquals("messages sent", initialSent + 1, outboundSize());
        Assert.assertTrue("message type IDLE O.K.", outboundIdleOK(initialSent));
        Assert.assertEquals("state before reply ", PowerManager.UNKNOWN, p.getPower());
        // arrange for reply
        sendIdleReply();
        Assert.assertEquals("state after reply ", PowerManager.IDLE, p.getPower());

        p.setPower(PowerManager.OFF);
        memo.getSlotManager().setCommandStationType(LnCommandStationType.COMMAND_STATION_PR2_ALONE);
        Assert.assertFalse("LocoNet implements IDLE", p.implementsIdle());
        initialSent = outboundSize();
        p.setPower(PowerManager.IDLE);
        // check no  message sent
        Assert.assertEquals("messages sent", initialSent, outboundSize());
    }

    // setup a default interface
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        controller = new LocoNetInterfaceScaffold();
        memo = new LocoNetSystemConnectionMemo(controller, null);
        memo.getLnTrafficController().memo = memo;

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
