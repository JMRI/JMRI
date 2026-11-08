package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;

import jmri.jmrit.logixng.actions.*;
import jmri.InstanceManager;
import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.script.ScriptEngineSelector;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionSimpleScript
 *
 * @author Daniel Bergqvist 2021
 */
public class ExpressionScriptTest extends AbstractDigitalExpressionTestBase {

    private static final String SCRIPT_TEXT = "result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )";
    private static final String ECMA_SCRIPT = "var Sensor = Java.type(\"jmri.Sensor\"); result.setValue( sensors.provideSensor(\"IS1\").getState() == Sensor.ACTIVE )";


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
                "Evaluate script: Single line command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE ) ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Always execute ::: Use default%n" +
                "            ? If%n" +
                "               Evaluate script: Single line command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE ) ::: Use default%n" +
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
        expression2.setScript(SCRIPT_TEXT);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Evaluate script: Single line command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )",
            expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionScript("IQDE321", "My expression");
        expression2.setScript(SCRIPT_TEXT);
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
        assertEquals( "Evaluate script: Single line command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )",
            expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> {
                ExpressionScript escipt = new ExpressionScript("IQA55:12:XY11", "A name");
                fail("escript created: " + escipt.toString() );
            }, "Expected Illegal system name exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class,
            () -> {
                ExpressionScript escipt = new ExpressionScript("IQA55:12:XY11", null);
                fail("escript created: " + escipt.toString() );
            }, "Expected Illegal system name exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Test without script
        expressionScript.setScript(null);
        assertEquals( 0, expressionScript.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class,
            () -> expressionScript.getChild(0));
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");

        // Test with script
        expressionScript.setScript(SCRIPT_TEXT);
        assertEquals( 0, expressionScript.getChildCount(), "getChildCount() returns 0");
    }

    @Test
    public void testDescription() {
        assertEquals("Script", expressionScript.getShortDescription());
        assertEquals("Evaluate script: Single line command. Script result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )",
            expressionScript.getLongDescription());
    }

    @Test
    public void testExpression_SingleJythonCommand() throws JmriException {
        // Test expression
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);

        // The action is not yet executed so the light should be off
        assertTrue( light.getState() == Light.OFF, "light is off");
        // Set the sensor to inactive so we get a property change when we activate the sensor later
        sensor.setState(Sensor.INACTIVE);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action should not have been executed yet so the light should be off
        assertTrue( light.getState() == Light.OFF, "light is off");
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        assertTrue( light.getState() == Light.ON, "light is on");


        // Test action when triggered because the script is listening on the sensor IS2
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        light.setCommandedState(Light.OFF);

        logixNG.unregisterListeners();

        // The action is not yet executed so the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");
        // Activate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");
        // Inactivate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.INACTIVE);
        // The action is not yet executed so the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Activate the sensor. This should execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        assertEquals( Light.ON, light.getState(), "light is on");

        // Unregister listeners
        expressionScript.unregisterListeners();
        light.setState(Light.OFF);
        // Turn the light off.
        light.setCommandedState(Light.OFF);
        // Activate the sensor. This not should execute the conditional since listerners are not registered.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // Listerners are not registered so the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");

        // Test evaluate() without script.
        expressionScript.setScript("");
        assertFalse(expressionScript.evaluate());
    }

    @Test
    @SuppressWarnings("null") // engine false positive, should be fixed in JUnit6
    public void testExpression_SingleEcmaCommand() throws JmriException {
        expressionScript.getScriptEngineSelector().setSelectedEngine(ScriptEngineSelector.ECMA_SCRIPT);
        expressionScript.setScript(ECMA_SCRIPT);

        // Java 17 doesn't have ECMA_SCRIPT
        JUnitAppender.suppressWarnMessage("Cannot select engine for the language ECMAScript");
        ScriptEngineSelector.Engine engine = expressionScript.getScriptEngineSelector().getSelectedEngine();
        
        assumeTrue( engine != null, "Engine not null");
        assertNotNull(engine);
        assumeTrue( ScriptEngineSelector.ECMA_SCRIPT.equals(engine.getLanguageName()),
            () -> "Engine Language Name was \"" + engine.getLanguageName() + "\" not \"" + ScriptEngineSelector.ECMA_SCRIPT+"\"");

        // Test expression
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);

        // The action is not yet executed so the light should be off
        assertTrue( light.getState() == Light.OFF, "light is off");
        // Set the sensor to inactive so we get a property change when we activate the sensor later
        sensor.setState(Sensor.INACTIVE);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action should not have been executed yet so the light should be off
        assertTrue( light.getState() == Light.OFF, "light is off");
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        assertTrue( light.getState() == Light.ON, "light is on");


        // Test action when triggered because the script is listening on the sensor IS2
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        light.setCommandedState(Light.OFF);

        logixNG.unregisterListeners();

        // The action is not yet executed so the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");
        // Activate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");
        // Inactivate the sensor. This should not execute the conditional.
        sensor2.setCommandedState(Sensor.INACTIVE);
        // The action is not yet executed so the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Activate the sensor. This should execute the conditional.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        assertEquals( Light.ON, light.getState(), "light is on");

        // Unregister listeners
        expressionScript.unregisterListeners();
        light.setState(Light.OFF);
        // Turn the light off.
        light.setCommandedState(Light.OFF);
        // Activate the sensor. This not should execute the conditional since listerners are not registered.
        sensor2.setCommandedState(Sensor.ACTIVE);
        // Listerners are not registered so the atomic boolean should be false
        assertEquals( Light.OFF, light.getState(), "light is off");

        // Test evaluate() without script.
        expressionScript.setScript("");
        assertFalse(expressionScript.evaluate());
    }

    @Test
    public void testExpression_RunScript() throws JmriException {
        expressionScript.setOperationType(ExpressionScript.OperationType.RunScript);
        expressionScript.setScript("java/test/jmri/jmrit/logixng/expressions/ExpressionScriptTest.py");

        // Test action
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);

        // Set the sensor to inactive so we get a property change when we activate the sensor later
        sensor.setState(Sensor.INACTIVE);
        // The action is not yet executed so the light should be off
        assertTrue( light.getState() == Light.OFF, "light is off");
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
        // The action should now be executed so the light should be on
        assertTrue( light.getState() == Light.ON, "light is on");
    }

    @Test
    public void testAction_GetAndSetLocalVariables() throws JmriException {

        ((MaleSocket)ifThenElse.getParent()).addLocalVariable("in", SymbolTable.InitialValueType.Integer, "10");
        var globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("out");

        expressionScript.setScript("symbolTable.setValue(\"out\",symbolTable.getValue(\"in\")*15)");

        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Execute the conditionalNG
        conditionalNG.execute();

        // The expression should now be evaluated so the global variable should have the correct value
        assertEquals( 150,
                ((java.math.BigInteger)globalVariable.getValue()).longValue(),
            "global variable has the correct value");
    }

    @Test
    public void testSetScript() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Test setScript() when listeners are registered
        expressionScript.setScript(SCRIPT_TEXT);
        assertNotNull( expressionScript.getScript(), "Script is not null");

        // Test bad script
        expressionScript.setScript("This is a bad script");
        assertEquals( expressionScript.getScript(), "This is a bad script");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
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

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        ifThenElse = new IfThenElse("IQDA321", null);
        ifThenElse.setExecuteType(IfThenElse.ExecuteType.AlwaysExecute);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");

        expressionScript = new ExpressionScript(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
        expressionScript.setScript(SCRIPT_TEXT);
        expressionScript.setRegisterListenerScript("sensors.provideSensor(\"IS1\").addPropertyChangeListener(self)");
        expressionScript.setUnregisterListenerScript("sensors.provideSensor(\"IS1\").removePropertyChangeListener(self)");
        MaleSocket socketExpressionScript = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionScript);
        ifThenElse.getChild(0).connect(socketExpressionScript);

        ActionLight actionLight = new ActionLight("IQDA322", null);
        actionLight.getSelectNamedBean().setNamedBean(InstanceManager.getDefault(LightManager.class).provide("IL1"));
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
        ifThenElse.getChild(1).connect(maleSocket2);

        // Set sensor to active to ensure the script returns true
        sensor.setState(Sensor.ACTIVE);
        // Ensure the sensor is active
        assertTrue( InstanceManager.getDefault(SensorManager.class).provideSensor("IS1").getState() == Sensor.ACTIVE );
        // Ensure the script returns true
        assertTrue(expressionScript.evaluate());

        _base = expressionScript;
        _baseMaleSocket = socketExpressionScript;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
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
