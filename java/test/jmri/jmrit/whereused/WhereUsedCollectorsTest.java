package jmri.jmrit.whereused;

import java.nio.file.Path;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the SensorWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class WhereUsedCollectorsTest {

    @Test
    public void testCollectorMethods() {
        Sensor sensor = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IT101");
        assertThat(WhereUsedCollectors.checkTurnouts(sensor)).isEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Feedback-1");
        assertThat(WhereUsedCollectors.checkTurnouts(sensor)).isNotEmpty();
        assertThat(WhereUsedCollectors.checkPanels(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Light-Control");
        assertThat(WhereUsedCollectors.checkLights(sensor)).isNotEmpty();
        assertThat(WhereUsedCollectors.checkPanels(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Occupancy");
        assertThat(WhereUsedCollectors.checkBlocks(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Main");
        assertThat(WhereUsedCollectors.checkRoutes(sensor)).isNotEmpty();
        assertThat(WhereUsedCollectors.checkLayoutBlocks(sensor)).isNotEmpty();
//        assertThat(WhereUsedCollectors.checkSignalHeadLogic(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-SML-Sensor");
        assertThat(WhereUsedCollectors.checkSignalMastLogic(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Group-Control-1");
        assertThat(WhereUsedCollectors.checkSignalGroups(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-OBlock-Error");
        assertThat(WhereUsedCollectors.checkOBlocks(sensor)).isNotEmpty();

        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB::Left-TO");
        assertThat(WhereUsedCollectors.checkWarrants(oblock)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("NX-LeftTO-A");
        assertThat(WhereUsedCollectors.checkEntryExit(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Group-Center");
        assertThat(WhereUsedCollectors.checkLogixConditionals(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Fwd");
        assertThat(WhereUsedCollectors.checkSections(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Stop-Allocation");
        assertThat(WhereUsedCollectors.checkTransits(sensor)).isNotEmpty();

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Transit-When-Action");
        assertThat(WhereUsedCollectors.checkTransits(sensor)).isNotEmpty();
    }

    @TempDir
    protected Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        JUnitUtil.initConfigureManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initReporterManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initSectionManager();
        JUnitUtil.clearBlockBossLogic();

        java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");  // NOI18N
        InstanceManager.getDefault(ConfigureManager.class).load(f);
    }

    @AfterEach
    public void tearDown() {
        new EditorFrameOperator("LE Panel").closeFrameWithConfirmations();
        new EditorFrameOperator("CPE Panel").closeFrameWithConfirmations();
        new EditorFrameOperator("Sensor SB").closeFrameWithConfirmations();

        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.clearBlockBossLogic();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedCollectorsTest.class);
}
