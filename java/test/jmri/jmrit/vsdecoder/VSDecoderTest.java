package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");

        // this created an an audio manager, clean that up
        JUnitUtil.removeMatchingThreads("VSDecoderManagerThread");
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        JUnitUtil.removeMatchingThreads("Steam1Sound.S1LoopThread");

        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderTest.class);

}
