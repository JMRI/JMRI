package jmri.jmrix.rps;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsReporterTest {

    @Test
    public void testCTor() {
        RpsReporter t = new RpsReporter("RR(0,0,0);(1,0,0);(1,1,0);(0,1,0)");

        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(RpsReporterTest.class);

}
