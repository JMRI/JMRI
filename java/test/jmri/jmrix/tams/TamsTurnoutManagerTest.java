package jmri.jmrix.tams;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the TurnoutManager class
 *
 * @author	Bob Jacobsen  Copyright 2013, 2016
 */
public class TamsTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private TamsInterfaceScaffold nis = null;
    private TamsSystemConnectionMemo tm = null;

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initInternalTurnoutManager();
        // prepare an interface, register
        nis = new TamsInterfaceScaffold();
        tm = new TamsSystemConnectionMemo(nis);
        // create and register the manager object
        l = new TamsTurnoutManager(tm);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "TT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("TT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (TamsTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("TT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("TT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnoutManagerTest.class);

}
