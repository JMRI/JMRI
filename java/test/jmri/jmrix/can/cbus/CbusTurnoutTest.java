package jmri.jmrix.can.cbus;


import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018
 */
public class CbusTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    private TrafficControllerScaffold tcis = null;
    // protected PropertyChangeListenerScaffold l; 

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue(("[5f8] 99 00 00 00 01").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue(("[5f8] 98 00 00 00 01").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }
    
    public void checkNoMsgSent(int previousSize) {
        Assert.assertTrue( previousSize == tcis.outbound.size() );
    }
    
    public void checkStatusRequestMsgSent() {
        Assert.assertTrue(("[5f8] 9A 00 00 00 01").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }    

    public void checkLongStatusRequestMsgSent() {
        Assert.assertTrue(("[5f8] 92 30 39 D4 31").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    } 
    
    @Test
    @Override
    public void testRequestUpdate() {

        t.requestUpdateFromLayout();
        checkStatusRequestMsgSent();

        t = new CbusTurnout("MT","-N12345E54321",tcis);
        t.requestUpdateFromLayout();
        checkLongStatusRequestMsgSent();

    }
    
    @Test
    public void testRequestUpdateSensors() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        jmri.InstanceManager.setDefault(jmri.SensorManager.class,new CbusSensorManager(memo));

        t.requestUpdateFromLayout();
        Assert.assertEquals(1,tcis.outbound.size());
        tcis.outbound.clear();
        
        try {
            t.provideFirstFeedbackSensor("MS+54321");
        } catch (jmri.JmriException ex) { }
        t.setFeedbackMode("ONESENSOR");
        t.requestUpdateFromLayout();
        Assert.assertEquals(2,tcis.outbound.size());
        tcis.outbound.clear();
        
        try {
            t.provideSecondFeedbackSensor("MS+4545");
        } catch (jmri.JmriException ex) { }
        t.setFeedbackMode("TWOSENSOR");
        t.requestUpdateFromLayout();
        Assert.assertEquals(3,tcis.outbound.size());
    }    
    
    @Test
    public void testNullEvent() {
        try {
            new CbusTurnout("MT",null,tcis);
            Assert.fail("Should have thrown an exception");
        } catch (NullPointerException e) {
        }
    }
    
    @Test
    public void testCTorShortEventSingle() {
        CbusTurnout t = new CbusTurnout("MT","+7",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorShortEventSinglePlus() {
        CbusTurnout t = new CbusTurnout("MT","+2",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorShortEventSingleMinus() {
        CbusTurnout t = new CbusTurnout("MT","-2",tcis);
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorShortEventDouble() {
        CbusTurnout t = new CbusTurnout("MT","+1;-1",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testLongEventSingleNoN() {
        CbusTurnout t = new CbusTurnout("MT","+654e321",tcis);
        Assert.assertNotNull("exists",t);
    }    

    @Test
    public void testLongEventDoubleNoN() {
        CbusTurnout t = new CbusTurnout("MT","-654e321;+123e456",tcis);
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorLongEventSingle() {
        CbusTurnout t = new CbusTurnout("MT","+n654e321",tcis);
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorLongEventDouble() {
        CbusTurnout t = new CbusTurnout("MT","+N299E17;-N123E456",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventJustOpsCode() {
        CbusTurnout t = new CbusTurnout("MT","X04;X05",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventOneByte() {
        CbusTurnout t = new CbusTurnout("MT","X2301;X30FF",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventTwoByte() {
        CbusTurnout t = new CbusTurnout("MT","X410001;X56FFFF",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventThreeByte() {
        CbusTurnout t = new CbusTurnout("MT","X6000010001;X72FFFFFF",tcis);
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorHexEventFourByte() {
        CbusTurnout t = new CbusTurnout("MT","X9000010001;X91FFFFFFFF",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventFiveByte() {
        CbusTurnout t = new CbusTurnout("MT","XB00D60010001;XB1FFFAAFFFFF",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventSixByte() {
        CbusTurnout t = new CbusTurnout("MT","XD00D0060010001;XD1FFFAAAFFFFFE",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventSevenByte() {
        CbusTurnout t = new CbusTurnout("MT","XF00D0A0600100601;XF1FFFFAAFAFFFFFE",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testShortEventSinglegetAddrThrown() {
        CbusTurnout t = new CbusTurnout("MT","+7",tcis);
        CanMessage m1 = t.getAddrThrown();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x98); // ASON OPC
        m2.setElement(1, 0x00);
        m2.setElement(2, 0x00);
        m2.setElement(3, 0x00);
        m2.setElement(4, 0x07);
        Assert.assertTrue("equals same", m1.equals(m2));
        
        m1 = null;
        m2 = null;
    }

    @Test
    public void testShortEventSinglegetAddrClosed() {
        CbusTurnout t = new CbusTurnout("MT","+7",tcis);
        CanMessage m1 = t.getAddrClosed();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x99); // ASOF OPC
        m2.setElement(1, 0x00);
        m2.setElement(2, 0x00);
        m2.setElement(3, 0x00);
        m2.setElement(4, 0x07);
        Assert.assertTrue("equals same", m1.equals(m2));
        
        m1 = null;
        m2 = null;
    }

    @Test
    public void testLongEventgetAddrThrown() {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m1 = t.getAddrThrown();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x90); // ACON OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        Assert.assertTrue("equals same", m1.equals(m2));
        
        m1 = null;
        m2 = null;
    }

    @Test
    public void testLongEventgetAddrClosed() {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m1 = t.getAddrClosed();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x91); // ACOF OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        Assert.assertTrue("equals same", m1.equals(m2));
        m2.setElement(0, 0x90); // ACON OPC
        Assert.assertFalse("not equals same", m1.equals(m2));
        
        m1 = null;
        m2 = null;
    }

    @Test
    public void testLongEventgetAddrThrownInverted() {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        t.setInverted(true);
        CanMessage m1 = t.getAddrThrown();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x91); // ACOF OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        Assert.assertTrue("equals same", m1.equals(m2));
        
        m1 = null;
        m2 = null;
    }

    @Test
    public void testLongEventgetAddrClosedInverted() {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        t.setInverted(true);
        CanMessage m1 = t.getAddrClosed();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x90); // ACON OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        Assert.assertTrue("equals same", m1.equals(m2));
        
        m1 = null;
        m2 = null;
    }

    @Test
    public void testTurnoutCanMessage() throws jmri.JmriException {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x95); // EVULN OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        t.message(m);
        Assert.assertTrue(t.getKnownState() == Turnout.UNKNOWN); 
        
        m.setElement(0, 0x90); // ACON OPC
        t.message(m);
        int val1 = t.getCommandedState();
        Assert.assertTrue("turnout closed via canmessage",( val1 == Turnout.THROWN ) );
        
        m.setElement(0, 0x91); // ACOF OPC
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        t.setInverted(true);
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.THROWN);
        
        m.setElement(0, 0x90); // ACON OPC
        t.setInverted(true);
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        m = null;
    }

    @Test
    public void testTurnoutCanReply() throws jmri.JmriException {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x95); // EVULN OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.UNKNOWN);        
        
        r.setElement(0, 0x90); // ACON OPC
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.THROWN);
        
        r.setElement(0, 0x91); // ACOF OPC
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        t.setInverted(true);
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.THROWN);
        
        r.setElement(0, 0x90); // ACON OPC
        t.setInverted(true);
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        r = null;
    }

    // with presence of node number should still resolve to short event turnout due to opc
    @Test
    public void testTurnoutCanMessageShortEvWithNode() throws jmri.JmriException {
        CbusTurnout t = new CbusTurnout("MT","+12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x95); // EVULN OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.UNKNOWN);        
        
        m.setElement(0, 0x98); // ASON OPC
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.THROWN);
        
        m.setElement(0, 0x99); // ASOF OPC
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        m.setElement(0, 0x98); // ASON OPC
        m.setExtended(true);
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        m.setRtr(true);
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        m.setExtended(false);
        t.message(m);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        m = null;
        
    }
    
    // with presence of node number should still resolve to short event turnout due to opc
    @Test
    public void testTurnoutCanReplyShortEvWithNode() throws jmri.JmriException {
        CbusTurnout t = new CbusTurnout("MT","+12345",tcis);
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x95); // EVULN OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.UNKNOWN);        
        
        r.setElement(0, 0x98); // ASON OPC
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.THROWN);
        
        r.setElement(0, 0x99); // ASOF OPC
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        
        r.setElement(0, 0x98); // ASON OPC
        r.setExtended(true);
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        r.setExtended(false);
        r.setRtr(true);
        t.reply(r);
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        r = null;
        
    }

    @Test
    public void testDelayedTurnoutCanMessage() throws jmri.JmriException {
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        
        CbusTurnout.DELAYED_FEEDBACK_INTERVAL=15;
        t.setFeedbackMode("DELAYED");
        t.message(m);
        Assert.assertTrue(t.getKnownState() == Turnout.INCONSISTENT); 
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.THROWN); }, "msg Turnout.THROWN didn't arrive");
        
        m.setElement(0, 0x91); // ACOF OPC
        t.message(m);
        Assert.assertTrue(t.getKnownState() == Turnout.INCONSISTENT); 
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.CLOSED); }, "msg Turnout.CLOSED didn't arrive");
        
        m = null;
    }
    
    
    @Test
    public void testDelayedTurnoutThrownCanReply() throws jmri.JmriException {
        
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CbusTurnout.DELAYED_FEEDBACK_INTERVAL=15;
        t.setFeedbackMode("DELAYED");
        
        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        
        t.reply(m);
        Assert.assertTrue(t.getKnownState() == Turnout.INCONSISTENT); 
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.THROWN); }, 
            "Turnout.THROWN didn't happen after delayed feedback");

        m = null;
    }
    
    @Test
    public void testDelayedTurnoutClosedCanReply() throws jmri.JmriException {
        
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        CbusTurnout.DELAYED_FEEDBACK_INTERVAL=15;
        t.setFeedbackMode("DELAYED");
        
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x91); // ACOF OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        
        t.reply(r);
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.INCONSISTENT); },
            "closed Turnout.INCONSISTENT didn't happen add retry?"); 
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.CLOSED); }, 
            " Turnout.CLOSED didn't happen after delayed feedback add retry?");
        
        t.dispose();
        r = null;
    }

    @Test
    public void testQueryTurnoutFromCbus() throws jmri.JmriException {
        
        CbusTurnout t = new CbusTurnout("MT","+N54321E12345",tcis);
        
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_ACOF);
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        
        t.reply(r); // turnout will be closed off
        Assert.assertTrue(t.getCommandedState() == Turnout.CLOSED);
        
        r.setElement(0, CbusConstants.CBUS_AREQ);
        t.reply(r); // turnout will be receive event status request
        
        Assert.assertEquals("AROF Request response sent","[5f8] 94 D4 31 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        r.setElement(0, CbusConstants.CBUS_ACON);
        t.reply(r); // turnout will be thrown on
        
        r.setElement(0, CbusConstants.CBUS_AREQ);
        t.reply(r); // turnout will be receive event status request
        
        Assert.assertEquals("ARON Request response sent","[5f8] 93 D4 31 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        CbusTurnout tSplit = new CbusTurnout("MT","+5;-7",tcis);
        
        r.setElement(0, CbusConstants.CBUS_ASON);
        r.setElement(1, 0x00);
        r.setElement(2, 0x00);
        r.setElement(3, 0x00);
        r.setElement(4, 0x05);
        
        tSplit.reply(r); // turnout will be thrown on
        
        Assert.assertTrue(tSplit.getCommandedState() == Turnout.THROWN);
        
        r.setElement(0, CbusConstants.CBUS_AREQ);
        tSplit.reply(r); // turnout will be receive event status LONG request
        
        Assert.assertEquals("ARSON Request response sent","[5f8] 9D 00 00 00 05",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        // turnout will be receive event status request for 2nd half of split, the incorrect side
        int size = tcis.outbound.size();
        r.setElement(4, 0x07);
        tSplit.reply(r);
        Assert.assertTrue("No response sent", size == tcis.outbound.size());
        
        r.setElement(0, CbusConstants.CBUS_ASOF); // turnout will be thrown off
        tSplit.reply(r);
        Assert.assertTrue(tSplit.getCommandedState() == Turnout.CLOSED);
        
        r.setElement(0, CbusConstants.CBUS_ASRQ);
        r.setElement(4, 0x05);
        tSplit.reply(r); // turnout will be receive event status SHORT request
        
        Assert.assertEquals("ARSOF Request response sent","[5f8] 9E 00 00 00 05",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
        
        tSplit.dispose();
        t.dispose();
        r = null;
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        tcis = new TrafficControllerScaffold();
        t = new CbusTurnout("MT", "+1;-1", tcis);
    }

    @After
    public void tearDown() {
        t.dispose();
        t = null;
        tcis=null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
    // private final static Logger log = LoggerFactory.getLogger(CbusTurnoutTest.class);
}
