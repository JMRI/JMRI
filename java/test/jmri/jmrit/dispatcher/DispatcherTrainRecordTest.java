package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * An Active Train loaded by Dispatcher refers to its train's record, and the
 * next Active Train for the same train refers to the same record. Uses the
 * MultiBlockStop layout and its 40 long test train, as
 * {@link DispatcherTrainLengthRequiredTest} does.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class DispatcherTrainRecordTest {

    private Path outPathTrainInfo = null;

    @Test
    public void testSameRecordRunAfterRun() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        Assertions.assertTrue(cm.load(new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml")));
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/MultiBlockStopdispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        Sensor sensorBlock1 = InstanceManager.getDefault(SensorManager.class).getSensor("Block 1");
        JUnitUtil.setBeanStateAndWait(sensorBlock1, Sensor.ACTIVE);
        TrainRecordManager records = InstanceManager.getDefault(TrainRecordManager.class);

        TrainInfo info = new TrainInfoFile().readTrainInfo("FWDREV40.xml");
        info.setAutoRun(false);
        info.setTrainDetection(TrainDetection.TRAINDETECTION_HEADONLY);
        assertEquals(0, d.loadTrainFromTrainInfo(info));
        ActiveTrain first = d.getActiveTrainsList().get(0);
        TrainRecord record = Objects.requireNonNull(first.getTrainRecord(), "the Active Train refers to a record");
        assertEquals(first.getTrainName(), record.getName());
        assertEquals(TrainRecord.Detection.HEAD_ONLY, record.getDetection());
        assertEquals(Double.valueOf(first.getMaxTrainLengthMM()), record.getLength(TrainRecord.LengthSource.ENTERED),
                "Max Train Length is the entered length");
        assertTrue(first.getMaxTrainLengthMM() > 0);
        assertEquals(java.util.List.of(first.getStartBlock()), record.getBlocks(),
                "placed in the start Block, where Dispatcher has been told the train is");
        assertEquals(TrainRecord.Confidence.CONFIRMED, record.getConfidence());
        d.terminateActiveTrain(first, true, false);
        assertTrue(d.getActiveTrainsList().isEmpty());
        JUnitUtil.waitFor(() -> d.getAllocatedSectionsList().isEmpty(), "the first run's Sections released");
        assertSame(record, records.getByName(first.getTrainName()), "the record outlives the run");
        assertFalse(record.isRemoved());

        info.setTrainDetection(TrainDetection.TRAINDETECTION_HEADANDTAIL);
        assertEquals(0, d.loadTrainFromTrainInfo(info));
        ActiveTrain second = d.getActiveTrainsList().get(0);
        assertSame(record, second.getTrainRecord(), "the next run refers to the same record");
        assertEquals(TrainRecord.Detection.HEAD_AND_TAIL, record.getDetection(), "with what was entered this time");
        assertEquals(1, records.getRecords().size());
        d.terminateActiveTrain(second, true, false);

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
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false, false);
        try {
            Files.delete(outPathTrainInfo);
        } catch (IOException e) {
            // already gone
        }
        // do not leave auto allocate on for tests that run after this one
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }

}
