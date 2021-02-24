package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.*;

import jmri.InstanceManager;
import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.True;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.script.JmriScriptEngineManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionScript
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionScriptTest extends AbstractDigitalActionTestBase {

    private final String _scriptText = ""
            + "import java\n"
            + "import java.beans\n"
            + "import jmri\n"
            + "import jmri.jmrit.logixng\n"
            + ""
            + "class MyAction(jmri.jmrit.logixng.actions.AbstractScriptDigitalAction, jmri.jmrit.logixng.FemaleSocketListener):\n"
            + ""
            + "  l = lights.provideLight(\"IL1\")\n"
            + "  s = sensors.provideSensor(\"IS2\")\n"
            + ""
            + "  def registerScriptListeners(self):\n"
            + "    self.s.addPropertyChangeListener(\"KnownState\", self)\n"
            + ""
            + "  def unregisterScriptListeners(self):\n"
            + "    self.s.removePropertyChangeListener(\"KnownState\", self)\n"
            + ""
            // One purpose with this function is to test that the script has
            // access to all the managers of LogixNG.
            + "  def getChild(self, index):\n"
            + "    if index == 0:\n"
            + "      return self.childAnalogAction\n"
            + "    elif index == 1:\n"
            + "      return self.childAnalogExpression\n"
            + "    elif index == 2:\n"
            + "      return self.childDigitalAction\n"
            + "    elif index == 3:\n"
            + "      return self.childDigitalBooleanAction\n"
            + "    elif index == 4:\n"
            + "      return self.childDigitalExpression\n"
            + "    elif index == 5:\n"
            + "      return self.childStringAction\n"
            + "    elif index == 6:\n"
            + "      return self.childStringExpression\n"
            + "    else:\n"
            + "      raise java.lang.IllegalArgumentException(\"index is bad\")\n"
            + ""
            + "  def getChildCount(self):\n"
            + "    return 7\n"
            + ""
            + "  def connected(self, femaleSocket):\n"
            + "    self.firePropertyChange(jmri.jmrit.logixng.Base.PROPERTY_SOCKET_CONNECTED, None, femaleSocket)\n"
            + ""
            + "  def disconnected(self, femaleSocket):\n"
            + "    self.firePropertyChange(jmri.jmrit.logixng.Base.PROPERTY_SOCKET_DISCONNECTED, None, femaleSocket)\n"
            + ""
            + "  def execute(self):\n"
            + "    if self.l is None:\n"
            + "      raise java.lang.NullPointerException()\n"
            + "    self.l.commandedState = ON\n"
            + ""
            // setup() method is used to lookup system names for child sockets,
            // turnouts, sensors, and so on. But we only want to check that it's
            // executed. So we set a memory to some value.
            + "  def setup(self):\n"
            + "    m = memories.provideMemory(\"IM1\")\n"
            + "    m.setValue(\"setup is executed\")\n"
            + ""
            + "  def vetoableChange(self,evt):\n"
            + "    if (\"CanDelete\" == evt.getPropertyName()):\n"
            + "      if (isinstance(evt.getOldValue(),jmri.Light)):\n"
            + "        if (evt.getOldValue() is self.l):\n"
            + "          raise java.beans.PropertyVetoException(self.getDisplayName(), evt)\n"
            + "    if (\"DoDelete\" == evt.getPropertyName()):\n"
            + "      if (isinstance(evt.getOldValue(),jmri.Light)):\n"
            + "        if (evt.getOldValue() is self.l):\n"
            + "          self.l = None\n"
            + ""
            + "myClass = MyAction(params._parentAction)\n"
            + "myClass.childAnalogAction = analogActions.createFemaleSocket(myClass, myClass, \"AA\")\n"
            + "myClass.childAnalogExpression = analogExpressions.createFemaleSocket(myClass, myClass, \"AE\")\n"
            + "myClass.childDigitalAction = digitalActions.createFemaleSocket(myClass, myClass, \"DA\")\n"
            + "myClass.childDigitalBooleanAction = digitalBooleanActions.createFemaleSocket(myClass, myClass, \"DBA\")\n"
            + "myClass.childDigitalExpression = digitalExpressions.createFemaleSocket(myClass, myClass, \"DE\")\n"
            + "myClass.childStringAction = stringActions.createFemaleSocket(myClass, myClass, \"SA\")\n"
            + "myClass.childStringExpression = stringExpressions.createFemaleSocket(myClass, myClass, \"SE\")\n"
            + "lights.addVetoableChangeListener(myClass)\n"
            + "params._scriptClass.set(myClass)\n";
    
    
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private IfThenElse ifThenElse;
    private ActionScript actionScript;
    private Sensor sensor;
    
    
    /*
     * ActionScript assumes that bindings are local to the script that
     * ActionScript executes. This test was written to check if that's the case
     * and it's kept here both as a proof of it and to verify that it stays
     * that way.
     * Daniel Bergqvist
     */
    @Test
    public void testThatBindingsAreLocal() throws ScriptException {
        String script = ""
                + "import java\n"
                + "import java.beans\n"
                + "import jmri\n"
                + ""
                + "class MyClass(java.beans.PropertyChangeListener):\n"
                + ""
                + "  s = sensors.provideSensor(\"IS%d\")\n"
                + ""
                + "  def __init__(self, ai):\n"
                + "    self.s.addPropertyChangeListener(\"KnownState\", self)\n"
                + "    self.myInt = ai\n"
                + ""
                + "  def propertyChange(self, evt):\n"
                + "    self.myInt.set(self.s.getState())\n"
                + ""
                + "MyClass(param)\n"
                ;
        
        
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor1.setCommandedState(Sensor.UNKNOWN);
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.UNKNOWN);
        
        AtomicInteger myInt1 = new AtomicInteger();
        AtomicInteger myInt2 = new AtomicInteger();
        
        JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();
        Bindings bindings = new SimpleBindings();
        bindings.put("param", myInt1);    // Give the script access to the local variable 'param'
        scriptEngineManager.getEngineByName(JmriScriptEngineManager.PYTHON)
                .eval(String.format(script,1), bindings);
        bindings.put("param", myInt2);    // Give the script access to the local variable 'param'
        scriptEngineManager.getEngineByName(JmriScriptEngineManager.PYTHON)
                .eval(String.format(script,2), bindings);
        
        sensor1.setCommandedState(Sensor.INACTIVE);
        sensor2.setCommandedState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, sensor1.getState());
        Assert.assertEquals(Sensor.INACTIVE, sensor2.getState());
        Assert.assertEquals(Sensor.INACTIVE, myInt1.get());
        Assert.assertEquals(Sensor.INACTIVE, myInt2.get());
        sensor1.setCommandedState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.ACTIVE, sensor1.getState());
        Assert.assertEquals(Sensor.INACTIVE, sensor2.getState());
        Assert.assertEquals(Sensor.ACTIVE, myInt1.get());
        Assert.assertEquals(Sensor.INACTIVE, myInt2.get());
        
        sensor1.setCommandedState(Sensor.INACTIVE);
        sensor2.setCommandedState(Sensor.INACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, sensor1.getState());
        Assert.assertEquals(Sensor.INACTIVE, sensor2.getState());
        Assert.assertEquals(Sensor.INACTIVE, myInt1.get());
        Assert.assertEquals(Sensor.INACTIVE, myInt2.get());
        sensor2.setCommandedState(Sensor.ACTIVE);
        Assert.assertEquals(Sensor.INACTIVE, sensor1.getState());
        Assert.assertEquals(Sensor.ACTIVE, sensor2.getState());
        Assert.assertEquals(Sensor.INACTIVE, myInt1.get());
        Assert.assertEquals(Sensor.ACTIVE, myInt2.get());
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
        AnalogActionBean childAction = new AnalogActionMemory("IQAA999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(childAction);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Execute script ::: Log error%n" +
                "   !~ AA%n" +
                "      Socket not connected%n" +
                "   ?~ AE%n" +
                "      Socket not connected%n" +
                "   ! DA%n" +
                "      Socket not connected%n" +
                "   !b DBA%n" +
                "      Socket not connected%n" +
                "   ? DE%n" +
                "      Socket not connected%n" +
                "   !s SA%n" +
                "      Socket not connected%n" +
                "   ?s SE%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Continuous action ::: Log error%n" +
                "            ? If%n" +
                "               Sensor IS1 is Active ::: Log error%n" +
                "            ! Then%n" +
                "               Execute script ::: Log error%n" +
                "                  !~ AA%n" +
                "                     Socket not connected%n" +
                "                  ?~ AE%n" +
                "                     Socket not connected%n" +
                "                  ! DA%n" +
                "                     Socket not connected%n" +
                "                  !b DBA%n" +
                "                     Socket not connected%n" +
                "                  ? DE%n" +
                "                     Socket not connected%n" +
                "                  !s SA%n" +
                "                     Socket not connected%n" +
                "                  ?s SE%n" +
                "                     Socket not connected%n" +
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
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Execute script", action2.getLongDescription());
        
        action2 = new ActionScript("IQDA321", "My action");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My action", action2.getUserName());
        Assert.assertEquals("String matches", "Execute script", action2.getLongDescription());
        
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
        Assert.assertTrue("getChildCount() returns 7", 7 == actionScript.getChildCount());
        Assert.assertTrue("getChild(0) is an AnalogAction", actionScript.getChild(0) instanceof AnalogAction);
        Assert.assertTrue("getChild(1) is an AnalogAction", actionScript.getChild(1) instanceof AnalogExpression);
        Assert.assertTrue("getChild(2) is an AnalogAction", actionScript.getChild(2) instanceof DigitalAction);
        Assert.assertTrue("getChild(3) is an AnalogAction", actionScript.getChild(3) instanceof DigitalBooleanAction);
        Assert.assertTrue("getChild(4) is an AnalogAction", actionScript.getChild(4) instanceof DigitalExpression);
        Assert.assertTrue("getChild(5) is an AnalogAction", actionScript.getChild(5) instanceof StringAction);
        Assert.assertTrue("getChild(6) is an AnalogAction", actionScript.getChild(6) instanceof StringExpression);
        
        hasThrown = false;
        try {
            actionScript.getChild(8);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index is bad", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testDescription() {
        Assert.assertTrue("Execute script".equals(actionScript.getShortDescription()));
        Assert.assertTrue("Execute script".equals(actionScript.getLongDescription()));
    }
    
    @Test
    public void testAction() throws Exception {
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
        
        actionScript.setScript(_scriptText);
        
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
        actionScript.setScript(null);
        actionScript.execute();
    }
    
    @Test
    public void testSetup() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Assert.assertNull("the value of memory is null", m1.getValue());
        
        // Test setup() without script
        actionScript.setScript(null);
        actionScript.setup();
        Assert.assertNull("the value of memory is null", m1.getValue());
        
        // Test setup() with script
        actionScript.setScript(_scriptText);
        actionScript.setup();
        Assert.assertEquals("the value of memory is hello", "setup is executed", m1.getValue());
    }
    
    @Test
    public void testSetScript() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", _scriptText);
        actionScript.setScript(_scriptText);
        Assert.assertNotNull("Script is not null", actionScript.getScriptText());
        
        // Test bad script
        actionScript.setScript("This is a bad script");
        Assert.assertNull("Script is null", actionScript.getScriptText());
        JUnitAppender.assertWarnMessage("cannot load script");
        
        // Test script that did not initialized params._scriptClass
        actionScript.setScript("");
        JUnitAppender.assertWarnMessage("script has not initialized params._scriptClass");
        
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", actionScript.getScriptText());
        actionScript.setScript(_scriptText);    // The actionScript needs a script to register listeners
        // Enable the conditionalNG. This will register the listeners
        conditionalNG.setEnabled(true);
        boolean hasThrown = false;
        try {
            actionScript.setScript(_scriptText);
        } catch (RuntimeException ex) {
            hasThrown = true;
        }
        Assert.assertTrue("Expected exception thrown", hasThrown);
        JUnitAppender.assertErrorMessage("setScript must not be called when listeners are registered");
        
        // Test registerListeners() without script. This shouldn't do anything
        // but we do it for coverage.
        actionScript.unregisterListeners();
        actionScript.setScript(null);
        actionScript.registerListeners();
    }
    
    @Test
    public void testVetoableChange() throws Exception {
        // This test calls actionScript.evaluate() to see if the script still has
        // the light registered. Once the light is deleted, actionScript.evaluate()
        // will throw a NullPointerException.
        
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Get the actionScript and set the light
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        Assert.assertNotNull("Light is not null", light);
        light.setCommandedState(Light.ON);
        
        actionScript.setScript(_scriptText);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        actionScript.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        actionScript.execute();
        
        // Test vetoableChange() for a string
        actionScript.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        actionScript.execute();
        actionScript.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        actionScript.execute();
        
        // Test vetoableChange() for another light
        actionScript.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        actionScript.execute();
        actionScript.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        actionScript.execute();
        
        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            InstanceManager.getDefault(LightManager.class).deleteBean(light, "CanDelete");
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        InstanceManager.getDefault(LightManager.class).deleteBean(light, "DoDelete");
        thrown = false;
        try {
            // If DoDelete has done its job, evaluate() will throw a NullPointerException.
            actionScript.execute();
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testDispose2() {
        // Test dispose() without script
        ActionScript expression = new ActionScript("IQDA321", null);
        expression.dispose();
        
        // Test dispose() with script
        expression = new ActionScript("IQDA321", null);
        expression.setScript(_scriptText);
        expression.dispose();
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
        ifThenElse.setType(IfThenElse.Type.ContinuousAction);
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
        MaleSocket socketActionScript = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionScript);
        ifThenElse.getChild(1).connect(socketActionScript);
        
        _base = actionScript;
        _baseMaleSocket = socketActionScript;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
