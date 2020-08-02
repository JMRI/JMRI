package jmri.jmrit.vsdecoder;

import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDFileTest {

    @Test
    public void testCTorFail() {
        Assert.assertThrows(IOException.class, () -> new VSDFile("test"));
    }

    String filename = "java/test/jmri/jmrit/vsdecoder/steam1min8.zip";

    @Test
    public void testCTor() throws IOException {
        VSDFile t = new VSDFile(filename);
        Assert.assertNotNull("exists",t);
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDFileTest.class);

}
