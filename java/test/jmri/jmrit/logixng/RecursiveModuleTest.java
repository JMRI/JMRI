package jmri.jmrit.logixng;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.FemaleSocketManager;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.digital.actions.ActionListenOnBeans;
import jmri.jmrit.logixng.digital.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.jmrit.logixng.digital.actions.ActionMemory;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.actions.Many;
import jmri.jmrit.logixng.digital.actions.ModuleDigitalAction;
import jmri.jmrit.logixng.digital.actions.PushStack;
import jmri.jmrit.logixng.digital.actions.PopStack;
import jmri.jmrit.logixng.digital.expressions.ExpressionMemory;
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
        n.setValue(0);
        Assert.assertEquals("1", result.getValue());
//        if (1==1) return;
        
        n.setValue(1);
        Assert.assertEquals("1", result.getValue());
        if (1==1) return;
        
        n.setValue(2);
        Assert.assertEquals("2", result.getValue());
        if (1==1) return;
        
        n.setValue(3);
        Assert.assertEquals((Object)3, result.getValue());
        
        n.setValue(4);
        Assert.assertEquals((Object)5, result.getValue());
        
        n.setValue(5);
        Assert.assertEquals((Object)8, result.getValue());
        
        n.setValue(6);
        Assert.assertEquals((Object)13, result.getValue());
        
        n.setValue(7);
        Assert.assertEquals((Object)21, result.getValue());
        
        n.setValue(8);
        Assert.assertEquals((Object)34, result.getValue());
        
        n.setValue(9);
        Assert.assertEquals((Object)55, result.getValue());
        
        n.setValue(10);
        Assert.assertEquals((Object)89, result.getValue());
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
        
        
//        Map<String, FemaleSocketManager.SocketType> socketTypes = InstanceManager.getDefault(FemaleSocketManager.class).getSocketTypes();
        
//        for (FemaleSocketManager.SocketType socketType : socketTypes.values()) {
//            System.out.format("Socket: %s, %s, %s%n", socketType.getName(), socketType.getDescr(), socketType.getClass());
//        }
        
        
        InstanceManager.getDefault(NamedTableManager.class).newStack("IQT1", null);
        
        n = InstanceManager.getDefault(MemoryManager.class).provide("IMN");
        n.setValue(1);
        
        result = InstanceManager.getDefault(MemoryManager.class).provide("IMRESULT");
        result.setValue("Hejsan");
        
        InstanceManager.getDefault(MemoryManager.class).provide("IM_TEMP1");
        InstanceManager.getDefault(MemoryManager.class).provide("IM_TEMP2");
        InstanceManager.getDefault(MemoryManager.class).provide("IM_TEMP3");
        InstanceManager.getDefault(MemoryManager.class).provide("IM_TEMP4");
        
        
