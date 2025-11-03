package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
 * <p>
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class ReporterTest {
        
    private Reporter r = null; 

    @Test
    public void testReporterCreation() {
        // Check that it is not a null object
        assertNotNull( r, "Created Reporter not null");
        // Check that the SystemName and UserName is as specified
        assertEquals( "SYS", r.getSystemName(), "Reporter SystemName is 'SYS'");
        assertEquals( "usr", r.getUserName(), "Reporter UserName is 'usr'");
        // Check that CurrentReport and LastReport return a null object
        assertNull( r.getCurrentReport(), "CurrentReport at initialisation is 'null'");
        assertNull( r.getLastReport(), "LastReport at initialisation is 'null'");
    }

    @Test
    public void testReportStringObject() {
        // Report a String
        r.setReport("Something To Report");
        // Check that both CurrentReport and LastReport are String objects
        assertInstanceOf( String.class, r.getCurrentReport(), "CurrentReport Object is 'String'");
        assertInstanceOf( String.class, r.getLastReport(), "LastReport Object is 'String'");
        // Check the value of both CurrentReport and LastReport
        assertEquals( "Something To Report", r.getCurrentReport(), "CurrentReport String is 'Something To Report'");
        assertEquals( "Something To Report", r.getLastReport(), "LastReport String is 'Something To Report'");

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the reported String
        assertNull( r.getCurrentReport(), "After null report, CurrentReport String is null");
        assertEquals( "Something To Report", r.getLastReport(), "After null report, LastReport String is 'Something To Report'");
    }

    @Test
    public void testReportOtherObject() {
        // Create an ObjectToReport object to report
        ObjectToReport otr = new ObjectToReport(42);
        // and report it.
        r.setReport(otr);

        // Check that both CurrentReport and LastReport are ObjectToReport objects
        assertInstanceOf( ObjectToReport.class, r.getCurrentReport(),
            "CurrentReport Object is 'ObjectToReport'");
        assertInstanceOf( ObjectToReport.class, r.getLastReport(),
            "LastReport Object is 'ObjectToReport'");
        // Check the value of the ObjectToReport objects
        assertEquals( 42, ((ObjectToReport) r.getCurrentReport()).getValue(), "CurrentReport value is '42'");
        assertEquals( 42, ((ObjectToReport) r.getLastReport()).getValue(), "LastReport value is '42'");
        // Check that the returned object is the earlier created ObjectToReport
        assertSame( otr, r.getCurrentReport(), "CurrentReport Object is identical to otr");
        assertSame( otr, r.getCurrentReport(), "LastReport Object is identical to otr");

        // Create a new ObjectToReport with an identical value
        ObjectToReport otr2 = new ObjectToReport(42);
        // Check that the returned object is different to this new one
        assertNotSame( otr2, r.getCurrentReport(), "CurrentReport Object is different");
        assertNotSame( otr2, r.getCurrentReport(), "LastReport Object is different");

        // Nothing to report now
        r.setReport(null);
        // Check that CurrentReport returns a null value, but LastReport returns the first created ObjectToReport
        assertNull( r.getCurrentReport(), "After null report, CurrentReport Object is null");
        assertEquals( 42, ((ObjectToReport) r.getLastReport()).getValue(), "After null report, LastReport value is '42'");
        assertSame( otr, r.getLastReport(), "After null report, LastReport Object is identical to otr");
    }

    @BeforeEach
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
            private int state = 0;
        };
    }

    @AfterEach
    public void tearDown(){
       r = null;
       jmri.util.JUnitUtil.tearDown();
    }

    // Utility class for testing reporter
    private static class ObjectToReport {

        private final int value;

        ObjectToReport(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}


