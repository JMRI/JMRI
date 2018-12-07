package jmri.jmrit.logix.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class WarrantManagerXmlTest {

    @Test
    @Ignore("causes missing data for other tests?")
    public void testCTor() {
        WarrantManagerXml t = new WarrantManagerXml();
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

    // private final static Logger log = LoggerFactory.getLogger(WarrantManagerXmlTest.class);

}
