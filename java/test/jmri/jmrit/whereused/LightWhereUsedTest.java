package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.NamedBean;
import jmri.NamedBeanUsageReport;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the LightWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class LightWhereUsedTest {

    // No Ctor test, class supplies static method.

    private NamedBean t;

    @Test
    public void testLightWhereUsedList() {

        List<NamedBeanUsageReport> list = assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testLightWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            LightWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new jmri.implementation.AbstractLight("IL5794") {};
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
