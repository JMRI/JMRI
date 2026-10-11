package jmri.jmrit.whereused;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JTextArea;

import jmri.NamedBeanUsageReport;
import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the OBlockWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class OBlockWhereUsedTest {

    // No Ctor test, class supplies static method.

    @Test
    public void testOBlockWhereUsedList() {

        // Pay the ransom to free PR#8715 to be merged
        OBlock b = new OBlock("OB1");
        List<NamedBeanUsageReport> list =
        assertDoesNotThrow( () ->
            b.getUsageReport(b) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testOBlockWhereUsed() {
        OBlock t = new OBlock("OB2");
        JTextArea ta = assertDoesNotThrow( () ->
            OBlockWhereUsed.getWhereUsed(t) );
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
