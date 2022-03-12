package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.cbus.swing.ModeSwitcherAction class.
 *
 * @author Andrew Crosland (C) 2020
 */
public class SprogCbusModeSwitcherActionTest {

    CanSystemConnectionMemo memo;
    TrafficControllerScaffold tc;
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
        tc.terminateThreads();
        tc = null;
        memo.dispose();
        memo = null;

        jmri.util.JUnitUtil.tearDown();
    }
}
