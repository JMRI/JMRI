package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.*;

/**
 * Swing tests for dispatcher train info.
 *
 * @author Dave Duchamp
 */
public class DispatcherTrainInfoFileTest {

    private DispatcherFrame d;  // need dispatcher now

    @Test
    @DisabledIfHeadless
    public void testFileRead() throws IOException, JDOMException {
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("TestTrain.xml");
        // test input information
        assertEquals( "Red Main Loop CW", ti.getTransitName(), "Transit Name");
        assertEquals( "IZ5", ti.getTransitId(), "Transit Id");
        assertEquals( "GTW 6418", ti.getTrainName(), "Train Name");
        assertEquals( " ", ti.getDccAddress(), "DCC Address");
        assertTrue( ti.getTrainInTransit(), "Train In Transit");
        assertEquals( "Red Siding-1", ti.getStartBlockName(), "Start Block Name");
        assertEquals( "IB1", ti.getStartBlockId(), "Start Block Id");
        assertEquals( 1, ti.getStartBlockSeq(), "Start Block Sequ");

        assertEquals( "Red Siding-7", ti.getDestinationBlockName(), "Destination Block Name");
        assertEquals( "IB1", ti.getDestinationBlockId(), "Destination Block Id");
        assertEquals( 7, ti.getDestinationBlockSeq(), "Destination Block Sequ");
        assertTrue( ti.getTrainFromRoster(), "Train From Roster");
        assertFalse( ti.getTrainFromTrains(), "Train From Trains");
        assertFalse( ti.getTrainFromUser(), "Train From User");
        assertEquals( 7, ti.getPriority(), "Priority");
        assertFalse( ti.getAutoRun(), "Run Auto");
        assertFalse( ti.getResetWhenDone(), "Reset When Done");
        assertEquals( 1, ti.getDelayedStart(), "Delayed Start");
        assertEquals( 8, ti.getDepartureTimeHr(), "Departure Time Hours");
        assertEquals( 10, ti.getDepartureTimeMin(), "Departure Time Minutes");
        assertEquals( "THROUGH_FREIGHT", ti.getTrainType(), "Train Type");

        assertEquals( ti.getSpeedFactor(), 0.8f, 0.0, "Speed Factor");
        assertEquals( ti.getMaxSpeed(), 0.6f, 0.0, "Maximum Speed");
        assertEquals( "RAMP_FAST", ti.getRampRate(), "Ramp Rate");
        assertEquals( ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection(), "Resistance Wheels");
        assertFalse( ti.getRunInReverse(), "Run In Reverse");
        assertFalse( ti.getSoundDecoder(), "Sound Decoder");
        assertEquals( ti.getMaxTrainLengthScaleFeet(), 222.0f, 0.01, "Maximum Train Length");
        assertEquals( 0, ti.getFNumberLight(), "Light Function key");
        assertEquals( 1, ti.getFNumberBell(), "Bell Function key");
        assertEquals( 2, ti.getFNumberHorn(), "Horn Function key");

    }