//        Sensor s = InstanceManager.getDefault(SensorManager.class).provide("IS1");
//        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        
        
        
        Module module = InstanceManager.getDefault(ModuleManager.class).createModule("IQM1", null);
        
        module.setRootSocketType(InstanceManager.getDefault(FemaleSocketManager.class)
                .getSocketTypeByType("DefaultFemaleDigitalActionSocket"));
        
        Many many901 = new Many("IQDA901", null);
        MaleSocket manySocket901 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many901);
        module.getRootSocket().connect(manySocket901);
        
        PopStack actionPop911 = new PopStack("IQDA911", null);
        actionPop911.setStack("IQT1");
        actionPop911.setMemory("IM_TEMP1");
        MaleSocket ifThenElseSocket912 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPop911);
        manySocket901.getChild(0).connect(ifThenElseSocket912);
        
        IfThenElse ifThenElse912 = new IfThenElse("IQDA912", null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse912.setType(IfThenElse.Type.CONTINOUS_ACTION);
        ifThenElseSocket912 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse912);
        manySocket901.getChild(1).connect(ifThenElseSocket912);
        
        ExpressionMemory expressionMemory913 = new ExpressionMemory("IQDE913", null);
        expressionMemory913.setMemory("IM_TEMP1");
        expressionMemory913.setConstantValue("0");
        expressionMemory913.setCompareTo(ExpressionMemory.CompareTo.VALUE);
        expressionMemory913.setMemoryOperation(ExpressionMemory.MemoryOperation.EQUAL);
        MaleSocket maleSocket913 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionMemory913);
        ifThenElseSocket912.getChild(0).connect(maleSocket913);
        
        ActionMemory actionMemory914 = new ActionMemory("IQDA914", null);
        actionMemory914.setMemory("IMRESULT");
        actionMemory914.setNewValue("1");
        actionMemory914.setMemoryOperation(ActionMemory.MemoryOperation.SET_TO_STRING);
        MaleSocket maleSocket914 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory914);
        ifThenElseSocket912.getChild(1).connect(maleSocket914);
        
        
        IfThenElse ifThenElse915 = new IfThenElse("IQDA915", null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse915.setType(IfThenElse.Type.CONTINOUS_ACTION);
        MaleSocket ifThenElseSocket915 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse915);
        ifThenElseSocket912.getChild(2).connect(ifThenElseSocket915);
        
        ExpressionMemory expressionMemory916 = new ExpressionMemory("IQDE916", null);
        expressionMemory916.setMemory("IM_TEMP1");
        expressionMemory916.setConstantValue("1");
        expressionMemory916.setCompareTo(ExpressionMemory.CompareTo.VALUE);
        expressionMemory916.setMemoryOperation(ExpressionMemory.MemoryOperation.EQUAL);
        MaleSocket maleSocket916 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionMemory916);
        ifThenElse915.getChild(0).connect(maleSocket916);
        
        ActionMemory actionMemory917 = new ActionMemory("IQDA917", null);
        actionMemory917.setMemory("IMRESULT");
        actionMemory917.setNewValue("1");
        actionMemory917.setMemoryOperation(ActionMemory.MemoryOperation.SET_TO_STRING);
        MaleSocket maleSocket917 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory917);
        ifThenElse915.getChild(1).connect(maleSocket917);
        
        
        
        
        Many many921 = new Many("IQDA921", null);
        MaleSocket manySocket921 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many921);
        ifThenElse915.getChild(2).connect(manySocket921);
        
        PushStack actionPush922 = new PushStack("IQDA922", null);
        actionPush922.setMemory("IM_TEMP1");
        actionPush922.setStack("IQT1");
        actionPush922.setOperation(PushStack.Operation.MEMORY);
        MaleSocket maleSocket922 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPush922);
        manySocket921.getChild(0).connect(maleSocket922);
        
        // Push parameter n on the stack
        PushStack actionPush924 = new PushStack("IQDA924", null);
        actionPush924.setData("{IM_TEMP1} - 1");
        actionPush924.setStack("IQT1");
        actionPush924.setOperation(PushStack.Operation.FORMULA);
        MaleSocket maleSocket924 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPush924);
        manySocket921.getChild(1).connect(maleSocket924);
        
        // Call the module for n-1
        ModuleDigitalAction moduleDigitalAction925 = new ModuleDigitalAction("IQDA925", null);
        moduleDigitalAction925.setModule("IQM1");
        MaleSocket maleSocket925 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction925);
        manySocket921.getChild(2).connect(maleSocket925);
        
        // Restore the memory IM_TEMP2
        PopStack actionPop926 = new PopStack("IQDA929", null);
        actionPop926.setMemory("IM_TEMP2");
        actionPop926.setStack("IQT1");
        MaleSocket maleSocket926 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPop926);
        manySocket921.getChild(3).connect(maleSocket926);
        
        // Store the result on the stack
        PushStack actionPush927 = new PushStack("IQDA927", null);
        actionPush927.setMemory("IMRESULT");
        actionPush927.setStack("IQT1");
        actionPush927.setOperation(PushStack.Operation.MEMORY);
        MaleSocket maleSocket927 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPush927);
        manySocket921.getChild(4).connect(maleSocket927);
        
        
        
        
        
        
        
        // Push parameter n on the stack
        PushStack actionPush931 = new PushStack("IQDA931", null);
        actionPush931.setData("{IM_TEMP2} - 2");
        actionPush931.setStack("IQT1");
        actionPush931.setOperation(PushStack.Operation.FORMULA);
        MaleSocket maleSocket931 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPush931);
        manySocket921.getChild(5).connect(maleSocket931);
        
        // Call the module
        ModuleDigitalAction moduleDigitalAction932 = new ModuleDigitalAction("IQDA932", null);
        moduleDigitalAction932.setModule("IQM1");
        MaleSocket maleSocket932 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction932);
        manySocket921.getChild(6).connect(maleSocket932);
        
        // Restore the old result to memory IM_TEMP2
        PopStack actionPop933 = new PopStack("IQDA933", null);
        actionPop933.setMemory("IM_TEMP2");
        actionPop933.setStack("IQT1");
        MaleSocket maleSocket933 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPop933);
        manySocket921.getChild(7).connect(maleSocket933);
        
        // Store the sum in IMRESULT
        ActionMemory actionMemory934 = new ActionMemory("IQDA934", null);
        actionMemory934.setMemory("IMRESULT");
        actionMemory934.setNewValue("{IMRESULT} + {IM_TEMP2}");
        actionMemory934.setMemoryOperation(ActionMemory.MemoryOperation.FORMULA);
        MaleSocket maleSocket934 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory934);
        manySocket921.getChild(8).connect(maleSocket934);
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
/*        
        ActionMemory actionMemory918 = new ActionMemory("IQDA918", null);
        actionMemory918.setMemory("IMRESULT");
        actionMemory918.setNewValue("aaa");
        actionMemory917.setMemoryOperation(ActionMemory.MemoryOperation.SET_TO_STRING);
        MaleSocket maleSocket918 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory918);
        ifThenElse915.getChild(2).connect(maleSocket918);
*/        
        
        
        
        
        
        
        
        
/*        
        PushStack actionPush = new PushStack("IQDA3", null);
        actionPush.setMemory("IMN");
        actionPush.setStack("IQT1");
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPush);
        manySocket.getChild(1).connect(maleSocket);
*/        
        
        
        
        // if IM_TEMP1 == 0 return 1
        // if IM_TEMP1 == 1 return 1
        // IM_TEMP2 = module(IM_TEMP1-1)
        // IM_TEMP3 = module(IM_TEMP1-2)
        // return IM_TEMP2 + IM_TEMP3
        
        
        
        
        
        
        
