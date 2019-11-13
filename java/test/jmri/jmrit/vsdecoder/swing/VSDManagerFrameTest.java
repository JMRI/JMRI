package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDManagerFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new VSDManagerFrame();
	}
    }

    @After
    @Override
    public void tearDown() {

        // this created an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressWarnMessage("Initialised Null audio system - no sounds will be available.");
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDManagerFrameTest.class);
}
