package jmri.jmrix.dcc;

import jmri.Turnout;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.dcc.DccTurnoutManager class.
 *
 * @author	Bob Jacobsen
 */
public class DccTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int n) {
        return "BT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("BT21", "my name");

        log.debug("received turnout value {}", o);
        Assert.assertTrue(null != (DccTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", l.getBySystemName("BT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   {}", l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("BT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));
    }

    @Test
    @Override
    public void testSetAndGetOutputInterval() { // DccTurnoutManager has no direct access to a Memo or TC
        Assert.assertEquals("default outputInterval", 0, l.getOutputInterval("BT21")); // only the prefix is used to find the manager
        l.setOutputInterval(30);
        Assert.assertEquals("new outputInterval from manager", 30, l.getOutputInterval("BT21")); // direct set & get
    }

    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        // create and register the manager object
        l = new DccTurnoutManager();
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DccTurnoutManagerTest.class);

}
