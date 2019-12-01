package jmri.jmrix.easydcc;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.easydcc.EasyDccTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        // create and register the manager object
        EasyDccSystemConnectionMemo memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        l = new EasyDccTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "ET" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("ET21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (EasyDccTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("ET21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("ET21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManagerTest.class);

}
