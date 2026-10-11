package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottleCreationAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottleCreationActionTest {

    @Test
    public void testCtor() {
        ThrottleCreationAction panel = new ThrottleCreationAction();
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
