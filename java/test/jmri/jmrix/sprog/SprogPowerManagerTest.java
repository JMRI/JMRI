package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * Tests for SprogPowerManager.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogPowerManagerTest extends jmri.jmrix.AbstractPowerManagerTestBase {

    private SprogTrafficControlScaffold stc = null;

    // service routines to simulate receiving on, off from interface
    @Override
    protected void hearOn() {
      stc.sendTestReply(new SprogReply("+"));
    }

    @Override
    protected void sendOnReply() {
       stc.sendTestReply(new SprogReply("+"));
    }

    @Override
    protected void sendOffReply() {
       stc.sendTestReply(new SprogReply("-"));
    }

    @Override
    protected void hearOff() {
       stc.sendTestReply(new SprogReply("-"));
    }
    
    @Override
    protected void sendIdleReply() {
        return;
    }

    @Override
    protected void hearIdle() {
        return;
    }

    @Override
    protected int numListeners() {
        return stc.numListeners();
    }

    @Override
    protected int outboundSize() {
        return stc.outbound.size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("+");
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("-");
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        return true;
    }

    @Test
    @Override
    @NotApplicable("unsolicited state changes are currently ignored")
    public void testStateOn(){
    }

    @Test
    @Override
    @NotApplicable("unsolicited state changes are currently ignored")
    public void testStateOff(){
    }

    @Override
    @Test
    public void testSetPowerOff() throws jmri.JmriException {
        int startingMessages = outboundSize();
        p.setPower(jmri.PowerManager.OFF);
        // Sprog sends the message 3 times.
        // check one message sent, correct form
        Assert.assertEquals("messages sent", startingMessages + 3, outboundSize());
        Assert.assertTrue("message type OK", outboundOffOK(startingMessages));
        Assert.assertTrue("message type OK", outboundOffOK(startingMessages+1));
        Assert.assertTrue("message type OK", outboundOffOK(startingMessages+2));
        Assert.assertEquals("state before reply ", jmri.PowerManager.UNKNOWN, p.getPower());
        // arrange for reply
        sendOffReply();
        Assert.assertEquals("state after reply ", jmri.PowerManager.OFF, p.getPower());

    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo();
        stc = new SprogTrafficControlScaffold(m);
        stc.setTestReplies(true);
        m.setSprogTrafficController(stc);  // constructor calls getSprogTrafficController.
        p = new SprogPowerManager(m);
    }

    @After
    public void tearDown() {
        stc.dispose();
        JUnitUtil.tearDown();
    }

}
