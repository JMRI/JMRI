package jmri.jmrit.logixng.configurexml;

import jmri.jmrit.logixng.expressions.ExpressionReference;
import jmri.jmrit.logixng.expressions.ExpressionEntryExit;
import jmri.jmrit.logixng.expressions.ResetOnTrue;
import jmri.jmrit.logixng.expressions.ExpressionSignalMast;
import jmri.jmrit.logixng.expressions.Or;
import jmri.jmrit.logixng.expressions.ExpressionClock;
import jmri.jmrit.logixng.expressions.Hold;
import jmri.jmrit.logixng.expressions.TriggerOnce;
import jmri.jmrit.logixng.expressions.Antecedent;
import jmri.jmrit.logixng.expressions.ExpressionWarrant;
import jmri.jmrit.logixng.expressions.ExpressionOBlock;
import jmri.jmrit.logixng.expressions.And;
import jmri.jmrit.logixng.expressions.ExpressionScript;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.expressions.ExpressionConditional;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionLight;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.True;
import jmri.jmrit.logixng.expressions.False;
import jmri.jmrit.logixng.expressions.ExpressionSignalHead;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.logixng.actions.ActionThrottle;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.ActionScript;
import jmri.jmrit.logixng.actions.ActionListenOnBeans;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.actions.ShutdownComputer;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.CallModule;
import jmri.jmrit.logixng.actions.ActionSensor;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.actions.DoStringAction;

import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jmri.*;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.analog.actions.*;
import jmri.jmrit.logixng.analog.expressions.*;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;
import jmri.jmrit.logixng.string.actions.*;
import jmri.jmrit.logixng.string.expressions.*;
import jmri.util.*;

import org.junit.*;

/**
 * Creates a LogixNG with all actions and expressions to test store and load.
 * <P>
 * It uses the Base.printTree(PrintWriter writer, String indent) method to
 * compare the LogixNGs before and after store and load.
 */
public class StoreAndLoadTest {
    
    @Test
    public void testLogixNGs() throws PropertyVetoException, Exception {
        Assert.assertFalse(GraphicsEnvironment.isHeadless());
/*        
        // FOR TESTING ONLY. REMOVE LATER.
        if (1==0) {
            if (!GraphicsEnvironment.isHeadless()) {

                for (jmri.Logix l : InstanceManager.getDefault(jmri.LogixManager.class).getNamedBeanSet()) {
                    String sysName = l.getSystemName();
                    if (!sysName.equals("SYS") && !sysName.startsWith("RTX")) {
                        jmri.jmrit.logixng.tools.ImportLogix il = new jmri.jmrit.logixng.tools.ImportLogix(l);
                        il.doImport();
                    }
                }
            }
        }
*/        
        Light light1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light1.setCommandedState(Light.OFF);
        Light light2 = InstanceManager.getDefault(LightManager.class).provide("IL2");
        light2.setCommandedState(Light.OFF);
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor1.setCommandedState(Sensor.INACTIVE);
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout1.setCommandedState(Turnout.CLOSED);
        Turnout turnout2 = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        turnout2.setCommandedState(Turnout.CLOSED);
        Turnout turnout3 = InstanceManager.getDefault(TurnoutManager.class).provide("IT3");
        turnout3.setCommandedState(Turnout.CLOSED);
        Turnout turnout4 = InstanceManager.getDefault(TurnoutManager.class).provide("IT4");
        turnout4.setCommandedState(Turnout.CLOSED);
        Turnout turnout5 = InstanceManager.getDefault(TurnoutManager.class).provide("IT5");
        turnout5.setCommandedState(Turnout.CLOSED);

        Memory memory1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Memory memory2 = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        Memory memory3 = InstanceManager.getDefault(MemoryManager.class).provide("IM3");
        
        LogixManager logixManager = InstanceManager.getDefault(LogixManager.class);
        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);
        
        jmri.Logix logixIX1 = logixManager.createNewLogix("IX1", null);
        logixIX1.setEnabled(true);
        
