package jmri.jmrit.logixng.actions;

import java.util.Locale;

import jmri.StringIO;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.StringActionBean;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test AbstractStringAction
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractStringActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName) throws Exception;
    
    @Test
    public void testBadSystemName() throws Exception {
        boolean hasThrown = false;
        try {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            Assert.assertNotNull("Bean is not null", bean);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Exception is correct", "system name is not valid: IQ111", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testGetBeanType() {
        Assert.assertTrue("String matches", "String action".equals(((StringActionBean)_base).getBeanType()));
    }
    
    @Test
    public void testState() throws JmriException {
        StringActionBean _action = (StringActionBean)_base;
        _action.setState(StringIO.INCONSISTENT);
        Assert.assertTrue("State matches", StringIO.INCONSISTENT == _action.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringAction.");
        _action.setState(StringIO.UNKNOWN);
        Assert.assertTrue("State matches", StringIO.UNKNOWN == _action.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringAction.");
        _action.setState(StringIO.INCONSISTENT);
        Assert.assertTrue("State matches", StringIO.INCONSISTENT == _action.getState());
        jmri.util.JUnitAppender.assertWarnMessage("Unexpected call to getState in AbstractStringAction.");
    }
    
}
