package jmri.jmrit.logixng.tools;

import jmri.*;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This test tests expression light
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionLightTest extends ImportExpressionTestBase {

    Light light;
    ConditionalVariable cv;
    
    @Override
    public boolean isStateOtherAllowed() {
        return false;
    }
    
    @Override
    public void setNamedBeanState(State state) throws JmriException {
        switch (state) {
            case ON:
                light.setState(Light.ON);
                break;
                
            case OFF:
            default:
                light.setState(Light.OFF);
                break;
        }
    }

    @Override
    public void setConditionalVariableState(State state) {
        switch (state) {
            case ON:
                cv.setType(Conditional.Type.LIGHT_ON);
                break;
                
            case OFF:
            case OTHER:
            default:
                cv.setType(Conditional.Type.LIGHT_OFF);
                break;
        }
    }

    @Override
    public ConditionalVariable newConditionalVariable() {
        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        cv = new ConditionalVariable();
        cv.setName("IL1");
        return cv;
    }
    
}
