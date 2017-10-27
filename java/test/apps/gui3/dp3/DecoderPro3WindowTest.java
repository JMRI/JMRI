package apps.gui3.dp3;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoderPro3WindowTest {

    @Test
    @Ignore("needs more setup")
    public void testCTor() {
        DecoderPro3Window t = new DecoderPro3Window();
        Assert.assertNotNull("exists", t);
        JUnitUtil.disposeFrame("Decoder Pro Wizard", true, true);
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderPro3WindowTest.class);
}
