package jmri;

import jmri.implementation.AbstractVariableLight;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue( light.isConsistentState(), "State is consistent");

        light.setState(VariableLight.OFF);
        assertTrue( light.isConsistentState(), "State is consistent");

        light.setState(VariableLight.INTERMEDIATE);
        assertTrue( light.isConsistentState(), "State is consistent");

        light.setState(VariableLight.TRANSITIONINGTOFULLON);
        assertFalse( light.isConsistentState(), "State is not consistent");

        light.setState(VariableLight.TRANSITIONINGHIGHER);
        assertFalse( light.isConsistentState(), "State is not consistent");

        light.setState(VariableLight.TRANSITIONINGLOWER);
        assertFalse( light.isConsistentState(), "State is not consistent");

        light.setState(VariableLight.TRANSITIONINGTOFULLOFF);
        assertFalse( light.isConsistentState(), "State is not consistent");

        light.setState(VariableLight.TRANSITIONING);
        assertFalse( light.isConsistentState(), "State is not consistent");
    }


    private jmri.Timebase clock;

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        clock.setRun(false);
        clock.setTime(java.time.Instant.EPOCH);  // just a specific time
     }

    @AfterEach
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();

          // the light has registered a listener to the
          // previous timebase, but we've stopped that.

          // force a new default Timebase object
          InstanceManager.reset(jmri.Timebase.class);
    }


    private static class MyVariableLight extends AbstractVariableLight {

        MyVariableLight(String systemName) {
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
