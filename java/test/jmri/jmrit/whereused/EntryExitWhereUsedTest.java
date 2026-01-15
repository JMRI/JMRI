package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.NamedBean;
import jmri.NamedBeanUsageReport;
import jmri.jmrit.entryexit.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the EntryExitWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class EntryExitWhereUsedTest {

    // No Ctor test, class supplies static method.

    private NamedBean t;

    @Test
    public void testEntryExitWhereUsedList() {

        List<NamedBeanUsageReport> list = assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
        JUnitAppender.assertErrorMessage("Signal not found at point");
        JUnitAppender.assertErrorMessage("Signal not found at point");
        JUnitAppender.assertErrorMessage("Signal not found at point");
        JUnitAppender.assertErrorMessage("Signal not found at point");
    }

    @Test
    public void testEntryExitWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            EntryExitWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
        JUnitAppender.assertErrorMessage("Signal not found at point");
        JUnitAppender.assertErrorMessage("Signal not found at point");
    }

    private static class NxWhereUsedDestPoints extends DestinationPoints {
        NxWhereUsedDestPoints(PointDetails point, String id, Source src) {
            super(point, id, src);
        }
    }

    private static class NxWhereUsedPointDetails extends PointDetails {
        NxWhereUsedPointDetails() {
            super(null, null);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        PointDetails point = new NxWhereUsedPointDetails();
        Source src = new Source(point);

        t = new NxWhereUsedDestPoints(new NxWhereUsedPointDetails(), "DP1", src);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
