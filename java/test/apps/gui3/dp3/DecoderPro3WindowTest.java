package apps.gui3.dp3;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoderPro3WindowTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        DecoderPro3Window t = new DecoderPro3Window();
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.initDebugProgrammerManager();
        jmri.InstanceManager.setDefault(jmri.jmrit.symbolicprog.ProgrammerConfigManager.class,new jmri.jmrit.symbolicprog.ProgrammerConfigManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderPro3WindowTest.class);
}
