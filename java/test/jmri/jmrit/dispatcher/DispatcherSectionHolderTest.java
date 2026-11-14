package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Dispatcher records the train holding each allocated Section on the Section,
 * and the holder goes when the Section is released.
 */
@DisabledIfHeadless
public class DispatcherSectionHolderTest {

    private Path outPathTrainInfo = null;

    private static Section section(String name) {
        return InstanceManager.getDefault(SectionManager.class).getSection(name);
    }

    @Test
    public void testHolderSetOnAllocationAndClearedOnRelease() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        Assertions.assertTrue(cm.load(new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml")));
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        JUnitUtil.setBeanStateAndWait(InstanceManager.getDefault(SensorManager.class).getSensor("Block 1"), Sensor.ACTIVE);

        TrainInfo info = new TrainInfoFile().readTrainInfo("FWDREV40.xml");
        info.setAutoRun(false);
        assertEquals(0, d.loadTrainFromTrainInfo(info));
        ActiveTrain at = d.getActiveTrainsList().get(0);
        JUnitUtil.waitFor(() -> d.getAllocatedSectionsList().size() == 3, "three Sections allocated");

        Section first = section("Section B1-3");
        Section middle = section("Section B4");
        Section last = section("Section B5-7");
        assertSame(at, first.getAllocatedTo(), "first Section held by the train");
        assertSame(at, middle.getAllocatedTo(), "middle Section held by the train");
        assertSame(at, last.getAllocatedTo(), "last Section held by the train");

        // the train moves on into Section B4 and Section B1-3 is released automatically
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 4"), Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(sm.getSensor("Block 1"), Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> first.getAllocatedTo() == null, "released Section has no holder");
        assertSame(at, middle.getAllocatedTo(), "Section the train is in still held");
        assertSame(at, last.getAllocatedTo(), "Section ahead still held");

        d.terminateActiveTrain(at, true, false);
        JUnitUtil.waitFor(() -> middle.getAllocatedTo() == null && last.getAllocatedTo() == null,
                "no holder left after the train is terminated");
        assertNull(first.getAllocatedTo());

        dw.requestClose();
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetFileUtilSupport();
        File outBaseTrainInfo = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        FileUtil.createDirectory(outBaseTrainInfo);
        Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/jmrit/dispatcher/traininfo"),
                "FWDREV40.xml").toPath();
        outPathTrainInfo = new File(outBaseTrainInfo, "FWDREV40.xml").toPath();
        Files.copy(inPath, outPathTrainInfo, StandardCopyOption.REPLACE_EXISTING);
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        // do not leave auto allocate on for tests that run after this one
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false, false);
        try {
            Files.delete(outPathTrainInfo);
        } catch (IOException e) {
            // already gone
        }
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }

}
