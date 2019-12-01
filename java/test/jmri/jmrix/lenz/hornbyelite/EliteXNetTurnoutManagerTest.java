package jmri.jmrix.lenz.hornbyelite;

import java.util.ArrayList;
import java.util.List;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2004
 */
public class EliteXNetTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "XT" + i;
    }

    XNetInterfaceScaffold lnis;

    @Test    
    @Override
    public void testMisses() {
        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testEliteXNetMessages() {
        // send messages for 20, 21
        // notify that somebody else changed it...
        XNetReply m1 = new XNetReply();
        m1.setElement(0, 0x42);
        m1.setElement(1, 0x05);
        m1.setElement(2, 0x02);
        m1.setElement(3, 0x45);
        lnis.sendTestMessage(m1);

        // notify that somebody else changed it...
        XNetReply m2 = new XNetReply();
        m2.setElement(0, 0x42);
        m2.setElement(1, 0x05);
        m2.setElement(2, 0x04);
        m2.setElement(3, 0x43);
        lnis.sendTestMessage(m2);

        // try to get turnouts to see if they exist
        Assert.assertTrue(null != l.getBySystemName("XT20"));
        Assert.assertTrue(null != l.getBySystemName("XT21"));

        // check the list
        List<String> testList = new ArrayList<String>(2);
        testList.add("XT20");
        testList.add("XT21");
        Assert.assertEquals("system name list", testList, l.getSystemNameList());
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("XT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (EliteXNetTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("XT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("XT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @Test
    @Override
    public void testThrownText(){
         Assert.assertEquals("thrown text",Bundle.getMessage("TurnoutStateThrown"),l.getThrownText());
    }

    @Test
    @Override
    public void testClosedText(){
         Assert.assertEquals("closed text",Bundle.getMessage("TurnoutStateClosed"),l.getClosedText());
    }

    @After
    public void tearDown() {
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        // create and register the manager object
        l = new EliteXNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutManagerTest.class);

}