    // Version 2
    @Test
    @DisabledIfHeadless
    public void testFileRead_V2() throws IOException, JDOMException {
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("TestTrainCW_V2.xml");
        // test input information
        assertEquals( 2, ti.getVersion(), "Version is 2");
        assertEquals( "SouthPlatform CW", ti.getTransitName(), "Transit Name");
        assertEquals( "IZ1", ti.getTransitId(), "transitid");
        assertEquals( "1000", ti.getTrainName(), "Train Name");
        assertEquals( "1000", ti.getDccAddress(), "DCC Address");
        assertTrue( ti.getTrainInTransit(), "Train In Transit");
        assertEquals( "South Platform-1", ti.getStartBlockName(), "Start Block Name");
        assertEquals( "IB:AUTO:0003", ti.getStartBlockId(), "Start Block Id");
        assertEquals( 1, ti.getStartBlockSeq(), "Start Block Sequ");
        assertEquals( "South Platform-5", ti.getDestinationBlockName(), "Destination Block Name");
        assertEquals( "IB:AUTO:0003", ti.getDestinationBlockId(), "Destination Block Id");
        assertEquals( 5, ti.getDestinationBlockSeq(), "Destination Block Sequ");
        assertFalse( ti.getTrainFromRoster(), "Train From Roster");
        assertFalse( ti.getTrainFromTrains(), "Train From Trains");
        assertTrue( ti.getTrainFromUser(), "Train From User");
        assertEquals( 5, ti.getPriority(), "Priority");
        assertTrue( ti.getAutoRun(), "Run Auto");
        assertFalse( ti.getResetWhenDone(), "Reset When Done");
        assertEquals( 0, ti.getDelayedStart(), "Delayed Start");
        assertNull( ti.getDelaySensorName(), "Start Sensor");
        assertEquals( 8, ti.getDepartureTimeHr(), "Departure Time Hours");
        assertEquals( 0, ti.getDepartureTimeMin(), "Departure Time Minutes");
        assertEquals( "LOCAL_PASSENGER", ti.getTrainType(), "Train Type");
        assertEquals( ti.getSpeedFactor(), 1.0f, 0.0, "Speed Factor");
        assertEquals( ti.getMaxSpeed(), 0.6f, 0.0, "Maximum Speed");
        assertEquals( "None", ti.getRampRate(), "Ramp Rate");
        assertEquals( ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN, ti.getTrainDetection(), "Resistance Wheels");
        assertFalse( ti.getRunInReverse(), "Run In Reverse");
        assertTrue( ti.getSoundDecoder(), "Sound Decoder");
        assertEquals( ti.getMaxTrainLengthScaleFeet(), 200.0f, 0.0, "Maximum Train Length");
        assertEquals( ti.getAllocationMethod(),3,0, "Allocation Method");
        assertFalse( ti.getUseSpeedProfile(), "Use Speed Profile");
        assertEquals( ti.getStopBySpeedProfileAdjust(),1.0f,0.0f, "Use Speed Profile Adjust block length");
    }

    // Version 3
    @Test
    @DisabledIfHeadless
    public void testFileRead_V3_withReverse() throws IOException, JDOMException {
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("FWDREV40.xml");
        // test input information
        assertEquals( ti.getVersion(),3, "Version is 3");
        assertEquals( "StopFWDandREV", ti.getTransitName(), "Transit Name");
        assertEquals( "StopFWDandREV", ti.getTransitId(), "transitid since version 3 same");
        assertEquals( "AAA", ti.getTrainName(), "Train Name");
        assertEquals( "3", ti.getDccAddress(), "DCC Address");
        assertTrue( ti.getTrainInTransit(), "Train In Transit");
        assertEquals( "Block1-1", ti.getStartBlockName(), "Start Block Name");
        assertEquals( "Block1", ti.getStartBlockId(), "Start Block Id");
        assertEquals( 1, ti.getStartBlockSeq(), "Start Block Sequ");
        assertEquals( "Block7", ti.getDestinationBlockName(), "Destination Block Name");
        assertEquals( "Block7", ti.getDestinationBlockId(), "Destination Block Id");
        assertEquals( 3, ti.getDestinationBlockSeq(), "Destination Block Sequ");
        assertFalse( ti.getTrainFromRoster(), "Train From Roster");
        assertFalse( ti.getTrainFromTrains(), "Train From Trains");
        assertTrue( ti.getTrainFromUser(), "Train From User");
        assertEquals( 5, ti.getPriority(), "Priority");
        assertTrue( ti.getAutoRun(), "Run Auto");
        assertTrue( ti.getResetWhenDone(), "Reset When Done");
        assertTrue( ti.getResetRestartSensor(), "Reset restart sensor");
        assertEquals( "TrainRestart", ti.getRestartSensorName(), "Restart sensor");
        assertEquals( "TrainRestart", ti.getReverseRestartSensorName(), "Reverse At End Sensor"); // not if file should have defaulted to restart value.
        assertTrue( ti.getReverseResetRestartSensor(), "Reset Reverse Reverse At End sensor");
        assertEquals( 0, ti.getDelayedStart(), "Delayed Start");
        assertNull( ti.getDelaySensorName(), "Start Sensor");
        assertEquals( 8, ti.getDepartureTimeHr(), "Departure Time Hours");
        assertEquals( 0, ti.getDepartureTimeMin(), "Departure Time Minutes");
        assertEquals( "LOCAL_PASSENGER", ti.getTrainType(), "Train Type");
        assertEquals( ti.getSpeedFactor(), 1.0f, 0.0, "Speed Factor");
        assertEquals( ti.getMaxSpeed(), 1.0f, 0.0, "Maximum Speed");
        assertEquals( "None", ti.getRampRate(), "Ramp Rate");
        assertEquals( ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection(), "Resistance Wheels");
        assertFalse( ti.getRunInReverse(), "Run In Reverse");
        assertFalse( ti.getSoundDecoder(), "Sound Decoder");
        assertEquals( ti.getMaxTrainLengthScaleFeet(), 40.0f, 0.0, "Maximum Train Length");
        assertEquals( ti.getAllocationMethod(),-1,0, "Allocation Method");
        assertFalse( ti.getUseSpeedProfile(), "Use Speed Profile");
        assertEquals( ti.getStopBySpeedProfileAdjust(),1.0f,0.0f, "Use Speed Profile Adjust block length");
    }

