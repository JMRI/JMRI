package jmri.jmrit.logixng.tools;

import jmri.*;

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
public class ImportExpressionSensorTest extends ImportExpressionTestBase {

    Sensor sensor;
    ConditionalVariable cv;
    
    @Override
    public void setNamedBeanState(State state) throws JmriException {
        switch (state) {
            case ON:
                sensor.setState(Sensor.ACTIVE);
                break;
                
            case OFF:
                sensor.setState(Sensor.INACTIVE);
                break;
                
            case OTHER:
            default:
                sensor.setState(Sensor.UNKNOWN);
                break;
        }
    }

    @Override
    public void setConditionalVariableState(State state) {
        switch (state) {
            case ON:
                cv.setType(Conditional.Type.SENSOR_ACTIVE);
                break;
                
            case OFF:
            case OTHER:
            default:
                cv.setType(Conditional.Type.SENSOR_INACTIVE);
                break;
        }
    }

    @Override
    public ConditionalVariable newConditionalVariable() {
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        cv = new ConditionalVariable();
        cv.setName("IS1");
        return cv;
    }
    
}
