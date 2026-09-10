package jmri.jmrit.throttle.buttons;

import jmri.PowerManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Steve Young Copyright (C)2026
 */
@DisabledIfHeadless
public class LargePowerManagerButtonTest {

    @Test
    void testCtors() {

        JUnitUtil.initDebugPowerManager(); // creates an internal memo + PowerManagerScaffold

        LargePowerManagerButton t = new LargePowerManagerButton();
        assertNotNull(t);

        t = new LargePowerManagerButton(false);
        assertNotNull(t);

        t = new LargePowerManagerButton(false, null);
        assertNotNull(t);

        t = new LargePowerManagerButton(false, jmri.InstanceManager.getDefault(PowerManager.class));
        assertNotNull(t);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
