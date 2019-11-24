package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
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
public class ExpressionScriptTest {

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
    
    
    @Test
    public void testCtor() {
        ExpressionScript t = new ExpressionScript("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionScript expressionScript = new ExpressionScript("IQDE321", null);
        Assert.assertTrue("Evaluate script".equals(expressionScript.getShortDescription()));
        Assert.assertTrue("Evaluate script".equals(expressionScript.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LogixNG logixNG =
                InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        IfThenElse actionIfThen =
                new IfThenElse(InstanceManager.getDefault(
                        DigitalActionManager.class).getAutoSystemName(), null,
                        IfThenElse.Type.TRIGGER_ACTION);
        
        MaleSocket socketIfThen =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionIfThen);
        
        conditionalNG.getChild(0).connect(socketIfThen);
        
        ExpressionScript expressionScript =
                new ExpressionScript(InstanceManager.getDefault(
                        DigitalExpressionManager.class).getAutoSystemName(), null);
        
        expressionScript.setScript(_scriptText);
        MaleSocket socketScript = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionScript);
        socketIfThen.getChild(0).connect(socketScript);
        
        ActionAtomicBoolean actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        socketIfThen.getChild(1).connect(socketAtomicBoolean);
        
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
        ExpressionScript expression =
                new ExpressionScript(
                        InstanceManager.getDefault(
                                DigitalExpressionManager.class).getAutoSystemName(), null);
        expression.setScript(_scriptText);
        
        Assert.assertNotNull("Script is not null", expression.getScriptText());
        expression.registerListeners();
        boolean thrown = false;
        try {
            expression.setScript(null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setScript must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // This test calls expression.evaluate() to see if the script still has
        // the light registered. Once the light is deleted, expression.evaluate()
        // will throw a NullPointerException.
        
        // Get the expression and set the light
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        Assert.assertNotNull("Light is not null", light);
        light.setCommandedState(Light.ON);
        
        ExpressionScript expression =
                new ExpressionScript(
                        InstanceManager.getDefault(
                                DigitalExpressionManager.class).getAutoSystemName(), null);
        expression.setScript(_scriptText);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertTrue("Evaluate returns true", expression.evaluate());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertTrue("Evaluate returns true", expression.evaluate());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertTrue("Evaluate returns true", expression.evaluate());
        
        // Test vetoableChange() for another light
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        Assert.assertTrue("Evaluate returns true", expression.evaluate());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        Assert.assertTrue("Evaluate returns true", expression.evaluate());
        
        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        thrown = false;
        try {
            // If DoDelete has done its job, evaluate() will throw a NullPointerException.
            expression.evaluate();
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
