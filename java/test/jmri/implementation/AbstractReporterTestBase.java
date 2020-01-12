package jmri.implementation;

import jmri.Reporter;
import org.junit.*;

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
        Assert.assertNotNull("Created Reporter not null", r);
        // Check that CurrentReport and LastReport return a null object
        Assert.assertNull("CurrentReport at initialisation is 'null'", r.getCurrentReport());
        Assert.assertNull("LastReport at initialisation is 'null'", r.getLastReport());
    }

    @Test
    public void testReport() {
        // Report a String
        r.setReport(generateObjectToReport());
        // Check that both CurrentReport and LastReport are not null
        Assert.assertNotNull("CurrentReport Object exists", r.getCurrentReport());
        Assert.assertNotNull("LastReport Object exists", r.getLastReport());
        // Check the value of both CurrentReport and LastReport
        Assert.assertEquals("CurrentReport equals LastReport",r.getLastReport(), r.getCurrentReport());

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the reported String
        Assert.assertNull("After null report, CurrentReport is null", r.getCurrentReport());
        Assert.assertNotNull("After null report, LastReport String is not null",r.getLastReport());
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
        r.setReport(generateObjectToReport());
        // Check that both CurrentReport and LastReport were seen
        Assert.assertTrue("CurrentReport seen", currentReportSeen);
        Assert.assertTrue("LastReport seen", lastReportSeen);

        // Nothing to report now
        currentReportSeen = false;
        lastReportSeen = false;
        r.setReport(null);
        // Check that CurrentReport was seen
        Assert.assertTrue("CurrentReport seen after null", currentReportSeen);
        // Check that LastReport was not seen (no change on null)
        Assert.assertFalse("LastReport seen after null", lastReportSeen);
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

    @Before
    abstract public void setUp();

    @After
    abstract public void tearDown();

}


