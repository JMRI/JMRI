package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of StopAllButton
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class StopAllButtonTest {

    @Test
    public void testCtor() {
        StopAllButton panel = new StopAllButton();
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
