package jmri.jmrit.logixng.digital.expressions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Test TriggerOnce
 * 
 * @author Daniel Bergqvist 2018
 */
public class TriggerOnceTest {

    @Test
    public void testCtor()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        ExpressionTurnout expression = new ExpressionTurnout("IQDE:AUTO:321", null);
        MaleDigitalExpressionSocket expressionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        DigitalExpressionBean t = new TriggerOnce("IQDE321", null, expressionSocket);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        ExpressionTurnout expression = new ExpressionTurnout("IQDE:AUTO:321", null);
        MaleDigitalExpressionSocket expressionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        DigitalExpressionBean e1 = new TriggerOnce("IQDE321", null, expressionSocket);
        Assert.assertTrue("Trigger once".equals(e1.getShortDescription()));
        Assert.assertTrue("Trigger once".equals(e1.getLongDescription()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
