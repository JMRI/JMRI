package jmri.jmrix.can.cbus.swing.modeswitcher;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the jmri.jmrix.can.cbus.swing.ModeSwitcherAction class.
 *
 * @author Andrew Crosland (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class SprogCbusModeSwitcherActionTest {

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tc = null;
    
    @Test
    public void testAction() {

        SprogCbusModeSwitcherAction action = new SprogCbusModeSwitcherAction("ModeSwitcherAction test", memo);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        tc = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;

        jmri.util.JUnitUtil.tearDown();
    }
}
