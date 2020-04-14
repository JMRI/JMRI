package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21XNetTurnoutManager class.
 *
 * @author	Bob Jacobsen Copyright 2004
 * @author	Paul Bender Copyright 2016
 */
public class Z21XNetTurnoutManagerTest extends jmri.jmrix.lenz.XNetTurnoutManagerTest {

    @Override
    @After
    public void tearDown() {
	lnis = null;
	l = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
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
