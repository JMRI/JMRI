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
 * Tests for the BlockWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class BlockWhereUsedTest {

    // No Ctor test, class supplies static method.

    private NamedBean t;

    @Test
    public void testBlockWhereUsedList() {

        List<NamedBeanUsageReport> list = assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testBlockWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            BlockWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new jmri.Block("IB79346");
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
