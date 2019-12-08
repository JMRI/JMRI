package jmri.jmrit.logixng.digital.actions;

import org.junit.Assert;
import org.junit.Test;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
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
            Assert.assertEquals("Exception is correct", "system name is not valid", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
        // If the digital action implements the interface DigitalActionWithEnableExecution
        // then this method must be overridden to check supportsEnableExecution().
        
        Assert.assertFalse("supportsEnableExecution() returns correct value",
                ((DigitalAction)_base).supportsEnableExecution());
        Assert.assertFalse("digital action does not implement DigitalActionWithEnableExecution",
                _base instanceof DigitalActionWithEnableExecution);
    }
    
}
