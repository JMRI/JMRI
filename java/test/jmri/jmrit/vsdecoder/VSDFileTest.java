package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class VSDFileTest {

    @Test(expected=java.io.FileNotFoundException.class)
    public void testCTorFail() throws java.util.zip.ZipException, java.io.IOException {
        try {
            VSDFile t = new VSDFile("test");
            Assert.assertNotNull("exists",t);
        } catch (java.nio.file.NoSuchFileException ex) {
            throw new java.io.FileNotFoundException(ex.toString()); // this is the Java 9 sequence
        }
    }

    String filename = "java/test/jmri/jmrit/vsdecoder/steam1min8.zip";

    @Test
    public void testCTor() throws java.util.zip.ZipException, java.io.IOException {
        VSDFile t = new VSDFile(filename);
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

    // private final static Logger log = LoggerFactory.getLogger(VSDFileTest.class);

}
