package jmri.configurexml;

import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
public class LoadAndCheckTest {

    /**
     * Test a file with current schema.
     *
     * @throws Exception rethrows any exception
     */
    @Test
    public void testLoadFileTest() throws Exception {
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new File("java/test/jmri/configurexml/load/LoadFileTest.xml"));

        // check existence of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    @Test
    public void testLoadMultipleSystems() throws Exception {
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new File("java/test/jmri/configurexml/load/LoadMultipleSystems.xml"));

        // check existence of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    @Test
    public void testLoad295() throws Exception {
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/configurexml/load/LoadFileTest295.xml"));

        // check existence of a few objects
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
