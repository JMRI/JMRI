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
    
    @Before
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
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
