package jmri.implementation;

import jmri.ExtendedReport;
import jmri.Reporter;

import org.junit.Assert;
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
    abstract protected ExtendedReport generateObjectToReport();

    @Test
    public void testCtor() {
        // Check that it is not a null object
        Assert.assertNotNull("Created Reporter not null", r);
        // Check that CurrentReport and LastReport return a null object
        Assert.assertNull("CurrentReport at initialisation is 'null'", r.getCurrentReport());
        Assert.assertNull("CurrentExtendedReport at initialisation is 'null'", r.getCurrentExtendedReport());
        Assert.assertNull("LastReport at initialisation is 'null'", r.getLastReport());
        Assert.assertNull("LastExtendedReport at initialisation is 'null'", r.getLastExtendedReport());
    }

    @Test
    public void testReport() {
        // Report a String
        r.setExtendedReport(generateObjectToReport());
        // Check that both CurrentReport and LastReport are not null
        Assert.assertNotNull("CurrentReport Object exists", r.getCurrentReport());
        Assert.assertNotNull("CurrentExtendedReport Object exists", r.getCurrentExtendedReport());
        Assert.assertNotNull("LastReport Object exists", r.getLastReport());
        Assert.assertNotNull("LastExtendedReport Object exists", r.getLastExtendedReport());
        // Check the value of both CurrentReport and LastReport
        Assert.assertEquals("CurrentReport equals LastReport",r.getLastReport(), r.getCurrentReport());
        Assert.assertEquals("CurrentExtendedReport equals LastExtendedReport",r.getLastExtendedReport(), r.getCurrentExtendedReport());

        // Nothing to report now
        r.setExtendedReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the reported String
        Assert.assertNull("After null report, CurrentReport is null", r.getCurrentReport());
        Assert.assertNull("After null report, CurrentExtendedReport is null", r.getCurrentExtendedReport());
        Assert.assertNotNull("After null report, LastReport String is not null",r.getLastReport());
        Assert.assertNotNull("After null report, LastExtendedReport String is not null",r.getLastExtendedReport());
    }

    @Test
    public void testGetBeanType(){
         Assert.assertEquals("bean type",r.getBeanType(),Bundle.getMessage("BeanNameReporter"));
    }

    @Test
    public void testPropertyChange() {
        currentReportSeen = false;
        lastReportSeen = false;
        r.addPropertyChangeListener(new TestReporterListener());
        // Report a String
        r.setExtendedReport(generateObjectToReport());
        // Check that both CurrentReport and LastReport were seen
        Assert.assertTrue("CurrentReport seen", currentReportSeen);
        Assert.assertTrue("LastReport seen", lastReportSeen);

        // Nothing to report now
        currentReportSeen = false;
        lastReportSeen = false;
        r.setExtendedReport(null);
        // Check that CurrentReport was seen
        Assert.assertTrue("CurrentReport seen after null", currentReportSeen);
        // Check that LastReport was not seen (no change on null)
        Assert.assertFalse("LastReport seen after null", lastReportSeen);
    }
    
    @Test
    public void testAddRemoveListener() {
        Assert.assertEquals("starts 0 listeners", 0, r.getNumPropertyChangeListeners());
        r.addPropertyChangeListener(new TestReporterListener());
        Assert.assertEquals("controller listener added", 1, r.getNumPropertyChangeListeners());
        r.dispose();
        Assert.assertTrue("controller listeners remaining < 1", r.getNumPropertyChangeListeners() < 1);
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

    @BeforeEach
    abstract public void setUp();

    @AfterEach
    abstract public void tearDown();

}


