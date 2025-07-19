package jmri.jmrix.rps;

import java.io.File;

import jmri.jmrit.display.EditorManager;
import jmri.InstanceManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.Editor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the rps.Reading class.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class RpsPositionIconTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtorAndID() throws Exception {
        // init test system
        new Engine() {
            void reset() {
                _instance = null;
            }
        }.reset();

        new ConfigXmlManager().load(new File("java/test/jmri/jmrix/rps/LocationTestPanel.xml"));

        // and push a good measurement
        Reading loco = new Reading("27", null);
        Measurement m = new Measurement(loco, 0.0, 0.0, 0.0, 0.133, 5, "source");
        Distributor.instance().submitMeasurement(m);

        Editor e = InstanceManager.getDefault(EditorManager.class).get("RPS Location Test Editor");
        Assertions.assertNotNull(e);
        Assertions.assertNotNull( e.getTargetFrame(), "has target frame");
        Assertions.assertEquals("RPS Location Test", e.getTargetFrame().getTitle());
        e.dispose();
        JUnitUtil.disposeFrame("RPS Location Test", false, true);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
