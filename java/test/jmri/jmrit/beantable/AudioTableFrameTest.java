package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AudioTableFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        if(!GraphicsEnvironment.isHeadless()){
           AudioTableAction a = new AudioTableAction();
           AudioTablePanel p = (AudioTablePanel) a.getPanel();
           frame = new AudioTableFrame(p,"Audio Table Frame Test");
	}
    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitAppender.suppressWarnMessage("Initialised Null audio system - no sounds will be available.");
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AudioTableFrameTest.class);

}
