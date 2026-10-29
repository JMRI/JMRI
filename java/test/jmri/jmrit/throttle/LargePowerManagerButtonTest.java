package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

import jmri.jmrit.throttle.buttons.LargePowerManagerButton;

/**
 * Test simple functioning of LargePowerManagerButton
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LargePowerManagerButtonTest {

    @Test
    public void testCtor() {
        LargePowerManagerButton panel = new LargePowerManagerButton();
        Assertions.assertNotNull( panel, "exists");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
