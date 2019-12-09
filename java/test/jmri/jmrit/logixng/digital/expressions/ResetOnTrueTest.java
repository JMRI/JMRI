package jmri.jmrit.logixng.digital.expressions;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;

/**
 * Test ResetOnTrue
 * 
 * @author Daniel Bergqvist 2018
 */
public class ResetOnTrueTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ResetOnTrue expressionResetOnTrue;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Reset on true%n" +
                "   ? E1%n" +
                "      Socket not connected%n" +
                "   ? E2%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Reset on true%n" +
                "                  ? E1%n" +
                "                     Socket not connected%n" +
                "                  ? E2%n" +
                "                     Socket not connected%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ResetOnTrue(systemName, null);
    }
    
    @Test
    public void testCtor()
            throws NamedBean.BadUserNameException,
                    NamedBean.BadSystemNameException,
                    SocketAlreadyConnectedException {
        ResetOnTrue expressionResetOnTrue = new ResetOnTrue("IQDE351", null);
        MaleDigitalExpressionSocket primaryExpressionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
        expressionResetOnTrue = new ResetOnTrue("IQDE352", null);
        MaleDigitalExpressionSocket secondaryExpressionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
        DigitalExpressionBean t = new ResetOnTrue("IQDE353", null, primaryExpressionSocket, secondaryExpressionSocket);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, NamedBean.BadSystemNameException, SocketAlreadyConnectedException {
        ExpressionTurnout expressionResetOnTrue = new ExpressionTurnout("IQDE351", null);
        MaleDigitalExpressionSocket primaryExpressionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
        expressionResetOnTrue = new ExpressionTurnout("IQDE352", null);
        MaleDigitalExpressionSocket secondaryExpressionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
        DigitalExpressionBean e1 = new ResetOnTrue("IQDE353", null, primaryExpressionSocket, secondaryExpressionSocket);
        Assert.assertTrue("Reset on true".equals(e1.getShortDescription()));
        Assert.assertTrue("Reset on true".equals(e1.getLongDescription()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.OTHER;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionResetOnTrue = new ResetOnTrue("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionResetOnTrue;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
