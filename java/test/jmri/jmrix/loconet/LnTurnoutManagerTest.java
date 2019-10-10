package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.List;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2005
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
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
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
        Assert.assertNotNull(l.getBySystemName("LT21"));
        Assert.assertNotNull(l.getBySystemName("LT22"));

        // check the list
        List<String> testList = new ArrayList<>(2);
        testList.add("LT21");
        testList.add("LT22");
        Assert.assertEquals("system name list", testList, l.getSystemNameList());
    }

    @Test
    public void testCreateFromMessage1 () {
        // Turnout LT61 () Switch input is Closed (input off).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3C, 0x70, 0x02});
        lnis.sendTestMessage(m);
        Assert.assertNotNull(l.getBySystemName("LT61"));
        Assert.assertEquals(Turnout.CLOSED, l.getBySystemName("LT61").getKnownState());
    }
    
    @Test
    public void testCreateFromMessage2 () {
        // Turnout LT62 () Switch input is Thrown (input on).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3D, 0x60, 0x13});
        lnis.sendTestMessage(m);
        Assert.assertNotNull(l.getBySystemName("LT62"));
        Assert.assertEquals(Turnout.THROWN, l.getBySystemName("LT62").getKnownState());
    }
    
    @Test
    public void testCreateFromMessage3 () {
        // Turnout LT63 () Aux input is Thrown (input ).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3E, 0x40, 0x30});
        lnis.sendTestMessage(m);
        Assert.assertNotNull(l.getBySystemName("LT63"));
        Assert.assertEquals("EXACT", l.getBySystemName("LT63").getFeedbackModeName());
        Assert.assertEquals(Turnout.INCONSISTENT, l.getBySystemName("LT63").getKnownState());
    }
    
    @Test
    public void testCreateFromMessage4 () {
        // Turnout LT64 () Aux input is Closed (input off).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3F, 0x50, 0x21});
        lnis.sendTestMessage(m);
        Assert.assertNotNull(l.getBySystemName("LT64"));
        Assert.assertEquals("EXACT", l.getBySystemName("LT64").getFeedbackModeName());
        Assert.assertEquals(Turnout.THROWN, l.getBySystemName("LT64").getKnownState());
    }
    
    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("LT21", "my name");

        log.debug("received turnout value {}", o);
        Assert.assertNotNull(o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", l.getBySystemName("LT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   {}", l.getByUserName("my name"));
        }

        Assert.assertNotNull(l.getBySystemName("LT21"));
        Assert.assertNotNull(l.getByUserName("my name"));
    }
    
        @Test
    public void testOpcLongAck() {
        Assert.assertEquals("Check no outbound messages", 0, lnis.outbound.size());
        ((LnTurnoutManager)l).mTurnoutNoRetry=false;

        Turnout t = ((LnTurnoutManager)l).provideTurnout("LT1");   // createNewTurnout does not register it.
        LocoNetMessage m = new LocoNetMessage(new int[] {0xb0, 0x00, 0x20, 0x6f});
        lnis.sendTestMessage(m);
        Assert.assertEquals("check now closed", Turnout.CLOSED, t.getKnownState());
        Assert.assertEquals("Check no outbound messages", 0, lnis.outbound.size());
        Assert.assertNotNull(((LnTurnoutManager)l).lastSWREQ);
        Assert.assertEquals(LnConstants.OPC_SW_REQ, ((LnTurnoutManager)l).lastSWREQ.getOpCode());
        Assert.assertEquals(0x00, ((LnTurnoutManager)l).lastSWREQ.getElement(1));
        Assert.assertEquals(0x20, ((LnTurnoutManager)l).lastSWREQ.getElement(2));

        Assert.assertEquals("Check no outbound messages", 0, lnis.outbound.size());
        Assert.assertEquals("Check that the turnout message was saved as 'last'",
                m, ((LnTurnoutManager)l).lastSWREQ);
        lnis.sendTestMessage(m);    // command station rejection of turnout command
        m.setOpCode(0xB4);
        m.setElement(1, 0x30);
        m.setElement(2, 0x00);
        m.setElement(3, 0x7b);
        Assert.assertEquals("check sent message opcode", 0xb4, m.getOpCode());
        lnis.sendTestMessage(m);    // command station rejection of turnout command
        Assert.assertEquals("Check one outbound messages", 1, lnis.outbound.size());
        Assert.assertEquals("check now closed (2)", Turnout.CLOSED, t.getKnownState());

        Assert.assertFalse("check turnout manager retry mechanism setting", ((LnTurnoutManager)l).mTurnoutNoRetry);

        jmri.util.JUnitUtil.fasterWaitFor(() -> {return 1 < lnis.outbound.size();});
        Assert.assertEquals("Check an outbound message", 1, lnis.outbound.size());
        
        Assert.assertEquals("Check outbound message opcode", LnConstants.OPC_SW_REQ, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("Check outbound message byte 1", 0x00, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("Check outbound message byte 2", 0x20, lnis.outbound.get(0).getElement(2));
    }



    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @Before
    public void setUp(){
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface, register
        memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        // create and register the manager object
        l = new LnTurnoutManager(memo, lnis, false);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        memo.dispose();
        lnis = null;
        l = null;
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnTurnoutManagerTest.class);

}
