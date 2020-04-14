package jmri.jmrix.ztc.ztc611;

import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.ztc.ztc611.ZTC611XNetTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2004
 */
public class ZTC611XNetTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "XT" + i;
    }

    XNetInterfaceScaffold lnis;

    @Test
    public void testAsAbstractFactory() {
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        ZTC611XNetTurnoutManager l = new ZTC611XNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);

        // ask for a Turnout, and check type
        TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();

        Turnout o = t.newTurnout("XT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (ZTC611XNetTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + t.getBySystemName("XT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + t.getByUserName("my name"));
        }

        Assert.assertTrue(null != t.getBySystemName("XT21"));
        Assert.assertTrue(null != t.getByUserName("my name"));

    }

    @Test
    @Override
    public void testThrownText(){
         Assert.assertEquals("thrown text","Thrown (+)",l.getThrownText());
    }

    @Test
    @Override
    public void testClosedText(){
         Assert.assertEquals("closed text","Closed (-)",l.getClosedText());
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
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new ZTC611XNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

    private final static Logger log = LoggerFactory.getLogger(ZTC611XNetTurnoutManagerTest.class);

}
