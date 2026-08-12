package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test LastResultOfDigitalExpression
 *
 * @author Daniel Bergqvist 2022
 */
public class LastResultOfDigitalExpressionTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private MaleDigitalExpressionSocket expressionLightSocket;
    private MaleDigitalExpressionSocket expressionOtherLightSocket;
    private LastResultOfDigitalExpression lastResultOfDigitalExpression;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Light light;


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
        return String.format("Last result of digital expression \"My other expression\" ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Or. Evaluate All ::: Use default%n" +
                "                  ? E1%n" +
                "                     Light IL1 is On ::: User Name: My other expression ::: Use default%n" +
                "                  ? E2%n" +
                "                     Last result of digital expression \"My other expression\" ::: Use default%n" +
                "                  ? E3%n" +
                "                     Socket not connected%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new LastResultOfDigitalExpression(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        LastResultOfDigitalExpression expression2;
        assertNotNull( light, "light is not null");
        light.setState(Light.ON);
        expressionLightSocket.evaluate();

        expression2 = new LastResultOfDigitalExpression("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Last result of digital expression \"''\"", expression2.getLongDescription(),
                "String matches");

        expression2 = new LastResultOfDigitalExpression("IQDE321", "My expr");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expr", expression2.getUserName(), "Username matches");
        expression2.getSelectNamedBean().setNamedBean(expressionLightSocket);
        assertEquals( "Last result of digital expression \"My other expression\"",
                expression2.getLongDescription(), "String matches");

        expression2 = new LastResultOfDigitalExpression("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(expressionLightSocket);
        assertEquals(expressionLightSocket, expression2.getSelectNamedBean().getNamedBean().getBean());
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new LastResultOfDigitalExpression("IQE55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new LastResultOfDigitalExpression("IQE55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, lastResultOfDigitalExpression.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            lastResultOfDigitalExpression.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        assertEquals("Last result of digital expression", lastResultOfDigitalExpression.getShortDescription());
        assertEquals("Last result of digital expression \"''\"", lastResultOfDigitalExpression.getLongDescription());
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        assertEquals("Last result of digital expression \"My other expression\"", lastResultOfDigitalExpression.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Turn light off
        light.setCommandedState(Light.OFF);
        expressionLightSocket.evaluate();
        assertFalse(lastResultOfDigitalExpression.evaluate());
        // Turn the light on
        light.setCommandedState(Light.ON);
        expressionLightSocket.evaluate();
        assertTrue(lastResultOfDigitalExpression.evaluate());
        // Turn the light off
        light.setCommandedState(Light.OFF);
        expressionLightSocket.evaluate();
        assertFalse(lastResultOfDigitalExpression.evaluate());
    }

    @Test
    public void testSetLight() {
        lastResultOfDigitalExpression.unregisterListeners();

        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IL99");
        assertNotEquals( otherLight, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean(),
                "Lights are different");
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        assertEquals( expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean(),
                "Expressions are equal");

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        assertNull( lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean(), "Expression is null");
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        assertEquals( expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean(),
                "Expressions are equal");
    }

    @Test
    public void testSetExpression() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        assertNull( lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean(),
                "light handle is null");

        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        assertEquals(expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        assertNull(lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean());
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the LastResultOfDigitalExpression has no light
        conditionalNG.setEnabled(false);
        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        assertNotNull(expressionLightSocket);

        assertNotNull(expressionOtherLightSocket);
        assertNotEquals(expressionLightSocket, expressionOtherLightSocket);

        // Test vetoableChange() for some other propery
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));

        // Test vetoableChange() for a string
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));

        // Test vetoableChange() for another light
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", expressionOtherLightSocket, null));
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", expressionOtherLightSocket, null));

        // Test vetoableChange() for its own light
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            lastResultOfDigitalExpression.getSelectNamedBean().vetoableChange(
                new PropertyChangeEvent(this, "CanDelete", expressionLightSocket, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals(expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());
        lastResultOfDigitalExpression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", expressionLightSocket, null));
        assertNull(lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean());
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        light = InstanceManager.getDefault(LightManager.class).provide("IL1");

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

        Or or = new Or("IQDE99", null);
        MaleSocket orSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(or);
        ifThenElse.getChild(0).connect(orSocket);

        ExpressionLight expressionLight = new ExpressionLight("IQDE9999", "My other expression");
        expressionLightSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight);
        expressionLight.getSelectNamedBean().setNamedBean(light);
        or.getChild(0).connect(expressionLightSocket);

        ExpressionLight expressionOtherLight = new ExpressionLight("IQDE999999", "My other other expression");
        expressionOtherLightSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionOtherLight);

        lastResultOfDigitalExpression = new LastResultOfDigitalExpression("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(lastResultOfDigitalExpression);
        or.getChild(1).connect(maleSocket2);

        _base = lastResultOfDigitalExpression;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        light.setCommandedState(Light.ON);
        expressionLightSocket.evaluate();

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
