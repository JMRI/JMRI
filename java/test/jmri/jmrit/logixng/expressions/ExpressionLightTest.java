package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
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
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionLight
 *
 * @author Daniel Bergqvist 2018
 */
public class ExpressionLightTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionLight expressionLight;
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
        return String.format("Light IL1 is On ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Light IL1 is On ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionLight(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ExpressionLight expression2;
        assertNotNull( light, "light is not null");
        light.setState(Light.ON);

        expression2 = new ExpressionLight("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Light '' is On", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionLight("IQDE321", "My light");
        assertNotNull( expression2, "object exists");
        assertEquals( "My light", expression2.getUserName(), "Username matches");
        assertEquals( "Light '' is On", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionLight("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(light);
        assertSame( light, expression2.getSelectNamedBean().getNamedBean().getBean(), "light is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Light IL1 is On", expression2.getLongDescription(), "String matches");

        Light l = InstanceManager.getDefault(LightManager.class).provide("IL2");
        expression2 = new ExpressionLight("IQDE321", "My light");
        expression2.getSelectNamedBean().setNamedBean(l);
        assertSame( l, expression2.getSelectNamedBean().getNamedBean().getBean(), "light is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My light", expression2.getUserName(), "Username matches");
        assertEquals( "Light IL2 is On", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionLight("IQE55:12:XY11", null);
            fail("Did not throw, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionLight("IQE55:12:XY11", "A name");
            fail("Did not throw, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionLight.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionLight.getChild(0),"Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testLightState() {
        assertEquals( "Off", ExpressionLight.LightState.Off.toString(), "String matches");
        assertEquals( "On", ExpressionLight.LightState.On.toString(), "String matches");
        assertEquals( "Other", ExpressionLight.LightState.Other.toString(), "String matches");

        assertSame( ExpressionLight.LightState.Off, ExpressionLight.LightState.get(Light.OFF), "objects are equal");
        assertSame( ExpressionLight.LightState.On, ExpressionLight.LightState.get(Light.ON), "objects are equal");
        assertSame( ExpressionLight.LightState.Other, ExpressionLight.LightState.get(Light.UNKNOWN), "objects are equal");
        assertSame( ExpressionLight.LightState.Other, ExpressionLight.LightState.get(Light.INCONSISTENT), "objects are equal");
        assertSame( ExpressionLight.LightState.Other, ExpressionLight.LightState.get(-1), "objects are equal");

        assertEquals( Light.ON, ExpressionLight.LightState.On.getID(), "ID matches");
        assertEquals( Light.OFF, ExpressionLight.LightState.Off.getID(), "ID matches");
        assertEquals( -1, ExpressionLight.LightState.Other.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionLight.getSelectNamedBean().removeNamedBean();
        assertEquals( "Light", expressionLight.getShortDescription());
        assertEquals( "Light '' is On", expressionLight.getLongDescription());
        expressionLight.getSelectNamedBean().setNamedBean(light);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionLight.setBeanState(ExpressionLight.LightState.Off);
        assertEquals( "Light IL1 is Off", expressionLight.getLongDescription());
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals( "Light IL1 is not Off", expressionLight.getLongDescription());
        expressionLight.setBeanState(ExpressionLight.LightState.Other);
        assertEquals( "Light IL1 is not Other", expressionLight.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Turn light off
        light.setCommandedState(Light.OFF);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the light on. This should not execute the conditional.
        light.setCommandedState(Light.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Close the switch. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the light on. This should execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Turn the light off. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Turn the light on. This should not execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the light off. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testSetLight() {
        expressionLight.unregisterListeners();

        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IL99");
        assertNotEquals( otherLight, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Lights are different");
        expressionLight.getSelectNamedBean().setNamedBean(otherLight);
        assertEquals( otherLight, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Lights are equal");

        NamedBeanHandle<Light> otherLightHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherLight.getDisplayName(), otherLight);
        expressionLight.getSelectNamedBean().removeNamedBean();
        assertNull( expressionLight.getSelectNamedBean().getNamedBean(), "Light is null");
        expressionLight.getSelectNamedBean().setNamedBean(otherLightHandle);
        assertEquals( otherLight, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Lights are equal");
        assertEquals( otherLightHandle, expressionLight.getSelectNamedBean().getNamedBean(), "LightHandles are equal");
    }

    @Test
    public void testSetLight2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        Light light11 = InstanceManager.getDefault(LightManager.class).provide("IL11");
        Light light12 = InstanceManager.getDefault(LightManager.class).provide("IL12");
        NamedBeanHandle<Light> lightHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(light12.getDisplayName(), light12);
        Light light13 = InstanceManager.getDefault(LightManager.class).provide("IL13");
        Light light14 = InstanceManager.getDefault(LightManager.class).provide("IL14");
        light14.setUserName("Some user name");

        expressionLight.getSelectNamedBean().removeNamedBean();
        assertNull( expressionLight.getSelectNamedBean().getNamedBean(), "light handle is null");

        expressionLight.getSelectNamedBean().setNamedBean(light11);
        assertSame( light11, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "light is correct");

        expressionLight.getSelectNamedBean().removeNamedBean();
        assertNull( expressionLight.getSelectNamedBean().getNamedBean(), "light handle is null");

        expressionLight.getSelectNamedBean().setNamedBean(lightHandle12);
        assertSame( lightHandle12, expressionLight.getSelectNamedBean().getNamedBean(), "light handle is correct");

        expressionLight.getSelectNamedBean().setNamedBean("A non existent light");
        assertNull( expressionLight.getSelectNamedBean().getNamedBean(), "light handle is null");
        JUnitAppender.assertErrorMessage("Light \"A non existent light\" is not found");

        expressionLight.getSelectNamedBean().setNamedBean(light13.getSystemName());
        assertSame( light13, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "light is correct");

        String userName = light14.getUserName();
        assertNotNull( userName, "light is not null");
        expressionLight.getSelectNamedBean().setNamedBean(userName);
        assertSame( light14, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "light is correct");
    }

    @Test
    public void testSetLightException() {
        // Test setLight() when listeners are registered
        assertNotNull( light, "Light is not null");
        assertNotNull( expressionLight.getSelectNamedBean().getNamedBean(), "Light is not null");
        expressionLight.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionLight.getSelectNamedBean().setNamedBean("A light"),
                "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Light light99 = InstanceManager.getDefault(LightManager.class).provide("IL99");
            NamedBeanHandle<Light> lightHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(light99.getDisplayName(), light99);
            expressionLight.getSelectNamedBean().setNamedBean(lightHandle99);
        }, "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionLight has no light
        conditionalNG.setEnabled(false);
        expressionLight.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expression and set the light
        assertNotNull( light, "Light is not null");

        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        assertNotNull( otherLight, "Light is not null");
        assertNotEquals( light, otherLight, "Light is not equal");

        // Test vetoableChange() for some other propery
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( light, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");

        // Test vetoableChange() for a string
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( light, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( light, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");

        // Test vetoableChange() for another light
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        assertEquals( light, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        assertEquals( light, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");

        // Test vetoableChange() for its own light
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionLight.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( light, expressionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");
        expressionLight.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        assertNull( expressionLight.getSelectNamedBean().getNamedBean(), "Light is null");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalLightManager();
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

        expressionLight = new ExpressionLight("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionLight;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        expressionLight.getSelectNamedBean().setNamedBean(light);
        light.setCommandedState(Light.ON);

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
