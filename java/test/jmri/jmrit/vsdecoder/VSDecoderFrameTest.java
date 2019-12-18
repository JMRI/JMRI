package jmri.jmrit.vsdecoder;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDecoderFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new VSDecoderFrame();
        }
    }

    @After
    @Override
    public void tearDown() {

        // this created an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderFrameTest.class);
}
