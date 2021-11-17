package jmri;

import jmri.implementation.AbstractVariableLight;

import org.junit.*;

/**
 * Tests for the VariableLight class
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class VariableLightTest {

    @Test
    public void testIsConsistentState() {
        MyVariableLight light = new MyVariableLight("IL1");

        light.setState(VariableLight.ON);
        Assert.assertTrue("State is consistent", light.isConsistentState());

        light.setState(VariableLight.OFF);
        Assert.assertTrue("State is consistent", light.isConsistentState());

        light.setState(VariableLight.INTERMEDIATE);
        Assert.assertTrue("State is consistent", light.isConsistentState());

        light.setState(VariableLight.TRANSITIONINGTOFULLON);
        Assert.assertFalse("State is not consistent", light.isConsistentState());

        light.setState(VariableLight.TRANSITIONINGHIGHER);
        Assert.assertFalse("State is not consistent", light.isConsistentState());

        light.setState(VariableLight.TRANSITIONINGLOWER);
        Assert.assertFalse("State is not consistent", light.isConsistentState());

        light.setState(VariableLight.TRANSITIONINGTOFULLOFF);
        Assert.assertFalse("State is not consistent", light.isConsistentState());

        light.setState(VariableLight.TRANSITIONING);
        Assert.assertFalse("State is not consistent", light.isConsistentState());
    }


    private jmri.Timebase clock;

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        clock.setRun(false);
        clock.setTime(java.time.Instant.EPOCH);  // just a specific time
     }

    @After
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();

          // the light has registered a listener to the
          // previous timebase, but we've stopped that.

          // force a new default Timebase object
          InstanceManager.reset(jmri.Timebase.class);
    }


    private static class MyVariableLight extends AbstractVariableLight {

        public MyVariableLight(String systemName) {
            super(systemName);
        }

        @Override
        public void setState(int newState) {
            mState = newState;
        }

        @Override
        protected void sendIntensity(double intensity) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void sendOnOffCommand(int newState) {
            // Do nothing
        }

        @Override
        protected int getNumberOfSteps() {
            throw new UnsupportedOperationException("Not supported");
        }

    }

}
