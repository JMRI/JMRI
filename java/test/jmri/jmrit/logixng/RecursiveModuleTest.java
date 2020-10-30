package jmri.jmrit.logixng;

import jmri.*;
import jmri.jmrit.logixng.digital.actions.ActionListenOnBeans;
import jmri.jmrit.logixng.digital.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.actions.Many;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test that a module can be used recursive by calculating the Fibonacci numbers.
 * 
 * @author Daniel Bergqvist 2020
 */
public class RecursiveModuleTest {
    
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private Memory n;
    private Memory result;
    
    
    @Test
    public void testFibonacci() {
        
    }
    
    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        n = InstanceManager.getDefault(MemoryManager.class).provide("IMN");
        result = InstanceManager.getDefault(MemoryManager.class).provide("IMRESULT");
        
        Sensor s = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        
        
        Many many = new Many("IQDA1", null);
        MaleSocket manySocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        conditionalNG.getChild(0).connect(manySocket);
        
        ActionListenOnBeans listenOnBeans = new ActionListenOnBeans("IQDA2", null);
        listenOnBeans.addReference(new NamedBeanReference("IMN", ActionListenOnBeans.NamedBeanType.MEMORY));
//        listenOnBeans.addReference("Turnoaut:IT1");
//        listenOnBeans.addReference("Turnout:IT1xx");
//        listenOnBeans.addReference("senSorIS1");
        MaleSocket listenSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(listenOnBeans);
        manySocket.getChild(0).connect(listenSocket);
        
        
/*        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        
        
        
        ExpressionSensor expression = new ExpressionSensor("IQDE1", null);
        expression.setSensor("IS1");
        expression.setSensorState(ExpressionSensor.SensorState.ACTIVE);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        ifThenElse.getChild(0).connect(maleSocket2);
*/        
        
        
        ActionTurnout action = new ActionTurnout("IQDA99", null);
        action.setTurnout("IT1");
        action.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
        MaleSocket maleSocket3 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        manySocket.getChild(1).connect(maleSocket3);
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
        
        
        
        
        
        
        n.setValue(null);
        s.setState(Sensor.INACTIVE);
        t.setState(Turnout.CLOSED);
        
        Assert.assertEquals(Turnout.CLOSED, t.getState());
        
        n.setValue(0);
        s.setState(Sensor.ACTIVE);
        
        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        
        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
