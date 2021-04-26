package jmri.jmrix.rps;

import java.awt.GraphicsEnvironment;
import java.io.File;

import jmri.jmrit.display.EditorManager;
import jmri.InstanceManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.Editor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * JUnit tests for the rps.Reading class.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class RpsPositionIconTest {

    @Test
    public void testCtorAndID() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        Assert.assertNotNull("has target frame", e.getTargetFrame());
        Assert.assertEquals("RPS Location Test", e.getTargetFrame().getTitle());
        e.dispose();
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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
