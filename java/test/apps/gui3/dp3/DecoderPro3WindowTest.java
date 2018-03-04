package apps.gui3.dp3;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoderPro3WindowTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DecoderPro3Window t = new DecoderPro3Window();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConnectionConfigManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        jmri.InstanceManager.setDefault(jmri.jmrit.symbolicprog.ProgrammerConfigManager.class,new jmri.jmrit.symbolicprog.ProgrammerConfigManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderPro3WindowTest.class);
}
