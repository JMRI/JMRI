package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class AudioTableFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();

        AudioTableAction a = new AudioTableAction();
        AudioTablePanel p = (AudioTablePanel) a.getPanel();
        frame = new AudioTableFrame(p, "Audio Table Frame Test");
    }

    @AfterEach
    @Override
    public void tearDown() {

        // Potentially no Audio Device installed
        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("Error initialising JOAL");
        // this created an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AudioTableFrameTest.class);

}
