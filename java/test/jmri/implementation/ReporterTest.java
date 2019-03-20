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
        r.setReport("Something To Report");
        // Check that both CurrentReport and LastReport are String objects
        Assert.assertTrue("CurrentReport Object is 'String'", r.getCurrentReport() instanceof String);
        Assert.assertTrue("LastReport Object is 'String'", r.getLastReport() instanceof String);
        // Check the value of both CurrentReport and LastReport
        Assert.assertEquals("CurrentReport String is 'Something To Report'", "Something To Report", r.getCurrentReport());
        Assert.assertEquals("LastReport String is 'Something To Report'", "Something To Report", r.getLastReport());

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the reported String
        Assert.assertEquals("After null report, CurrentReport String is null", null, r.getCurrentReport());
        Assert.assertEquals("After null report, LastReport String is 'Something To Report'", "Something To Report", r.getLastReport());
    }

    @Test
    public void testReportOtherObject() {
        // Create an ObjectToReport object to report
        ObjectToReport otr = new ObjectToReport(42);
        // and report it.
        r.setReport(otr);

        // Check that both CurrentReport and LastReport are ObjectToReport objects
        Assert.assertTrue("CurrentReport Object is 'ObjectToReport'", r.getCurrentReport() instanceof ObjectToReport);
        Assert.assertTrue("LastReport Object is 'ObjectToReport'", r.getLastReport() instanceof ObjectToReport);
        // Check the value of the ObjectToReport objects
        Assert.assertEquals("CurrentReport value is '42'", 42, ((ObjectToReport) r.getCurrentReport()).getValue());
        Assert.assertEquals("LastReport value is '42'", 42, ((ObjectToReport) r.getLastReport()).getValue());
        // Check that the returned object is the earlier created ObjectToReport
        Assert.assertSame("CurrentReport Object is identical to otr", otr, r.getCurrentReport());
        Assert.assertSame("LastReport Object is identical to otr", otr, r.getCurrentReport());

        // Create a new ObjectToReport with an identical value
        ObjectToReport otr2 = new ObjectToReport(42);
        // Check that the returned object is different to this new one
        Assert.assertNotSame("CurrentReport Object is different", otr2, r.getCurrentReport());
        Assert.assertNotSame("LastReport Object is different", otr2, r.getCurrentReport());

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the first created ObjectToReport
        Assert.assertEquals("After null report, CurrentReport Object is null", null, r.getCurrentReport());
        Assert.assertEquals("After null report, LastReport value is '42'", 42, ((ObjectToReport) r.getLastReport()).getValue());
        Assert.assertSame("After null report, LastReport Object is identical to otr", otr, r.getLastReport());
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


