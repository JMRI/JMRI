package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.StringIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.StringActionBean;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.Test;

/**
 * Test AbstractStringAction
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractStringActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);
    
    @Test
    public void testBadSystemName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            fail("Bean is not null " + bean);
        }, "Exception is thrown");
        assertEquals( "system name is not valid: IQ111", e.getMessage(), "Exception is correct");
    }
    
    @Test
    public void testGetBeanType() {
        assertEquals( "String action", ((StringActionBean)_base).getBeanType(), "String matches");
    }
    
    @Test
    public void testState() throws JmriException {
        StringActionBean _action = (StringActionBean)_base;
        _action.setState(StringIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractStringAction.");
        assertSame( StringIO.INCONSISTENT, _action.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringAction.");
        _action.setState(StringIO.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractStringAction.");
        assertSame( StringIO.UNKNOWN, _action.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringAction.");
        _action.setState(StringIO.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in AbstractStringAction.");
        assertSame( StringIO.INCONSISTENT, _action.getState(), "State matches");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringAction.");
    }
    
}
