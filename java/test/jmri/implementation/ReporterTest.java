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
 * <p>
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class ReporterTest {
        
    private Reporter r = null; 

    @Test
    public void testReporterCreation() {
        // Check that it is not a null object
        Assert.assertNotNull("Created Reporter not null", r);
        // Check that the SystemName and UserName is as specified
        Assert.assertEquals("Reporter SystemName is 'SYS'", "SYS", r.getSystemName());
        Assert.assertEquals("Reporter UserName is 'usr'", "usr", r.getUserName());
        // Check that CurrentReport and LastReport return a null object
        Assert.assertNull("CurrentReport at initialisation is 'null'", r.getCurrentReport());
        Assert.assertNull("LastReport at initialisation is 'null'", r.getLastReport());
    }

    @Test
    public void testReportStringObject() {
        // Report a String
        r.setReport(new StringReport("Something To Report"));
        // Check that both CurrentReport and LastReport are String objects
        Assert.assertTrue("CurrentReport Object is 'StringReport'", r.getCurrentReport() instanceof StringReport);
        Assert.assertTrue("LastReport Object is 'StringReport'", r.getLastReport() instanceof StringReport);
        // Check the value of both CurrentReport and LastReport
        Assert.assertEquals("CurrentReport String is 'Something To Report'", "Something To Report", r.getCurrentReport().getString());
        Assert.assertEquals("LastReport String is 'Something To Report'", "Something To Report", r.getLastReport().getString());

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the reported String
        Assert.assertEquals("After null report, CurrentReport String is null", null, r.getCurrentReport());
        Assert.assertEquals("After null report, LastReport String is 'Something To Report'", "Something To Report", r.getLastReport());
    }

    @Before
    public void setUp(){
       jmri.util.JUnitUtil.setUp();
       r = new AbstractReporter("SYS", "usr") {
            @Override
            public int getState() {
                return state;
            }

            @Override
            public void setState(int s) {
                state = s;
            }
            int state = 0;
        };
    }

    @After
    public void tearDown(){
       r = null;
       jmri.util.JUnitUtil.tearDown();
    }

    // Utility class for testing reporter
    public static class ObjectToReport {

        private int value;

        public ObjectToReport(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}


