package jmri.jmrix.easydcc;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the EasyDccSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EasyDccSystemConnectionMemoTest extends SystemConnectionMemoTestBase<EasyDccSystemConnectionMemo> {

    @Test
    public void checkConfigureManagers() {
        scm.configureManagers();
        Assert.assertNotNull("Throttle Manager after configureManagers", scm.getThrottleManager());
        Assert.assertNotNull("Turnout Manager after configureManagers", scm.getTurnoutManager());
        Assert.assertNotNull("Power Manager after configureManagers", scm.getPowerManager());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new EasyDccSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.clearShutDownManager(); // remove shutdown tasks left behind.
        JUnitUtil.tearDown();
    }

}
