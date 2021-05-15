package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;

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
public class ImportActionLightTest extends ImportActionTestBase {

    Light light;
    ConditionalAction ca;
    
    @Override
    public void setNamedBeanState(boolean on) throws JmriException {
        if (on) light.setState(Light.ON);
        else light.setState(Light.OFF);
    }

    @Override
    public boolean checkNamedBeanState(boolean on) {
        if (on) return light.getState() == Light.ON;
        else return light.getState() == Light.OFF;
    }

    @Override
    public void setConditionalActionState(State state) {
        switch (state) {
            case ON:
                ca.setActionData(Light.ON);
                break;
                
            case OFF:
                ca.setActionData(Light.OFF);
                break;
                
            case TOGGLE:
            default:
                ca.setActionData(Route.TOGGLE);
                break;
        }
    }

    @Override
    public ConditionalAction newConditionalAction() {
        light = InstanceManager.getDefault(LightManager.class).provide("IL2");
        ca = new DefaultConditionalAction();
        ca.setActionData(Light.ON);
        ca.setType(Conditional.Action.SET_LIGHT);
        ca.setDeviceName("IL2");
        return ca;
    }
    
}
