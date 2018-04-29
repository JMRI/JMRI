package jmri.jmrix.secsi;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	tests for the SerialTurnoutManager class
 *
 * @author	Bob Jacobsen Copyright 2004, 2008
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        SerialTrafficController t = new SerialTrafficControlScaffold();
        SecsiSystemConnectionMemo memo = new SecsiSystemConnectionMemo();
        memo.setTrafficController(t);
        t.registerNode(new SerialNode(0, SerialNode.DAUGHTER,t));
        // create and register the manager object
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "VT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("VT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (SerialTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("VT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("VT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
