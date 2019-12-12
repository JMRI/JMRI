package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
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

    private final String _scriptText_old = ""
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
    
    private final String _scriptText = ""
            + "import java\n"
            + "import java.beans\n"
            + "import jmri\n"
            + ""
            + "class MyAction(jmri.jmrit.logixng.digital.actions.AbstractScriptDigitalAction, jmri.jmrit.logixng.FemaleSocketListener):\n"
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
    public String getExpectedPrintedTree() {
        return String.format(
                "Execute script%n" +
                "   !~ AA%n" +
                "      Socket not connected%n" +
                "   ?~ AE%n" +
                "      Socket not connected%n" +
                "   ! DA%n" +
                "      Socket not connected%n" +
                "   ! DBA%n" +
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
                "      ! %n" +
                "         If E then A1 else A2%n" +
"            ? E%n" +
"               Sensor IS1 is Active%n" +
"            ! A1%n" +
"               Execute script%n" +
"                  !~ AA%n" +
"                     Socket not connected%n" +
"                  ?~ AE%n" +
"                     Socket not connected%n" +
"                  ! DA%n" +
"                     Socket not connected%n" +
"                  ! DBA%n" +
"                     Socket not connected%n" +
"                  ? DE%n" +
"                     Socket not connected%n" +
"                  !s SA%n" +
"                     Socket not connected%n" +
"                  ?s SE%n" +
"                     Socket not connected%n" +
"            ! A2%n" +
"               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionScript(systemName, null);
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
/*        
        Assert.assertTrue("getChildCount() returns 0", 0 == actionScript.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionScript.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
*/
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
        // Register listeners
////        conditionalNG.registerListeners();
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // Execute the conditionalNG
//        conditionalNG.execute();
        // Set the sensor to execute the conditionalNG
        sensor.setState(Sensor.ACTIVE);
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
/*        
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
*/
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
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.activateLogixNG();
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
