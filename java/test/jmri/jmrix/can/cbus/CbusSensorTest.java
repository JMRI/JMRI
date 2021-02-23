package jmri.jmrix.can.cbus;

import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensor class.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class CbusSensorTest extends jmri.implementation.AbstractSensorTestBase {
    
    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {
        Assert.assertEquals(("[5f8] 98 00 00 00 01"),(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }

    @Override
    public void checkInactiveMsgSent() {
        Assert.assertEquals(("[5f8] 99 00 00 00 01"),(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }

    @Override
    public void checkStatusRequestMsgSent() {
        Assert.assertEquals(("[5f8] 9A 00 00 00 01"),(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }
    
    // X923039D431
    
    public void checkLongStatusRequestMsgSent() {
        Assert.assertEquals(("[5f8] 92 30 39 D4 31"),(tcis.outbound.elementAt(tcis.outbound.size() - 1).toString()));
    }
    
    public void checkNoMsgSent() {
        Assert.assertTrue(tcis.outbound.isEmpty());
    }

    @Test
    public void testIncomingChange() {
        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01}, tcis.getCanid()
        );
        CanMessage mInactive = new CanMessage(
                new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01}, tcis.getCanid()
        );

        // check states
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);

        ((CbusSensor)t).message(mActive);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);

        ((CbusSensor)t).message(mInactive);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);

    }

    @Test
    public void testLocalChange1() throws jmri.JmriException {
        tcis.outbound.clear();
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        checkActiveMsgSent();

        tcis.outbound.clear();
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        checkInactiveMsgSent();
        
        tcis.outbound.clear();
        t.setKnownState(Sensor.UNKNOWN);
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);
        checkNoMsgSent();        

        tcis.outbound.clear();
        t.setInverted(true);
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        checkInactiveMsgSent();

        tcis.outbound.clear();
        t.setInverted(true);
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        checkActiveMsgSent();
        
        tcis.outbound.clear();
        t.requestUpdateFromLayout();
        checkStatusRequestMsgSent();

        t = new CbusSensor("MS","+N12345E54321",tcis);
        t.requestUpdateFromLayout();
        checkLongStatusRequestMsgSent();
    }

    @Test
    public void testNullEvent() {
        try {
            CbusSensor fail = new CbusSensor("MS",null,tcis);
            Assert.fail("Should have thrown an exception" + fail);
        } catch (NullPointerException e) {
        }
    }


    @Test
    public void testCTorShortEventSingle() {
        t = new CbusSensor("MS","+7",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorShortEventSingleNegative() {
        t = new CbusSensor("MS","-7",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorShortEventDouble() {
        t = new CbusSensor("MS","+1;-1",tcis);
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testLongEventSingleNoN() {
        t = new CbusSensor("MS","+654e321",tcis);
        Assert.assertNotNull("exists",t);
    }    


    @Test
    public void testLongEventDoubleNoN() {
        t = new CbusSensor("MS","-654e321;+123e456",tcis);
        Assert.assertNotNull("exists",t);
    }    

    
    @Test
    public void testCTorLongEventSingle() {
        t = new CbusSensor("MS","+n654e321",tcis);
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorLongEventDouble() {
        t = new CbusSensor("MS","+N299E17;-N123E456",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventJustOpsCode() {
        t = new CbusSensor("MS","X04;X05",tcis);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventOneByte() {
        t = new CbusSensor("MS","X2301;X30FF",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testCTorHexEventTwoByte() {
        t = new CbusSensor("MS","X410001;X56FFFF",tcis);
        Assert.assertNotNull("exists",t);
    }

    
    @Test
    public void testCTorHexEventThreeByte() {
        t = new CbusSensor("MS","X6000010001;X72FFFFFF",tcis);
        Assert.assertNotNull("exists",t);
    }    
    
    
    
    @Test
    public void testCTorHexEventFourByte() {
        t = new CbusSensor("MS","X9000010001;X91FFFFFFFF",tcis);
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testCTorHexEventFiveByte() {
        t = new CbusSensor("MS","XB00D60010001;XB1FFFAAFFFFF",tcis);
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testCTorHexEventSixByte() {
        t = new CbusSensor("MS","XD00D0060010001;XD1FFFAAAFFFFFE",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testCTorHexEventSevenByte() {
        t = new CbusSensor("MS","XF00D0A0600100601;XF1FFFFAAFAFFFFFE",tcis);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testShortEventSinglegetAddrActive() {
        t = new CbusSensor("MS","+7",tcis);
        CanMessage m1 = ((CbusSensor)t).getAddrActive();
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
    public void testShortEventSinglegetAddrInactive() {
        t = new CbusSensor("MS","+7",tcis);
        CanMessage m1 = ((CbusSensor)t).getAddrInactive();
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
    public void testLongEventgetAddrActive() {
        t = new CbusSensor("MS","+N54321E12345",tcis);
        CanMessage m1 = ((CbusSensor)t).getAddrActive();
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
    public void testLongEventgetAddrInactive() {
        t = new CbusSensor("MS","+N54321E12345",tcis);
        CanMessage m1 = ((CbusSensor)t).getAddrInactive();
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
    public void testLongEventgetAddrActiveInverted() {
        t = new CbusSensor("MS","+N54321E12345",tcis);
        t.setInverted(true);
        CanMessage m1 = ((CbusSensor)t).getAddrActive();
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
    public void testLongEventgetAddrInactiveInverted() {
        t = new CbusSensor("MS","+N54321E12345",tcis);
        t.setInverted(true);
        CanMessage m1 = ((CbusSensor)t).getAddrInactive();
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
    public void testSensorCanMessage() throws jmri.JmriException {
        t = new CbusSensor("MS","+N54321E12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x95); // EVULN OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN); 
        
        m.setElement(0, 0x90); // ACON OPC
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        
        m.setElement(0, 0x91); // ACOF OPC
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
        t.setInverted(true);
        t.setKnownState(Sensor.UNKNOWN);
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        
        m.setElement(0, 0x90); // ACON OPC
        t.setInverted(true);
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);    
    }

    @Test
    public void testSensorCanReply() throws jmri.JmriException {
        t = new CbusSensor("MS","+N54321E12345",tcis);
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x95); // EVULN OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);        
        
        r.setElement(0, 0x90); // ACON OPC
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        
        r.setElement(0, 0x91); // ACOF OPC
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
        t.setInverted(true);
        t.setKnownState(Sensor.UNKNOWN);
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        
        r.setElement(0, 0x90); // ACON OPC
        t.setInverted(true);
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);    
    }
    
    // with presence of node number should still resolve to short event turnout due to opc
    @Test
    public void testSensorCanReplyShortEvWithNode() throws jmri.JmriException {
        t = new CbusSensor("MS","+12345",tcis);
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x95); // EVULN OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);        
        
        r.setElement(0, 0x98); // ASON OPC
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        
        r.setElement(0, 0x99); // ASOF OPC
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
        
        r.setElement(0, 0x98); // ASON OPC
        r.setExtended(true);
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
        r.setExtended(false);
        r.setRtr(true);
        ((CbusSensor)t).reply(r);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
    
    }
    
    // with presence of node number should still resolve to short event turnout due to opc
    @Test
    public void testSensorCanMessageShortEvWithNode() throws jmri.JmriException {
    
        t = new CbusSensor("MS","+12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x95); // EVULN OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN); 
        
        m.setElement(0, 0x98); // ASON OPC
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        
        m.setElement(0, 0x99); // ASOF OPC
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
        
        m.setElement(0, 0x98); // ASON OPC
        m.setExtended(true);
        ((CbusSensor)t).message(m);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        
        m.setExtended(false);
        m.setRtr(true);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
    
    }
    
    @Test
    public void checkNoMsgSentOnSetStateUnknownInconsistent() throws jmri.JmriException {
        
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);
        Assert.assertEquals(("tcis 0"),0,(tcis.outbound.size()));
        
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        Assert.assertEquals(("tcis 1"),1,(tcis.outbound.size()));

        t.setKnownState(Sensor.UNKNOWN);
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);
        Assert.assertEquals(("tcis still 1"),1,(tcis.outbound.size()));
        
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        Assert.assertEquals(("tcis 2"),2,(tcis.outbound.size()));
        
        t.setKnownState(Sensor.INCONSISTENT);
        Assert.assertTrue(t.getKnownState() == Sensor.INCONSISTENT);
        Assert.assertEquals(("tcis still 2"),2,(tcis.outbound.size()));
        
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        Assert.assertEquals(("tcis 3"),3,(tcis.outbound.size()));

    }
    
    private TrafficControllerScaffold tcis;
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        tcis = new TrafficControllerScaffold();
        t = new CbusSensor("MS", "+1;-1", tcis);
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        t = null;
        tcis.terminateThreads();
        tcis=null;
        JUnitUtil.tearDown();

    }
    // private final static Logger log = LoggerFactory.getLogger(CbusSensorTest.class);
}
