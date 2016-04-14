package jmri.jmrit.display;

import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * PanelEditorTest.java
 *
 * Description:
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class PanelEditorTest extends TestCase {

    TurnoutIcon to = null;

    public void testShow() throws Exception {
        jmri.util.JUnitUtil.initConfigureManager();

        // load and display
        File f = new File("java/test/jmri/jmrit/display/verify/PanelEditorTest1.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    public void testShow2() throws Exception {
        jmri.util.JUnitUtil.initConfigureManager();

        // load and display
        File f = new File("java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    public void testShow3() throws Exception {
        jmri.util.JUnitUtil.initConfigureManager();

        // load and display
        File f = new File("java/test/jmri/jmrit/display/configurexml/load/OneOfEach.3.3.3.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);

    }

    // from here down is testing infrastructure
    public PanelEditorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PanelEditorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PanelEditorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

	// static private Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
