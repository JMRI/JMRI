package jmri.jmrit.dispatcher;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Swing tests for dispatcher train info.
 *
 * @author	Dave Duchamp
 */
public class DispatcherTrainInfoTest {

    @Test
    public void testAccessMethods() throws Exception {

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
        ti.setResistanceWheels(true);
        ti.setRunInReverse(false);
        ti.setSoundDecoder(true);
        ti.setMaxTrainLength(225);
        ti.setAllocationMethod(8);
        ti.setUseSpeedProfile(true);
        ti.setStopBySpeedProfile(true);
        ti.setStopBySpeedProfileAdjust(0.5f);
        // test it
        Assert.assertEquals("Transit Name", ti.getTransitName(), "Transit");
        Assert.assertEquals("Train Name", ti.getTrainName(), "Train");
        Assert.assertEquals("DCC Address", ti.getDccAddress(), "346");
        Assert.assertTrue("Train In Transit", ti.getTrainInTransit());
        Assert.assertEquals("Start Block Name", ti.getStartBlockName(), "IB12");
        Assert.assertEquals("Destination Block Name", ti.getDestinationBlockName(), "IB22");
        Assert.assertTrue("Train From Roster", ti.getTrainFromRoster());
        Assert.assertFalse("Train From Trains", ti.getTrainFromTrains());
        Assert.assertFalse("Train From User", ti.getTrainFromUser());
        Assert.assertEquals("Priority", ti.getPriority(), 8);
        Assert.assertTrue("Run Auto", ti.getAutoRun());
        Assert.assertFalse("Reset When Done", ti.getResetWhenDone());
        Assert.assertEquals("Delayed Start", ti.getDelayedStart(), 1);
        Assert.assertEquals("Departure Time Hours", ti.getDepartureTimeHr(), 10);
        Assert.assertEquals("Departure Time Minutes", ti.getDepartureTimeMin(), 30);
        Assert.assertEquals("Train Type", ti.getTrainType(), "2");

        Assert.assertEquals("Speed Factor", ti.getSpeedFactor(), 0.8f, 0.0);
        Assert.assertEquals("Maximum Speed", ti.getMaxSpeed(), 0.6f, 0.0);
        Assert.assertEquals("Ramp Rate", ti.getRampRate(), "2");
        Assert.assertTrue("Resistance Wheels", ti.getResistanceWheels());
        Assert.assertFalse("Run In Reverse", ti.getRunInReverse());
        Assert.assertTrue("Sound Decoder", ti.getSoundDecoder());
        Assert.assertEquals("Allocation Method", ti.getAllocationMethod(),8,0);
        Assert.assertTrue("Use Speed Profile", ti.getUseSpeedProfile());
        Assert.assertTrue("Stop By Speed Profile", ti.getStopBySpeedProfile());
        Assert.assertEquals("Stop By Speed Profile using percentage of block", ti.getStopBySpeedProfileAdjust(),0.5f, 0.0);

    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
