package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AudioTableFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AudioTableAction a = new AudioTableAction();
        AudioTablePanel p = (AudioTablePanel) a.getPanel();
        AudioTableFrame t = new AudioTableFrame(p,"Audio Table Frame Test");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitAppender.suppressWarnMessage("Initialised Null audio system - no sounds will be available.");
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AudioTableFrameTest.class);

}
