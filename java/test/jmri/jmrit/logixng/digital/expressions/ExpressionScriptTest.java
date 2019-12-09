package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
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
            + "class MyExpression(jmri.jmrit.logixng.digital.expressions.AbstractScriptDigitalExpression):\n"
            + ""
            + "  l = lights.provideLight(\"IL1\")\n"
            + ""
            + "  def registerScriptListeners(self):\n"
            + "    self.l.addPropertyChangeListener(\"KnownState\", self);\n"
            + ""
            + "  def unregisterScriptListeners():\n"
            + "    l.removePropertyChangeListener(\"KnownState\", this);\n"
            + ""
            + "  def evaluate(self):\n"
            + "    if self.l is None:\n"
            + "      raise java.lang.NullPointerException()\n"
            + "    return self.l.commandedState == ON\n"
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
            + ""
            + "params._scriptClass.set(MyExpression(params._parentExpression))\n";
    
    
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
    public String getExpectedPrintedTree() {
        return String.format("Evaluate script%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Evaluate script%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionScript(systemName, null);
    }
    
    @Test
    public void testCtor() {
        ExpressionScript t = new ExpressionScript("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        Assert.assertTrue("Evaluate script".equals(expressionScript.getShortDescription()));
        Assert.assertTrue("Evaluate script".equals(expressionScript.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        expressionScript.setScript(_scriptText);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should not execute the conditional.
        light.setCommandedState(Light.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Close the switch. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }
    
    @Test
    public void testSetScript() {
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", _scriptText);
        expressionScript.setScript(_scriptText);
        
        Assert.assertNotNull("Script is not null", expressionScript.getScriptText());
        expressionScript.registerListeners();
        boolean thrown = false;
        try {
            expressionScript.setScript(null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setScript must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
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
            expressionScript.vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        expressionScript.vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        thrown = false;
        try {
            // If DoDelete has done its job, evaluate() will throw a NullPointerException.
            expressionScript.evaluate();
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.activateLogixNG();
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
