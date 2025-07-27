package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals("String matches", "Off", ActionLight.LightState.Off.toString());
        Assert.assertEquals("String matches", "On", ActionLight.LightState.On.toString());
        Assert.assertEquals("String matches", "Toggle", ActionLight.LightState.Toggle.toString());

        Assert.assertTrue("objects are equal", ActionLight.LightState.Off == ActionLight.LightState.get(Light.OFF));
        Assert.assertTrue("objects are equal", ActionLight.LightState.On == ActionLight.LightState.get(Light.ON));
        Assert.assertTrue("objects are equal", ActionLight.LightState.Toggle == ActionLight.LightState.get(-1));

        boolean hasThrown = false;
        try {
            ActionLight.LightState.get(100);    // The number 100 is a state that ActionLight.LightState isn't aware of
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "invalid light state".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
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
        Assert.assertTrue("object exists", _base != null);

        ActionLight action2;
        Assert.assertNotNull("light is not null", light);
        light.setState(Light.ON);

        action2 = new ActionLight("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set light '' to state On", action2.getLongDescription());

        action2 = new ActionLight("IQDA321", "My light");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My light", action2.getUserName());
        Assert.assertEquals("String matches", "Set light '' to state On", action2.getLongDescription());

        action2 = new ActionLight("IQDA321", null);
        action2.getSelectNamedBean().setNamedBean(light);
        Assert.assertTrue("light is correct", light == action2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set light IL1 to state On", action2.getLongDescription());

        Light l = InstanceManager.getDefault(LightManager.class).provide("IL1");
        action2 = new ActionLight("IQDA321", "My light");
        action2.getSelectNamedBean().setNamedBean(l);
        Assert.assertTrue("light is correct", l == action2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My light", action2.getUserName());
        Assert.assertEquals("String matches", "Set light IL1 to state On", action2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ActionLight("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ActionLight("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionLight.getChildCount());

        boolean hasThrown = false;
        try {
            actionLight.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
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
        Assert.assertNull("light handle is null", actionLight.getSelectNamedBean().getNamedBean());

        actionLight.getSelectNamedBean().setNamedBean(light11);
        Assert.assertTrue("light is correct", light11 == actionLight.getSelectNamedBean().getNamedBean().getBean());

        actionLight.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("light handle is null", actionLight.getSelectNamedBean().getNamedBean());

        actionLight.getSelectNamedBean().setNamedBean(lightHandle12);
        Assert.assertTrue("light handle is correct", lightHandle12 == actionLight.getSelectNamedBean().getNamedBean());

        actionLight.getSelectNamedBean().setNamedBean("A non existent light");
        Assert.assertNull("light handle is null", actionLight.getSelectNamedBean().getNamedBean());
        JUnitAppender.assertErrorMessage("Light \"A non existent light\" is not found");

        actionLight.getSelectNamedBean().setNamedBean(light13.getSystemName());
        Assert.assertTrue("light is correct", light13 == actionLight.getSelectNamedBean().getNamedBean().getBean());

        actionLight.getSelectNamedBean().setNamedBean(light14.getUserName());
        Assert.assertTrue("light is correct", light14 == actionLight.getSelectNamedBean().getNamedBean().getBean());
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {

        // Set the light
        light.setCommandedState(Light.OFF);
        // The light should be off
        Assert.assertTrue("light is off",light.getCommandedState() == Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.ON);

        // Test to set light to off
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Off);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.OFF);

        // Test to set light to toggle
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.ON);

        // Test to set light to toggle
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.OFF);
    }

    @Test
    public void testIndirectAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IL102");

        Assert.assertTrue(conditionalNG.isActive());
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
        Assert.assertEquals(Light.ON, t1.getCommandedState());
        Assert.assertEquals(Light.OFF, t2.getCommandedState());
        Assert.assertEquals(Light.OFF, t3.getCommandedState());
        Assert.assertEquals(Light.OFF, t4.getCommandedState());
        Assert.assertEquals(Light.OFF, t5.getCommandedState());

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
        Assert.assertEquals(Light.OFF, t1.getCommandedState());
        Assert.assertEquals(Light.ON, t2.getCommandedState());
        Assert.assertEquals(Light.OFF, t3.getCommandedState());
        Assert.assertEquals(Light.OFF, t4.getCommandedState());
        Assert.assertEquals(Light.OFF, t5.getCommandedState());

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
        Assert.assertEquals(Light.OFF, t1.getCommandedState());
        Assert.assertEquals(Light.OFF, t2.getCommandedState());
        Assert.assertEquals(Light.ON, t3.getCommandedState());
        Assert.assertEquals(Light.OFF, t4.getCommandedState());
        Assert.assertEquals(Light.OFF, t5.getCommandedState());

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
        Assert.assertEquals(Light.OFF, t1.getCommandedState());
        Assert.assertEquals(Light.OFF, t2.getCommandedState());
        Assert.assertEquals(Light.OFF, t3.getCommandedState());
        Assert.assertEquals(Light.ON, t4.getCommandedState());
        Assert.assertEquals(Light.OFF, t5.getCommandedState());

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
        Assert.assertEquals(Light.OFF, t1.getCommandedState());
        Assert.assertEquals(Light.OFF, t2.getCommandedState());
        Assert.assertEquals(Light.OFF, t3.getCommandedState());
        Assert.assertEquals(Light.OFF, t4.getCommandedState());
        Assert.assertEquals(Light.ON, t5.getCommandedState());
    }

    @Test
    public void testIndirectStateAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IL102");

        Assert.assertTrue(conditionalNG.isActive());


        // Test direct addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        // Test Off
        light.setState(Light.ON);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Off);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        light.setState(Light.OFF);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.On);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.ON, light.getCommandedState());


        // Test reference by memory addressing
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionLight.getSelectEnum().setReference("{IM1}");
        // Test Off
        m1.setValue("Off");
        light.setState(Light.ON);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        m1.setValue("On");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.ON, light.getCommandedState());


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
        Assert.assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "On");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.ON, light.getCommandedState());


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
        Assert.assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "On");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.ON, light.getCommandedState());


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
        Assert.assertEquals(Light.OFF, light.getCommandedState());
        // Test On
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "O");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "n");
        light.setState(Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct light should be thrown
        Assert.assertEquals(Light.ON, light.getCommandedState());
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        Assert.assertNotNull("Light is not null", light);

        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);

        // Test vetoableChange() for some other propery
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Light matches", light, actionLight.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for a string
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Light matches", light, actionLight.getSelectNamedBean().getNamedBean().getBean());
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Light matches", light, actionLight.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for another light
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, actionLight.getSelectNamedBean().getNamedBean().getBean());
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, actionLight.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            actionLight.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Light matches", light, actionLight.getSelectNamedBean().getNamedBean().getBean());
        actionLight.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        Assert.assertNull("Light is null", actionLight.getSelectNamedBean().getNamedBean());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.ITEM == _base.getCategory());
    }

    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Light", _base.getShortDescription());
    }

    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set light IL1 to state On", _base.getLongDescription());
    }

    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasThrown = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
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

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
