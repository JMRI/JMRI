package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21XNetTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2004
 * @author	Paul Bender Copyright 2016
 */
public class Z21XNetTurnoutManagerTest extends jmri.jmrix.lenz.XNetTurnoutManagerTest {

    @Test
    @Override
    public void testSetAndGetOutputInterval() { // Z21/XNetTurnoutManager has no direct access to Memo, ask TC
        Assert.assertEquals("default outputInterval", 250, l.getOutputInterval("XT21")); // only the prefix is used to find the manager
        lnis.getSystemConnectionMemo().setOutputInterval(30);
        Assert.assertEquals("new outputInterval in memo", 30, lnis.getSystemConnectionMemo().getOutputInterval()); // direct set & get
        lnis.getSystemConnectionMemo().setOutputInterval(40);
        Assert.assertEquals("new outputInterval from manager", 40, l.getOutputInterval("XT21")); // test method in manager
    }

    @Override
    @After
    public void tearDown() {
	lnis = null;
	l = null;
        JUnitUtil.tearDown();
    }

    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        // create and register the manager object
        l = new Z21XNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

}
