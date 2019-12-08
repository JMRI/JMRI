package jmri.jmrit.logixng.digital.expressions;

import org.junit.Assert;
import org.junit.Test;
import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalExpressionTestBase extends AbstractBaseTestBase {

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
    
}
