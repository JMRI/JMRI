package jmri.jmrix.tmcc;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the TMCCSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TmccSystemConnectionMemoTest extends SystemConnectionMemoTestBase<TmccSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void checkConfigureManagers() {
        scm.configureManagers();
        Assert.assertNotNull("Throttle Manager after configureManagers", scm.getThrottleManager());
        Assert.assertNotNull("Turnout Manager after configureManagers", scm.getTurnoutManager());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new TmccSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();

    }

}
