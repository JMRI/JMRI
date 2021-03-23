package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This test tests expression turnout
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportActionTurnoutTest extends ImportActionTestBase {

    Turnout turnout;
    ConditionalAction ca;
    
    @Override
    public void setNamedBeanState(boolean on) throws JmriException {
        if (on) turnout.setState(Turnout.THROWN);
        else turnout.setState(Turnout.CLOSED);
    }

    @Override
    public boolean checkNamedBeanState(boolean on) {
        if (on) return turnout.getState() == Turnout.THROWN;
        else return turnout.getState() == Turnout.CLOSED;
    }

    @Override
    public void setConditionalActionState(State state) {
        switch (state) {
            case ON:
                ca.setActionData(Turnout.THROWN);
                break;
                
            case OFF:
                ca.setActionData(Turnout.CLOSED);
                break;
                
            case TOGGLE:
            default:
                ca.setActionData(Route.TOGGLE);
                break;
        }
    }

    @Override
    public ConditionalAction newConditionalAction() {
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        System.out.format("New turnout IT2: %s%n", turnout.getSystemName());
        ca = new DefaultConditionalAction();
        ca.setActionData(Turnout.THROWN);
        ca.setType(Conditional.Action.SET_TURNOUT);
        ca.setDeviceName("IT2");
        return ca;
    }
    
}
