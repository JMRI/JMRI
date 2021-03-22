package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This test tests expression sensor
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportActionSensorTest extends ImportActionTestBase {

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
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        ca = new DefaultConditionalAction();
        ca.setActionData(Sensor.ACTIVE);
        ca.setType(Conditional.Action.SET_SENSOR);
        ca.setDeviceName("IS2");
        return ca;
    }
    
}
