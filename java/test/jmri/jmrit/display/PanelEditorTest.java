package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * PanelEditorTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen
 */
public class PanelEditorTest {

    @Test
    public void testShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/display/verify/PanelEditorTest1.xml");
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
