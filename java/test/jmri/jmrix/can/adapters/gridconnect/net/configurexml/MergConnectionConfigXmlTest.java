package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MergConnectionConfigXmlTest {

    @Test
    public void testCTor() {
        MergConnectionConfigXml t = new MergConnectionConfigXml();
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

    // private final static Logger log = LoggerFactory.getLogger(MergConnectionConfigXmlTest.class);

}
