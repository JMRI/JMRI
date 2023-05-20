package jmri.jmrit.vsdecoder;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDecoderManagerTest {

    @Test
    public void testCTor() {
        VSDecoderManager t = new VSDecoderManager();
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

        // Potentially no Audio Device installed
        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("Error initialising JOAL");

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderManagerTest.class);

}
