package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottlesListPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesListPanelTest {

    @Test
    public void testCtor() {
        ThrottlesListPanel panel = new ThrottlesListPanel();
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
