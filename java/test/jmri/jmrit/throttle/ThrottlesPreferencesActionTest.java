package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottlesPreferencesAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesPreferencesActionTest {

    @Test
    public void testCtor() {
        ThrottlesPreferencesAction panel = new ThrottlesPreferencesAction();
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
