package jmri.jmrit.logixng.expressions;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("light is not null", light);
        light.setState(Light.ON);
        expressionLightSocket.evaluate();

        expression2 = new LastResultOfDigitalExpression("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Last result of digital expression \"''\"", expression2.getLongDescription());

        expression2 = new LastResultOfDigitalExpression("IQDE321", "My expr");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expr", expression2.getUserName());
        expression2.getSelectNamedBean().setNamedBean(expressionLightSocket);
        Assert.assertEquals("String matches", "Last result of digital expression \"My other expression\"", expression2.getLongDescription());

        expression2 = new LastResultOfDigitalExpression("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(expressionLightSocket);
        Assert.assertEquals(expressionLightSocket, expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());

        boolean thrown = false;
        try {
            // Illegal system name
            new LastResultOfDigitalExpression("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new LastResultOfDigitalExpression("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == lastResultOfDigitalExpression.getChildCount());

        boolean hasThrown = false;
        try {
            lastResultOfDigitalExpression.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testCategory() {
        Assert.assertEquals("Category matches", LogixNG_Category.OTHER, _base.getCategory());
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        Assert.assertEquals("Last result of digital expression", lastResultOfDigitalExpression.getShortDescription());
        Assert.assertEquals("Last result of digital expression \"''\"", lastResultOfDigitalExpression.getLongDescription());
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        Assert.assertEquals("Last result of digital expression \"My other expression\"", lastResultOfDigitalExpression.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Turn light off
        light.setCommandedState(Light.OFF);
        expressionLightSocket.evaluate();
        Assert.assertFalse(lastResultOfDigitalExpression.evaluate());
        // Turn the light on
        light.setCommandedState(Light.ON);
        expressionLightSocket.evaluate();
        Assert.assertTrue(lastResultOfDigitalExpression.evaluate());
        // Turn the light off
        light.setCommandedState(Light.OFF);
        expressionLightSocket.evaluate();
        Assert.assertFalse(lastResultOfDigitalExpression.evaluate());
    }

    @Test
    public void testSetLight() {
        lastResultOfDigitalExpression.unregisterListeners();

        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IL99");
        Assert.assertNotEquals("Lights are different", otherLight, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        Assert.assertEquals("Expressions are equal", expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Expression is null", lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean());
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        Assert.assertEquals("Expressions are equal", expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());
    }

    @Test
    public void testSetExpression() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("light handle is null", lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean());

        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionLightSocket);
        Assert.assertEquals(expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();
        Assert.assertNull(lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean());
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

        Assert.assertNotNull(expressionLightSocket);

        Assert.assertNotNull(expressionOtherLightSocket);
        Assert.assertNotEquals(expressionLightSocket, expressionOtherLightSocket);

        // Test vetoableChange() for some other propery
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));

        // Test vetoableChange() for a string
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));

        // Test vetoableChange() for another light
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", expressionOtherLightSocket, null));
        lastResultOfDigitalExpression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", expressionOtherLightSocket, null));

        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            lastResultOfDigitalExpression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", expressionLightSocket, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals(expressionLightSocket, lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean().getBean());
        lastResultOfDigitalExpression.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", expressionLightSocket, null));
        Assert.assertNull(lastResultOfDigitalExpression.getSelectNamedBean().getNamedBean());
    }

    // The minimal setup for log4J
    @Before
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
