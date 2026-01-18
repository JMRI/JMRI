package jmri.jmrit.logixng.tools;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 * This test tests action delayed turnout
 * 
 * @author Daniel Bergqvist (C) 2021
 */
public class ImportActionDelayedTurnoutTest extends ImportActionTestBase {

    private Turnout turnout = null;
    private ConditionalAction ca = null;

    @Override
    public void setNamedBeanState(boolean on) throws JmriException {
        if (on) {
            turnout.setState(Turnout.THROWN);
        } else {
            turnout.setState(Turnout.CLOSED);
        }
    }

    @Override
    public boolean checkNamedBeanState(boolean on) {
        if (on) {
            return turnout.getState() == Turnout.THROWN;
        } else {
            return turnout.getState() == Turnout.CLOSED;
        }
    }

    @Override
    public void setConditionalActionState(State state) {
        assertNotNull(ca);
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
        Memory memory = InstanceManager.getDefault(MemoryManager.class).provide("IMMYMEMORY");
        memory.setValue("0.1"); // 100 millisec
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        ca = new DefaultConditionalAction();
        ca.setActionData(Turnout.THROWN);
        ca.setType(Conditional.Action.DELAYED_TURNOUT);
//        ca.setType(Conditional.Action.SET_SENSOR);
        ca.setActionString("@IMMYMEMORY");
//        ca.setActionString("100");
        ca.setDeviceName("IT2");
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

    @Disabled("This test is not stable")
    @Test
    @Override
    public void testOn() throws JmriException {
    }

    @Disabled("This test is not stable")
    @Test
    @Override
    public void testOff() throws JmriException {
    }

    @Disabled("This test doesn't work yet")
    @Test
    @Override
    public void testToggle() throws JmriException {
    }

}
