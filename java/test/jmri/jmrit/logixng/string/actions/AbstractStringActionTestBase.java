package jmri.jmrit.logixng.string.actions;

import java.util.Locale;
import jmri.StringIO;
import jmri.JmriException;
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

    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal", "Set memory", Bundle.getMessage("StringActionMemory0"));
        Assert.assertEquals("strings are equal", "Set memory IM1", Bundle.getMessage("StringActionMemory1", "IM1"));
        Assert.assertEquals("strings are equal", "Set memory", Bundle.getMessage(Locale.CANADA, "StringActionMemory0"));
        Assert.assertEquals("strings are equal", "Set memory IM1", Bundle.getMessage(Locale.CANADA, "StringActionMemory1", "IM1"));
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
