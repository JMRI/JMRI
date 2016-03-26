package jmri.configurexml;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Functional checks of loading basic configuration files. When done across
 * various versions of schema, this checks ability to read older files in newer
 * versions; completeness of reading code; etc.
 * <p>
 * More specific checks can be done in separate test files for specific types.
 *
 * @author Bob Jacobsen Copyright 2009
 * @since 3.9.2 (from earlier form)
 */
public class LoadAndCheckTest extends LoadAndStoreTestBase {

    /**
     * Test a file with current schema.
     *
     * @throws Exception
     */
    public void testLoadFileTest() throws Exception {
        // load file
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/configurexml/load/LoadFileTest.xml"));

        // check existance of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    public void testLoadFileTest277() throws Exception {
        // load file
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/configurexml/load/LoadFileTest277.xml"));

        // check existance of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    public void testLoadMultipleSystems() throws Exception {
        // load file
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/configurexml/load/LoadMultipleSystems.xml"));

        // check existance of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    public void testLoad295() throws Exception {
        // load file
        InstanceManager.configureManagerInstance()
                .load(new java.io.File("java/test/jmri/configurexml/load/LoadFileTest295.xml"));

        // check existance of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    // from here down is testing infrastructure
    public LoadAndCheckTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LoadAndCheckTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoadAndCheckTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }

    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
