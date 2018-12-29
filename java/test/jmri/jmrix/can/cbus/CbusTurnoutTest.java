package jmri.jmrix.can.cbus;


import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Turnout;
import jmri.util.JUnitUtil;
// import jmri.util.PropertyChangeListenerScaffold;

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
        Assert.assertTrue(("[78] 99 00 00 00 01").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue(("[78] 98 00 00 00 01").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }
    
    public void checkNoMsgSent(int previousSize) {
        Assert.assertTrue( previousSize == tcis.outbound.size() );
    }
    
    public void checkStatusRequestMsgSent() {
        Assert.assertTrue(("[78] 9A 00 00 00 01").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }    

    public void checkLongStatusRequestMsgSent() {
        Assert.assertTrue(("[78] 92 30 39 D4 31").equals(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
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
    public void testNullEvent() {
        try {
            new CbusTurnout("MT",null,tcis);
            Assert.fail("Should have thrown an exception");
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
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
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        tcis = new TrafficControllerScaffold();
        t = new CbusTurnout("MT", "+1;-1", tcis);
        // l = new PropertyChangeListenerScaffold();
    }

    @After
    public void tearDown() {
        t.dispose();
        tcis=null;
        // l = null;
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(CbusTurnoutTest.class);
}
