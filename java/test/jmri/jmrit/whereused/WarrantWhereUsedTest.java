package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.NamedBeanUsageReport;
import jmri.jmrit.logix.Warrant;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the WarrantWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class WarrantWhereUsedTest {

    // No Ctor test, class supplies static method.

    @Test
    public void testWarrantWhereUsedList() {

        Warrant t = new Warrant("IW1", "Warrant User Name");
        List<NamedBeanUsageReport> list =
        assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testWarrantWhereUsed() {
        Warrant t = new Warrant("IW2", "Warrant2 User Name");
        JTextArea ta = assertDoesNotThrow( () ->
            WarrantWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
