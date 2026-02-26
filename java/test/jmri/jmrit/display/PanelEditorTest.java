package jmri.jmrit.display;

import java.io.File;

import jmri.*;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen
 */
@DisabledIfHeadless
public class PanelEditorTest {

    @Test
    public void testShow() throws JmriException {
        // load and display
        File f = new File("java/test/jmri/jmrit/display/valid/PanelEditorTest1.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
    }

    @Test
    public void testShow2() throws JmriException {
        // load and display
        File f = new File("java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    @Test
    public void testShow3() throws JmriException {
        // load and display
        File f = new File("java/test/jmri/jmrit/display/configurexml/load/OneOfEach.3.3.3.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
