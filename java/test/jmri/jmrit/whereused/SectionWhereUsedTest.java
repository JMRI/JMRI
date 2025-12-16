package jmri.jmrit.whereused;

import java.util.*;

import javax.swing.JTextArea;

import jmri.*;
import jmri.implementation.DefaultSection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the SectionWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SectionWhereUsedTest {

    // No Ctor test, class supplies static method.

    private NamedBean t;

    @Test
    public void testSectionWhereUsedList() {

        List<NamedBeanUsageReport> list =
        assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testSectionWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            SectionWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new DefaultSection("892");
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
