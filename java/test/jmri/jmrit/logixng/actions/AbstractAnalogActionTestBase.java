package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.AnalogIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.AnalogActionBean;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.Test;

/**
 * Test AbstractAnalogAction
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractAnalogActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);

    @Test
    public void testBadSystemName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            fail("Bean is not null " + bean);
        }, "Exception is thrown");
        assertEquals( "system name is not valid", e.getMessage(), "Exception is correct");
    }

    @Test
    public void testGetBeanType() {
        assertEquals( "Analog action", ((AnalogActionBean)_base).getBeanType(), "String matches");
    }

    @Test
    public void testState() throws JmriException {
        AnalogActionBean _action = (AnalogActionBean)_base;
        _action.setState(AnalogIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractAnalogAction.");
        assertSame( AnalogIO.INCONSISTENT, _action.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogAction.");
        _action.setState(AnalogIO.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractAnalogAction.");
        assertSame( AnalogIO.UNKNOWN, _action.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogAction.");
        _action.setState(AnalogIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractAnalogAction.");
        assertSame( AnalogIO.INCONSISTENT, _action.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractAnalogAction.");
    }

}
