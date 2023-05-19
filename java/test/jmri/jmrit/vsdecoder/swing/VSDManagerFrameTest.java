package jmri.jmrit.vsdecoder.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class VSDManagerFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        frame = new VSDManagerFrame();
    }

    @AfterEach
    @Override
    public void tearDown() {

        // Potentially no Audio Device installed
        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("Error initialising JOAL");

        // this created an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        JUnitUtil.removeMatchingThreads("VSDecoderManagerThread");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDManagerFrameTest.class);
}