    // Version 4
    @Test
    @DisabledIfHeadless
    public void testFileRead_V4_withReverse() throws IOException, JDOMException {
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("FWDREV120.xml");
        // test input information
        assertEquals( 4, ti.getVersion(), "Version is 4");
        assertEquals( "StopFWDandREV", ti.getTransitName(), "Transit Name");
        assertEquals( "StopFWDandREV", ti.getTransitId(), "transitid"); // since version 3 same
        assertEquals( "AAA", ti.getTrainName(), "Train Name");
        assertEquals( "3", ti.getDccAddress(), "DCC Address");
        assertTrue( ti.getTrainInTransit(), "Train In Transit");
        assertEquals( "Block3-1", ti.getStartBlockName(), "Start Block Name");
        assertEquals( 1, ti.getStartBlockSeq(), "Start Block Sequ");
        assertEquals( "Block7", ti.getDestinationBlockName(), "Destination Block Name");
        assertEquals( "Block7", ti.getDestinationBlockId(), "Destination Block Id"); // since 3 both the same
        assertEquals( 3, ti.getDestinationBlockSeq(), "Destination Block Sequ");
        assertFalse( ti.getTrainFromRoster(), "Train From Roster");
        assertFalse( ti.getTrainFromTrains(), "Train From Trains");
        assertTrue( ti.getTrainFromUser(), "Train From User");
        assertEquals( 5, ti.getPriority(), "Priority");
        assertTrue( ti.getAutoRun(), "Run Auto");
        assertTrue( ti.getResetWhenDone(), "Reset When Done");
        assertEquals( "TrainRestart", ti.getRestartSensorName(), "Restart sensor");
        assertEquals( "TrainReverseRestart", ti.getReverseRestartSensorName(), "Reverse At End Sensor");
        assertFalse( ti.getReverseResetRestartSensor(), "Reset Reverse Reverse At End sensor");
        assertEquals( 0, ti.getDelayedStart(), "Delayed Start");
        assertNull( ti.getDelaySensorName(), "Start Sensor");
        assertEquals( 8, ti.getDepartureTimeHr(), "Departure Time Hours");
        assertEquals( 0, ti.getDepartureTimeMin(), "Departure Time Minutes");
        assertEquals( "LOCAL_PASSENGER", ti.getTrainType(), "Train Type");
        assertEquals( ti.getSpeedFactor(), 1.0f, 0.0, "Speed Factor");
        assertEquals( ti.getMaxSpeed(), 1.0f, 0.0, "Maximum Speed");
        assertEquals( "None", ti.getRampRate(), "Ramp Rate");
        assertEquals( ActiveTrain.TrainDetection.TRAINDETECTION_WHOLETRAIN,ti.getTrainDetection(), "Resistance Wheels");
        assertFalse( ti.getRunInReverse(), "Run In Reverse");
        assertFalse( ti.getSoundDecoder(), "Sound Decoder");
        assertEquals( 120.0f, ti.getMaxTrainLengthScaleFeet(), 0.0, "Maximum Train Length");
        assertEquals( ti.getAllocationMethod(),-1,0, "Allocation Method");
        assertFalse( ti.getUseSpeedProfile(), "Use Speed Profile");
        assertEquals( ti.getStopBySpeedProfileAdjust(),1.0f,0.0f, "Use Speed Profile Adjust block length");
    }

