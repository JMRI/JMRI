package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.NamedBeanUsageReport;
import jmri.implementation.AbstractSignalMast;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SignalMastWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalMastWhereUsedTest {

    // No Ctor test, class supplies static method.

    private AbstractSignalMast t;

    @Test
    public void testSignalMastWhereUsedList() {

        List<NamedBeanUsageReport> list =
        assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testSignalMastWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            SignalMastWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new AbstractSignalMast("1234"){};
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
