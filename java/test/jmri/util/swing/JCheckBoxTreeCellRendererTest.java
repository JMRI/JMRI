package jmri.util.swing;

import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 * Tests for JCheckBoxTreeCellRenderer.
 * @author Steve Young Copyright (C) 2025
 */
public class JCheckBoxTreeCellRendererTest {

    @Test
    public void testJCheckBoxTreeCellRendererCtor() {
        var t = new JCheckBoxTreeCellRenderer();
        Assertions.assertNotNull(t);
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