    // Version 5
    @Test
    @DisabledIfHeadless
    public void testFileRead_V5() throws IOException, JDOMException {
        TrainInfoFile tif = new TrainInfoFile();
        tif.setFileLocation("java/test/jmri/jmrit/dispatcher/traininfo/");
        TrainInfo ti = tif.readTrainInfo("TestInfoV5-WholeTrain.xml");
        // test input information
        assertEquals( 5, ti.getVersion(),"Version is 5");
        assertEquals( "0912M-0405-0912M-C", ti.getTransitName(), "Transit Name");
        assertEquals( "0912M-0405-0912M-C", ti.getTransitId(), "transitid"); // since version 3 same
        assertEquals( " ", ti.getTrainName(), "Train Name");
        assertEquals( " ", ti.getDccAddress(), "DCC Address");
        assertTrue( ti.getTrainInTransit(), "Train In Transit");
        assertEquals( "B-09-12-M-1", ti.getStartBlockName(), "Start Block Name");
        assertEquals( "B-09-12-M", ti.getStartBlockId(), "Start Block Name");
        assertEquals( 1, ti.getStartBlockSeq(), "Start Block Sequ");
        assertEquals( "B-09-12-M-13", ti.getDestinationBlockName(), "Destination Block Name");
        assertEquals( "B-09-12-M", ti.getDestinationBlockId(), "Destination Block Id"); // since 3 both the same
        assertEquals( 13, ti.getDestinationBlockSeq(), "Destination Block Sequ");
        assertFalse( ti.getTrainFromRoster(), "Train From Roster");
        assertFalse( ti.getTrainFromTrains(), "Train From Trains");
        assertFalse( ti.getTrainFromUser(), "Train From User");
        assertTrue( ti.getTrainFromSetLater(), "Train From SetLater");
        assertEquals( 5, ti.getPriority(), "Priority");
        assertTrue( ti.getAutoRun(), "Run Auto");
        assertTrue( ti.getResetWhenDone(), "Reset When Done");
        assertNull( ti.getRestartSensorName(), "Restart sensor");
        assertNull( ti.getReverseRestartSensorName(), "Reverse At End Sensor");
        assertTrue( ti.getReverseResetRestartSensor(), "Reset Reverse Reverse At End sensor");
        assertEquals( 0, ti.getDelayedStart(), "Delayed Start");
        assertNull( ti.getDelaySensorName(), "Start Sensor");
        assertEquals( 8, ti.getDepartureTimeHr(), "Departure Time Hours");
        assertEquals( 0, ti.getDepartureTimeMin(), "Departure Time Minutes");
        assertEquals( "LOCAL_FREIGHT", ti.getTrainType(), "Train Type");
        assertEquals( 1.0f, ti.getSpeedFactor(), 0.0, "Speed Factor");
        assertEquals( 1.0f, ti.getMaxSpeed(), 0.0, "Maximum Speed");
        assertEquals( "Speed Profile", ti.getRampRate(), "Ramp Rate");
        assertEquals( ActiveTrain.TrainDetection.TRAINDETECTION_HEADANDTAIL,ti.getTrainDetection(), "TrainDetection");
        assertFalse( ti.getRunInReverse(), "Run In Reverse");
        assertTrue( ti.getSoundDecoder(), "Sound Decoder");
        assertEquals( 80.0f, ti.getMaxTrainLengthScaleFeet(), 0.0, "Maximum Train Length");
        assertEquals( 0, ti.getAllocationMethod(), "Allocation Method");
        assertTrue( ti.getUseSpeedProfile(), "Use Speed Profile");
        assertEquals( 0.5f, ti.getStopBySpeedProfileAdjust(), 0.0f, "Use Speed Profile Adjust block length");
        assertEquals( 4, ti.getFNumberLight(), "Light Function Key");
        assertEquals( 5, ti.getFNumberBell(), "Bell Function Key");
        assertEquals( 6, ti.getFNumberHorn(), "Horn Function Key");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };
        JUnitUtil.initTimeProviderManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        File f = new File("java/test/jmri/jmrit/dispatcher/MultiBlockStop.xml");

        assertDoesNotThrow(() -> {
            cm.load(f);
        });

        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class)
                .initializeLayoutBlockPaths();

        // load dispatcher, with all the correct options
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/TestTrainDispatcherOptions.xml");

        d = InstanceManager.getDefault(DispatcherFrame.class);
        JUnitAppender.assertWarnMessage("Layout Editor panel - Test Layout - not found.");

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.resetFileUtilSupport();
        JUnitUtil.tearDown();
    }
}
