package jmri.jmrit.logixng.tools;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.*;

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
public class ImportExpressionTurnoutTest extends ImportExpressionTestBase {

    private Turnout turnout = null;
    private ConditionalVariable cv = null;

    @Override
    public void setNamedBeanState(State state) throws JmriException {
        assertNotNull(turnout);
        switch (state) {
            case ON:
                turnout.setState(Turnout.CLOSED);
                break;
                
            case OFF:
                turnout.setState(Turnout.THROWN);
                break;

            case OTHER:
            default:
                turnout.setState(Turnout.UNKNOWN);
                break;
        }
    }

    @Override
    public void setConditionalVariableState(State state) {
        assertNotNull(cv);
        switch (state) {
            case ON:
                cv.setType(Conditional.Type.TURNOUT_CLOSED);
                break;

            case OFF:
            case OTHER:
            default:
                cv.setType(Conditional.Type.TURNOUT_THROWN);
                break;
        }
    }

    @Override
    public ConditionalVariable newConditionalVariable() {
        turnout = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        cv = new ConditionalVariable();
        cv.setName("IT2");
        return cv;
    }

}
