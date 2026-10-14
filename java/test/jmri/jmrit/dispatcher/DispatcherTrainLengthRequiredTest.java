package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * A train whose tail is not always detected must have a Max Train Length.
 * Uses the MultiBlockStop layout and its 40 long test train.
 */
@DisabledIfHeadless
public class DispatcherTrainLengthRequiredTest {

    private Path outPathTrainInfo = null;

    @Test
    public void testTrainWithoutLengthIsRefused() throws Exception {
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

        TrainInfo info = new TrainInfoFile().readTrainInfo("FWDREV40.xml");
        info.setAutoRun(false);

        for (TrainDetection detection : new TrainDetection[]{
                TrainDetection.TRAINDETECTION_HEADONLY, TrainDetection.TRAINDETECTION_HEADANDTAIL}) {
            info.setTrainDetection(detection);
            info.setMaxTrainLengthScaleMeters(0.0f);
            assertEquals(-1, d.loadTrainFromTrainInfo(info), "refused without a length: " + detection);
            JUnitAppender.assertErrorMessageStartsWith("Train [" + info.getTrainName() + "] uses");
            assertTrue(d.getActiveTrainsList().isEmpty(), "no train created: " + detection);
            assertTrue(d.getAllocatedSectionsList().isEmpty(), "nothing allocated: " + detection);
        }

        info.setTrainDetection(TrainDetection.TRAINDETECTION_HEADONLY);
        info.setMaxTrainLengthScaleMeters(40.0f);
        assertEquals(0, d.loadTrainFromTrainInfo(info), "accepted with a length");
        assertEquals(1, d.getActiveTrainsList().size());
        ActiveTrain at = d.getActiveTrainsList().get(0);
        assertTrue(at.getMaxTrainLengthMM() > 0, "length stored on the train");
        d.terminateActiveTrain(at, true, false);

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
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }

}
