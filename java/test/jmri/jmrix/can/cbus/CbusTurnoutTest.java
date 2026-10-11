package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018
 */
public class CbusTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    @Override
    public void checkClosedMsgSent() {
        assertEquals("[5f8] 99 00 00 00 01",tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    @Override
    public void checkThrownMsgSent() {
        assertEquals("[5f8] 98 00 00 00 01",tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkNoMsgSent(int previousSize) {
        assertEquals( previousSize,tcis.outbound.size() );
    }

    public void checkStatusRequestMsgSent() {
        assertEquals("[5f8] 9A 00 00 00 01",tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
    }

    public void checkLongStatusRequestMsgSent() {
        assertEquals("[5f8] 92 30 39 D4 31",tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());
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
        assertEquals(1,tcis.outbound.size());
        tcis.outbound.clear();

        assertDoesNotThrow( () ->
            t.provideFirstFeedbackSensor("MS+54321"));
        t.setFeedbackMode("ONESENSOR");
        t.requestUpdateFromLayout();
        assertEquals(2,tcis.outbound.size());
        tcis.outbound.clear();

        assertDoesNotThrow( () ->
            t.provideSecondFeedbackSensor("MS+4545"));
        t.setFeedbackMode("TWOSENSOR");
        t.requestUpdateFromLayout();
        assertEquals(3,tcis.outbound.size());
        memo.dispose();
    }

    @Test
    public void testNullEvent() {
        Exception ex = assertThrows(NullPointerException.class, () -> {
            t = new CbusTurnout("MT",null,tcis); });

        // On Java 11 and below, the message is null.
        // On Java 17 and above, the message is text.
        boolean messageIsCorrect = ex.getMessage() == null
                || "Cannot invoke \"java.lang.CharSequence.length()\" because \"this.text\" is null".equals(ex.getMessage());
        assertTrue(messageIsCorrect);
    }

    @Test
    public void testCTorShortEventSingle() {
        t = new CbusTurnout("MT","+7",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorShortEventSinglePlus() {
        t = new CbusTurnout("MT","+2",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorShortEventSingleMinus() {
        t = new CbusTurnout("MT","-2",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorShortEventDouble() {
        t = new CbusTurnout("MT","+1;-1",tcis);
        assertNotNull(t,"exists");
    }


    @Test
    public void testLongEventSingleNoN() {
        t = new CbusTurnout("MT","+654e321",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testLongEventDoubleNoN() {
        t = new CbusTurnout("MT","-654e321;+123e456",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorLongEventSingle() {
        t = new CbusTurnout("MT","+n654e321",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorLongEventDouble() {
        t = new CbusTurnout("MT","+N299E17;-N123E456",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventJustOpsCode() {
        t = new CbusTurnout("MT","X04;X05",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventOneByte() {
        t = new CbusTurnout("MT","X2301;X30FF",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventTwoByte() {
        t = new CbusTurnout("MT","X410001;X56FFFF",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventThreeByte() {
        t = new CbusTurnout("MT","X6000010001;X72FFFFFF",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventFourByte() {
        t = new CbusTurnout("MT","X9000010001;X91FFFFFFFF",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventFiveByte() {
        t = new CbusTurnout("MT","XB00D60010001;XB1FFFAAFFFFF",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventSixByte() {
        t = new CbusTurnout("MT","XD00D0060010001;XD1FFFAAAFFFFFE",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testCTorHexEventSevenByte() {
        t = new CbusTurnout("MT","XF00D0A0600100601;XF1FFFFAAFAFFFFFE",tcis);
        assertNotNull(t,"exists");
    }

    @Test
    public void testShortEventSinglegetAddrThrown() {
        t = new CbusTurnout("MT","+7",tcis);
        CanMessage m1 = ((CbusTurnout)t).getAddrThrown();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x98); // ASON OPC
        m2.setElement(1, 0x00);
        m2.setElement(2, 0x00);
        m2.setElement(3, 0x00);
        m2.setElement(4, 0x07);
        assertEquals(m1,m2,"equals same");

    }

    @Test
    public void testShortEventSinglegetAddrClosed() {
        t = new CbusTurnout("MT","+7",tcis);
        CanMessage m1 = ((CbusTurnout)t).getAddrClosed();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x99); // ASOF OPC
        m2.setElement(1, 0x00);
        m2.setElement(2, 0x00);
        m2.setElement(3, 0x00);
        m2.setElement(4, 0x07);
        assertEquals(m1,m2,"equals same");

    }

    @Test
    public void testLongEventgetAddrThrown() {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m1 = ((CbusTurnout)t).getAddrThrown();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x90); // ACON OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        assertEquals(m1,m2,"equals same");

    }

    @Test
    public void testLongEventgetAddrClosed() {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m1 = ((CbusTurnout)t).getAddrClosed();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x91); // ACOF OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        assertEquals(m1,m2,"equals same");
        m2.setElement(0, 0x90); // ACON OPC
        assertNotEquals(m1,m2,"not equals same");
    }

    @Test
    public void testLongEventgetAddrThrownInverted() {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        t.setInverted(true);
        CanMessage m1 = ((CbusTurnout)t).getAddrThrown();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x91); // ACOF OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        assertEquals(m1,m2,"equals same");

    }

    @Test
    public void testLongEventgetAddrClosedInverted() {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        t.setInverted(true);
        CanMessage m1 = ((CbusTurnout)t).getAddrClosed();
        CanMessage m2 = new CanMessage(tcis.getCanid());
        m2.setNumDataElements(5);
        m2.setElement(0, 0x90); // ACON OPC
        m2.setElement(1, 0xd4);
        m2.setElement(2, 0x31);
        m2.setElement(3, 0x30);
        m2.setElement(4, 0x39);
        assertEquals(m1,m2,"equals same");

    }

    @Test
    public void testTurnoutCanMessage() throws jmri.JmriException {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x95); // EVULN OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.UNKNOWN,t.getKnownState());

        m.setElement(0, 0x90); // ACON OPC
        ((CbusTurnout)t).message(m);
        int val1 = t.getCommandedState();
        assertEquals(Turnout.THROWN,val1,"turnout closed via canmessage");

        m.setElement(0, 0x91); // ACOF OPC
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        t.setInverted(true);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.THROWN,t.getCommandedState());

        m.setElement(0, 0x90); // ACON OPC
        t.setInverted(true);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.CLOSED,t.getCommandedState());
    }

    @Test
    public void testTurnoutCanReply() throws jmri.JmriException {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x95); // EVULN OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.UNKNOWN,t.getCommandedState());

        r.setElement(0, 0x90); // ACON OPC
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.THROWN,t.getCommandedState());

        r.setElement(0, 0x91); // ACOF OPC
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        t.setInverted(true);
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.THROWN,t.getCommandedState());

        r.setElement(0, 0x90); // ACON OPC
        t.setInverted(true);
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

    }

    // with presence of node number should still resolve to short event turnout due to opc
    @Test
    public void testTurnoutCanMessageShortEvWithNode() throws jmri.JmriException {
        t = new CbusTurnout("MT","+12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x95); // EVULN OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.UNKNOWN,t.getCommandedState());

        m.setElement(0, 0x98); // ASON OPC
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.THROWN,t.getCommandedState());

        m.setElement(0, 0x99); // ASOF OPC
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        m.setElement(0, 0x98); // ASON OPC
        m.setExtended(true);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        m.setRtr(true);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        m.setExtended(false);
        ((CbusTurnout)t).message(m);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

    }

    // with presence of node number should still resolve to short event turnout due to opc
    @Test
    public void testTurnoutCanReplyShortEvWithNode() throws jmri.JmriException {
        t = new CbusTurnout("MT","+12345",tcis);
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x95); // EVULN OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.UNKNOWN,t.getCommandedState());

        r.setElement(0, 0x98); // ASON OPC
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.THROWN,t.getCommandedState());

        r.setElement(0, 0x99); // ASOF OPC
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.CLOSED,t.getCommandedState());


        r.setElement(0, 0x98); // ASON OPC
        r.setExtended(true);
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        r.setExtended(false);
        r.setRtr(true);
        ((CbusTurnout)t).reply(r);
        assertEquals(Turnout.CLOSED,t.getCommandedState());

    }

    @Test
    public void testDelayedTurnoutCanMessage() throws jmri.JmriException {
        t = new CbusTurnout("MT","+N54321E12345",tcis);
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);

        t.setFeedbackMode("DELAYED");
        Assertions.assertEquals(Turnout.UNKNOWN, t.getKnownState());

        ((CbusTurnout)t).message(m);
        JUnitUtil.waitFor(()-> t.getKnownState() == Turnout.INCONSISTENT,
                "Turnout message goes to INCONSISTENT before THROWN");
        JUnitUtil.waitFor(()-> t.getKnownState() == Turnout.THROWN,
                "Turnout.THROWN after delay");

        m.setElement(0, 0x91); // ACOF OPC
        ((CbusTurnout)t).message(m);
        JUnitUtil.waitFor(()-> t.getKnownState() == Turnout.INCONSISTENT,
                "Turnout message goes to INCONSISTENT before CLOSED");
        JUnitUtil.waitFor(()-> t.getKnownState() == Turnout.CLOSED,
                "Turnout.CLOSED after delay");

    }


    @Test
    public void testDelayedTurnoutThrownCanReply() throws jmri.JmriException {

        t = new CbusTurnout("MT","+N54321E12345",tcis);
        t.setFeedbackMode("DELAYED");
        Assertions.assertEquals(Turnout.UNKNOWN, t.getKnownState());

        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xd4);
        m.setElement(2, 0x31);
        m.setElement(3, 0x30);
        m.setElement(4, 0x39);

        ((CbusTurnout)t).reply(m);
        JUnitUtil.waitFor(()-> t.getKnownState() == Turnout.INCONSISTENT,
            "thrown Turnout.INCONSISTENT didn't happen");
        JUnitUtil.waitFor(()-> t.getKnownState() == Turnout.THROWN,
            "Turnout.THROWN didn't happen after delayed feedback");

    }

    @Test
    public void testDelayedTurnoutClosedCanReply() throws jmri.JmriException {

        t = new CbusTurnout("MT","+N54321E12345",tcis);
        t.setFeedbackMode("DELAYED");

        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x91); // ACOF OPC
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);

        ((CbusTurnout)t).reply(r);
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.INCONSISTENT); },
            "closed Turnout.INCONSISTENT didn't happen");
        JUnitUtil.waitFor(()->{ return(t.getKnownState() == Turnout.CLOSED); },
            " Turnout.CLOSED didn't happen after delayed feedback");

    }

    @Test
    public void testQueryTurnoutFromCbus() throws jmri.JmriException {

        t = new CbusTurnout("MT","+N54321E12345",tcis);

        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, CbusConstants.CBUS_ACOF);
        r.setElement(1, 0xd4);
        r.setElement(2, 0x31);
        r.setElement(3, 0x30);
        r.setElement(4, 0x39);

        ((CbusTurnout)t).reply(r); // turnout will be closed off
        assertEquals(Turnout.CLOSED,t.getCommandedState());

        r.setElement(0, CbusConstants.CBUS_AREQ);
        ((CbusTurnout)t).reply(r); // turnout will be receive event status request

        assertEquals("[5f8] 94 D4 31 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "AROF Request response sent");

        r.setElement(0, CbusConstants.CBUS_ACON);
        ((CbusTurnout)t).reply(r); // turnout will be thrown on

        r.setElement(0, CbusConstants.CBUS_AREQ);
        ((CbusTurnout)t).reply(r); // turnout will be receive event status request

        assertEquals("[5f8] 93 D4 31 30 39",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "ARON Request response sent");

        CbusTurnout tSplit = new CbusTurnout("MT","+5;-7",tcis);

        r.setElement(0, CbusConstants.CBUS_ASON);
        r.setElement(1, 0x00);
        r.setElement(2, 0x00);
        r.setElement(3, 0x00);
        r.setElement(4, 0x05);

        tSplit.reply(r); // turnout will be thrown on

        assertEquals(Turnout.THROWN,tSplit.getCommandedState());

        r.setElement(0, CbusConstants.CBUS_AREQ);
        tSplit.reply(r); // turnout will be receive event status LONG request

        assertEquals("[5f8] 9D 00 00 00 05",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "ARSON Request response sent");

        // turnout will be receive event status request for 2nd half of split, the incorrect side
        int size = tcis.outbound.size();
        r.setElement(4, 0x07);
        tSplit.reply(r);
        assertEquals(size, tcis.outbound.size(), "No response sent");

        r.setElement(0, CbusConstants.CBUS_ASOF); // turnout will be thrown off
        tSplit.reply(r);
        assertEquals(Turnout.CLOSED,tSplit.getCommandedState());

        r.setElement(0, CbusConstants.CBUS_ASRQ);
        r.setElement(4, 0x05);
        tSplit.reply(r); // turnout will be receive event status SHORT request

        assertEquals("[5f8] 9E 00 00 00 05",
            tcis.outbound.elementAt(tcis.outbound.size() - 1).toString(),
            "ARSOF Request response sent");

        tSplit.dispose();

    }

    private TrafficControllerScaffold tcis;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        tcis = new TrafficControllerScaffold();
        t = new CbusTurnout("MT", "+1;-1", tcis);
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        t = null;
        tcis.terminateThreads();
        tcis=null;
        JUnitUtil.tearDown();

    }
    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusTurnoutTest.class);
}
