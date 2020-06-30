package jmri.jmrit.logixng.configurexml;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleAnalogActionSocket;
import jmri.jmrit.logixng.MaleAnalogExpressionSocket;
import jmri.jmrit.logixng.MaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleStringActionSocket;
import jmri.jmrit.logixng.MaleStringExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.digital.actions.ActionLight;
import jmri.jmrit.logixng.digital.actions.ActionSensor;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import jmri.jmrit.logixng.digital.actions.DoStringAction;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.actions.Many;
import jmri.jmrit.logixng.digital.actions.ShutdownComputer;
import jmri.jmrit.logixng.digital.expressions.And;
import jmri.jmrit.logixng.digital.expressions.Antecedent;
import jmri.jmrit.logixng.digital.expressions.ExpressionLight;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.digital.expressions.False;
import jmri.jmrit.logixng.digital.expressions.Hold;
import jmri.jmrit.logixng.digital.expressions.Or;
import jmri.jmrit.logixng.digital.expressions.ResetOnTrue;
import jmri.jmrit.logixng.digital.expressions.ExpressionTimer;
import jmri.jmrit.logixng.digital.expressions.TriggerOnce;
import jmri.jmrit.logixng.digital.expressions.True;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.digital.actions.ActionThrottle;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a LogixNG with all actions and expressions to test store and load.
 * <P>
 * It uses the Base.printTree(PrintWriter writer, String indent) method to
 * compare the LogixNGs before and after store and load.
 */
public class StoreAndLoadTest {

