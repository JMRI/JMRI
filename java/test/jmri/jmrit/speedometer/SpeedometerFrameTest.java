package jmri.jmrit.speedometer;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SpeedometerFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedometerFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerFrame frame = new SpeedometerFrame();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testSetInputs(){
        // this test only checks to see that we don't throw an exception when
        // setting the input values.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerFrame frame = new SpeedometerFrame();
        frame.setInputs("IS1","IS2","IS3","5280","5280");
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testVerifyInputsValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerFrame frame = new SpeedometerFrame();
        // set the input values
        frame.setInputs("IS1","IS2","IS3","5280","5280");
        // then use reflection to call verifyInputs
        java.lang.reflect.Method verifyInputsValidMethod = null;
        try {
            verifyInputsValidMethod = frame.getClass().getDeclaredMethod("verifyInputs", boolean.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method verifyInputsValid in SpeedometerFrame class ");
        }

        // override the default permissions.
        Assert.assertNotNull(verifyInputsValidMethod);
        verifyInputsValidMethod.setAccessible(true);
        try {
           int valid = (int) verifyInputsValidMethod.invoke(frame,false);
           Assert.assertEquals("Expected Valid Sensors",2,valid);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method verifyInputsValid in SpeedometerFrame class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("verifyInputsValid execution failed reason: " + cause.getMessage());
        }
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testVerifyInputsInValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerFrame frame = new SpeedometerFrame();
        // don't set any values in the inputs.
        // then use reflection to call verifyInputs
        java.lang.reflect.Method verifyInputsValidMethod = null;
        try {
            verifyInputsValidMethod = frame.getClass().getDeclaredMethod("verifyInputs", boolean.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method verifyInputsValid in SpeedometerFrame class ");
        }

        // override the default permissions.
        Assert.assertNotNull(verifyInputsValidMethod);
        verifyInputsValidMethod.setAccessible(true);
        try {
           int valid = (int) verifyInputsValidMethod.invoke(frame,false);
           Assert.assertEquals("Expected Valid Sensors",0,valid);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method verifyInputsValid in SpeedometerFrame class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("verifyInputsValid execution failed reason: " + cause.getMessage());
        }
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name must start with \"IS\".");
        JUnitAppender.assertErrorMessage("Start sensor invalid:");
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testStartButton(){
        // this test only checks to see that we don't throw an exception when
        // pressing the buttons and all information is filled in.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
