package jmri.jmrit.vsdecoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the vsdecoder.VSDNavigation class.
 *
 * Based on VSDecoderTest by Paul Bender
 * @author Klaus Killinger Copyright (C) 2022
 */
public class VSDNavigationTest {

    @Test
    public void testCTor() throws java.util.zip.ZipException, java.io.IOException {
        VSDecoder t = new VSDecoder("Test", "steam1min8","java/test/jmri/jmrit/vsdecoder/steam1min8.zip");
        assertNotNull( t, "exists");
        VSDNavigation n = new VSDNavigation(t);
        assertNotNull( n, "exists");
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

    // private final static Logger log = LoggerFactory.getLogger(VSDNavigationTest.class);

}
