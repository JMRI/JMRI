package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class LnTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "LT" + i;
    }

    @Test
    @Override
    public void testMisses() {
        // try to get nonexistant turnouts
        assertNull(l.getByUserName("foo"));
        assertNull(l.getBySystemName("bar"));
    }

    @Test
    public void testLocoNetMessages() {
        // send messages for 21, 22
        // notify the Ln that somebody else changed it...
        LocoNetMessage m1 = new LocoNetMessage(4);
        m1.setOpCode(0xb1);
        m1.setElement(1, 0x14);     // set CLOSED
        m1.setElement(2, 0x20);
        m1.setElement(3, 0x7b);
        lnis.sendTestMessage(m1);

        // notify the Ln that somebody else changed it...
        LocoNetMessage m2 = new LocoNetMessage(4);
        m2.setOpCode(0xb0);
        m2.setElement(1, 0x15);     // set CLOSED
        m2.setElement(2, 0x20);
        m2.setElement(3, 0x7a);
        lnis.sendTestMessage(m2);

        // try to get turnouts to see if they exist
        assertNotNull(l.getBySystemName("LT21"));
        assertNotNull(l.getBySystemName("LT22"));

        // check the list
        List<String> testList = new ArrayList<>(2);
        testList.add("LT21");
        testList.add("LT22");

        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("getSystemNameList");

        assertEquals(2, l.getNamedBeanSet().size(), "2 Turnouts in nambedbeanset");
        assertTrue(l.getNamedBeanSet().contains(l.getBySystemName("LT21")));
        assertTrue(l.getNamedBeanSet().contains(l.getBySystemName("LT22")));

    }

    @Test
    public void testCreateFromMessage1() {
        // Turnout LT61 () Switch input is Closed (input off).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3C, 0x70, 0x02});
        lnis.sendTestMessage(m);
        assertNotNull(l.getBySystemName("LT61"));
        assertEquals(Turnout.CLOSED, l.getBySystemName("LT61").getKnownState());
    }

    @Test
    public void testCreateFromMessage2() {
        // Turnout LT62 () Switch input is Thrown (input on).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3D, 0x60, 0x13});
        lnis.sendTestMessage(m);
        assertNotNull(l.getBySystemName("LT62"));
        assertEquals(Turnout.THROWN, l.getBySystemName("LT62").getKnownState());
    }

    @Test
    public void testCreateFromMessage3() {
        // Turnout LT63 () Aux input is Thrown (input ).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3E, 0x40, 0x30});
        lnis.sendTestMessage(m);
        assertNotNull(l.getBySystemName("LT63"));
        assertEquals("EXACT", l.getBySystemName("LT63").getFeedbackModeName());
        assertEquals(Turnout.INCONSISTENT, l.getBySystemName("LT63").getKnownState());
    }

    @Test
    public void testCreateFromMessage4() {
        // Turnout LT64 () Aux input is Closed (input off).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3F, 0x50, 0x21});
        lnis.sendTestMessage(m);
        assertNotNull(l.getBySystemName("LT64"));
        assertEquals("EXACT", l.getBySystemName("LT64").getFeedbackModeName());
        assertEquals(Turnout.THROWN, l.getBySystemName("LT64").getKnownState());
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("LT21", "my name");

        log.debug("received turnout value {}", o);
        assertNotNull(o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", l.getBySystemName("LT21"));
            log.debug("by user name:   {}", l.getByUserName("my name"));
        }

        assertNotNull(l.getBySystemName("LT21"));
        assertNotNull(l.getByUserName("my name"));
    }

    @Test
    public void testOpcLongAck() {
        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");
        ((LnTurnoutManager) l).mTurnoutNoRetry = false;

        Turnout t = ((LnTurnoutManager) l).provideTurnout("LT1");   // createNewTurnout does not register it.
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb0, 0x00, 0x20, 0x6f});
        lnis.sendTestMessage(m);
        assertEquals(Turnout.CLOSED, t.getKnownState(), "check now closed");
        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");
        assertNotNull(((LnTurnoutManager) l).lastSWREQ);
        assertEquals(LnConstants.OPC_SW_REQ, ((LnTurnoutManager) l).lastSWREQ.getOpCode());
        assertEquals(0x00, ((LnTurnoutManager) l).lastSWREQ.getElement(1));
        assertEquals(0x20, ((LnTurnoutManager) l).lastSWREQ.getElement(2));

        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");
        assertEquals(m, ((LnTurnoutManager) l).lastSWREQ, "Check that the turnout message was saved as 'last'");
        lnis.sendTestMessage(m);    // command station rejection of turnout command
        m.setOpCode(0xB4);
        m.setElement(1, 0x30);
        m.setElement(2, 0x00);
        m.setElement(3, 0x7b);
        assertEquals(0xb4, m.getOpCode(), "check sent message opcode");
        lnis.sendTestMessage(m);    // command station rejection of turnout command
        assertEquals(1, lnis.outbound.size(), "Check one outbound messages");
        assertEquals(Turnout.CLOSED, t.getKnownState(), "check now closed (2)");

        assertFalse(((LnTurnoutManager) l).mTurnoutNoRetry, "check turnout manager retry mechanism setting");

        JUnitUtil.fasterWaitFor(() -> !lnis.outbound.isEmpty(), "outbound sent");
        assertEquals(1, lnis.outbound.size(), "Check an outbound message");

        assertEquals(LnConstants.OPC_SW_REQ, lnis.outbound.get(0).getOpCode(), "Check outbound message opcode");
        assertEquals(0x00, lnis.outbound.get(0).getElement(1), "Check outbound message byte 1");
        assertEquals(0x20, lnis.outbound.get(0).getElement(2), "Check outbound message byte 2");
    }

    @Test
    public void testOpcLongAckToEnquiry() {
        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");
        ((LnTurnoutManager) l).mTurnoutNoRetry = false;

        ((LnTurnoutManager) l).provideTurnout("LT1018");   // This is effectively an enquiry command
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb0, 0x79, 0x37, 0x01});
        lnis.sendTestMessage(m);
        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");
        assertNotNull(((LnTurnoutManager) l).lastSWREQ);
        assertEquals(LnConstants.OPC_SW_REQ, ((LnTurnoutManager) l).lastSWREQ.getOpCode());
        assertEquals(0x79, ((LnTurnoutManager) l).lastSWREQ.getElement(1));
        assertEquals(0x37, ((LnTurnoutManager) l).lastSWREQ.getElement(2));

        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");
        assertEquals(m, ((LnTurnoutManager) l).lastSWREQ, "Check that the turnout message was saved as 'last'");
        lnis.sendTestMessage(m);    // command station rejection of turnout command
        m.setOpCode(0xB4);
        m.setElement(1, 0x30);
        m.setElement(2, 0x00);
        m.setElement(3, 0x7b);
        assertEquals(0xb4, m.getOpCode(), "check sent message opcode");
        lnis.sendTestMessage(m);    // command station rejection of turnout command
        assertEquals(0, lnis.outbound.size(), "Check no outbound messages");

        assertFalse(((LnTurnoutManager) l).mTurnoutNoRetry, "check turnout manager retry mechanism setting");
    }

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        // prepare an interface, register
        memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        // create and register the manager object
        l = new LnTurnoutManager(memo, lnis, false);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        lnis = null;
        l = null;
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnTurnoutManagerTest.class);

}
