package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.dispatcher.ActiveTrain.TrainLengthUnits;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Swing tests for dispatcher train info.
 *
 * @author Dave Duchamp
 */
public class DispatcherTrainInfoTest {

    @Test
    public void testAccessMethods() {

        TrainInfo ti = new TrainInfo();
        // set up TrainInfo object
        ti.setTransitName("Transit");
        ti.setTrainName("Train");
        ti.setDccAddress("346");
        ti.setTrainInTransit(true);
        ti.setStartBlockName("IB12");
        ti.setDestinationBlockName("IB22");
        ti.setTrainFromRoster(true);
        ti.setTrainFromTrains(false);
        ti.setTrainFromUser(false);
        ti.setPriority(8);
        ti.setAutoRun(true);
        ti.setResetWhenDone(false);
        ti.setDelayedStart(0x01);
        ti.setDepartureTimeHr(10);
        ti.setDepartureTimeMin(30);
        ti.setTrainType("2");

        ti.setSpeedFactor(0.8f);
        ti.setMaxSpeed(0.6f);
        ti.setRampRate("2");
        ti.setTrainDetection(TrainDetection.TRAINDETECTION_HEADONLY);
        ti.setRunInReverse(false);
        ti.setSoundDecoder(true);
        ti.setMaxTrainLengthScaleMeters(1000);
        assertEquals( 3280.84f, ti.getMaxTrainLengthScaleFeet(), 0.01f, "setMaxTrainLengthScaleFeetFromMeters");
        assertEquals( 1000.0f, ti.getMaxTrainLengthScaleMeters(), 0.01f, "getMaxTrainLengthScaleMeters");
        ti.setMaxTrainLengthScaleFeet(2000);
        assertEquals( 609.6f, ti.getMaxTrainLengthScaleMeters(), 0.01f, "setMaxTrainLengthScaleMetersFromFeet");
        assertEquals( 2000.0f, ti.getMaxTrainLengthScaleFeet(), 0.01f, "getMaxTrainLengthScaleFeet");
        ti.setTrainLengthUnits(TrainLengthUnits.TRAINLENGTH_ACTUALINCHS);
        ti.setAllocationMethod(8);
        ti.setUseSpeedProfile(true);
        ti.setStopBySpeedProfile(true);
        ti.setStopBySpeedProfileAdjust(0.5f);
        // test it
        assertEquals( "Transit", ti.getTransitName(), "Transit Name");
        assertEquals( "Train", ti.getTrainName(), "Train Name");
        assertEquals( "346", ti.getDccAddress(), "DCC Address");
        assertTrue( ti.getTrainInTransit(), "Train In Transit");
        assertEquals( "IB12", ti.getStartBlockName(), "Start Block Name");
        assertEquals( "IB22", ti.getDestinationBlockName(), "Destination Block Name");
        assertTrue( ti.getTrainFromRoster(), "Train From Roster");
        assertFalse( ti.getTrainFromTrains(), "Train From Trains");
        assertFalse( ti.getTrainFromUser(), "Train From User");
        assertEquals( 8, ti.getPriority(), "Priority");
        assertTrue( ti.getAutoRun(), "Run Auto");
        assertFalse( ti.getResetWhenDone(), "Reset When Done");
        assertEquals( 1, ti.getDelayedStart(), "Delayed Start");
        assertEquals( 10, ti.getDepartureTimeHr(), "Departure Time Hours");
        assertEquals( 30, ti.getDepartureTimeMin(), "Departure Time Minutes");
        assertEquals( "2", ti.getTrainType(), "Train Type");

        assertEquals( 0.8f, ti.getSpeedFactor(), 0.0, "Speed Factor");
        assertEquals( 0.6f, ti.getMaxSpeed(), 0.0, "Maximum Speed");
        assertEquals( "2", ti.getRampRate(), "Ramp Rate");
        assertEquals( ActiveTrain.TrainDetection.TRAINDETECTION_HEADONLY,ti.getTrainDetection(), "Train Detection");
        assertFalse( ti.getRunInReverse(), "Run In Reverse");
        assertTrue( ti.getSoundDecoder(), "Sound Decoder");
        assertEquals( 8, ti.getAllocationMethod(), "Allocation Method");
        assertTrue( ti.getUseSpeedProfile(), "Use Speed Profile");
        assertTrue( ti.getStopBySpeedProfile(), "Stop By Speed Profile");
        assertEquals( 0.5f, ti.getStopBySpeedProfileAdjust(), 0.0, "Stop By Speed Profile using percentage of block");
        assertEquals( ti.getTrainLengthUnits(),TrainLengthUnits.TRAINLENGTH_ACTUALINCHS, "Train Length Units");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
