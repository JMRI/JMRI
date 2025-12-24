package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.LogixNG_Category;
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
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Power is On. Ignore unknown state", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionPower("IQDE321", "My power");
        assertNotNull( expression2, "object exists");
        assertEquals( "My power", expression2.getUserName(), "Username matches");
        assertEquals( "Power is On. Ignore unknown state", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionPower("IQE55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionPower("IQE55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionPower.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionPower.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testPowerState() {
        assertEquals( "Off", ExpressionPower.PowerState.Off.toString(), "String matches");
        assertEquals( "On", ExpressionPower.PowerState.On.toString(), "String matches");
        assertEquals( "Idle", ExpressionPower.PowerState.Idle.toString(), "String matches");
        assertEquals( "On or Off", ExpressionPower.PowerState.OnOrOff.toString(), "String matches");

        assertSame( ExpressionPower.PowerState.Off, ExpressionPower.PowerState.get(PowerManager.OFF), "objects are equal");
        assertSame( ExpressionPower.PowerState.On, ExpressionPower.PowerState.get(PowerManager.ON), "objects are equal");
        assertSame( ExpressionPower.PowerState.Idle, ExpressionPower.PowerState.get(PowerManager.IDLE), "objects are equal");
        assertSame( ExpressionPower.PowerState.Unknown, ExpressionPower.PowerState.get(PowerManager.UNKNOWN), "objects are equal");
//        Assert.assertTrue("objects are equal", ExpressionPower.PowerState.OnOrOff == ExpressionPower.PowerState.get(-1));

        assertEquals( PowerManager.ON, ExpressionPower.PowerState.On.getID(), "ID matches");
        assertEquals( PowerManager.OFF, ExpressionPower.PowerState.Off.getID(), "ID matches");
        assertEquals( PowerManager.IDLE, ExpressionPower.PowerState.Idle.getID(), "ID matches");
        assertEquals( -1, ExpressionPower.PowerState.OnOrOff.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        assertEquals( "Power", expressionPower.getShortDescription());
        assertEquals( "Power is On. Ignore unknown state", expressionPower.getLongDescription());
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionPower.setBeanState(ExpressionPower.PowerState.Off);
        assertEquals("Power is Off. Ignore unknown state", expressionPower.getLongDescription());
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals( "Power is not Off. Ignore unknown state", expressionPower.getLongDescription());
        expressionPower.setBeanState(ExpressionPower.PowerState.OnOrOff);
        assertEquals( "Power is not On or Off. Ignore unknown state", expressionPower.getLongDescription());
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
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the power on. This should not execute the conditional.
        powerManager.setPower(PowerManager.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Close the switch. This should not execute the conditional.
        powerManager.setPower(PowerManager.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the power on. This should execute the conditional.
        powerManager.setPower(PowerManager.ON);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Turn the power off. This should not execute the conditional.
        powerManager.setPower(PowerManager.OFF);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Turn the power on. This should not execute the conditional.
        powerManager.setPower(PowerManager.ON);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the power off. This should not execute the conditional.
        powerManager.setPower(PowerManager.OFF);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testExpression2() throws SocketAlreadyConnectedException, JmriException {
        expressionPower.setBeanState(ExpressionPower.PowerState.Off);
        powerManager.setPower(PowerManager.OFF);
        assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.On);
        powerManager.setPower(PowerManager.OFF);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.Idle);
        powerManager.setPower(PowerManager.OFF);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.OnOrOff);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.Is);
        powerManager.setPower(PowerManager.OFF);
        assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        assertFalse(expressionPower.evaluate());

        expressionPower.setBeanState(ExpressionPower.PowerState.OnOrOff);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        powerManager.setPower(PowerManager.OFF);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.ON);
        assertFalse(expressionPower.evaluate());
        powerManager.setPower(PowerManager.IDLE);
        assertTrue(expressionPower.evaluate());
        powerManager.setPower(PowerManager.UNKNOWN);
        assertTrue(expressionPower.evaluate());
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
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

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
