package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottleManagerTest;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;


/**
 * Tests for the jmri.jmrix.lenz.z21XNetThrottleManager class
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class Z21XNetThrottleManagerTest extends XNetThrottleManagerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        tm = new Z21XNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