        Conditional conditionalIX1C1 = conditionalManager.createNewConditional("IX1C1", "First conditional");
        logixIX1.addConditional(conditionalIX1C1.getSystemName(), 0);
        
        InstanceManager.getDefault(SignalHeadManager.class)
                .register(new VirtualSignalHead("IH1"));
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        InstanceManager.getDefault(SignalMastManager.class)
                .provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        
        InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class)
                .register(new OBlock("OB99"));
        
        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
        ConditionalNG_Manager conditionalNGManager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        AnalogActionManager analogActionManager = InstanceManager.getDefault(AnalogActionManager.class);
        AnalogExpressionManager analogExpressionManager = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalBooleanActionManager digitalBooleanActionManager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        StringActionManager stringActionManager = InstanceManager.getDefault(StringActionManager.class);
        StringExpressionManager stringExpressionManager = InstanceManager.getDefault(StringExpressionManager.class);
        
        
        
        
        // Create module IQM1
        jmri.jmrit.logixng.Module module =
                InstanceManager.getDefault(ModuleManager.class).createModule("IQM1", null);
        
        module.addParameter("n", true, false);
        module.addParameter("result", false, true);
        module.addLocalVariable("temp1", SymbolTable.InitialValueType.None, null);
        module.addLocalVariable("temp2", SymbolTable.InitialValueType.None, null);
        
        module.setRootSocketType(InstanceManager.getDefault(FemaleSocketManager.class)
                .getSocketTypeByType("DefaultFemaleDigitalActionSocket"));
        
        jmri.jmrit.logixng.actions.Many many901 = new jmri.jmrit.logixng.actions.Many("IQDA901", null);
        MaleSocket manySocket901 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many901);
        module.getRootSocket().connect(manySocket901);
        
        
        
        
        // Create an empty LogixNG
        logixNG_Manager.createLogixNG("An empty logixNG");
        
        // Create a LogixNG with an empty ConditionalNG
        LogixNG logixNG = logixNG_Manager.createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG =
                conditionalNGManager.createConditionalNG("An empty conditionalNG");
        logixNG.addConditionalNG(conditionalNG);
        logixNG.setEnabled(false);
        conditionalNG.setEnabled(false);
        
        
        logixNG = logixNG_Manager.createLogixNG("A logixNG");
        conditionalNG =
                conditionalNGManager.createConditionalNG("Yet another conditionalNG");
        logixNG.addConditionalNG(conditionalNG);
        logixNG.setEnabled(true);
        conditionalNG.setEnabled(true);
        
        FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
        MaleDigitalActionSocket actionManySocket =
                digitalActionManager.registerAction(new jmri.jmrit.logixng.actions.Many(
                                        digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionManySocket);
        femaleSocket.setLock(Base.Lock.HARD_LOCK);
        
        
        
        int index = 0;
        
        ActionLight actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        MaleSocket maleSocket = digitalActionManager.registerAction(actionLight);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.setLight(light1);
        actionLight.setLightState(ActionLight.LightState.Off);
        maleSocket = digitalActionManager.registerAction(actionLight);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ActionListenOnBeans actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("light:IL1");
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ActionLocalVariable actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setVariable("result");
        actionLocalVariable.setData("1");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ActionMemory actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setOtherMemory(memory2);
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setData("n + 3");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ActionScript actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        actionScript.setComment("A comment");
        actionScript.setScript("import java\n");
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ActionSensor actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.setSensor(sensor1);
        actionSensor.setSensorState(ActionSensor.SensorState.Inactive);
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ActionThrottle actionThrottle = new ActionThrottle(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionThrottle);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionThrottle = new ActionThrottle(digitalActionManager.getAutoSystemName(), null);
        actionThrottle.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(actionThrottle);
        actionManySocket.getChild(index++).connect(maleSocket);
        
/*        
        ActionTimer actionTimer = new ActionTimer(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionTimer);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionTimer = new ActionTimer(digitalActionManager.getAutoSystemName(), null);
        actionTimer.setComment("A comment");
        actionTimer.setDelay(100);
        maleSocket = digitalActionManager.registerAction(actionTimer);
        actionManySocket.getChild(index++).connect(maleSocket);
*/        
        
        ActionTurnout actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.setTurnout(turnout1);
        actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.Closed);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        CallModule callModule = new CallModule(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(callModule);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        callModule = new CallModule(digitalActionManager.getAutoSystemName(), null);
        callModule.setComment("A comment");
        callModule.setModule("IQM1");
        maleSocket = digitalActionManager.registerAction(callModule);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        DoAnalogAction doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        doAnalogAction.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        DoStringAction doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        doStringAction.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(doStringAction);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        IfThenElse ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.CONTINOUS_ACTION);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.TRIGGER_ACTION);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        jmri.jmrit.logixng.actions.Logix logix =
                new jmri.jmrit.logixng.actions.Logix(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(logix);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        logix = new jmri.jmrit.logixng.actions.Logix(digitalActionManager.getAutoSystemName(), null);
        logix.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(logix);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        //================================================================================
        //================================================================================
        //================================================================================
        //================================================================================
        //================================================================================
        //================================================================================
        //================================================================================
        //================================================================================
/*        
        jmri.jmrit.logixng.digital.boolean_actions.Many booleanMany =
                new jmri.jmrit.logixng.digital.boolean_actions.Many(digitalBooleanActionManager.getAutoSystemName(), null);
        maleSocket = digitalBooleanActionManager.registerAction(booleanMany);
        logix.getChild(1).connect(maleSocket);
        
        
        OnChange onChange =
                new OnChange(digitalBooleanActionManager.getAutoSystemName(), null, OnChange.Trigger.CHANGE);
        maleSocket = digitalBooleanActionManager.registerAction(onChange);
        booleanMany.getChild(0).connect(maleSocket);
        
        onChange = new OnChange(digitalBooleanActionManager.getAutoSystemName(), null, OnChange.Trigger.CHANGE_TO_FALSE);
        onChange.setComment("A comment");
        maleSocket = digitalBooleanActionManager.registerAction(onChange);
        booleanMany.getChild(1).connect(maleSocket);
*/        
        
        
        
        
        jmri.jmrit.logixng.actions.Many many =
                new jmri.jmrit.logixng.actions.Many(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(many);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        many = new jmri.jmrit.logixng.actions.Many(digitalActionManager.getAutoSystemName(), null);
        many.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(many);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        ShutdownComputer shutdownComputer =
                new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        
        
        actionThrottle = new ActionThrottle(digitalActionManager.getAutoSystemName(), null);
        actionThrottle.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(actionThrottle);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        maleSocket.getChild(0).connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));
        
        maleSocket.getChild(1).connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));
        
        maleSocket.getChild(2).connect(
                digitalExpressionManager.registerExpression(
                        new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null)));
        
        
        
        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.TRIGGER_ACTION);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        actionManySocket.getChild(index++).connect(maleSocket);
        
        
        And and1 = new And(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(and1);
        ifThenElse.getChild(0).connect(maleSocket);
        
        And and = new And(digitalExpressionManager.getAutoSystemName(), null);
        and.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(and);
        and1.getChild(0).connect(maleSocket);
        
        
        index = 0;
        
        
        Antecedent antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        and.getChild(index++).connect(maleSocket);
        
        antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        antecedent.setComment("A comment");
        antecedent.setAntecedent("R1 or R2");
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionClock expressionClock = new ExpressionClock(digitalExpressionManager.getAutoSystemName(), null);
        expressionClock.setType(ExpressionClock.Type.SystemClock);
        expressionClock.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionClock);
        and.getChild(index++).connect(maleSocket);
        
        expressionClock = new ExpressionClock(digitalExpressionManager.getAutoSystemName(), null);
        expressionClock.setComment("A comment");
        expressionClock.setRange(10, 20);
        expressionClock.setType(ExpressionClock.Type.FastClock);
        expressionClock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionClock);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionConditional expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.True);
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(index++).connect(maleSocket);
        
        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.setConditional("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.False);
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionEntryExit expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setEntryExitState(ExpressionEntryExit.EntryExitState.ACTIVE);
        expressionEntryExit.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        and.getChild(index++).connect(maleSocket);
        
        expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setComment("A comment");
        expressionEntryExit.setDestinationPoints("Something");
        expressionEntryExit.setEntryExitState(ExpressionEntryExit.EntryExitState.INACTIVE);
        expressionEntryExit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionLight expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setLightState(ExpressionLight.LightState.On);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(index++).connect(maleSocket);
        
        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.setLight(light1);
        expressionLight.setLightState(ExpressionLight.LightState.Off);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionLocalVariable expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(index++).connect(maleSocket);
        
        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setConstantValue("10");
        expressionLocalVariable.setCaseInsensitive(true);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.GreaterThan);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(index++).connect(maleSocket);
        
        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setVariable("MyVar");
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Memory);
        expressionLocalVariable.setMemory(memory2);
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Memory);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionMemory expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Memory);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(index++).connect(maleSocket);
        
        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        expressionMemory.setMemory(memory1);
        expressionMemory.setConstantValue("10");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.LessThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(index++).connect(maleSocket);
        
        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.setMemory(memory2);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Memory);
        expressionMemory.setOtherMemory(memory3);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Memory);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionOBlock expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setOBlockStatus(OBlock.OBlockStatus.Dark);
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(index++).connect(maleSocket);
        
        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.setOBlock("OB99");
        expressionOBlock.setOBlockStatus(OBlock.OBlockStatus.Occupied);
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionReference expressionReference = new ExpressionReference(digitalExpressionManager.getAutoSystemName(), null);
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LogixNGTable);
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionReference);
        and.getChild(index++).connect(maleSocket);
        
        expressionReference = new ExpressionReference(digitalExpressionManager.getAutoSystemName(), null);
        expressionReference.setComment("A comment");
        expressionReference.setReference("IL1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Light);
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionReference);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionScript expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        and.getChild(index++).connect(maleSocket);
        
        expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        expressionScript.setComment("A comment");
        expressionScript.setScript("import jmri\n");
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        and.getChild(index++).connect(maleSocket);
        JUnitAppender.assertErrorMessage("script has not initialized params._scriptClass");
        
        
        ExpressionSensor expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setSensorState(ExpressionSensor.SensorState.Active);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(index++).connect(maleSocket);
        
        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.setSensor(sensor1);
        expressionSensor.setSensorState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionSignalHead expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(index++).connect(maleSocket);
        
        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setQueryType(ExpressionSignalHead.QueryType.Lit);
        expressionSignalHead.setAppearance(SignalHead.FLASHLUNAR);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(index++).connect(maleSocket);
        
        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setQueryType(ExpressionSignalHead.QueryType.Appearance);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(index++).connect(maleSocket);
        
        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setQueryType(ExpressionSignalHead.QueryType.Appearance);
        expressionSignalHead.setAppearance(SignalHead.FLASHYELLOW);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionSignalMast expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(index++).connect(maleSocket);
        
        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setQueryType(ExpressionSignalMast.QueryType.Lit);
        expressionSignalMast.setAspect("Medium Approach Slow");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(index++).connect(maleSocket);
        
        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setQueryType(ExpressionSignalMast.QueryType.Aspect);
        expressionSignalMast.setAspect("");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(index++).connect(maleSocket);
        
        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setQueryType(ExpressionSignalMast.QueryType.Aspect);
        expressionSignalMast.setAspect("Medium Approach Slow");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(index++).connect(maleSocket);
        
