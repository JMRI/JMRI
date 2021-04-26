package jmri.managers;

import jmri.*;
import jmri.implementation.AbstractVariableLight;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test the DefaultVariableLightManager.
 *
 * @author  Daniel Bergqvist Copyright (C) 2020
 */
public class DefaultVariableLightManagerTest {

    private VariableLight newVariableLight(String sysName, String userName) {
        return new MyVariableLight(sysName, userName);
    }

    @Test
    public void testDispose() {
        VariableLightManager l = InstanceManager.getDefault(VariableLightManager.class);
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testRegister() {
        // create
        VariableLight t = newVariableLight("L11", "mine");
        
        VariableLightManager l = InstanceManager.getDefault(VariableLightManager.class);
        
        Assert.assertThrows(UnsupportedOperationException.class, () -> l.register(t));
        Assert.assertThrows(UnsupportedOperationException.class, () -> l.deregister(t));
    }

    @Test
    public void testLights() {
        // Test that the VariableLightManager registers variable lights but not
        // lights that are not variable.

        // When this light is created, the VariableLightManager isn't created yet.
        Light light = new MyLight("IL1");
        InstanceManager.getDefault(LightManager.class).register(light);
        VariableLight variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL1");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);

        // Check that we can deregister light without problem
        InstanceManager.getDefault(LightManager.class).deregister(light);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL1");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);

        // When this light is created, the VariableLightManager is created.
        Light light2 = new MyLight("IL2");
        InstanceManager.getDefault(LightManager.class).register(light2);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL2");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);

        // Check that we can deregister light without problem
        InstanceManager.getDefault(LightManager.class).deregister(light);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL2");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);
    }

    @Test
    public void testVariableLights() {
        // Test that the VariableLightManager registers variable lights.

        // When this light is created, the VariableLightManager isn't created yet.
        Light variableLight = new MyVariableLight("IL1", "MyLight");
        InstanceManager.getDefault(LightManager.class).register(variableLight);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL1");
        Assert.assertNotNull("variable light exists in VariableLightManager", variableLight);

        // Check that we can deregister light and that it get deregstered from VariableLightManager as well
        InstanceManager.getDefault(LightManager.class).deregister(variableLight);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL1");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);

        // When this light is created, the VariableLightManager is created.
        Light variableLight2 = new MyVariableLight("IL2", "MyLight");
        InstanceManager.getDefault(LightManager.class).register(variableLight2);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL2");
        Assert.assertNotNull("variable light exists in VariableLightManager", variableLight);

        // Check that we can deregister light and that it get deregstered from VariableLightManager as well
        InstanceManager.getDefault(LightManager.class).deregister(variableLight2);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getBySystemName("IL2");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);
    }

    @Test
    public void testVariableLights_UserName() {
        // Test that the VariableLightManager registers variable lights but not
        // lights that are not variable.

        Light light = new MyLight("IL1");
        InstanceManager.getDefault(LightManager.class).register(light);
        VariableLight variableLight = InstanceManager.getDefault(VariableLightManager.class).getByUserName("A light");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);

        // Check that we can deregister light without problem
        InstanceManager.getDefault(LightManager.class).deregister(light);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getByUserName("A light");
        Assert.assertNull("light does not exists in VariableLightManager", variableLight);

        Light variableLight2 = new MyVariableLight("IL2", "A variable light");
        InstanceManager.getDefault(LightManager.class).register(variableLight2);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getByUserName("A variable light");
        Assert.assertNotNull("variableLight exists in VariableLightManager", variableLight);

        // Check that we can deregister variableLight and that it get deregstered from VariableLightManager as well
        InstanceManager.getDefault(LightManager.class).deregister(variableLight2);
        variableLight = InstanceManager.getDefault(VariableLightManager.class).getByUserName("A variable light");
        Assert.assertNull("variableLight does not exists in VariableLightManager", variableLight);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalLightManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }



    private class MyLight extends jmri.implementation.AbstractLight {

        public MyLight(String systemName) {
            super(systemName);
        }

    }


    private class MyVariableLight extends AbstractVariableLight {

        double _value = 0.0;

        public MyVariableLight(String sys, String userName) {
            super(sys, userName);
        }

        @Override
        public void setCommandedAnalogValue(double value) throws JmriException {
            _value = value;
        }

        @Override
        public double getCommandedAnalogValue() {
            return _value;
        }

        @Override
        public double getMin() {
            return Float.MIN_VALUE;
        }

        @Override
        public double getMax() {
            return Float.MAX_VALUE;
        }

        @Override
        public double getResolution() {
            return 0.1;
        }

        @Override
        public int getNumberOfSteps() {
            return 10;
        }

        @Override
        public AbsoluteOrRelative getAbsoluteOrRelative() {
            return AbsoluteOrRelative.ABSOLUTE;
        }

        @Override
        protected void sendIntensity(double intensity) {
            // Do nothing
        }
        
        @Override
        protected void sendOnOffCommand(int newState) {
            // Do nothing
        }
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyVariableLightManagerTest.class);

}
