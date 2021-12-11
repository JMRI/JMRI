package jmri.jmrit.logixng.expressions;

import java.util.ArrayList;

import jmri.jmrit.logixng.actions.*;
import jmri.InstanceManager;
import jmri.*;
import jmri.jmrit.logixng.*;
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
public class ExpressionScriptTest extends AbstractDigitalExpressionTestBase {

    private final String _scriptText = "result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )";
    
    
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private IfThenElse ifThenElse;
    private ExpressionScript expressionScript;
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
                "Evaluate script: Jython command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE ) ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Always execute ::: Use default%n" +
                "            ? If%n" +
                "               Evaluate script: Jython command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE ) ::: Use default%n" +
                "            ! Then%n" +
                "               Set light IL1 to state On ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionScript(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        ExpressionScript expression2;
        
        expression2 = new ExpressionScript("IQDE321", null);
        expression2.setScript(_scriptText);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Evaluate script: Jython command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )", expression2.getLongDescription());
        
        expression2 = new ExpressionScript("IQDE321", "My expression");
        expression2.setScript(_scriptText);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "Evaluate script: Jython command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionScript("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionScript("IQA55:12:XY11", "A name");
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
        expressionScript.setScript(null);
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionScript.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionScript.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
        
        // Test with script
        expressionScript.setScript(_scriptText);
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionScript.getChildCount());
    }
    
    @Test
    public void testDescription() {
        Assert.assertEquals("Script", expressionScript.getShortDescription());
        Assert.assertEquals("Evaluate script: Jython command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )", expressionScript.getLongDescription());
    }
    
    @Test
    public void testExpression_JythonCommand() throws Exception {
        // Test expression
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        
        // The action is not yet executed so the light should be off
        Assert.assertTrue("light is off", light.getState() == Light.OFF);
        // Set the sensor to inactive so we get a property change when we activate the sensor later
        sensor.setState(Sensor.INACTIVE);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action should not have been executed yet so the light should be off
        Assert.assertTrue("light is off", light.getState() == Light.OFF);
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on", light.getState() == Light.ON);
        
        
        // Test action when triggered because the script is listening on the sensor IS2
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        light.setCommandedState(Light.OFF);
        
        logixNG.unregisterListeners();
        
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
        expressionScript.unregisterListeners();
        light.setState(Light.OFF);
        // Turn the light off.
        light.setCommandedState(Light.OFF);
        // Activate the sensor. This not should execute the conditional since listerners are not registered.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // Listerners are not registered so the atomic boolean should be false
        Assert.assertEquals("light is off",Light.OFF,light.getState());
        
        // Test evaluate() without script.
        expressionScript.setScript("");
        Assert.assertFalse(expressionScript.evaluate());
    }
    
    @Test
    public void testExpression_RunScript() throws Exception {
        expressionScript.setOperationType(ExpressionScript.OperationType.RunScript);
        expressionScript.setScript("java/test/jmri/jmrit/logixng/expressions/ExpressionScriptTest.py");
        
        // Test action
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        
        // Set the sensor to inactive so we get a property change when we activate the sensor later
        sensor.setState(Sensor.INACTIVE);
        // The action is not yet executed so the light should be off
        Assert.assertTrue("light is off", light.getState() == Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on", light.getState() == Light.ON);
    }
    
    @Test
    public void testSetScript() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", _scriptText);
        expressionScript.setScript(_scriptText);
        Assert.assertNotNull("Script is not null", expressionScript.getScript());
        
        // Test bad script
        expressionScript.setScript("This is a bad script");
        Assert.assertEquals("This is a bad script", expressionScript.getScript());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
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
        
        expressionScript = new ExpressionScript(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
        expressionScript.setScript(_scriptText);
        expressionScript.setRegisterListenerScript("sensors.provideSensor(\"IS1\").addPropertyChangeListener(self)");
        expressionScript.setUnregisterListenerScript("sensors.provideSensor(\"IS1\").removePropertyChangeListener(self)");
        MaleSocket socketExpressionScript = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionScript);
        ifThenElse.getChild(0).connect(socketExpressionScript);
        
        ActionLight actionLight = new ActionLight("IQDA322", null);
        actionLight.setLight(InstanceManager.getDefault(LightManager.class).provide("IL1"));
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
        ifThenElse.getChild(1).connect(maleSocket2);
        
        // Set sensor to active to ensure the script returns true
        sensor.setState(Sensor.ACTIVE);
        // Ensure the sensor is active
        Assert.assertTrue( InstanceManager.getDefault(SensorManager.class).provideSensor("IS1").getState() == Sensor.ACTIVE );
        // Ensure the script returns true
        Assert.assertTrue(expressionScript.evaluate());
        
        _base = expressionScript;
        _baseMaleSocket = socketExpressionScript;
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
        logixNG = null;
        conditionalNG = null;
        ifThenElse = null;
        expressionScript = null;
        sensor = null;
    }
    
}
