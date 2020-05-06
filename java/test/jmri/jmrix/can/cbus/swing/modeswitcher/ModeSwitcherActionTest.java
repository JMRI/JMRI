package jmri.jmrix.can.cbus.swing.modeswitcher;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.can.cbus.swing.ModeSwitcherAction class.
 *
 * @author Andrew Crosland (C) 2020
 */
public class ModeSwitcherActionTest {

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ModeSwitcherAction action = new ModeSwitcherAction("ModeSwitcherAction test");
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
}
