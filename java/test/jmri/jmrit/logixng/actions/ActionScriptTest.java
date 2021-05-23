package jmri.jmrit.logixng.actions;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.True;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionSimpleScript
 * 
 * @author Daniel Bergqvist 2021
 */
public class ActionScriptTest extends AbstractDigitalActionTestBase {

    private final String _scriptText = "lights.provideLight(\"IL1\").commandedState = ON";
    
    
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private IfThenElse ifThenElse;
    private ActionScript actionScript;
    private Sensor sensor;
    
    
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
        AnalogActionBean childAction = new AnalogActionMemory("IQAA999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(childAction);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Execute simple script: Jython command. Script lights.provideLight(\"IL1\").commandedState = ON ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Always execute ::: Use default%n" +
                "            ? If%n" +
                "               Sensor IS1 is Active ::: Use default%n" +
                "            ! Then%n" +
                "               Execute simple script: Jython command. Script lights.provideLight(\"IL1\").commandedState = ON ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionScript(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        ActionScript action2;
        
        action2 = new ActionScript("IQDA321", null);
        action2.setScript(_scriptText);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Execute simple script: Jython command. Script lights.provideLight(\"IL1\").commandedState = ON", action2.getLongDescription());
        
        action2 = new ActionScript("IQDA321", "My action");
        action2.setScript(_scriptText);
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My action", action2.getUserName());
        Assert.assertEquals("String matches", "Execute simple script: Jython command. Script lights.provideLight(\"IL1\").commandedState = ON", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionScript("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionScript("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Test without script
        actionScript.setScript(null);
        Assert.assertTrue("getChildCount() returns 0", 0 == actionScript.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionScript.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        // Test with script
        actionScript.setScript(_scriptText);
        Assert.assertTrue("getChildCount() returns 0", 0 == actionScript.getChildCount());
    }
    
    @Test
    public void testDescription() {
        Assert.assertEquals("Script", actionScript.getShortDescription());
        Assert.assertEquals("Execute simple script: Jython command. Script lights.provideLight(\"IL1\").commandedState = ON", actionScript.getLongDescription());
    }
    
    @Test
    public void testAction_JythonCommand() throws Exception {
        // Test action
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        
        // The action is not yet executed so the light should be off
        Assert.assertTrue("light is off", light.getState() == Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on", light.getState() == Light.ON);
        
        
        // Test action when triggered because the script is listening on the sensor IS2
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        light.setCommandedState(Light.OFF);
        
        logixNG.unregisterListeners();
        
        // Disconnect the expressionSensor and replace it with a True expression
        // since we always want the result "true" for this test.
        ifThenElse.getChild(0).disconnect();
        True expressionTrue = new True("IQDE322", null);
        MaleSocket maleSocketTrue =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTrue);
        ifThenElse.getChild(0).connect(maleSocketTrue);
        
//        actionScript.setScript(_scriptText);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        // Activate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        // Inactivate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.INACTIVE);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Activate the sensor. This should execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertEquals("light is on",Light.ON,light.getState());
        
        // Unregister listeners
        actionScript.unregisterListeners();
        light.setState(Light.OFF);
        // Turn the light off.
        light.setCommandedState(Light.OFF);
        // Activate the sensor. This not should execute the conditional since listerners are not registered.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // Listerners are not registered so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        
        // Test execute() without script. This shouldn't do anything but we
        // do it for coverage.
        actionScript.setScript("");
        actionScript.execute();
    }
    
    @Test
    public void testAction_RunScript() throws Exception {
        
        actionScript.setOperationType(ActionScript.OperationType.RunScript);
        actionScript.setScript("java/test/jmri/jmrit/logixng/actions/ActionScriptTest.py");
        
        // Test action
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL9");
        light.setCommandedState(Light.OFF);
        
        // The action is not yet executed so the light should be off
        Assert.assertTrue("light is off", light.getState() == Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on", light.getState() == Light.ON);
        
        
        // Test action when triggered because the script is listening on the sensor IS2
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        light.setCommandedState(Light.OFF);
        
        logixNG.unregisterListeners();
        
        // Disconnect the expressionSensor and replace it with a True expression
        // since we always want the result "true" for this test.
        ifThenElse.getChild(0).disconnect();
        True expressionTrue = new True("IQDE322", null);
        MaleSocket maleSocketTrue =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTrue);
        ifThenElse.getChild(0).connect(maleSocketTrue);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        // Activate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        // Inactivate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.INACTIVE);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Activate the sensor. This should execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertEquals("light is on",Light.ON,light.getState());
        
        // Unregister listeners
        actionScript.unregisterListeners();
        light.setState(Light.OFF);
        // Turn the light off.
        light.setCommandedState(Light.OFF);
        // Activate the sensor. This not should execute the conditional since listerners are not registered.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // Listerners are not registered so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        
        // Test execute() without script. This shouldn't do anything but we
        // do it for coverage.
        actionScript.setScript("");
        actionScript.execute();
    }
    
    @Test
    public void testSetScript() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", _scriptText);
        actionScript.setScript(_scriptText);
        Assert.assertNotNull("Script is not null", actionScript.getScript());
        
        // Test bad script
        actionScript.setScript("This is a bad script");
        Assert.assertEquals("This is a bad script", actionScript.getScript());
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
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        
        ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setType(IfThenElse.Type.AlwaysExecute);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        
        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        expressionSensor.setSensor(sensor);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        actionScript = new ActionScript(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionScript.setScript(_scriptText);
        MaleSocket socketActionSimpleScript = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionScript);
        ifThenElse.getChild(1).connect(socketActionSimpleScript);
        
        _base = actionScript;
        _baseMaleSocket = socketActionSimpleScript;
        
        if (! logixNG.setParentForAllChildren(new ArrayList())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
