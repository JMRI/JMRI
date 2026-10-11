package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.NamedBean;
import jmri.NamedBeanUsageReport;
import jmri.implementation.DefaultRoute;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the RouteWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class RouteWhereUsedTest {

    // No Ctor test, class supplies static method.

    private NamedBean t;

    @Test
    public void testRouteWhereUsedList() {

        List<NamedBeanUsageReport> list =
        assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testRouteWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            RouteWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new DefaultRoute("6185");
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