/*        
        ExpressionTimer expressionTimer = new ExpressionTimer(digitalExpressionManager.getAutoSystemName(), null);
        expressionTimer.setTimerType(ExpressionTimer.TimerType.REPEAT_DOUBLE_DELAY);
        maleSocket = digitalExpressionManager.registerExpression(expressionTimer);
        and.getChild(index++).connect(maleSocket);
        
        expressionTimer = new ExpressionTimer(digitalExpressionManager.getAutoSystemName(), null);
        expressionTimer.setComment("A comment");
        expressionTimer.setTimerDelay(3, 4);
        expressionTimer.setTimerType(ExpressionTimer.TimerType.WAIT_ONCE_TRIG_ONCE);
        maleSocket = digitalExpressionManager.registerExpression(expressionTimer);
        and.getChild(index++).connect(maleSocket);
*/        
        
        ExpressionTurnout expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setTriggerOnChange(false);
        expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.Thrown);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(index++).connect(maleSocket);
        
        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.setTurnout(turnout1);
        expressionTurnout.setTriggerOnChange(true);
        expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.Closed);
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(index++).connect(maleSocket);
        
        
        ExpressionWarrant expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setType(ExpressionWarrant.Type.ROUTE_FREE);
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(index++).connect(maleSocket);
        
        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.setWarrant("Something");
        expressionWarrant.setType(ExpressionWarrant.Type.ROUTE_SET);
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(index++).connect(maleSocket);
        
        
        False false1 = new False(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(false1);
        and.getChild(index++).connect(maleSocket);
        
        false1 = new False(digitalExpressionManager.getAutoSystemName(), null);
        false1.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(false1);
        and.getChild(index++).connect(maleSocket);
        
        
        jmri.jmrit.logixng.expressions.Formula formula =
                new jmri.jmrit.logixng.expressions.Formula(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(formula);
        and.getChild(index++).connect(maleSocket);
        
        formula = new jmri.jmrit.logixng.expressions.Formula(digitalExpressionManager.getAutoSystemName(), null);
        formula.setComment("A comment");
        formula.setFormula("n + 1");
        maleSocket = digitalExpressionManager.registerExpression(formula);
        and.getChild(index++).connect(maleSocket);
        
        
        Hold hold = new Hold(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(hold);
        and.getChild(index++).connect(maleSocket);
        
        hold = new Hold(digitalExpressionManager.getAutoSystemName(), null);
        hold.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(hold);
        and.getChild(index++).connect(maleSocket);
        
        
        Or or = new Or(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(or);
        and.getChild(index++).connect(maleSocket);
        
        or = new Or(digitalExpressionManager.getAutoSystemName(), null);
        or.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(or);
        and.getChild(index++).connect(maleSocket);
        
        
        ResetOnTrue resetOnTrue = new ResetOnTrue(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(resetOnTrue);
        and.getChild(index++).connect(maleSocket);
        
        resetOnTrue = new ResetOnTrue(digitalExpressionManager.getAutoSystemName(), null);
        resetOnTrue.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(resetOnTrue);
        and.getChild(index++).connect(maleSocket);
        
        
        TriggerOnce triggerOnce = new TriggerOnce(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(triggerOnce);
        and.getChild(index++).connect(maleSocket);
        
        triggerOnce = new TriggerOnce(digitalExpressionManager.getAutoSystemName(), null);
        triggerOnce.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(triggerOnce);
        and.getChild(index++).connect(maleSocket);
        
        
        True true1 = new True(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(true1);
        and.getChild(index++).connect(maleSocket);
        
        true1 = new True(digitalExpressionManager.getAutoSystemName(), null);
        true1.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(true1);
        and.getChild(index++).connect(maleSocket);
        
        
        
        
        
        
        
        
/*        
        if (1==1) {
            final String treeIndent = "   ";
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent);
            
            System.out.println("--------------------------------------------");
            System.out.println("The current tree:");
            System.out.println("XXX"+stringWriter.toString()+"XXX");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            
            log.error("--------------------------------------------");
            log.error("The current tree:");
            log.error("XXX"+stringWriter.toString()+"XXX");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
//            return;
        }
*/        
        
        
        
        // Store panels
        jmri.ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Unable to get default configure manager");
        } else {
            FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
            File firstFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG_temp.xml");
            File secondFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG.xml");
            log.info("Temporary first file: %s%n", firstFile.getAbsoluteFile());
            log.info("Temporary second file: %s%n", secondFile.getAbsoluteFile());
            
            final String treeIndent = "   ";
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent);
            final String originalTree = stringWriter.toString();
            
            boolean results = cm.storeUser(firstFile);
            log.debug(results ? "store was successful" : "store failed");
            if (!results) {
                log.error("Failed to store panel");
                throw new RuntimeException("Failed to store panel");
            }
            
            // Add the header comment to the xml file
            addHeader(firstFile, secondFile);
            
            
            //**********************************
            // Delete all the LogixNGs, ConditionalNGs, and so on before reading the file.
            //**********************************
            
            java.util.Set<LogixNG> logixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
            for (LogixNG aLogixNG : logixNG_Set) {
                logixNG_Manager.deleteLogixNG(aLogixNG);
            }
            
            java.util.Set<ConditionalNG> conditionalNGSet = new java.util.HashSet<>(conditionalNGManager.getNamedBeanSet());
            for (ConditionalNG aConditionalNG : conditionalNGSet) {
                conditionalNGManager.deleteConditionalNG(aConditionalNG);
            }
            
            java.util.Set<MaleAnalogActionSocket> analogActionSet = new java.util.HashSet<>(analogActionManager.getNamedBeanSet());
            for (MaleAnalogActionSocket aAnalogAction : analogActionSet) {
                analogActionManager.deleteAnalogAction(aAnalogAction);
            }
            
            java.util.Set<MaleAnalogExpressionSocket> analogExpressionSet = new java.util.HashSet<>(analogExpressionManager.getNamedBeanSet());
            for (MaleAnalogExpressionSocket aAnalogExpression : analogExpressionSet) {
                analogExpressionManager.deleteAnalogExpression(aAnalogExpression);
            }
            
            java.util.Set<MaleDigitalActionSocket> digitalActionSet = new java.util.HashSet<>(digitalActionManager.getNamedBeanSet());
            for (MaleDigitalActionSocket aDigitalActionSocket : digitalActionSet) {
                digitalActionManager.deleteDigitalAction(aDigitalActionSocket);
            }
            
            java.util.Set<MaleDigitalBooleanActionSocket> digitalBooleanActionSet = new java.util.HashSet<>(digitalBooleanActionManager.getNamedBeanSet());
            for (MaleDigitalBooleanActionSocket aDigitalBooleanAction : digitalBooleanActionSet) {
                digitalBooleanActionManager.deleteDigitalBooleanAction(aDigitalBooleanAction);
            }
            
            java.util.Set<MaleDigitalExpressionSocket> digitalExpressionSet = new java.util.HashSet<>(digitalExpressionManager.getNamedBeanSet());
            for (MaleDigitalExpressionSocket aDigitalExpression : digitalExpressionSet) {
                digitalExpressionManager.deleteDigitalExpression(aDigitalExpression);
            }
            
            java.util.Set<MaleStringActionSocket> stringActionSet = new java.util.HashSet<>(stringActionManager.getNamedBeanSet());
            for (MaleStringActionSocket aStringAction : stringActionSet) {
                stringActionManager.deleteStringAction(aStringAction);
            }
            
            java.util.Set<MaleStringExpressionSocket> stringExpressionSet = new java.util.HashSet<>(stringExpressionManager.getNamedBeanSet());
            for (MaleStringExpressionSocket aStringExpression : stringExpressionSet) {
                stringExpressionManager.deleteStringExpression(aStringExpression);
            }
            
            Assert.assertEquals(0, logixNG_Manager.getNamedBeanSet().size());
            Assert.assertEquals(0, conditionalNGManager.getNamedBeanSet().size());
            Assert.assertEquals(0, analogActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, analogExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, stringActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, stringExpressionManager.getNamedBeanSet().size());
            
            
            
            //**********************************
            // Try to load file
            //**********************************
            
            java.util.Set<ConditionalNG> conditionalNG_Set =
                    new java.util.HashSet<>(conditionalNGManager.getNamedBeanSet());
            for (ConditionalNG aConditionalNG : conditionalNG_Set) {
                conditionalNGManager.deleteConditionalNG(aConditionalNG);
            }
            java.util.SortedSet<MaleAnalogActionSocket> set1 = analogActionManager.getNamedBeanSet();
            List<MaleSocket> l = new ArrayList<>(set1);
            for (MaleSocket x1 : l) {
                analogActionManager.deleteBean((MaleAnalogActionSocket)x1, "DoDelete");
            }
            java.util.SortedSet<MaleAnalogExpressionSocket> set2 = analogExpressionManager.getNamedBeanSet();
            l = new ArrayList<>(set2);
            for (MaleSocket x2 : l) {
                analogExpressionManager.deleteBean((MaleAnalogExpressionSocket)x2, "DoDelete");
            }
            java.util.SortedSet<MaleDigitalActionSocket> set3 = digitalActionManager.getNamedBeanSet();
            l = new ArrayList<>(set3);
            for (MaleSocket x3 : l) {
                digitalActionManager.deleteBean((MaleDigitalActionSocket)x3, "DoDelete");
            }
            java.util.SortedSet<MaleDigitalExpressionSocket> set4 = digitalExpressionManager.getNamedBeanSet();
            l = new ArrayList<>(set4);
            for (MaleSocket x4 : l) {
                digitalExpressionManager.deleteBean((MaleDigitalExpressionSocket)x4, "DoDelete");
            }
            java.util.SortedSet<MaleStringActionSocket> set5 = stringActionManager.getNamedBeanSet();
            l = new ArrayList<>(set5);
            for (MaleSocket x5 : l) {
                stringActionManager.deleteBean((MaleStringActionSocket)x5, "DoDelete");
            }
            java.util.SortedSet<MaleStringExpressionSocket> set6 = stringExpressionManager.getNamedBeanSet();
            l = new ArrayList<>(set6);
            for (MaleSocket x6 : l) {
                stringExpressionManager.deleteBean((MaleStringExpressionSocket)x6, "DoDelete");
            }

            results = cm.load(secondFile);
            log.debug(results ? "load was successful" : "store failed");
            if (results) {
                logixNG_Manager.resolveAllTrees();
                logixNG_Manager.setupAllLogixNGs();
                
                stringWriter = new StringWriter();
                printWriter = new PrintWriter(stringWriter);
                logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent);
                
                if (!originalTree.equals(stringWriter.toString())) {
                    log.error("--------------------------------------------");
                    log.error("Old tree:");
                    log.error("XXX"+originalTree+"XXX");
                    log.error("--------------------------------------------");
                    log.error("New tree:");
                    log.error("XXX"+stringWriter.toString()+"XXX");
                    log.error("--------------------------------------------");
/*                    
                    System.out.println("--------------------------------------------");
                    System.out.println("Old tree:");
                    System.out.println("XXX"+originalTree+"XXX");
                    System.out.println("--------------------------------------------");
                    System.out.println("New tree:");
                    System.out.println("XXX"+stringWriter.toString()+"XXX");
                    System.out.println("--------------------------------------------");
*/                    
//                    log.error(conditionalNGManager.getBySystemName(originalTree).getChild(0).getConnectedSocket().getSystemName());

                    Assert.fail("tree has changed");
//                    throw new RuntimeException("tree has changed");
                }
            } else {
                Assert.fail("Failed to load panel");
//                throw new RuntimeException("Failed to load panel");
            }
        }
    }
    
    
    private void addHeader(File inFile, File outFile) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8)))) {
            
            String line = reader.readLine();
            writer.println(line);
            
            writer.println("<!--");
            writer.println("*****************************************************************************");
            writer.println();
            writer.println("DO NOT EDIT THIS FILE!!!");
            writer.println();
            writer.println("This file is created by jmri.jmrit.logixng.configurexml.StoreAndLoadTest");
            writer.println("and put in the temp/temp folder. LogixNG uses both the standard JMRI load");
            writer.println("and store test, and a LogixNG specific store and load test.");
            writer.println();
            writer.println("After adding new stuff to StoreAndLoadTest, copy the file temp/temp/LogixNG.xml");
            writer.println("to the folder java/test/jmri/jmrit/logixng/configurexml/load");
            writer.println();
            writer.println("******************************************************************************");
            writer.println("-->");
            
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
        }
    }
    
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
//        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();
        
//        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitAppender.clearBacklog();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndLoadTest.class);

}