    @Ignore
    @Test
    public void testLogixNGs() throws PropertyVetoException, Exception {
        
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
        
        // FOR TESTING ONLY. REMOVE LATER.
        int test = 1;
        if (test == 1) {
            int store = 1;
            int load = 1;
            try {
                if (store == 1) {
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
                    
                    LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
                    LogixNG logixNG = logixNG_Manager.createLogixNG("A logixNG");
                    ConditionalNG conditionalNG =
                            InstanceManager.getDefault(ConditionalNG_Manager.class)
                                    .createConditionalNG("A conditionalNG");
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .setupInitialConditionalNGTree(conditionalNG);
                    logixNG.addConditionalNG(conditionalNG);
                    logixNG.setEnabled(true);
                    conditionalNG.setEnabled(true);

                    logixNG = logixNG_Manager.createLogixNG("Another logixNG");
                    conditionalNG =
                            InstanceManager.getDefault(ConditionalNG_Manager.class)
                                    .createConditionalNG(""
                                            + "");
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .setupInitialConditionalNGTree(conditionalNG);
                    logixNG.addConditionalNG(conditionalNG);

                    logixNG = logixNG_Manager.createLogixNG("Yet another logixNG");
                    conditionalNG =
                            InstanceManager.getDefault(ConditionalNG_Manager.class)
                                    .createConditionalNG("Yet another conditionalNG");
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .setupInitialConditionalNGTree(conditionalNG);
                    logixNG.addConditionalNG(conditionalNG);

                    MaleSocket socketMany = conditionalNG.getChild(0).getConnectedSocket();
                    MaleSocket socketIfThen = socketMany.getChild(0).getConnectedSocket();
                    
                    Or expressionOr = new Or(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00001", null);
                    MaleSocket socketOr = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionOr);
                    socketIfThen.getChild(0).connect(socketOr);
                    
                    int index = 0;
                    
                    Or expressionOr2 = new Or(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00002", "My Or expression");
                    MaleSocket socketOr2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionOr2);
                    socketOr.getChild(index++).connect(socketOr2);
                    
                    And expressionAnd = new And(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00003", null);
                    MaleSocket socketAnd = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionAnd);
                    socketOr.getChild(index++).connect(socketAnd);
                    
                    And expressionAnd2 = new And(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00004", "My And expression");
                    MaleSocket socketAnd2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionAnd2);
                    socketOr.getChild(index++).connect(socketAnd2);
                    
                    ExpressionTurnout expressionTurnout3 = new ExpressionTurnout(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00005", null);
                    expressionTurnout3.setTurnout(turnout3);
                    expressionTurnout3.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
                    MaleSocket socketTurnout3 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout3);
                    expressionAnd.getChild(0).connect(socketTurnout3);
                    
                    ExpressionTurnout expressionTurnout4 = new ExpressionTurnout(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00006", "My new turnout");
                    expressionTurnout4.setTurnout(turnout4);
                    expressionTurnout4.setTurnoutState(ExpressionTurnout.TurnoutState.CLOSED);
                    expressionTurnout4.set_Is_IsNot(Is_IsNot_Enum.IS);
                    MaleSocket socketTurnout4 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout4);
                    expressionAnd.getChild(1).connect(socketTurnout4);
                    
                    ExpressionTurnout expressionTurnout5 = new ExpressionTurnout(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00007", null);
                    expressionTurnout5.setTurnout(turnout5);
                    expressionTurnout5.setTurnoutState(ExpressionTurnout.TurnoutState.OTHER);
                    expressionTurnout5.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
                    MaleSocket socketTurnout5 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout5);
                    expressionAnd.getChild(2).connect(socketTurnout5);
                    
                    Antecedent expressionAntecedent = new Antecedent(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00008", null);
                    expressionAntecedent.setAntecedent("R1");
                    MaleSocket socketAntecedent = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionAntecedent);
                    socketOr.getChild(index++).connect(socketAntecedent);
                    
                    Antecedent expressionAntecedent2 = new Antecedent(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00009", "My Antecedent expression");
                    expressionAntecedent2.setAntecedent("R1");
                    MaleSocket socketAntecedent2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionAntecedent2);
                    socketOr.getChild(index++).connect(socketAntecedent2);
                    
                    Antecedent expressionAntecedent3 = new Antecedent(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00010", "My Antecedent expression");
                    expressionAntecedent3.setAntecedent("R1");
                    MaleSocket socketAntecedent3 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionAntecedent3);
                    socketAntecedent2.getChild(0).connect(socketAntecedent3);
                    
                    False expressionFalse = new False(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00011", null);
                    MaleSocket socketFalse = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionFalse);
                    socketOr.getChild(index++).connect(socketFalse);
                    
                    False expressionFalse2 = new False(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00012", "My False expression");
                    MaleSocket socketFalse2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionFalse2);
                    socketOr.getChild(index++).connect(socketFalse2);
                    
                    Hold expressionHold = new Hold(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00013", null);
                    MaleSocket socketHold = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionHold);
                    socketOr.getChild(index++).connect(socketHold);
                    
                    Hold expressionHold2 = new Hold(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00014", "My Hold expression");
                    MaleSocket socketHold2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionHold2);
                    socketOr.getChild(index++).connect(socketHold2);
                    
                    Hold expressionHold3 = new Hold(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00015", "My other Hold expression");
                    MaleSocket socketHold3 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionHold3);
                    socketHold2.getChild(0).connect(socketHold3);
                    
                    Hold expressionHold4 = new Hold(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00016", "My other Hold expression");
                    MaleSocket socketHold4 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionHold4);
                    socketHold2.getChild(1).connect(socketHold4);
                    
                    ResetOnTrue expressionResetOnTrue = new ResetOnTrue(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00017", null);
                    MaleSocket socketResetOnTrue = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue);
                    socketOr.getChild(index++).connect(socketResetOnTrue);
                    
                    ResetOnTrue expressionResetOnTrue2 = new ResetOnTrue(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00018", "My ResetOnTrue expression");
                    MaleSocket socketResetOnTrue2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue2);
                    expressionResetOnTrue.getChild(0).connect(socketResetOnTrue2);
                    
                    ResetOnTrue expressionResetOnTrue3 = new ResetOnTrue(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00019", "My ResetOnTrue expression");
                    MaleSocket socketResetOnTrue3 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionResetOnTrue3);
                    expressionResetOnTrue.getChild(1).connect(socketResetOnTrue3);
                    
                    ExpressionTimer expressionTimer = new ExpressionTimer(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00020", null);
                    MaleSocket socketTimer = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTimer);
                    socketOr.getChild(index++).connect(socketTimer);
                    
                    ExpressionTimer expressionTimer2 = new ExpressionTimer(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00021", "My Timer expression");
                    MaleSocket socketTimer2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTimer2);
                    socketOr.getChild(index++).connect(socketTimer2);
                    
                    TriggerOnce expressionTriggerOnce = new TriggerOnce(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00022", null);
                    MaleSocket socketTriggerOnce = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTriggerOnce);
                    socketOr.getChild(index++).connect(socketTriggerOnce);
                    
                    TriggerOnce expressionTriggerOnce2 = new TriggerOnce(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00023", "My TriggerOnce expression");
                    MaleSocket socketTriggerOnce2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTriggerOnce2);
                    expressionTriggerOnce.getChild(0).connect(socketTriggerOnce2);
                    
                    True expressionTrue = new True(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00024", null);
                    MaleSocket socketTrue = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTrue);
                    socketOr.getChild(index++).connect(socketTrue);
                    
                    True expressionTrue2 = new True(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00025", "My True expression");
                    MaleSocket socketTrue2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTrue2);
                    socketOr.getChild(index++).connect(socketTrue2);
                    
                    ExpressionLight expressionLight = new ExpressionLight(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00026", null);
                    expressionLight.setLight(light1);
                    expressionLight.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionLight.setLightState(ExpressionLight.LightState.ON);
                    MaleSocket socketLight = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight);
                    socketOr.getChild(index++).connect(socketLight);
                    
                    ExpressionLight expressionLight2 = new ExpressionLight(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00027", "My light");
                    expressionLight2.setLight((Light)null);
                    expressionLight2.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionLight2.setLightState(ExpressionLight.LightState.ON);
                    MaleSocket socketLight2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight2);
                    socketOr.getChild(index++).connect(socketLight2);
                    
                    ExpressionSensor expressionSensor = new ExpressionSensor(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00028", null);
                    expressionSensor.setSensor(sensor1);
                    expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionSensor.setSensorState(ExpressionSensor.SensorState.ACTIVE);
                    MaleSocket socketSensor = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
                    socketOr.getChild(index++).connect(socketSensor);
                    
                    ExpressionSensor expressionSensor2 = new ExpressionSensor(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00029", "My sensor");
                    expressionSensor2.setSensor((Sensor)null);
                    expressionSensor2.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionSensor2.setSensorState(ExpressionSensor.SensorState.ACTIVE);
                    MaleSocket socketSensor2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor2);
                    socketOr.getChild(index++).connect(socketSensor2);
                    
                    ExpressionTurnout expressionTurnout = new ExpressionTurnout(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00030", null);
                    expressionTurnout.setTurnout(turnout1);
                    expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionTurnout.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
                    MaleSocket socketTurnout = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout);
                    socketOr.getChild(index++).connect(socketTurnout);
                    
                    ExpressionTurnout expressionTurnout2 = new ExpressionTurnout(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:00031", "My turnout");
                    expressionTurnout2.setTurnout((Turnout)null);
                    expressionTurnout2.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionTurnout2.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
                    MaleSocket socketTurnout2 = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTurnout2);
                    socketOr.getChild(index++).connect(socketTurnout2);
                    
                    
                    
                    // If then
                    Many expressionMany = new Many(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00010", null);
                    MaleSocket socketSecondMany = InstanceManager.getDefault(DigitalActionManager.class).registerAction(expressionMany);
                    socketIfThen.getChild(1).connect(socketSecondMany);
                    
                    // If else
                    Many expressionMany2 = new Many(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:10010", null);
                    MaleSocket socketSecondMany2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(expressionMany2);
                    socketIfThen.getChild(2).connect(socketSecondMany2);
                    
                    index = 0;
/*                    
                    HoldAnything actionHoldAnything = new HoldAnything(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00011", "My hold anything");
                    MaleSocket socketHoldAnything = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionHoldAnything);
                    socketSecondMany.getChild(index++).connect(socketHoldAnything);
*/                    
                    IfThenElse actionIfThen2 = new IfThenElse(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00012", "My if then", IfThenElse.Type.TRIGGER_ACTION);
                    MaleSocket socketIfThen2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionIfThen2);
                    socketSecondMany.getChild(index++).connect(socketIfThen2);
                    
                    Many actionMany = new Many(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00013", "My many");
                    MaleSocket socketMany2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMany);
                    socketSecondMany.getChild(index++).connect(socketMany2);
                    
                    ShutdownComputer actionShutdownComputer = new ShutdownComputer(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00014", "My shutdown computer", 10);
                    MaleSocket socketShutdownComputer = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionShutdownComputer);
                    socketSecondMany.getChild(index++).connect(socketShutdownComputer);
                    
                    DoAnalogAction actionDoAnalogAction = new DoAnalogAction(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00015", null);
                    MaleSocket socketDoAnalogAction = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoAnalogAction);
                    socketSecondMany.getChild(index++).connect(socketDoAnalogAction);
                    
                    DoStringAction actionDoStringAction = new DoStringAction(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00016", null);
                    MaleSocket socketDoStringAction = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDoStringAction);
                    socketSecondMany.getChild(index++).connect(socketDoStringAction);
                    
                    ShutdownComputer expressionShutdownComputer = new ShutdownComputer(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00017", null, 10);
                    MaleSocket socketShutdownComputer2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(expressionShutdownComputer);
                    socketSecondMany.getChild(index++).connect(socketShutdownComputer2);
                    
                    ActionLight actionLight = new ActionLight(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00018", null);
//                    actionLight.setLight(light2);
                    actionLight.setLightState(ActionLight.LightState.ON);
                    socketLight2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
                    socketSecondMany.getChild(index++).connect(socketLight2);
                    
                    actionLight = new ActionLight(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:10019", "My light action");
                    actionLight.setLight(light2);
                    actionLight.setLightState(ActionLight.LightState.ON);
                    socketLight2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
                    socketSecondMany.getChild(index++).connect(socketLight2);
                    
                    ActionSensor actionSensor = new ActionSensor(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00020", null);
//                    actionSensor.setSensor(sensor2);
                    actionSensor.setSensorState(ActionSensor.SensorState.ACTIVE);
                    socketSensor2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensor);
                    socketSecondMany.getChild(index++).connect(socketSensor2);
                    
                    actionSensor = new ActionSensor(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00021", "My sensor action");
                    actionSensor.setSensor(sensor2);
                    actionSensor.setSensorState(ActionSensor.SensorState.ACTIVE);
                    socketSensor2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensor);
                    socketSecondMany.getChild(index++).connect(socketSensor2);
                    
                    ActionTurnout actionTurnout = new ActionTurnout(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00022", null);
//                    actionTurnout.setTurnout(turnout2);
                    actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
                    socketTurnout2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
                    socketSecondMany.getChild(index++).connect(socketTurnout2);
                    
                    actionTurnout = new ActionTurnout(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00023", "My turnout action");
                    actionTurnout.setTurnout(turnout2);
                    actionTurnout.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
                    socketTurnout2 = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionTurnout);
                    socketSecondMany.getChild(index++).connect(socketTurnout2);
                    
                    AnalogExpressionConstant locoConstant = new AnalogExpressionConstant(logixNG_Manager.getSystemNamePrefix()+"AE:AUTO:00001", null);
                    locoConstant.setValue(10);
                    MaleSocket socketLocoConstant = InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(locoConstant);
                    
                    AnalogExpressionConstant speedConstant = new AnalogExpressionConstant(logixNG_Manager.getSystemNamePrefix()+"AE:AUTO:00002", null);
                    speedConstant.setValue(0.5);
                    MaleSocket socketSpeedConstant = InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(speedConstant);
                    
                    Sensor sensorDirection = InstanceManager.getDefault(SensorManager.class).provide("IS100");
                    sensorDirection.setCommandedState(Sensor.ACTIVE);
                    ExpressionSensor directionSensor = new ExpressionSensor(logixNG_Manager.getSystemNamePrefix()+"DE:AUTO:10028", null);
                    expressionSensor.setSensor(sensorDirection);
                    expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS);
                    expressionSensor.setSensorState(ExpressionSensor.SensorState.ACTIVE);
                    MaleSocket socketDirectionSensor = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(directionSensor);
                    
                    ActionThrottle actionThrottle = new ActionThrottle(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:10023", "My throttle action");
                    actionThrottle.getChild(0).connect(socketLocoConstant);
                    actionThrottle.getChild(1).connect(socketSpeedConstant);
                    actionThrottle.getChild(2).connect(socketDirectionSensor);
                    MaleSocket socketThrottle = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle);
                    socketSecondMany.getChild(index++).connect(socketThrottle);
                    
                    actionThrottle = new ActionThrottle(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:10024", "My other throttle action");
                    socketThrottle = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle);
                    socketSecondMany.getChild(index++).connect(socketThrottle);
                    
                    Memory memory1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
                    Memory memory2 = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
                    Memory memory3 = InstanceManager.getDefault(MemoryManager.class).provide("IM3");
                    Memory memory4 = InstanceManager.getDefault(MemoryManager.class).provide("IM4");
                    
                    AnalogExpressionMemory analogExpressionMemory = new AnalogExpressionMemory(logixNG_Manager.getSystemNamePrefix()+"AE:AUTO:00003", null);
                    analogExpressionMemory.setMemory(memory1);
                    MaleSocket socketAnalogExpressionMemory = InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpressionMemory);

                    AnalogActionMemory analogActionMemory = new AnalogActionMemory(logixNG_Manager.getSystemNamePrefix()+"AA:AUTO:00001", null);
                    analogActionMemory.setMemory(memory2);
                    MaleSocket socketAnalogActionMemory = InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);

                    DoAnalogAction doAnalogAction = new DoAnalogAction(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00101", "My do analog action");
                    doAnalogAction.getChild(0).connect(socketAnalogExpressionMemory);
//DANIEL                    doAnalogAction.setAnalogExpressionSocketSystemName(socketAnalogExpressionMemory.getSystemName());
                    doAnalogAction.getChild(1).connect(socketAnalogActionMemory);
//DANIEL                    doAnalogAction.setAnalogActionSocketSystemName(socketAnalogActionMemory.getSystemName());
                    MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(doAnalogAction);
                    socketSecondMany.getChild(index++).connect(socket);
                    
                    analogExpressionMemory = new AnalogExpressionMemory(logixNG_Manager.getSystemNamePrefix()+"AE:AUTO:00004", "My expression");
                    socketAnalogExpressionMemory = InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpressionMemory);

                    analogActionMemory = new AnalogActionMemory(logixNG_Manager.getSystemNamePrefix()+"AA:AUTO:00002", "My action");
                    socketAnalogActionMemory = InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);

                    doAnalogAction = new DoAnalogAction(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00102", null);
//                    doAnalogAction.setAnalogExpressionSocketSystemName(socketAnalogExpressionMemory.getSystemName());
                    doAnalogAction.getChild(0).connect(socketAnalogExpressionMemory);
//                    doAnalogAction.setAnalogActionSocketSystemName(socketAnalogActionMemory.getSystemName());
                    doAnalogAction.getChild(1).connect(socketAnalogActionMemory);
                    socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(doAnalogAction);
                    socketSecondMany.getChild(index++).connect(socket);
                    
                    StringExpressionMemory stringExpressionMemory = new StringExpressionMemory(logixNG_Manager.getSystemNamePrefix()+"SE:AUTO:00001", null);
                    stringExpressionMemory.setMemory(memory3);
                    MaleSocket socketStringExpressionMemory = InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpressionMemory);

                    StringActionMemory stringActionMemory = new StringActionMemory(logixNG_Manager.getSystemNamePrefix()+"SA:AUTO:00001", null);
                    stringActionMemory.setMemory(memory4);
                    MaleSocket socketStringActionMemory = InstanceManager.getDefault(StringActionManager.class).registerAction(stringActionMemory);

                    DoStringAction doStringAction = new DoStringAction(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00103", "My do string action");
                    doStringAction.getChild(0).connect(socketStringExpressionMemory);
//DANIEL                    doStringAction.setStringExpressionSocketSystemName(socketStringExpressionMemory.getSystemName());
                    doStringAction.getChild(1).connect(socketStringActionMemory);
//DANIEL                    doStringAction.setStringActionSocketSystemName(socketStringActionMemory.getSystemName());
                    socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(doStringAction);
                    socketSecondMany.getChild(index++).connect(socket);

                    stringExpressionMemory = new StringExpressionMemory(logixNG_Manager.getSystemNamePrefix()+"SE:AUTO:00002", "My expression");
                    socketStringExpressionMemory = InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpressionMemory);

                    stringActionMemory = new StringActionMemory(logixNG_Manager.getSystemNamePrefix()+"SA:AUTO:00002", "My action");
                    socketStringActionMemory = InstanceManager.getDefault(StringActionManager.class).registerAction(stringActionMemory);

                    doStringAction = new DoStringAction(logixNG_Manager.getSystemNamePrefix()+"DA:AUTO:00104", null);
                    doStringAction.getChild(0).connect(socketStringExpressionMemory);
//DANIEL                    doStringAction.setStringExpressionSocketSystemName(socketStringExpressionMemory.getSystemName());
                    doStringAction.getChild(1).connect(socketStringActionMemory);
//DANIEL                    doStringAction.setStringActionSocketSystemName(socketStringActionMemory.getSystemName());
                    socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(doStringAction);
                    socketSecondMany.getChild(index++).connect(socket);
                    
                    logixNG_Manager.resolveAllTrees();
                    logixNG_Manager.setupAllLogixNGs();
                    
                    logixNG.setEnabled(true);
                    conditionalNG.setEnabled(true);
                    
                    // Store panels
                    jmri.ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
                    if (cm == null) {
                        log.error("Failed to make backup due to unable to get default configure manager");
                    } else {
                        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
                        File firstFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG_temp.xml");
                        File secondFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG.xml");
                        log.info("Temporary first file: %s%n", firstFile.getAbsoluteFile());
                        log.info("Temporary second file: %s%n", secondFile.getAbsoluteFile());
//                        System.out.format("Temporary file: %s%n", file.getAbsoluteFile());

                        final String treeIndent = "   ";
                        StringWriter stringWriter = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(stringWriter);
                        logixNG.printTree(Locale.ENGLISH, printWriter, treeIndent);
                        final String originalTree = stringWriter.toString();
                        
                        boolean results = cm.storeUser(firstFile);
                        log.debug(results ? "store was successful" : "store failed");
                        if (!results) {
                            log.error("Failed to store panel");
                            throw new RuntimeException("Failed to store panel");
//                            System.exit(-1);
                        }
                        
                        // Add the header comment to the xml file
                        addHeader(firstFile, secondFile);
                        
                        if (load == 1) {
                            java.util.Set<LogixNG> logixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
                            for (LogixNG aLogixNG : logixNG_Set) {
                                logixNG_Manager.deleteLogixNG(aLogixNG);
                            }
                            
                            java.util.Set<ConditionalNG> conditionalNG_Set = new java.util.HashSet<>(InstanceManager.getDefault(ConditionalNG_Manager.class).getNamedBeanSet());
                            for (ConditionalNG aConditionalNG : conditionalNG_Set) {
                                InstanceManager.getDefault(ConditionalNG_Manager.class).deleteConditionalNG(aConditionalNG);
                            }
                            java.util.SortedSet<MaleAnalogActionSocket> set1 = InstanceManager.getDefault(AnalogActionManager.class).getNamedBeanSet();
                            List<MaleSocket> l = new ArrayList<>(set1);
                            for (MaleSocket x1 : l) {
                                InstanceManager.getDefault(AnalogActionManager.class).deleteBean((MaleAnalogActionSocket)x1, "DoDelete");
                            }
                            java.util.SortedSet<MaleAnalogExpressionSocket> set2 = InstanceManager.getDefault(AnalogExpressionManager.class).getNamedBeanSet();
                            l = new ArrayList<>(set2);
                            for (MaleSocket x2 : l) {
                                InstanceManager.getDefault(AnalogExpressionManager.class).deleteBean((MaleAnalogExpressionSocket)x2, "DoDelete");
                            }
                            java.util.SortedSet<MaleDigitalActionSocket> set3 = InstanceManager.getDefault(DigitalActionManager.class).getNamedBeanSet();
                            l = new ArrayList<>(set3);
                            for (MaleSocket x3 : l) {
                                InstanceManager.getDefault(DigitalActionManager.class).deleteBean((MaleDigitalActionSocket)x3, "DoDelete");
                            }
                            java.util.SortedSet<MaleDigitalExpressionSocket> set4 = InstanceManager.getDefault(DigitalExpressionManager.class).getNamedBeanSet();
                            l = new ArrayList<>(set4);
                            for (MaleSocket x4 : l) {
                                InstanceManager.getDefault(DigitalExpressionManager.class).deleteBean((MaleDigitalExpressionSocket)x4, "DoDelete");
                            }
                            java.util.SortedSet<MaleStringActionSocket> set5 = InstanceManager.getDefault(StringActionManager.class).getNamedBeanSet();
                            l = new ArrayList<>(set5);
                            for (MaleSocket x5 : l) {
                                InstanceManager.getDefault(StringActionManager.class).deleteBean((MaleStringActionSocket)x5, "DoDelete");
                            }
                            java.util.SortedSet<MaleStringExpressionSocket> set6 = InstanceManager.getDefault(StringExpressionManager.class).getNamedBeanSet();
                            l = new ArrayList<>(set6);
                            for (MaleSocket x6 : l) {
                                InstanceManager.getDefault(StringExpressionManager.class).deleteBean((MaleStringExpressionSocket)x6, "DoDelete");
                            }

                            results = cm.load(secondFile);
                            log.debug(results ? "load was successful" : "store failed");
                            if (results) {
                                logixNG_Manager.resolveAllTrees();
                                logixNG_Manager.setupAllLogixNGs();
                                
                                logixNG = logixNG_Manager.getLogixNG("Yet another logixNG");
                                stringWriter = new StringWriter();
                                printWriter = new PrintWriter(stringWriter);
                                logixNG.printTree(Locale.ENGLISH, printWriter, treeIndent);
                                
                                if (!originalTree.equals(stringWriter.toString())) {
                                    log.error("--------------------------------------------");
                                    log.error("Old tree:");
                                    log.error("XXX"+originalTree+"XXX");
                                    log.error("--------------------------------------------");
                                    log.error("New tree:");
                                    log.error("XXX"+stringWriter.toString()+"XXX");
                                    log.error("--------------------------------------------");
                                    
//                                    log.error(InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName(originalTree).getChild(0).getConnectedSocket().getSystemName());
                                    
                                    throw new RuntimeException("tree has changed");
                                }
                            } else {
                                throw new RuntimeException("Failed to load panel");
                            }
                        }
                    }
                }
                
            } catch (JmriException ex) {
                log.error("Failed to store panel", ex);
                throw new RuntimeException("Failed to store panel");
//                System.exit(-1);
            }
        }
    }
    
    
    private void addHeader(File inFile, File outFile) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inFile, StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile, StandardCharsets.UTF_8)))) {
            
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
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();
        
        JUnitUtil.initLogixNGManager();
   }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(StoreAndLoadTest.class);

}
