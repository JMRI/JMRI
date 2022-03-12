package jmri.jmrit.vsdecoder;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DieselSoundTest {

    @Test
    public void testCTor() {
        DieselSound t = new DieselSound("test");
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
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DieselSoundTest.class);

}
