package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottleManagerTest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


/**
 * Tests for the jmri.jmrix.lenz.z21XNetThrottleManager class
 *
 * @author Paul Bender Copyright (C) 2015,2016
 */
public class Z21XNetThrottleManagerTest extends XNetThrottleManagerTest {

    private XNetInterfaceScaffold tc;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        tm = new Z21XNetThrottleManager(new XNetSystemConnectionMemo(tc));
    }

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
