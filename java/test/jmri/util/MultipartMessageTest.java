package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MultipartMessageTest {

    @Test
    @Ignore("This test needs more setup; requires a webserver to connect to")
    public void testCTor() throws java.io.IOException {
        MultipartMessage t = new MultipartMessage("http://localhost:80","");
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MultipartMessageTest.class.getName());

}
