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
        Assert.assertTrue(null != l.getBySystemName("LT21"));
        Assert.assertTrue(null != l.getBySystemName("LT22"));

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
        Assert.assertTrue(null != l.getBySystemName("LT61"));
        Assert.assertEquals(Turnout.CLOSED, l.getBySystemName("LT61").getKnownState());
    }
    
    @Test
    public void testCreateFromMessage2 () {
        // Turnout LT62 () Switch input is Thrown (input on).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3D, 0x60, 0x13});
        lnis.sendTestMessage(m);
        Assert.assertTrue(null != l.getBySystemName("LT62"));
        Assert.assertEquals(Turnout.THROWN, l.getBySystemName("LT62").getKnownState());
    }
    
    @Test
    public void testCreateFromMessage3 () {
        // Turnout LT63 () Aux input is Thrown (input ).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3E, 0x40, 0x30});
        lnis.sendTestMessage(m);
        Assert.assertTrue(null != l.getBySystemName("LT63"));
        Assert.assertEquals("EXACT", l.getBySystemName("LT63").getFeedbackModeName());
        Assert.assertEquals(Turnout.INCONSISTENT, l.getBySystemName("LT63").getKnownState());
    }
    
    @Test
    public void testCreateFromMessage4 () {
        // Turnout LT64 () Aux input is Closed (input off).
        LocoNetMessage m = new LocoNetMessage(new int[]{0xb1, 0x3F, 0x50, 0x21});
        lnis.sendTestMessage(m);
        Assert.assertTrue(null != l.getBySystemName("LT64"));
        Assert.assertEquals("EXACT", l.getBySystemName("LT64").getFeedbackModeName());
        Assert.assertEquals(Turnout.THROWN, l.getBySystemName("LT64").getKnownState());
    }
    
    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("LT21", "my name");

        log.debug("received turnout value {}", o);
        Assert.assertTrue(null != (LnTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", l.getBySystemName("LT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   {}", l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("LT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));
    }

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Override
    @Before
    public void setUp(){
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface, register
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        // create and register the manager object
        l = new LnTurnoutManager(lnis, lnis, memo.getSystemPrefix(), false);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        memo.dispose();
        lnis = null;
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnTurnoutManagerTest.class);

}
