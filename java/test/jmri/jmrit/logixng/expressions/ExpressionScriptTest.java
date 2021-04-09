package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.AnalogAction;
import jmri.jmrit.logixng.AnalogExpression;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalBooleanAction;
import jmri.jmrit.logixng.DigitalExpression;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.StringAction;
import jmri.jmrit.logixng.StringExpression;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionScript
 * 
 * @author Daniel Bergqvist 2018
 */
public class ExpressionScriptTest extends AbstractDigitalExpressionTestBase {

    private final String _scriptText = ""
            + "import java\n"
            + "import java.beans\n"
            + "import jmri\n"
            + ""
            + "class MyExpression(jmri.jmrit.logixng.expressions.AbstractScriptDigitalExpression, jmri.jmrit.logixng.FemaleSocketListener):\n"
            + ""
            + "  l = lights.provideLight(\"IL1\")\n"
            + ""
            + "  def registerScriptListeners(self):\n"
            + "    self.l.addPropertyChangeListener(\"KnownState\", self)\n"
            + ""
            + "  def unregisterScriptListeners(self):\n"
            + "    self.l.removePropertyChangeListener(\"KnownState\", self)\n"
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
            + "  def evaluate(self):\n"
            + "    if self.l is None:\n"
            + "      raise java.lang.NullPointerException()\n"
            + "    return self.l.commandedState == ON\n"
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
            + "myClass = MyExpression(params._parentExpression)\n"
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
    private ExpressionScript expressionScript;
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
        DigitalExpressionBean childExpression = new True("IQDE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Evaluate script ::: Log error%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Trigger action ::: Log error%n" +
                "            ? If%n" +
                "               Evaluate script ::: Log error%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Log error%n" +
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
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Evaluate script", expression2.getLongDescription());
        
        expression2 = new ExpressionScript("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "Evaluate script", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionScript("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionScript("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
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
        Assert.assertTrue("getChildCount() returns 7", 7 == expressionScript.getChildCount());
        Assert.assertTrue("getChild(0) is an AnalogAction", expressionScript.getChild(0) instanceof AnalogAction);
        Assert.assertTrue("getChild(1) is an AnalogAction", expressionScript.getChild(1) instanceof AnalogExpression);
        Assert.assertTrue("getChild(2) is an AnalogAction", expressionScript.getChild(2) instanceof DigitalAction);
        Assert.assertTrue("getChild(3) is an AnalogAction", expressionScript.getChild(3) instanceof DigitalBooleanAction);
        Assert.assertTrue("getChild(4) is an AnalogAction", expressionScript.getChild(4) instanceof DigitalExpression);
        Assert.assertTrue("getChild(5) is an AnalogAction", expressionScript.getChild(5) instanceof StringAction);
        Assert.assertTrue("getChild(6) is an AnalogAction", expressionScript.getChild(6) instanceof StringExpression);
        
        hasThrown = false;
        try {
            expressionScript.getChild(8);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index is bad", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription() {
        Assert.assertTrue("Evaluate script".equals(expressionScript.getShortDescription()));
        Assert.assertTrue("Evaluate script".equals(expressionScript.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws Exception {
        expressionScript.setScript(_scriptText);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the light on. This should not execute the conditional.
        light.setCommandedState(Light.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the light off. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the light on. This should execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        // Unregister listeners
        expressionScript.unregisterListeners();
        atomicBoolean.set(false);
        // Turn the light off. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // Turn the light on. This not should execute the conditional since listerners are not registered.
        light.setCommandedState(Light.ON);
        // Listerners are not registered so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        
        // Test execute() without script. This shouldn't do anything but we
        // do it for coverage.
        expressionScript.setScript(null);
        expressionScript.evaluate();
    }
    
    @Test
    public void testSetup() {
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Assert.assertNull("the value of memory is null", m1.getValue());
        
        // Test setup() without script
        expressionScript.setScript(null);
        expressionScript.setup();
        Assert.assertNull("the value of memory is null", m1.getValue());
        
        // Test setup() with script
        expressionScript.setScript(_scriptText);
        expressionScript.setup();
        Assert.assertEquals("the value of memory is hello", "setup is executed", m1.getValue());
    }
    
    @Test
    public void testSetScript() {
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", _scriptText);
        expressionScript.setScript(_scriptText);
        Assert.assertNotNull("Script is not null", expressionScript.getScriptText());
        
        // Test bad script
        expressionScript.setScript("This is a bad script");
        Assert.assertNull("Script is null", expressionScript.getScriptText());
        JUnitAppender.assertErrorMessage("cannot load script");
        
        // Test script that did not initialized params._scriptClass
        expressionScript.setScript("");
        JUnitAppender.assertWarnMessage("script has not initialized params._scriptClass");
        
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", expressionScript.getScriptText());
        expressionScript.setScript(_scriptText);    // The expressionScript needs a script to register listeners
        expressionScript.registerListeners();
        boolean hasThrown = false;
        try {
            expressionScript.setScript(_scriptText);
        } catch (RuntimeException ex) {
            hasThrown = true;
        }
        Assert.assertTrue("Expected exception thrown", hasThrown);
        JUnitAppender.assertErrorMessage("setScript must not be called when listeners are registered");
    }
    
    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionScript has no script
        conditionalNG.setEnabled(false);
        expressionScript.setScript(null);
        conditionalNG.setEnabled(true);
    }
    
    @Test
    public void testVetoableChange() throws Exception {
        // This test calls expressionScript.evaluate() to see if the script still has
        // the light registered. Once the light is deleted, expressionScript.evaluate()
        // will throw a NullPointerException.
        
        // Get the expressionScript and set the light
        Assert.assertNotNull("Light is not null", light);
        light.setCommandedState(Light.ON);
        
        expressionScript.setScript(_scriptText);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        expressionScript.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertTrue("Evaluate returns true", expressionScript.evaluate());
        
        // Test vetoableChange() for a string
        expressionScript.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertTrue("Evaluate returns true", expressionScript.evaluate());
        expressionScript.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertTrue("Evaluate returns true", expressionScript.evaluate());
        
        // Test vetoableChange() for another light
        expressionScript.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        Assert.assertTrue("Evaluate returns true", expressionScript.evaluate());
        expressionScript.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        Assert.assertTrue("Evaluate returns true", expressionScript.evaluate());
        
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
            expressionScript.evaluate();
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testDispose2() {
        // Test dispose() without script
        ExpressionScript expression = new ExpressionScript("IQDE321", null);
        expression.dispose();
        
        // Test dispose() with script
        expression = new ExpressionScript("IQDE321", null);
        expression.setScript(_scriptText);
        expression.dispose();
    }
    
    @Test
    @Override
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    @Test
    @Override
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        logixNG.setEnabled(false);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionScript = new ExpressionScript("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionScript);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionScript;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        
        logixNG.setEnabled(true);
        conditionalNG.setEnabled(true);
        maleSocket.setEnabled(true);
        logixNG.setParentForAllChildren();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
