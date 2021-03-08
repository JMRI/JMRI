package jmri.jmrit.logixng.tools;

import jmri.*;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression conditional
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionConditionalTest extends ImportExpressionTestBase {

    Conditional conditional;
    ConditionalVariable cv;
    
    @Override
    public void setNamedBeanState(State state) throws JmriException {
        switch (state) {
            case ON:
                conditional.setState(Conditional.TRUE);
                break;
                
            case OFF:
                conditional.setState(Conditional.FALSE);
                break;
                
            case OTHER:
            default:
                conditional.setState(Sensor.UNKNOWN);
                break;
        }
    }

    @Override
    public void setConditionalVariableState(State state) {
        switch (state) {
            case ON:
                cv.setType(Conditional.Type.CONDITIONAL_TRUE);
                break;
                
            case OFF:
            case OTHER:
            default:
                cv.setType(Conditional.Type.CONDITIONAL_FALSE);
                break;
        }
    }

    @Override
    public ConditionalVariable newConditionalVariable() {
        InstanceManager.getDefault(LogixManager.class).createNewLogix("IX2", null);
        conditional = InstanceManager.getDefault(ConditionalManager.class).createNewConditional("IX2C1", null);
        cv = new ConditionalVariable();
        cv.setName("IX2C1");
        return cv;
    }
    
}
