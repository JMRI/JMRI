package jmri.implementation;

import jmri.Report;
import org.junit.*;

/**
 * Tests for StringReport
 * 
 * @author Daniel Bergqvist Copyright (c) 2019
 */
public class StringReportTest {
    
    @Test
    public void testCtor() {
        Report report = new StringReport("A report");
        Assert.assertNotNull("AbstractStringIO constructor return", report);
    }
    
    @Test
    public void testStringReport() {
        Report report = new StringReport("A report");
        Assert.assertTrue("string is correct",
                "A report".equals(report.getString()));
        
        report = new StringReport("Another report");
        Assert.assertTrue("string is correct",
                "Another report".equals(report.getString()));
    }
    
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
