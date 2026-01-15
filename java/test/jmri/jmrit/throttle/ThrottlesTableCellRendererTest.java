package jmri.jmrit.throttle;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottlesTableCellRenderer
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesTableCellRendererTest {

    @Test
    public void testCtor() {
        ThrottlesTableCellRenderer panel = new ThrottlesTableCellRenderer();
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
