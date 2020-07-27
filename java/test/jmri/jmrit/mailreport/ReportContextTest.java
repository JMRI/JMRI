package jmri.jmrit.mailreport;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
        
        t.getReport(false);  // check it runs OK
    }

    @Test
    public void testCheckForNodeID() {
        ReportContext t = new ReportContext();
        Assert.assertNotNull("exists",t);
        
        String output = t.getReport(false);
        Assert.assertTrue(output.contains("JMRI Network ID:"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ReportContextTest.class);

}
