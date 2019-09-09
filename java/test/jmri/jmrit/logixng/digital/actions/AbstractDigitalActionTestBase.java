package jmri.jmrit.logixng.digital.actions;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalActionTestBase extends AbstractBaseTestBase {

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
