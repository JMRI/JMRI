package jmri.jmrit.speedometer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SpeedometerFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SpeedometerFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        SpeedometerFrame frame = new SpeedometerFrame();
        assertNotNull(frame, "exists");
        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfHeadless
    public void testSetInputs(){
        // this test only checks to see that we don't throw an exception when
        // setting the input values.
        SpeedometerFrame frame = new SpeedometerFrame();
        frame.setInputs("IS1","IS2","IS3","5280","5280");
        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfHeadless
    public void testVerifyInputsValid() {
        SpeedometerFrame frame = new SpeedometerFrame();
        // set the input values
        frame.setInputs("IS1","IS2","IS3","5280","5280");

        int valid = frame.verifyInputs(false);
        assertEquals(2,valid, "Expected Valid Sensors");

        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfHeadless
    public void testVerifyInputsInValid() {
        SpeedometerFrame frame = new SpeedometerFrame();
        // don't set any values in the inputs.

        int valid = frame.verifyInputs(false);
        assertEquals(0,valid,"Expected Valid Sensors");

        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name must start with \"IS\".");
        JUnitAppender.assertErrorMessage("Start sensor invalid:");

        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfHeadless
    public void testStartButton(){
        // this test only checks to see that we don't throw an exception when
        // pressing the buttons and all information is filled in.
        SpeedometerFrame frame = new SpeedometerFrame();
        frame.setVisible(true);
        SpeedometerScaffold operator = new SpeedometerScaffold();
        operator.setStartSensorValue("IS1");
        operator.setStopSensor1Value("IS2");
        operator.setDistance1Value("200");
        operator.setStopSensor2Value("IS3");
        operator.setDistance2Value("400");
        operator.pushStartButton();
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
