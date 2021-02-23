package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This test tests action delayed sensor
 * 
 * @author Daniel Bergqvist (C) 2021
 */
public class ImportActionDelayedSensorTest extends ImportActionTestBase {

    Sensor sensor;
    ConditionalAction ca;
    
    @Override
    public void setNamedBeanState(boolean on) throws JmriException {
        if (on) sensor.setState(Sensor.ACTIVE);
        else sensor.setState(Sensor.INACTIVE);
    }

    @Override
    public boolean checkNamedBeanState(boolean on) {
        if (on) return sensor.getState() == Sensor.ACTIVE;
        else return sensor.getState() == Sensor.INACTIVE;
    }

    @Override
    public void setConditionalActionState(State state) {
        switch (state) {
            case ON:
                ca.setActionData(Sensor.ACTIVE);
                break;
                
            case OFF:
                ca.setActionData(Sensor.INACTIVE);
                break;
                
            case TOGGLE:
            default:
                ca.setActionData(Route.TOGGLE);
                break;
        }
    }

    @Override
    public ConditionalAction newConditionalAction() {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IMMYMEMORY");
        memory.setValue("0.1"); // 100 millisec
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        ca = new DefaultConditionalAction();
        ca.setActionData(Sensor.ACTIVE);
        ca.setType(Conditional.Action.DELAYED_SENSOR);
//        ca.setType(Conditional.Action.SET_SENSOR);
        ca.setActionString("@IMMYMEMORY");
//        ca.setActionString("100");
        ca.setDeviceName("IS2");
        return ca;
    }
    
    @Override
    public void doWait(boolean expectSuccess, boolean on) {
        // Check that the action has not executed yet
//        assertBoolean("Verify", true, checkNamedBeanState(!on));
        
        // Wait for the action to execute
        boolean result = JUnitUtil.waitFor(() -> {return checkNamedBeanState(on);});
        
        // Verify that the action has executed
        assertBoolean("Wait for it", expectSuccess, result);
    }
    
    @Ignore("This test is not stable")
    @Test
    @Override
    public void testOn() throws JmriException {
    }
    
    @Ignore("This test is not stable")
    @Test
    @Override
    public void testOff() throws JmriException {
    }
    
    @Ignore("This test doesn't work yet")
    @Test
    @Override
    public void testToggle() throws JmriException {
    }
    
}
