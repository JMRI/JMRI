package jmri.jmrit.vsdecoder;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Diesel3SoundTest {

    @Test
    public void testCTor() {
        Diesel3Sound t = new Diesel3Sound("test");
        Assert.assertNotNull("exists",t);

        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("VSDecoderManagerThread");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitAppender.suppressWarnMessage("Error loading OpenAL libraries: Could not initialize class jogamp.openal.ALImpl");
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Diesel3SoundTest.class);

}
