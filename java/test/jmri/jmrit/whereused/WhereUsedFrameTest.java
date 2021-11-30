package jmri.jmrit.whereused;

import java.nio.file.Path;

import jmri.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.managers.*;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import jmri.util.swing.JemmyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the WhereUsedFrame Class
 * @author Dave Sand Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class WhereUsedFrameTest {

    private WhereUsedFrame frame;

    @Test
    public void testTypeSelection() {
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleWhereUsed"));  // NOI18N

        // For each item type, create the bean combo box and verify it.  Build the report using a direct call.
        String typeComboBoxLabel = Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelItemType"));
        JLabelOperator typeLabelOperator = new JLabelOperator(jfo, typeComboBoxLabel);
        JComboBoxOperator jcoType = new JComboBoxOperator((JComboBox) typeLabelOperator.getLabelFor());

        // Turnout
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.TURNOUT);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(ProxyTurnoutManager.class);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout("LE Left");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.TURNOUT, turnout);

        // Sensor
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.SENSOR);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(ProxySensorManager.class);
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor("S-Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SENSOR, sensor);

        // Light
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.LIGHT);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(ProxyLightManager.class);
        Light light = InstanceManager.getDefault(LightManager.class).getLight("L-Sensor Control");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.LIGHT, light);

        // Signal Head
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.SIGNALHEAD);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(AbstractSignalHeadManager.class);
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead("Left-AU");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SIGNALHEAD, signalHead);

        // Signal Mast
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.SIGNALMAST);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(DefaultSignalMastManager.class);
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("Left-B");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SIGNALMAST, signalMast);

        // Reporter
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.REPORTER);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(ProxyReporterManager.class);
        Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter("Test Reporter");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.REPORTER, reporter);

        // Memory
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.MEMORY);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(DefaultMemoryManager.class);
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory("BlockMemory");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.MEMORY, memory);

        // Route
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.ROUTE);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(DefaultRouteManager.class);
        Route route = InstanceManager.getDefault(RouteManager.class).getRoute("Sensors");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.ROUTE, route);

        // OBlock
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.OBLOCK);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(OBlockManager.class);
        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB::Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.OBLOCK, oblock);

        // Block
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.BLOCK);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(BlockManager.class);
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock("B-Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.BLOCK, block);

        // Section
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.SECTION);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(SectionManager.class);
        Section section = InstanceManager.getDefault(SectionManager.class).getSection("LeftTO to Main");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.SECTION, section);

        // Warrant
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.WARRANT);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(WarrantManager.class);
        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant("IW::TestWarrant");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.WARRANT, warrant);

        // EntryExit
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.ENTRYEXIT);
        assertThat(frame._itemNameBox.getManager()).isInstanceOf(EntryExitPairs.class);
        DestinationPoints dp = InstanceManager.getDefault(EntryExitPairs.class).getNamedBean("NX-LeftTO-A (Left-A) to NX-RIghtTO-B (Right-B)");
        frame.buildWhereUsedListing(WhereUsedFrame.ItemType.ENTRYEXIT, dp);
    }

    @Test
    @ToDo("Add appropriate assertions for thee result of creating the report.")
    public void testCreateReportForTurnoutObject() {
        frame.setVisible(true);
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout("LE Left");
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleWhereUsed"));  // NOI18N

        String typeComboBoxLabel = Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelItemType"));
        JLabelOperator typeLabelOperator = new JLabelOperator(jfo, typeComboBoxLabel);
        JComboBoxOperator jcoType = new JComboBoxOperator((JComboBox) typeLabelOperator.getLabelFor());
        jcoType.setSelectedItem(WhereUsedFrame.ItemType.TURNOUT);

        String nameComboBoxLabel = Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelItemName"));
        JLabelOperator nameLabelOperator = new JLabelOperator(jfo, nameComboBoxLabel);
        JComboBoxOperator jcoName = new JComboBoxOperator((JComboBox) nameLabelOperator.getLabelFor());
        jcoName.setSelectedItem(turnout);
        new JButtonOperator(jfo, Bundle.getMessage("ButtonCreate"));
    }

    @Test
    public void testSave() {
        frame.setVisible(true);

        // Cancel save request
        Thread cancelFile = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonCancel"));  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()-> !(cancelFile.isAlive()), "cancelFile finished");  // NOI18N

        // Complete save request
        Thread saveFile = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonSave"));  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(()-> !(saveFile.isAlive()), "saveFile finished");  // NOI18N

        // Replace duplicate file
        checkDuplicateFileOperation("SaveDuplicateReplace");

        // Append duplicate file
        checkDuplicateFileOperation("SaveDuplicateAppend");

        // Cancel duplicate file
        checkDuplicateFileOperation("ButtonCancel");
    }

    private void checkDuplicateFileOperation(String operation) {
        Thread saveFile4 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SaveDialogTitle"), Bundle.getMessage("ButtonSave"));  // NOI18N
        Thread cancelFile2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SaveDuplicateTitle"), Bundle.getMessage(operation));  // NOI18N
        frame.saveWhereUsedPressed();
        JUnitUtil.waitFor(() -> !(saveFile4.isAlive()), "save " + operation + " finished");  // NOI18N
        JUnitUtil.waitFor(() -> !(cancelFile2.isAlive()), "cancel " + operation + " finished");  // NOI18N
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
        frame = new WhereUsedFrame();
   }

    @AfterEach
    public  void tearDown() {
        JUnitUtil.dispose(frame);
        frame = null;
        new EditorFrameOperator("LE Panel").closeFrameWithConfirmations();
        new EditorFrameOperator("CPE Panel").closeFrameWithConfirmations();
        new EditorFrameOperator("Sensor SB").closeFrameWithConfirmations();

        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.clearBlockBossLogic();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedFrameTest.class);
}
