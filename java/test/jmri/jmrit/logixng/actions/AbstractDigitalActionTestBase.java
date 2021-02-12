package jmri.jmrit.logixng.actions;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);
    
    @Test
    public void testBadSystemName() {
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
    public void testBundle() {
        Assert.assertEquals("strings are equal", "Set memory", Bundle.getMessage("ActionMemory_Short"));
        Assert.assertEquals("strings are equal", "Set memory IM1 to null", Bundle.getMessage("ActionMemory_Long_Null", "IM1"));
        Assert.assertEquals("strings are equal", "Set memory", Bundle.getMessage(Locale.CANADA, "ActionMemory_Short"));
        Assert.assertEquals("strings are equal", "Set memory IM1 to null", Bundle.getMessage(Locale.CANADA, "ActionMemory_Long_Null", "IM1"));
    }
    
    @Test
    public void testGetBeanType() {
        Assert.assertTrue("String matches", "Digital action".equals(((DigitalActionBean)_base).getBeanType()));
    }
    
}
