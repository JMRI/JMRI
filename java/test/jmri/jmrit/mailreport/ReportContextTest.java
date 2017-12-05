package jmri.jmrit.mailreport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ReportContextTest {

    @Test
    public void testCTor() {
        ReportContext t = new ReportContext();
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testThatItRuns() {
        ReportContext t = new ReportContext();
        Assert.assertNotNull("exists",t);
        
        String output = t.getReport(false);
    }

    @Test
    public void testCheckForNodeID() {
        ReportContext t = new ReportContext();
        Assert.assertNotNull("exists",t);
        
        String output = t.getReport(false);
        Assert.assertTrue(output.contains("JMRI Node ID:"));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ReportContextTest.class);

}
