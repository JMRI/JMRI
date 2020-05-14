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
public class VSDecoderTest {

    @Test
    public void testCTor() throws java.util.zip.ZipException, java.io.IOException {
        VSDecoder t = new VSDecoder("Test", "steam1min8","java/test/jmri/jmrit/vsdecoder/steam1min8.zip");
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");

        // this created an an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderTest.class);

}
