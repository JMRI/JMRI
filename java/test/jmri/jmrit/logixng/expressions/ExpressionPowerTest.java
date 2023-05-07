package jmri.jmrit.logixng.expressions;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionPower
 *
 * @author Daniel Bergqvist 2022
 */
public class ExpressionPowerTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionPower expressionPower;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private PowerManager powerManager;


    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Power is On. Ignore unknown state ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Power is On. Ignore unknown state ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionPower(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        ExpressionPower expression2;
        powerManager.setPower(PowerManager.ON);

        expression2 = new ExpressionPower("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Power is On. Ignore unknown state", expression2.getLongDescription());

        expression2 = new ExpressionPower("IQDE321", "My power");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My power", expression2.getUserName());
        Assert.assertEquals("String matches", "Power is On. Ignore unknown state", expression2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionPower("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ExpressionPower("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionPower.getChildCount());

        boolean hasThrown = false;
        try {
            expressionPower.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testPowerState() {
        Assert.assertEquals("String matches", "Off", ExpressionPower.PowerState.Off.toString());
        Assert.assertEquals("String matches", "On", ExpressionPower.PowerState.On.toString());
        Assert.assertEquals("String matches", "Idle", ExpressionPower.PowerState.Idle.toString());
        Assert.assertEquals("String matches", "On or Off", ExpressionPower.PowerState.OnOrOff.toString());

        Assert.assertTrue("objects are equal", ExpressionPower.PowerState.Off == ExpressionPower.PowerState.get(PowerManager.OFF));
        Assert.assertTrue("objects are equal", ExpressionPower.PowerState.On == ExpressionPower.PowerState.get(PowerManager.ON));
        Assert.assertTrue("objects are equal", ExpressionPower.PowerState.Idle == ExpressionPower.PowerState.get(PowerManager.IDLE));
        Assert.assertTrue("objects are equal", ExpressionPower.PowerState.Unknown == ExpressionPower.PowerState.get(PowerManager.UNKNOWN));
//        Assert.assertTrue("objects are equal", ExpressionPower.PowerState.OnOrOff == ExpressionPower.PowerState.get(-1));

        Assert.assertEquals("ID matches", PowerManager.ON, ExpressionPower.PowerState.On.getID());
        Assert.assertEquals("ID matches", PowerManager.OFF, ExpressionPower.PowerState.Off.getID());
        Assert.assertEquals("ID matches", PowerManager.IDLE, ExpressionPower.PowerState.Idle.getID());
        Assert.assertEquals("ID matches", -1, ExpressionPower.PowerState.OnOrOff.getID());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        Assert.assertTrue("Power".equals(expressionPower.getShortDescription()));
        Assert.assertTrue("Power is On. Ignore unknown state".equals(expressionPower.getLongDescription()));
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionPower.setBeanState(ExpressionPower.PowerState.Off);
        Assert.assertTrue("Power is Off. Ignore unknown state".equals(expressionPower.getLongDescription()));
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        Assert.assertTrue("Power is not Off. Ignore unknown state".equals(expressionPower.getLongDescription()));
        expressionPower.setBeanState(ExpressionPower.PowerState.OnOrOff);
        Assert.assertTrue("Power is not On or Off. Ignore unknown state".equals(expressionPower.getLongDescription()));
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Turn power off
        powerManager.setPower(PowerManager.OFF);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the power on. This should not execute the conditional.
        powerManager.setPower(PowerManager.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Close the switch. This should not execute the conditional.
        powerManager.setPower(PowerManager.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the power on. This should execute the conditional.
        powerManager.setPower(PowerManager.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Turn the power off. This should not execute the conditional.
        powerManager.setPower(PowerManager.OFF);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Test IS_NOT
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Turn the power on. This should not execute the conditional.
        powerManager.setPower(PowerManager.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the power off. This should not execute the conditional.
        powerManager.setPower(PowerManager.OFF);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }

    @Test
    public void testExpression2() throws SocketAlreadyConnectedException, JmriException {
        expressionPower.setBeanState(ExpressionPower.PowerState.Off);
        powerManager.setPower(PowerManager.OFF);
        Assert.assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        Assert.assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.On);
        powerManager.setPower(PowerManager.OFF);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        Assert.assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        Assert.assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.Idle);
        powerManager.setPower(PowerManager.OFF);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        Assert.assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        Assert.assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.OnOrOff);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.Is);
        powerManager.setPower(PowerManager.OFF);
        Assert.assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        Assert.assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        Assert.assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.OnOrOff);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        powerManager.setPower(PowerManager.OFF);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        Assert.assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        Assert.assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        Assert.assertTrue(expressionPower.evaluate());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initLogixNGManager();

        _category = Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket socketIfThenElse =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(socketIfThenElse);

        expressionPower = new ExpressionPower("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionPower);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionPower;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        powerManager = InstanceManager.getDefault(PowerManager.class);
        powerManager.setPower(PowerManager.ON);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