/*        
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA904", null);
        MaleSocket maleSocket901 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doAnalogAction);
        manySocket1.getChild(0).connect(maleSocket901);
        
        
        AnalogExpressionMemory analogExpressionMemory = new AnalogExpressionMemory("IQAE901", null);
        analogExpressionMemory.setMemory("IMN");
        MaleSocket maleSocket902 =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpressionMemory);
        doAnalogAction.getChild(0).connect(maleSocket902);
        
        
        AnalogActionMemory analogActionMemory = new AnalogActionMemory("IQAA901", null);
        analogActionMemory.setMemory("IMRESULT");
        MaleSocket maleSocket903 =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(maleSocket903);
*/        
        
        
        
        
        
        
        
        
        
        
        
        
        
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
        
        PushStack actionPush = new PushStack("IQDA3", null);
        actionPush.setMemory("IMN");
        actionPush.setStack("IQT1");
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionPush);
        manySocket.getChild(1).connect(maleSocket);
        
        ModuleDigitalAction moduleDigitalAction = new ModuleDigitalAction("IQDA4", null);
        moduleDigitalAction.setModule("IQM1");
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(moduleDigitalAction);
        manySocket.getChild(2).connect(maleSocket2);
        
/*        
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA4", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doAnalogAction);
        manySocket.getChild(1).connect(maleSocket);
        
        
        AnalogExpressionMemory analogExpressionMemory = new AnalogExpressionMemory("IQAE1", null);
        analogExpressionMemory.setMemory("IMN");
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpressionMemory);
        doAnalogAction.getChild(0).connect(maleSocket2);
        
        
        AnalogActionMemory analogActionMemory = new AnalogActionMemory("IQAA1", null);
        analogActionMemory.setMemory("IMRESULT");
        MaleSocket maleSocket3 =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(maleSocket3);
*/        
        
        
        
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
        
/*        
        ActionTurnout action = new ActionTurnout("IQDA99", null);
        action.setTurnout("IT1");
        action.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
        MaleSocket maleSocket4 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        manySocket.getChild(2).connect(maleSocket4);
*/        
        
        
//        n.setValue(null);
//        s.setState(Sensor.INACTIVE);
//        t.setState(Turnout.CLOSED);
        
//        Assert.assertEquals(Turnout.CLOSED, t.getState());
        
//        n.setValue(0);
//        s.setState(Sensor.ACTIVE);
        
        
//        n.setValue(99.0);
//        Assert.assertEquals((Object)99.0, result.getValue());
        
//        Assert.assertEquals(Turnout.THROWN, t.getState());
        
        
        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
        
        
        
        final String treeIndent = "   ";
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        logixNG.printTree(Locale.ENGLISH, printWriter, treeIndent);
        System.out.println(stringWriter.toString());
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println();
        
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        module.printTree(Locale.ENGLISH, printWriter, treeIndent);
        System.out.println(stringWriter.toString());
        
        System.out.println();
        System.out.println("===========================================");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        
        
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
