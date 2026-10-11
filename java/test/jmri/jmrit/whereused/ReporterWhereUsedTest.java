package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the ReporterWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class ReporterWhereUsedTest {

    // No Ctor test, class supplies static method.

    private NamedBean t;

    @Test
    public void testReporterWhereUsedList() {

        List<NamedBeanUsageReport> list = assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testReporterWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            ReporterWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new jmri.implementation.AbstractReporter("654321") {
            @Override
            public void setState(int s) throws JmriException {}

            @Override
            public int getState() {
                return NamedBean.UNKNOWN;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
