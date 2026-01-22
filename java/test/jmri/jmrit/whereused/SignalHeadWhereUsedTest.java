package jmri.jmrit.whereused;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.swing.JTextArea;

import jmri.NamedBeanUsageReport;
import jmri.Turnout;
import jmri.implementation.AbstractSignalHead;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SignalHeadWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalHeadWhereUsedTest {

    // No Ctor test, class supplies static method.

    private AbstractSignalHead t;

    @Test
    public void testSignalHeadWhereUsedList() {

        List<NamedBeanUsageReport> list =
        assertDoesNotThrow( () ->
            t.getUsageReport(t) );
        assertEquals( 0, list.size());
    }

    @Test
    public void testSignalHeadWhereUsed() {

        JTextArea ta = assertDoesNotThrow( () ->
            SignalHeadWhereUsed.getWhereUsed(t) );
        assertTrue( ta.getText().contains(t.getDisplayName()));
        assertTrue( ta.getText().contains("Listener count: 0"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new AbstractSignalHead("4321"){
            @Override
            public boolean isTurnoutUsed(Turnout t) {
                return false;
            }

            @Override
            public void setAppearance(int newAppearance) {
            }

            @Override
            public void setLit(boolean newLit) {}

            @Override
            public void setHeld(boolean newHeld) {}
        };
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
