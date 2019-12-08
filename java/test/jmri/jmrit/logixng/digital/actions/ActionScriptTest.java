package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
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

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionScript actionScript;
    
    private final String _scriptText = ""
            + "import java\n"
            + "import java.beans\n"
            + "import jmri\n"
            + ""
            + "class MyAction(jmri.jmrit.logixng.digital.actions.AbstractScriptDigitalAction):\n"
            + ""
            + "  l = lights.provideLight(\"IL1\")\n"
            + ""
            + "  def execute(self):\n"
            + "    if self.l is None:\n"
            + "      raise java.lang.NullPointerException()\n"
            + "    self.l.commandedState = ON\n"
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
            + "params._scriptClass.set(MyAction(params._parentAction))\n";
    
    
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
        return String.format("Execute script%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Execute script%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionScript(systemName, null);
    }
    
    @Test
    public void testCtor() {
        ActionScript t = new ActionScript("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        Assert.assertTrue("Execute script".equals(actionScript.getShortDescription()));
        Assert.assertTrue("Execute script".equals(actionScript.getLongDescription()));
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        
        // The action is not yet executed so the light should be off
        Assert.assertTrue("light is off", light.getState() == Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Execute the conditionalNG
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on", light.getState() == Light.ON);
    }
    
    @Test
    public void testSetScript() {
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Script is not null", _scriptText);
        ActionScript action =
                new ActionScript(
                        InstanceManager.getDefault(
                                DigitalActionManager.class).getAutoSystemName(), null);
        action.setScript(_scriptText);
        
        Assert.assertNotNull("Script is not null", action.getScriptText());
        action.registerListeners();
        boolean thrown = false;
        try {
            action.setScript(null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setScript must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // This test calls action.execute() to see if the script still has
        // the light registered. Once the light is deleted, action.execute()
        // will throw a NullPointerException.
        
        // Get the expression and set the light
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        Assert.assertNotNull("Light is not null", light);
        light.setCommandedState(Light.ON);
        
        ActionScript action =
                new ActionScript(
                        InstanceManager.getDefault(
                                DigitalActionManager.class).getAutoSystemName(), null);
        action.setScript(_scriptText);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        action.execute();
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        action.execute();
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        action.execute();
        
        // Test vetoableChange() for another light
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        action.execute();
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        action.execute();
        
        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        thrown = false;
        try {
            // If DoDelete has done its job, execute() will throw a NullPointerException.
            action.execute();
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
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        actionScript = new ActionScript(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionScript.setScript(_scriptText);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionScript);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionScript;
        _baseMaleSocket = socket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
