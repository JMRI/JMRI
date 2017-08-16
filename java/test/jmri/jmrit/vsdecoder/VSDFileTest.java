package jmri.jmrit.vsdecoder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class VSDFileTest {

    @Test(expected=java.io.FileNotFoundException.class)
    public void testCTorFail() throws java.util.zip.ZipException,java.io.IOException {
        VSDFile t = new VSDFile("test");
        Assert.assertNotNull("exists",t);
    }

    String filename = "java/test/jmri/jmrit/vsdecoder/steam1min8.zip";

    @Test
    public void testCTor() throws java.util.zip.ZipException,java.io.IOException {
        VSDFile t = new VSDFile(filename);
        Assert.assertNotNull("exists",t);
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(VSDFileTest.class.getName());

}
