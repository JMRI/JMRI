package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionLight
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionLightTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionLight actionLight;
    private Light light;


    @Test
    public void testLightState() {
        assertEquals( "Off", ActionLight.LightState.Off.toString(), "String matches");
        assertEquals( "On", ActionLight.LightState.On.toString(), "String matches");
        assertEquals( "Toggle", ActionLight.LightState.Toggle.toString(), "String matches");

        assertSame( ActionLight.LightState.Off, ActionLight.LightState.get(Light.OFF), "objects are equal");
        assertSame( ActionLight.LightState.On, ActionLight.LightState.get(Light.ON), "objects are equal");
        assertSame( ActionLight.LightState.Toggle, ActionLight.LightState.get(-1), "objects are equal");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            ActionLight.LightState.get(100),    // The number 100 is a state that ActionLight.LightState isn't aware of
                "Exception is thrown");
        assertEquals( "invalid light state", ex.getMessage(), "Error message is correct");
    }

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
        return String.format("Set light IL1 to state On ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set light IL1 to state On ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionLight(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( _base, "object exists");

        ActionLight action2;
        assertNotNull( light, "light is not null");
        light.setState(Light.ON);

        action2 = new ActionLight("IQDA321", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set light '' to state On", action2.getLongDescription(), "String matches");

        action2 = new ActionLight("IQDA321", "My light");
        assertNotNull( action2, "object exists");
        assertEquals( "My light", action2.getUserName(), "Username matches");
        assertEquals( "Set light '' to state On", action2.getLongDescription(), "String matches");

        action2 = new ActionLight("IQDA321", null);
        action2.getSelectNamedBean().setNamedBean(light);
        assertSame( light, action2.getSelectNamedBean().getNamedBean().getBean(), "light is correct");
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set light IL1 to state On", action2.getLongDescription(), "String matches");

        Light l = InstanceManager.getDefault(LightManager.class).provide("IL1");
        action2 = new ActionLight("IQDA321", "My light");
        action2.getSelectNamedBean().setNamedBean(l);
        assertSame( l, action2.getSelectNamedBean().getNamedBean().getBean(), "light is correct");
        assertNotNull( action2, "object exists");
        assertEquals( "My light", action2.getUserName(), "Username matches");
        assertEquals( "Set light IL1 to state On", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionLight("IQA55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionLight("IQA55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionLight.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionLight.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testSetLight() {
        Light light11 = InstanceManager.getDefault(LightManager.class).provide("IL11");
        Light light12 = InstanceManager.getDefault(LightManager.class).provide("IL12");
        NamedBeanHandle<Light> lightHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(light12.getDisplayName(), light12);
        Light light13 = InstanceManager.getDefault(LightManager.class).provide("IL13");
        Light light14 = InstanceManager.getDefault(LightManager.class).provide("IL14");
        light14.setUserName("Some user name");

        actionLight.getSelectNamedBean().removeNamedBean();
        assertNull( actionLight.getSelectNamedBean().getNamedBean(), "light handle is null");

        actionLight.getSelectNamedBean().setNamedBean(light11);
        assertSame( light11, actionLight.getSelectNamedBean().getNamedBean().getBean(), "light is correct");

        actionLight.getSelectNamedBean().removeNamedBean();
        assertNull( actionLight.getSelectNamedBean().getNamedBean(), "light handle is null");

        actionLight.getSelectNamedBean().setNamedBean(lightHandle12);
        assertSame( lightHandle12, actionLight.getSelectNamedBean().getNamedBean(), "light handle is correct");

        actionLight.getSelectNamedBean().setNamedBean("A non existent light");
        assertNull( actionLight.getSelectNamedBean().getNamedBean(), "light handle is null");
        JUnitAppender.assertErrorMessage("Light \"A non existent light\" is not found");

        actionLight.getSelectNamedBean().setNamedBean(light13.getSystemName());
        assertSame( light13, actionLight.getSelectNamedBean().getNamedBean().getBean(), "light is correct");

        String l14UserName = light14.getUserName();
        assertNotNull(l14UserName);
        actionLight.getSelectNamedBean().setNamedBean(l14UserName);
        assertSame( light14, actionLight.getSelectNamedBean().getNamedBean().getBean(), "light is correct");
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {

        // Set the light
        light.setCommandedState(Light.OFF);
        // The light should be off
        assertEquals( Light.OFF, light.getCommandedState(), "light is off");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        assertEquals( Light.ON, light.getCommandedState(), "light is on");

        // Test to set light to off
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Off);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be off
        assertEquals( Light.OFF, light.getCommandedState(), "light is off");

        // Test to set light to toggle
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        assertEquals( Light.ON, light.getCommandedState(), "light is on");

        // Test to set light to toggle
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be off
        assertEquals( Light.OFF, light.getCommandedState(), "light is off");
    }

    @Test
    public void testIndirectAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IL102");

        assertTrue(conditionalNG.isActive());
        Light t1 = InstanceManager.getDefault(LightManager.class).provide("IL101");
        Light t2 = InstanceManager.getDefault(LightManager.class).provide("IL102");
        Light t3 = InstanceManager.getDefault(LightManager.class).provide("IL103");
        Light t4 = InstanceManager.getDefault(LightManager.class).provide("IL104");
        Light t5 = InstanceManager.getDefault(LightManager.class).provide("IL105");

        actionLight.getSelectEnum().setEnum(ActionLight.LightState.On);
        actionLight.getSelectNamedBean().setNamedBean(t1.getSystemName());
        actionLight.getSelectNamedBean().setReference("{IM1}");    // Points to "IL102"
        actionLight.getSelectNamedBean().setLocalVariable("myLight");
        actionLight.getSelectNamedBean().setFormula("\"IL10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refLight", SymbolTable.InitialValueType.String, "IL103");
        _baseMaleSocket.addLocalVariable("myLight", SymbolTable.InitialValueType.String, "IL104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");

        // Test direct addressing
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        t1.setState(Light.OFF);
        t2.setState(Light.OFF);
        t3.setState(Light.OFF);
        t4.setState(Light.OFF);
        t5.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.ON, t1.getCommandedState());
        assertEquals(Light.OFF, t2.getCommandedState());
        assertEquals(Light.OFF, t3.getCommandedState());
        assertEquals(Light.OFF, t4.getCommandedState());
        assertEquals(Light.OFF, t5.getCommandedState());

        // Test reference by memory addressing
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Light.OFF);
        t2.setState(Light.OFF);
        t3.setState(Light.OFF);
        t4.setState(Light.OFF);
        t5.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, t1.getCommandedState());
        assertEquals(Light.ON, t2.getCommandedState());
        assertEquals(Light.OFF, t3.getCommandedState());
        assertEquals(Light.OFF, t4.getCommandedState());
        assertEquals(Light.OFF, t5.getCommandedState());

        // Test reference by local variable addressing
        actionLight.getSelectNamedBean().setReference("{refLight}");    // Points to "IL103"
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Light.OFF);
        t2.setState(Light.OFF);
        t3.setState(Light.OFF);
        t4.setState(Light.OFF);
        t5.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, t1.getCommandedState());
        assertEquals(Light.OFF, t2.getCommandedState());
        assertEquals(Light.ON, t3.getCommandedState());
        assertEquals(Light.OFF, t4.getCommandedState());
        assertEquals(Light.OFF, t5.getCommandedState());

        // Test local variable addressing
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setState(Light.OFF);
        t2.setState(Light.OFF);
        t3.setState(Light.OFF);
        t4.setState(Light.OFF);
        t5.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, t1.getCommandedState());
        assertEquals(Light.OFF, t2.getCommandedState());
        assertEquals(Light.OFF, t3.getCommandedState());
        assertEquals(Light.ON, t4.getCommandedState());
        assertEquals(Light.OFF, t5.getCommandedState());

        // Test formula addressing
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        t1.setState(Light.OFF);
        t2.setState(Light.OFF);
        t3.setState(Light.OFF);
        t4.setState(Light.OFF);
        t5.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, t1.getCommandedState());
        assertEquals(Light.OFF, t2.getCommandedState());
        assertEquals(Light.OFF, t3.getCommandedState());
        assertEquals(Light.OFF, t4.getCommandedState());
        assertEquals(Light.ON, t5.getCommandedState());
    }

    @Test
    public void testIndirectStateAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IL102");

        assertTrue(conditionalNG.isActive());


        // Test direct addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        // Test Off
        light.setState(Light.ON);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Off);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        light.setState(Light.OFF);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.On);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.ON, light.getCommandedState());


        // Test reference by memory addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionLight.getSelectEnum().setReference("{IM1}");
        // Test Off
        m1.setValue("Off");
        light.setState(Light.ON);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        m1.setValue("On");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.ON, light.getCommandedState());


        // Test reference by local variable addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionLight.getSelectEnum().setReference("{refVariable}");
        // Test Off
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Off");
        light.setState(Light.ON);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "On");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.ON, light.getCommandedState());


        // Test local variable addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionLight.getSelectEnum().setLocalVariable("myVariable");
        // Test Off
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Off");
        light.setState(Light.ON);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "On");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.ON, light.getCommandedState());


        // Test formula addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionLight.getSelectEnum().setFormula("refVariable + myVariable");
        // Test Off
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "O");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ff");
        light.setState(Light.ON);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "O");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "n");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        assertEquals(Light.ON, light.getCommandedState());
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        assertNotNull( light, "Light is not null");

        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        assertNotNull( otherLight, "Light is not null");
        assertNotEquals( light, otherLight, "Light is not equal");

        // Test vetoableChange() for some other propery
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( light, actionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");

        // Test vetoableChange() for a string
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( light, actionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( light, actionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");

        // Test vetoableChange() for another light
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        assertEquals( light, actionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        assertEquals( light, actionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");

        // Test vetoableChange() for its own light
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            actionLight.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( light, actionLight.getSelectNamedBean().getNamedBean().getBean(), "Light matches");
        actionLight.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        assertNull( actionLight.getSelectNamedBean().getNamedBean(), "Light is null");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Light", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Set light IL1 to state On", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initTimeProviderManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionLight = new ActionLight(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionLight.getSelectNamedBean().setNamedBean(light);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.On);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
        conditionalNG.getChild(0).connect(socket);

        _base = actionLight;
        _baseMaleSocket = socket;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
