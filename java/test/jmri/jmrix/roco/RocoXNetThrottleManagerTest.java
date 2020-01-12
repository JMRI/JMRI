package jmri.jmrix.roco;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottleManagerTest;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;


/**
 * Tests for the jmri.jmrix.roco.RocoXNetThrottleManager class
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class RocoXNetThrottleManagerTest extends XNetThrottleManagerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new RocoCommandStation());
        tm = new RocoXNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @After
    @Override
    public void tearDown() {
        tm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
