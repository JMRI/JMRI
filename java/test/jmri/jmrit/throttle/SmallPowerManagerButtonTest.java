package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SmallPowerManagerButton
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SmallPowerManagerButtonTest {

    @Test
    public void testCtor() {
        SmallPowerManagerButton panel = new SmallPowerManagerButton();
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
