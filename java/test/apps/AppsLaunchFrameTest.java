package apps;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AppsLaunchFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        AppsLaunchFrame t = new AppsLaunchFrame(new AppsLaunchPane() {
            @Override
            protected String windowHelpID() {
                return "foo";
            }
        }, "Test Launch Frame");
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConnectionConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AppsLaunchFrameTest.class);
}
