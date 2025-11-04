package jmri.configurexml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
     * @throws jmri.JmriException
     */
    @Test
    public void testLoadFileTest() throws JmriException {
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new File("java/test/jmri/configurexml/load/LoadFileTest.xml"));

        // check existence of a few objects
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

        JUnitAppender.assertWarnMessage("Converting route system name IR1 to IO1");
        JUnitAppender.assertWarnMessage("System names for 1 Routes changed; this may have operational impacts.");

    }

    @Test
    public void testLoadMultipleSystems() throws JmriException {
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new File("java/test/jmri/configurexml/load/LoadMultipleSystems.xml"));

        // check existence of a few objects
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

    }

    @Test
    public void testLoad295() throws JmriException {
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/configurexml/load/LoadFileTest295.xml"));

        // check existence of a few objects
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("IS1"));
        assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));

        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no sensor"));

        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM1"));
        assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));

        JUnitAppender.assertWarnMessage("Converting route system name IR1 to IO1");
        JUnitAppender.assertWarnMessage("System names for 1 Routes changed; this may have operational impacts.");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        System.setProperty("jmri.test.no-dialogs", "true");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
        System.setProperty("jmri.test.no-dialogs", "false");
    }
}
