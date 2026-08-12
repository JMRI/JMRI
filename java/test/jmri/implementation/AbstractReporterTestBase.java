package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.Reporter;

import org.junit.jupiter.api.*;

/**
 * Tests for the Reporter class
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2018
 */
abstract public class AbstractReporterTestBase {
        
    // concrete classes should create r in setUp and remove in tearDown
    protected Reporter r = null; 

    // concrete classes should generate an appropriate report.
    abstract protected Object generateObjectToReport();

    @Test
    public void testCtor() {
        // Check that it is not a null object
        assertNotNull(r, "Created Reporter not null");
        // Check that CurrentReport and LastReport return a null object
        assertNull(r.getCurrentReport(), "CurrentReport at initialisation is 'null'");
        assertNull(r.getLastReport(), "LastReport at initialisation is 'null'");
    }

    @Test
    public void testReport() {
        // Report a String
        r.setReport(generateObjectToReport());
        // Check that both CurrentReport and LastReport are not null
        assertNotNull(r.getCurrentReport(), "CurrentReport Object exists");
        assertNotNull(r.getLastReport(), "LastReport Object exists");
        // Check the value of both CurrentReport and LastReport
        assertEquals(r.getLastReport(), r.getCurrentReport(), "CurrentReport equals LastReport");

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the reported String
        assertNull(r.getCurrentReport(), "After null report, CurrentReport is null");
        assertNotNull(r.getLastReport(), "After null report, LastReport String is not null");
    }

    @Test
    public void testGetBeanType(){
         assertEquals(Bundle.getMessage("BeanNameReporter"), r.getBeanType(), "bean type");
    }

    @Test
    public void testPropertyChange() {
        currentReportSeen = false;
        lastReportSeen = false;
        r.addPropertyChangeListener(new TestReporterListener());
        // Report a String
        r.setReport(generateObjectToReport());
        // Check that both CurrentReport and LastReport were seen
        assertTrue(currentReportSeen, "CurrentReport seen");
        assertTrue(lastReportSeen, "LastReport seen");

        // Nothing to report now
        currentReportSeen = false;
        lastReportSeen = false;
        r.setReport(null);
        // Check that CurrentReport was seen
        assertTrue(currentReportSeen, "CurrentReport seen after null");
        // Check that LastReport was not seen (no change on null)
        assertFalse(lastReportSeen, "LastReport seen after null");
    }

    @Test
    public void testAddRemoveListener() {
        int initialListeners = r.getNumPropertyChangeListeners();
        r.addPropertyChangeListener(new TestReporterListener());
        assertEquals(initialListeners+1, r.getNumPropertyChangeListeners(), "controller listener added");
        r.dispose();
        assertEquals(0, r.getNumPropertyChangeListeners(), "0 listeners after dispose");
    }

    protected boolean currentReportSeen = false;
    protected boolean lastReportSeen = false;

    public class TestReporterListener implements java.beans.PropertyChangeListener {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e){
            if (e.getPropertyName().equals("currentReport")) {
                currentReportSeen = true;
            } else if (e.getPropertyName().equals("lastReport")) {
                lastReportSeen = true;
            }
        }
    }

    abstract public void setUp();

    abstract public void tearDown();

}


