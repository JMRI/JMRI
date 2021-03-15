package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.io.File;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen
 */
public class PanelEditorTest {

    @Test
    public void testShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/display/valid/PanelEditorTest1.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
    }

    @Test
    public void testShow2() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    @Test
    public void testShow3() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/display/configurexml/load/OneOfEach.3.3.3.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
