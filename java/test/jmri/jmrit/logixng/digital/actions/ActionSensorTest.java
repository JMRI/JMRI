package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionSensor
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionSensorTest extends AbstractDigitalActionTestBase {

    @Override
    public LogixNG getLogixNG() {
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Set sensor '' to Active%n");
    }
    
    @Test
    public void testCtor() {
        ActionSensor t = new ActionSensor("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IT1");
        sensor.setCommandedState(Sensor.INACTIVE);
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = new DefaultConditionalNG(logixNG.getSystemName()+":1");
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        ActionSensor actionSensor = new ActionSensor();
        actionSensor.setSensor(sensor);
        actionSensor.setSensorState(ActionSensor.SensorState.ACTIVE);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensor);
        conditionalNG.getChild(0).connect(socket);
        // The action is not yet executed so the sensor should be closed
        Assert.assertTrue("sensor is closed",sensor.getCommandedState() == Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.ACTIVE);
        
        // Test to set sensor to closed
        actionSensor.setSensorState(ActionSensor.SensorState.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.INACTIVE);
        
        // Test to set sensor to toggle
        actionSensor.setSensorState(ActionSensor.SensorState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.ACTIVE);
        
        // Test to set sensor to toggle
        actionSensor.setSensorState(ActionSensor.SensorState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be thrown
        Assert.assertTrue("sensor is thrown",sensor.getCommandedState() == Sensor.INACTIVE);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the sensor
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Assert.assertNotNull("Sensor is not null", sensor);
        ActionSensor action = new ActionSensor();
        action.setSensor(sensor);
        
        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        Assert.assertNotNull("Sensor is not null", otherSensor);
        Assert.assertNotEquals("Sensor is not equal", sensor, otherSensor);
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for another sensor
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for its own sensor
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", sensor, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        Assert.assertNull("Sensor is null", action.getSensor());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSensorManager();
        _base = new ActionSensor("IQDA321", null);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
