package jmri.jmrit.logixng.configurexml;

import jmri.jmrit.logixng.TableRowOrColumn;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.BlockOrder;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.expressions.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.*;

import org.apache.commons.lang3.mutable.MutableInt;

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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Block block1 = InstanceManager.getDefault(BlockManager.class).provide("IB1");
        block1.setValue("Block 1 Value");
        Block block2 = InstanceManager.getDefault(BlockManager.class).provide("IB2");
        block2.setUserName("Some block");
        block1.setValue("Block 2 Value");
        Light light1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light1.setCommandedState(Light.OFF);
        Light light2 = InstanceManager.getDefault(LightManager.class).provide("IL2");
        light2.setUserName("Some light");
        light2.setCommandedState(Light.OFF);
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor1.setCommandedState(Sensor.INACTIVE);
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor2.setCommandedState(Sensor.INACTIVE);
        sensor2.setUserName("Some sensor");
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        turnout1.setCommandedState(Turnout.CLOSED);
        Turnout turnout2 = InstanceManager.getDefault(TurnoutManager.class).provide("IT2");
        turnout2.setCommandedState(Turnout.CLOSED);
        turnout2.setUserName("Some turnout");
        Turnout turnout3 = InstanceManager.getDefault(TurnoutManager.class).provide("IT3");
        turnout3.setCommandedState(Turnout.CLOSED);
        Turnout turnout4 = InstanceManager.getDefault(TurnoutManager.class).provide("IT4");
        turnout4.setCommandedState(Turnout.CLOSED);
        Turnout turnout5 = InstanceManager.getDefault(TurnoutManager.class).provide("IT5");
        turnout5.setCommandedState(Turnout.CLOSED);

        Memory memory1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        Memory memory2 = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        memory2.setUserName("Some memory");
        Memory memory3 = InstanceManager.getDefault(MemoryManager.class).provide("IM3");

        LogixManager logixManager = InstanceManager.getDefault(LogixManager.class);
        ConditionalManager conditionalManager = InstanceManager.getDefault(ConditionalManager.class);

        jmri.Logix logixIX1 = logixManager.createNewLogix("IX1", null);
        logixIX1.setEnabled(true);

        Conditional conditionalIX1C1 = conditionalManager.createNewConditional("IX1C1", "First conditional");
        logixIX1.addConditional(conditionalIX1C1.getSystemName(), 0);

        InstanceManager.getDefault(SignalHeadManager.class)
                .register(new VirtualSignalHead("IH1"));
        InstanceManager.getDefault(SignalHeadManager.class)
                .register(new VirtualSignalHead("IH2"));

        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        InstanceManager.getDefault(SignalMastManager.class)
                .provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");

        InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class)
                .register(new OBlock("OB98"));
        InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class)
                .register(new OBlock("OB99"));

        InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class)
                .register(new Warrant("IW99", "Test Warrant"));
        Warrant warrant = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).getWarrant("IW99");
        warrant.addBlockOrder(new BlockOrder(InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock("OB98")));
        warrant.addBlockOrder(new BlockOrder(InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock("OB99")));

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
        ConditionalNG_Manager conditionalNGManager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        AnalogActionManager analogActionManager = InstanceManager.getDefault(AnalogActionManager.class);
        AnalogExpressionManager analogExpressionManager = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalBooleanActionManager digitalBooleanActionManager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        StringActionManager stringActionManager = InstanceManager.getDefault(StringActionManager.class);
        StringExpressionManager stringExpressionManager = InstanceManager.getDefault(StringExpressionManager.class);
        LogixNG_InitializationManager logixNG_InitializationManager = InstanceManager.getDefault(LogixNG_InitializationManager.class);


        // Load table turnout_and_signals.csv
        jmri.jmrit.logixng.NamedTable csvTable =
                InstanceManager.getDefault(NamedTableManager.class)
                        .loadTableFromCSV("IQT1", null, "program:java/test/jmri/jmrit/logixng/panel_and_data_files/turnout_and_signals.csv");
        Assert.assertNotNull(csvTable);

        // Create module IQM1
        Module module =
                InstanceManager.getDefault(ModuleManager.class).createModule("IQM1", null,
                        InstanceManager.getDefault(FemaleSocketManager.class)
                                .getSocketTypeByType("DefaultFemaleDigitalActionSocket"));

        module.addParameter("other", true, true);
        module.addParameter("n", true, false);
        module.addParameter("result", false, true);
        module.addLocalVariable("temp1", SymbolTable.InitialValueType.None, null);
        module.addLocalVariable("temp2", SymbolTable.InitialValueType.None, null);

        DigitalMany many901 = new DigitalMany("IQDA901", null);
        MaleSocket manySocket901 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many901);
        module.getRootSocket().connect(manySocket901);




        // Create an empty LogixNG
        logixNG_Manager.createLogixNG("An empty logixNG");

        // Create a LogixNG with an empty ConditionalNG
        LogixNG logixNG = logixNG_Manager.createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG =
                conditionalNGManager.createConditionalNG(logixNG, "An empty conditionalNG");
        logixNG.setEnabled(false);
        conditionalNG.setEnabled(false);


        // Create an empty ConditionalNG on the debug thread
        conditionalNG =
                conditionalNGManager.createConditionalNG(
                        logixNG, "A second empty conditionalNG", LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        conditionalNG.setEnabled(false);


        // Create an empty ConditionalNG on another thread
        LogixNG_Thread.createNewThread(53, "My logixng thread");
        conditionalNG =
                conditionalNGManager.createConditionalNG(logixNG, "A third empty conditionalNG", 53);
        conditionalNG.setEnabled(false);


        // Create an empty ConditionalNG on another thread
        LogixNG_Thread.createNewThread("My other logixng thread");
        conditionalNG = conditionalNGManager.createConditionalNG(
                logixNG, "A fourth empty conditionalNG", LogixNG_Thread.getThreadID("My other logixng thread"));
        conditionalNG.setEnabled(false);


        logixNG = logixNG_Manager.createLogixNG("A logixNG in the initialization table");
        conditionalNGManager.createConditionalNG(logixNG, "Yet another another conditionalNG");
        logixNG_InitializationManager.add(logixNG);


        logixNG = logixNG_Manager.createLogixNG("Another logixNG in the initialization table");
        conditionalNGManager.createConditionalNG(logixNG, "Yet another another another conditionalNG");
        logixNG_InitializationManager.add(logixNG);


        logixNG = logixNG_Manager.createLogixNG("A logixNG");
        conditionalNG =
                conditionalNGManager.createConditionalNG(logixNG, "Yet another conditionalNG");
        logixNG.setEnabled(false);
        conditionalNG.setEnabled(true);

        FemaleSocket femaleRootSocket = conditionalNG.getFemaleSocket();
        MaleDigitalActionSocket actionManySocket =
                digitalActionManager.registerAction(new DigitalMany(
                                        digitalActionManager.getAutoSystemName(), null));
        femaleRootSocket.connect(actionManySocket);



        int indexAction = 0;


        ActionBlock actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        MaleSocket maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct / Direct :: SetValue
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Direct / Direct / Direct :: SetValue");

        actionBlock.setAddressing(NamedBeanAddressing.Direct);
        actionBlock.setBlock(block1);

        actionBlock.setOperationAddressing(NamedBeanAddressing.Direct);
        actionBlock.setOperationDirect(ActionBlock.DirectOperation.SetValue);

        actionBlock.setDataAddressing(NamedBeanAddressing.Direct);
        actionBlock.setBlockValue("ABC");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct :: SetOccupied
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Direct / Direct :: SetOccupied");

        actionBlock.setAddressing(NamedBeanAddressing.Direct);
        actionBlock.setBlock(block1);

        actionBlock.setOperationAddressing(NamedBeanAddressing.Direct);
        actionBlock.setOperationDirect(ActionBlock.DirectOperation.SetOccupied);

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / LocalVariable
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Direct / LocalVariable");

        actionBlock.setAddressing(NamedBeanAddressing.Direct);
        actionBlock.setBlock(block1);

        actionBlock.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionBlock.setOperationLocalVariable("index2");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// LocalVariable / Formula
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("LocalVariable / Formula");

        actionBlock.setAddressing(NamedBeanAddressing.LocalVariable);
        actionBlock.setLocalVariable("index");

        actionBlock.setOperationAddressing(NamedBeanAddressing.Formula);
        actionBlock.setOperationFormula("\"IT\"+index2");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Formula / Reference
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Formula / Reference");

        actionBlock.setAddressing(NamedBeanAddressing.Formula);
        actionBlock.setFormula("\"IT\"+index");

        actionBlock.setOperationAddressing(NamedBeanAddressing.Reference);
        actionBlock.setOperationReference("{IM2}");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Reference / Direct :: SetNullValue
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Reference / Direct :: SetAltColorOn");

        actionBlock.setAddressing(NamedBeanAddressing.Reference);
        actionBlock.setReference("{IM1}");

        actionBlock.setOperationAddressing(NamedBeanAddressing.Direct);
        actionBlock.setOperationDirect(ActionBlock.DirectOperation.SetNullValue);

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionClock actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// StartClock
        actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        actionClock.setComment("StartClock");
        actionClock.setBeanState(ActionClock.ClockState.StartClock);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// StopClock
        actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        actionClock.setComment("StopClock");
        actionClock.setBeanState(ActionClock.ClockState.StopClock);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// SetClock
        actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        actionClock.setComment("SetClock");
        actionClock.setBeanState(ActionClock.ClockState.SetClock);
        actionClock.setClockTime(720);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionLight actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.setLight(light1);
        actionLight.setBeanState(ActionLight.LightState.Off);
        actionLight.setAddressing(NamedBeanAddressing.Direct);
        actionLight.setFormula("\"IT\"+index");
        actionLight.setLocalVariable("index");
        actionLight.setReference("{IM1}");
        actionLight.setStateAddressing(NamedBeanAddressing.LocalVariable);
        actionLight.setStateFormula("\"IT\"+index2");
        actionLight.setStateLocalVariable("index2");
        actionLight.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.setLight(light1);
        actionLight.setBeanState(ActionLight.LightState.Off);
        actionLight.setAddressing(NamedBeanAddressing.LocalVariable);
        actionLight.setFormula("\"IT\"+index");
        actionLight.setLocalVariable("index");
        actionLight.setReference("{IM1}");
        actionLight.setStateAddressing(NamedBeanAddressing.Formula);
        actionLight.setStateFormula("\"IT\"+index2");
        actionLight.setStateLocalVariable("index2");
        actionLight.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.setLight(light1);
        actionLight.setBeanState(ActionLight.LightState.Off);
        actionLight.setAddressing(NamedBeanAddressing.Formula);
        actionLight.setFormula("\"IT\"+index");
        actionLight.setLocalVariable("index");
        actionLight.setReference("{IM1}");
        actionLight.setStateAddressing(NamedBeanAddressing.Reference);
        actionLight.setStateFormula("\"IT\"+index2");
        actionLight.setStateLocalVariable("index2");
        actionLight.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogErrorOnce);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.setLight(light1);
        actionLight.setBeanState(ActionLight.LightState.Off);
        actionLight.setAddressing(NamedBeanAddressing.Reference);
        actionLight.setFormula("\"IT\"+index");
        actionLight.setLocalVariable("index");
        actionLight.setReference("{IM1}");
        actionLight.setStateAddressing(NamedBeanAddressing.Direct);
        actionLight.setStateFormula("\"IT\"+index2");
        actionLight.setStateLocalVariable("index2");
        actionLight.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ShowDialogBox);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionListenOnBeans actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Light:"+light1.getSystemName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Light:"+light2.getUserName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Memory:"+memory1.getSystemName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Memory:"+memory2.getUserName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Sensor:"+sensor1.getSystemName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Sensor:"+sensor2.getUserName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Turnout:"+turnout1.getSystemName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Turnout:"+turnout2.getUserName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionLocalVariable actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.setMemory(memory3);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyMemoryToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.setMemory(memory3);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyVariableToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.setMemory(memory3);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.SetToNull);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.setMemory(memory3);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.SetToString);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.setMemory(memory3);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionMemory actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setAddressing(NamedBeanAddressing.Direct);
        actionMemory.setFormula("\"IT\"+index");
        actionMemory.setLocalVariable("index");
        actionMemory.setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
        actionMemory.setOtherMemory(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setAddressing(NamedBeanAddressing.Formula);
        actionMemory.setFormula("\"IT\"+index");
        actionMemory.setLocalVariable("index");
        actionMemory.setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        actionMemory.setOtherMemory(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setAddressing(NamedBeanAddressing.LocalVariable);
        actionMemory.setFormula("\"IT\"+index");
        actionMemory.setLocalVariable("index");
        actionMemory.setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        actionMemory.setOtherMemory(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setAddressing(NamedBeanAddressing.Reference);
        actionMemory.setFormula("\"IT\"+index");
        actionMemory.setLocalVariable("index");
        actionMemory.setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
        actionMemory.setOtherMemory(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setAddressing(NamedBeanAddressing.Direct);
        actionMemory.setFormula("\"IT\"+index");
        actionMemory.setLocalVariable("index");
        actionMemory.setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
        actionMemory.setOtherMemory(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionOBlock actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct / Direct :: SetValue
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Direct / Direct / Direct :: SetValue");

        actionOBlock.setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOBlock("OB99");

        actionOBlock.setOperationAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOperationDirect(ActionOBlock.DirectOperation.SetValue);

        actionOBlock.setDataAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOBlockValue("ABC");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct :: ClearError
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Direct / Direct :: ClearError");

        actionOBlock.setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOBlock("OB99");

        actionOBlock.setOperationAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOperationDirect(ActionOBlock.DirectOperation.ClearError);

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / LocalVariable
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Direct / LocalVariable");

        actionOBlock.setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOBlock("OB99");

        actionOBlock.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionOBlock.setOperationLocalVariable("index2");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// LocalVariable / Formula
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("LocalVariable / Formula");

        actionOBlock.setAddressing(NamedBeanAddressing.LocalVariable);
        actionOBlock.setLocalVariable("index");

        actionOBlock.setOperationAddressing(NamedBeanAddressing.Formula);
        actionOBlock.setOperationFormula("\"IT\"+index2");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Formula / Reference
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Formula / Reference");

        actionOBlock.setAddressing(NamedBeanAddressing.Formula);
        actionOBlock.setFormula("\"IT\"+index");

        actionOBlock.setOperationAddressing(NamedBeanAddressing.Reference);
        actionOBlock.setOperationReference("{IM2}");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogErrorOnce);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Reference / Direct :: SetOutOfService
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Reference / Direct :: SetOutOfService");

        actionOBlock.setAddressing(NamedBeanAddressing.Reference);
        actionOBlock.setReference("{IM1}");

        actionOBlock.setOperationAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOperationDirect(ActionOBlock.DirectOperation.SetOutOfService);

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ShowDialogBox);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionPower actionPower = new ActionPower(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionPower);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionPower = new ActionPower(digitalActionManager.getAutoSystemName(), null);
        actionPower.setComment("A comment");
        actionPower.setBeanState(ActionPower.PowerState.Off);
        maleSocket = digitalActionManager.registerAction(actionPower);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionPower = new ActionPower(digitalActionManager.getAutoSystemName(), null);
        actionPower.setComment("A comment");
        actionPower.setBeanState(ActionPower.PowerState.On);
        maleSocket = digitalActionManager.registerAction(actionPower);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionScript simpleScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(simpleScript);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        simpleScript.setComment("A comment");
        simpleScript.setScript("import java\n");
        simpleScript.setOperationAddressing(NamedBeanAddressing.Direct);
        simpleScript.setOperationFormula("a+b");
        simpleScript.setOperationLocalVariable("myVar");
        simpleScript.setOperationReference("{M1}");
        simpleScript.setScriptAddressing(NamedBeanAddressing.Formula);
        simpleScript.setScriptFormula("c+d");
        simpleScript.setScriptLocalVariable("myOtherVar");
        simpleScript.setScriptReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        simpleScript.setComment("A comment");
        simpleScript.setScript("myFile.py");
        simpleScript.setOperationAddressing(NamedBeanAddressing.Formula);
        simpleScript.setOperationFormula("a+b");
        simpleScript.setOperationLocalVariable("myVar");
        simpleScript.setOperationReference("{M1}");
        simpleScript.setScriptAddressing(NamedBeanAddressing.LocalVariable);
        simpleScript.setScriptFormula("c+d");
        simpleScript.setScriptLocalVariable("myOtherVar");
        simpleScript.setScriptReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        simpleScript.setComment("A comment");
        simpleScript.setScript("import java\n");
        simpleScript.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        simpleScript.setOperationFormula("a+b");
        simpleScript.setOperationLocalVariable("myVar");
        simpleScript.setOperationReference("{M1}");
        simpleScript.setScriptAddressing(NamedBeanAddressing.Reference);
        simpleScript.setScriptFormula("c+d");
        simpleScript.setScriptLocalVariable("myOtherVar");
        simpleScript.setScriptReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        simpleScript.setComment("A comment");
        simpleScript.setScript("import java\n");
        simpleScript.setOperationAddressing(NamedBeanAddressing.Reference);
        simpleScript.setOperationFormula("a+b");
        simpleScript.setOperationLocalVariable("myVar");
        simpleScript.setOperationReference("{M1}");
        simpleScript.setScriptAddressing(NamedBeanAddressing.Direct);
        simpleScript.setScriptFormula("c+d");
        simpleScript.setScriptLocalVariable("myOtherVar");
        simpleScript.setScriptReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionSensor actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSensor);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.setSensor(sensor1);
        actionSensor.setBeanState(ActionSensor.SensorState.Inactive);
        actionSensor.setAddressing(NamedBeanAddressing.Direct);
        actionSensor.setFormula("\"IT\"+index");
        actionSensor.setLocalVariable("index");
        actionSensor.setReference("{IM1}");
        actionSensor.setStateAddressing(NamedBeanAddressing.LocalVariable);
        actionSensor.setStateFormula("\"IT\"+index2");
        actionSensor.setStateLocalVariable("index2");
        actionSensor.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.setSensor(sensor1);
        actionSensor.setBeanState(ActionSensor.SensorState.Inactive);
        actionSensor.setAddressing(NamedBeanAddressing.LocalVariable);
        actionSensor.setFormula("\"IT\"+index");
        actionSensor.setLocalVariable("index");
        actionSensor.setReference("{IM1}");
        actionSensor.setStateAddressing(NamedBeanAddressing.Formula);
        actionSensor.setStateFormula("\"IT\"+index2");
        actionSensor.setStateLocalVariable("index2");
        actionSensor.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.setSensor(sensor1);
        actionSensor.setBeanState(ActionSensor.SensorState.Inactive);
        actionSensor.setAddressing(NamedBeanAddressing.Formula);
        actionSensor.setFormula("\"IT\"+index");
        actionSensor.setLocalVariable("index");
        actionSensor.setReference("{IM1}");
        actionSensor.setStateAddressing(NamedBeanAddressing.Reference);
        actionSensor.setStateFormula("\"IT\"+index2");
        actionSensor.setStateLocalVariable("index2");
        actionSensor.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.setSensor(sensor1);
        actionSensor.setBeanState(ActionSensor.SensorState.Inactive);
        actionSensor.setAddressing(NamedBeanAddressing.Reference);
        actionSensor.setFormula("\"IT\"+index");
        actionSensor.setLocalVariable("index");
        actionSensor.setReference("{IM1}");
        actionSensor.setStateAddressing(NamedBeanAddressing.Direct);
        actionSensor.setStateFormula("\"IT\"+index2");
        actionSensor.setStateLocalVariable("index2");
        actionSensor.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionSignalHead actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        actionSignalHead.setComment("A comment");
        actionSignalHead.setSignalHead("IH1");
        actionSignalHead.setAddressing(NamedBeanAddressing.Direct);
        actionSignalHead.setFormula("\"IT\"+index");
        actionSignalHead.setLocalVariable("index");
        actionSignalHead.setReference("{IM1}");
        actionSignalHead.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalHead.setOperationFormula("\"IT\"+index2");
        actionSignalHead.setOperationLocalVariable("index2");
        actionSignalHead.setOperationReference("{IM2}");
        actionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Formula);
        actionSignalHead.setAppearance(SignalHead.FLASHGREEN);
        actionSignalHead.setAppearanceFormula("\"IT\"+index3");
        actionSignalHead.setAppearanceLocalVariable("index3");
        actionSignalHead.setAppearanceReference("{IM3}");
        actionSignalHead.setExampleSignalHead("IH2");
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        actionSignalHead.setComment("A comment");
        actionSignalHead.setSignalHead("IH1");
        actionSignalHead.setAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalHead.setFormula("\"IT\"+index");
        actionSignalHead.setLocalVariable("index");
        actionSignalHead.setReference("{IM1}");
        actionSignalHead.setOperationAddressing(NamedBeanAddressing.Formula);
        actionSignalHead.setOperationFormula("\"IT\"+index2");
        actionSignalHead.setOperationLocalVariable("index2");
        actionSignalHead.setOperationReference("{IM2}");
        actionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Reference);
        actionSignalHead.setAppearance(SignalHead.FLASHLUNAR);
        actionSignalHead.setAppearanceFormula("\"IT\"+index3");
        actionSignalHead.setAppearanceLocalVariable("index3");
        actionSignalHead.setAppearanceReference("{IM3}");
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        actionSignalHead.setComment("A comment");
        actionSignalHead.setSignalHead("IH1");
        actionSignalHead.setAddressing(NamedBeanAddressing.Formula);
        actionSignalHead.setFormula("\"IT\"+index");
        actionSignalHead.setLocalVariable("index");
        actionSignalHead.setReference("{IM1}");
        actionSignalHead.setOperationAddressing(NamedBeanAddressing.Reference);
        actionSignalHead.setOperationFormula("\"IT\"+index2");
        actionSignalHead.setOperationLocalVariable("index2");
        actionSignalHead.setOperationReference("{IM2}");
        actionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Direct);
        actionSignalHead.setAppearance(SignalHead.FLASHRED);
        actionSignalHead.setAppearanceFormula("\"IT\"+index3");
        actionSignalHead.setAppearanceLocalVariable("index3");
        actionSignalHead.setAppearanceReference("{IM3}");
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        actionSignalHead.setComment("A comment");
        actionSignalHead.setSignalHead("IH1");
        actionSignalHead.setAddressing(NamedBeanAddressing.Reference);
        actionSignalHead.setFormula("\"IT\"+index");
        actionSignalHead.setLocalVariable("index");
        actionSignalHead.setReference("{IM1}");
        actionSignalHead.setOperationAddressing(NamedBeanAddressing.Direct);
        actionSignalHead.setOperationFormula("\"IT\"+index2");
        actionSignalHead.setOperationLocalVariable("index2");
        actionSignalHead.setOperationReference("{IM2}");
        actionSignalHead.setAppearanceAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalHead.setAppearance(SignalHead.FLASHYELLOW);
        actionSignalHead.setAppearanceFormula("\"IT\"+index3");
        actionSignalHead.setAppearanceLocalVariable("index3");
        actionSignalHead.setAppearanceReference("{IM3}");
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionSignalMast actionSignalMast = new ActionSignalMast(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSignalMast);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalMast = new ActionSignalMast(digitalActionManager.getAutoSystemName(), null);
        actionSignalMast.setComment("A comment");
        actionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.setAddressing(NamedBeanAddressing.Direct);
        actionSignalMast.setFormula("\"IT\"+index");
        actionSignalMast.setLocalVariable("index");
        actionSignalMast.setReference("{IM1}");
        actionSignalMast.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalMast.setOperationFormula("\"IT\"+index2");
        actionSignalMast.setOperationLocalVariable("index2");
        actionSignalMast.setOperationReference("{IM2}");
        actionSignalMast.setAspectAddressing(NamedBeanAddressing.Formula);
        actionSignalMast.setAspect("Medium Approach Slow");
        actionSignalMast.setAspectFormula("\"IT\"+index3");
        actionSignalMast.setAspectLocalVariable("index3");
        actionSignalMast.setAspectReference("{IM3}");
        actionSignalMast.setExampleSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        maleSocket = digitalActionManager.registerAction(actionSignalMast);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalMast = new ActionSignalMast(digitalActionManager.getAutoSystemName(), null);
        actionSignalMast.setComment("A comment");
        actionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.setAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalMast.setFormula("\"IT\"+index");
        actionSignalMast.setLocalVariable("index");
        actionSignalMast.setReference("{IM1}");
        actionSignalMast.setOperationAddressing(NamedBeanAddressing.Formula);
        actionSignalMast.setOperationFormula("\"IT\"+index2");
        actionSignalMast.setOperationLocalVariable("index2");
        actionSignalMast.setOperationReference("{IM2}");
        actionSignalMast.setAspectAddressing(NamedBeanAddressing.Reference);
        actionSignalMast.setAspect("Medium Approach");
        actionSignalMast.setAspectFormula("\"IT\"+index3");
        actionSignalMast.setAspectLocalVariable("index3");
        actionSignalMast.setAspectReference("{IM3}");
        maleSocket = digitalActionManager.registerAction(actionSignalMast);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalMast = new ActionSignalMast(digitalActionManager.getAutoSystemName(), null);
        actionSignalMast.setComment("A comment");
        actionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.setAddressing(NamedBeanAddressing.Formula);
        actionSignalMast.setFormula("\"IT\"+index");
        actionSignalMast.setLocalVariable("index");
        actionSignalMast.setReference("{IM1}");
        actionSignalMast.setOperationAddressing(NamedBeanAddressing.Reference);
        actionSignalMast.setOperationFormula("\"IT\"+index2");
        actionSignalMast.setOperationLocalVariable("index2");
        actionSignalMast.setOperationReference("{IM2}");
        actionSignalMast.setAspectAddressing(NamedBeanAddressing.Direct);
        actionSignalMast.setAspect("Approach");
        actionSignalMast.setAspectFormula("\"IT\"+index3");
        actionSignalMast.setAspectLocalVariable("index3");
        actionSignalMast.setAspectReference("{IM3}");
        maleSocket = digitalActionManager.registerAction(actionSignalMast);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalMast = new ActionSignalMast(digitalActionManager.getAutoSystemName(), null);
        actionSignalMast.setComment("A comment");
        actionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.setAddressing(NamedBeanAddressing.Reference);
        actionSignalMast.setFormula("\"IT\"+index");
        actionSignalMast.setLocalVariable("index");
        actionSignalMast.setReference("{IM1}");
        actionSignalMast.setOperationAddressing(NamedBeanAddressing.Direct);
        actionSignalMast.setOperationFormula("\"IT\"+index2");
        actionSignalMast.setOperationLocalVariable("index2");
        actionSignalMast.setOperationReference("{IM2}");
        actionSignalMast.setAspectAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalMast.setAspect("Medium Approach Slow");
        actionSignalMast.setAspectFormula("\"IT\"+index3");
        actionSignalMast.setAspectLocalVariable("index3");
        actionSignalMast.setAspectReference("{IM3}");
        maleSocket = digitalActionManager.registerAction(actionSignalMast);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionThrottle actionThrottle = new ActionThrottle(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionThrottle);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionThrottle = new ActionThrottle(digitalActionManager.getAutoSystemName(), null);
        actionThrottle.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(actionThrottle);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionTimer actionTimer = new ActionTimer(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionTimer);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTimer = new ActionTimer(digitalActionManager.getAutoSystemName(), null);
        actionTimer.setComment("A comment");
        actionTimer.setDelay(0, 100);
        actionTimer.setStartImmediately(false);
        actionTimer.setRunContinuously(true);
        maleSocket = digitalActionManager.registerAction(actionTimer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTimer = new ActionTimer(digitalActionManager.getAutoSystemName(), null);
        actionTimer.setComment("A comment");
        actionTimer.setNumActions(3);
        actionTimer.setDelay(0, 2400);
        actionTimer.setDelay(1, 10);
        actionTimer.setDelay(2, 500);
        actionTimer.setStartImmediately(true);
        actionTimer.setRunContinuously(false);
        actionTimer.setNumActions(2);
        maleSocket = digitalActionManager.registerAction(actionTimer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        And andTemp1 = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp1.setComment("Start expression");
        maleSocket = digitalExpressionManager.registerExpression(andTemp1);
        maleSocket.setEnabled(false);
        actionTimer.getChild(0).connect(maleSocket);

        andTemp1 = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp1.setComment("Stop expression");
        maleSocket = digitalExpressionManager.registerExpression(andTemp1);
        actionTimer.getChild(1).connect(maleSocket);

        DigitalMany manyTemp1 = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        manyTemp1.setComment("Action socket 1");
        maleSocket = digitalActionManager.registerAction(manyTemp1);
        maleSocket.setEnabled(false);
        actionTimer.getChild(2).connect(maleSocket);

        manyTemp1 = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        manyTemp1.setComment("Action socket 2");
        maleSocket = digitalActionManager.registerAction(manyTemp1);
        actionTimer.getChild(3).connect(maleSocket);


        ActionTurnout actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.setTurnout(turnout1);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Closed);
        actionTurnout.setAddressing(NamedBeanAddressing.Direct);
        actionTurnout.setFormula("\"IT\"+index");
        actionTurnout.setLocalVariable("index");
        actionTurnout.setReference("{IM1}");
        actionTurnout.setStateAddressing(NamedBeanAddressing.LocalVariable);
        actionTurnout.setStateFormula("\"IT\"+index2");
        actionTurnout.setStateLocalVariable("index2");
        actionTurnout.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.setTurnout(turnout1);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Closed);
        actionTurnout.setAddressing(NamedBeanAddressing.LocalVariable);
        actionTurnout.setFormula("\"IT\"+index");
        actionTurnout.setLocalVariable("index");
        actionTurnout.setReference("{IM1}");
        actionTurnout.setStateAddressing(NamedBeanAddressing.Formula);
        actionTurnout.setStateFormula("\"IT\"+index2");
        actionTurnout.setStateLocalVariable("index2");
        actionTurnout.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.setTurnout(turnout1);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Closed);
        actionTurnout.setAddressing(NamedBeanAddressing.Formula);
        actionTurnout.setFormula("\"IT\"+index");
        actionTurnout.setLocalVariable("index");
        actionTurnout.setReference("{IM1}");
        actionTurnout.setStateAddressing(NamedBeanAddressing.Reference);
        actionTurnout.setStateFormula("\"IT\"+index2");
        actionTurnout.setStateLocalVariable("index2");
        actionTurnout.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.setTurnout(turnout1);
        actionTurnout.setBeanState(ActionTurnout.TurnoutState.Closed);
        actionTurnout.setAddressing(NamedBeanAddressing.Reference);
        actionTurnout.setFormula("\"IT\"+index");
        actionTurnout.setLocalVariable("index");
        actionTurnout.setReference("{IM1}");
        actionTurnout.setStateAddressing(NamedBeanAddressing.Direct);
        actionTurnout.setStateFormula("\"IT\"+index2");
        actionTurnout.setStateLocalVariable("index2");
        actionTurnout.setStateReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionWarrant actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct / Direct :: SetTrainName
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / Direct / Direct :: SetTrainName");

        actionWarrant.setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setWarrant("IW99");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setOperationDirect(ActionWarrant.DirectOperation.SetTrainName);

        actionWarrant.setDataAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setTrainIdName("ABC");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct / Direct :: ControlAutoTrain - Resume
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / Direct / Direct :: ControlAutoTrain - Resume");

        actionWarrant.setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setWarrant("IW99");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setOperationDirect(ActionWarrant.DirectOperation.ControlAutoTrain);

        actionWarrant.setDataAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setControlAutoTrain(ActionWarrant.ControlAutoTrain.Resume);

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct :: AllocateWarrantRoute
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / Direct :: AllocateWarrantRoute");

        actionWarrant.setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setWarrant("IW99");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setOperationDirect(ActionWarrant.DirectOperation.AllocateWarrantRoute);

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / LocalVariable
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / LocalVariable");

        actionWarrant.setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setWarrant("IW99");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionWarrant.setOperationLocalVariable("index2");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// LocalVariable / Formula
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("LocalVariable / Formula");

        actionWarrant.setAddressing(NamedBeanAddressing.LocalVariable);
        actionWarrant.setLocalVariable("index");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.Formula);
        actionWarrant.setOperationFormula("\"IT\"+index2");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Formula / Reference
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Formula / Reference");

        actionWarrant.setAddressing(NamedBeanAddressing.Formula);
        actionWarrant.setFormula("\"IT\"+index");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.Reference);
        actionWarrant.setOperationReference("{IM2}");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Reference / Direct :: DeallocateWarrant
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Reference / Direct :: DeallocateWarrant");

        actionWarrant.setAddressing(NamedBeanAddressing.Reference);
        actionWarrant.setReference("{IM1}");

        actionWarrant.setOperationAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setOperationDirect(ActionWarrant.DirectOperation.DeallocateWarrant);

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrit.logixng.actions.DigitalCallModule callModule = new jmri.jmrit.logixng.actions.DigitalCallModule(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(callModule);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        callModule = new jmri.jmrit.logixng.actions.DigitalCallModule(digitalActionManager.getAutoSystemName(), null);
        callModule.setComment("A comment");
        callModule.setModule("IQM1");
        callModule.addParameter("Abc", InitialValueType.FloatingNumber, "12.32", Module.ReturnValueType.LocalVariable, "SomeVar");
        callModule.addParameter("Def", InitialValueType.Formula, "12 + 32", Module.ReturnValueType.Memory, "M1");
        callModule.addParameter("Ghi", InitialValueType.Integer, "21", Module.ReturnValueType.None, null);
        callModule.addParameter("Jkl", InitialValueType.LocalVariable, "MyVar", Module.ReturnValueType.Memory, "M34");
        callModule.addParameter("Mno", InitialValueType.Memory, "M2", Module.ReturnValueType.LocalVariable, "SomeVar");
        callModule.addParameter("Pqr", InitialValueType.None, null, Module.ReturnValueType.LocalVariable, "SomeVar");
        callModule.addParameter("Stu", InitialValueType.Reference, "{MyVar}", Module.ReturnValueType.LocalVariable, "SomeVar");
        callModule.addParameter("Vxy", InitialValueType.String, "Some string", Module.ReturnValueType.LocalVariable, "SomeVar");
        maleSocket = digitalActionManager.registerAction(callModule);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        DoAnalogAction doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        doAnalogAction.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        DoStringAction doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        doStringAction.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(doStringAction);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        EnableLogix enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(enableLogix);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.setLogix(logixIX1);
        enableLogix.setOperationDirect(EnableLogix.Operation.Enable);
        enableLogix.setAddressing(NamedBeanAddressing.Direct);
        enableLogix.setFormula("\"IT\"+index");
        enableLogix.setLocalVariable("index");
        enableLogix.setReference("{IM1}");
        enableLogix.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        enableLogix.setOperationFormula("\"IT\"+index2");
        enableLogix.setOperationLocalVariable("index2");
        enableLogix.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.setLogix(logixIX1);
        enableLogix.setOperationDirect(EnableLogix.Operation.Disable);
        enableLogix.setAddressing(NamedBeanAddressing.LocalVariable);
        enableLogix.setFormula("\"IT\"+index");
        enableLogix.setLocalVariable("index");
        enableLogix.setReference("{IM1}");
        enableLogix.setOperationAddressing(NamedBeanAddressing.Formula);
        enableLogix.setOperationFormula("\"IT\"+index2");
        enableLogix.setOperationLocalVariable("index2");
        enableLogix.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.setLogix(logixIX1);
        enableLogix.setOperationDirect(EnableLogix.Operation.Enable);
        enableLogix.setAddressing(NamedBeanAddressing.Formula);
        enableLogix.setFormula("\"IT\"+index");
        enableLogix.setLocalVariable("index");
        enableLogix.setReference("{IM1}");
        enableLogix.setOperationAddressing(NamedBeanAddressing.Reference);
        enableLogix.setOperationFormula("\"IT\"+index2");
        enableLogix.setOperationLocalVariable("index2");
        enableLogix.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.setLogix(logixIX1);
        enableLogix.setOperationDirect(EnableLogix.Operation.Enable);
        enableLogix.setAddressing(NamedBeanAddressing.Reference);
        enableLogix.setFormula("\"IT\"+index");
        enableLogix.setLocalVariable("index");
        enableLogix.setReference("{IM1}");
        enableLogix.setOperationAddressing(NamedBeanAddressing.Direct);
        enableLogix.setOperationFormula("\"IT\"+index2");
        enableLogix.setOperationLocalVariable("index2");
        enableLogix.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionEntryExit entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(entryExit);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.setOperationDirect(ActionEntryExit.Operation.SetNXPairDisabled);
        entryExit.setAddressing(NamedBeanAddressing.Direct);
        entryExit.setFormula("\"IT\"+index");
        entryExit.setLocalVariable("index");
        entryExit.setReference("{IM1}");
        entryExit.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        entryExit.setOperationFormula("\"IT\"+index2");
        entryExit.setOperationLocalVariable("index2");
        entryExit.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.setOperationDirect(ActionEntryExit.Operation.SetNXPairEnabled);
        entryExit.setAddressing(NamedBeanAddressing.LocalVariable);
        entryExit.setFormula("\"IT\"+index");
        entryExit.setLocalVariable("index");
        entryExit.setReference("{IM1}");
        entryExit.setOperationAddressing(NamedBeanAddressing.Formula);
        entryExit.setOperationFormula("\"IT\"+index2");
        entryExit.setOperationLocalVariable("index2");
        entryExit.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.setOperationDirect(ActionEntryExit.Operation.SetNXPairSegment);
        entryExit.setAddressing(NamedBeanAddressing.Formula);
        entryExit.setFormula("\"IT\"+index");
        entryExit.setLocalVariable("index");
        entryExit.setReference("{IM1}");
        entryExit.setOperationAddressing(NamedBeanAddressing.Reference);
        entryExit.setOperationFormula("\"IT\"+index2");
        entryExit.setOperationLocalVariable("index2");
        entryExit.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.setOperationDirect(ActionEntryExit.Operation.SetNXPairDisabled);
        entryExit.setAddressing(NamedBeanAddressing.Reference);
        entryExit.setFormula("\"IT\"+index");
        entryExit.setLocalVariable("index");
        entryExit.setReference("{IM1}");
        entryExit.setOperationAddressing(NamedBeanAddressing.Direct);
        entryExit.setOperationFormula("\"IT\"+index2");
        entryExit.setOperationLocalVariable("index2");
        entryExit.setOperationReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ExecuteDelayed executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setResetIfAlreadyStarted(false);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setComment("A comment");
        executeDelayed.setDelayAddressing(NamedBeanAddressing.Direct);
        executeDelayed.setDelay(100);
        executeDelayed.setResetIfAlreadyStarted(true);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setComment("A comment");
        executeDelayed.setDelayAddressing(NamedBeanAddressing.LocalVariable);
        executeDelayed.setDelayLocalVariable("MyVar");
        executeDelayed.setResetIfAlreadyStarted(true);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setComment("A comment");
        executeDelayed.setDelayAddressing(NamedBeanAddressing.Reference);
        executeDelayed.setDelayReference("{MyMemory}");
        executeDelayed.setResetIfAlreadyStarted(true);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setComment("A comment");
        executeDelayed.setDelayAddressing(NamedBeanAddressing.Formula);
        executeDelayed.setDelayFormula("MyVar + 10");
        executeDelayed.setResetIfAlreadyStarted(true);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        For actionFor =
                new For(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionFor);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionFor = new For(digitalActionManager.getAutoSystemName(), null);
        actionFor.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(actionFor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        IfThenElse ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.ExecuteOnChange);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.AlwaysExecute);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrit.logixng.actions.Logix logix =
                new jmri.jmrit.logixng.actions.Logix(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(logix);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logix = new jmri.jmrit.logixng.actions.Logix(digitalActionManager.getAutoSystemName(), null);
        logix.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(logix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        DigitalBooleanMany booleanMany =
                new DigitalBooleanMany(digitalBooleanActionManager.getAutoSystemName(), null);
        maleSocket = digitalBooleanActionManager.registerAction(booleanMany);
        maleSocket.setEnabled(false);
        logix.getChild(1).connect(maleSocket);

        DigitalBooleanMany booleanMany2 =
                new DigitalBooleanMany(digitalBooleanActionManager.getAutoSystemName(), null);
        booleanMany2.setComment("A comment");
        maleSocket = digitalBooleanActionManager.registerAction(booleanMany2);
        booleanMany.getChild(0).connect(maleSocket);


        DigitalBooleanOnChange onChange =
                new DigitalBooleanOnChange(digitalBooleanActionManager.getAutoSystemName(),
                        null, DigitalBooleanOnChange.Trigger.CHANGE);
        maleSocket = digitalBooleanActionManager.registerAction(onChange);
        maleSocket.setEnabled(false);
        booleanMany.getChild(1).connect(maleSocket);

        onChange = new DigitalBooleanOnChange(digitalBooleanActionManager.getAutoSystemName(),
                null, DigitalBooleanOnChange.Trigger.CHANGE_TO_FALSE);
        onChange.setComment("A comment");
        maleSocket = digitalBooleanActionManager.registerAction(onChange);
        booleanMany.getChild(2).connect(maleSocket);


        jmri.jmrit.logixng.actions.LogData logData = new jmri.jmrit.logixng.actions.LogData(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(logData);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logData = new jmri.jmrit.logixng.actions.LogData(digitalActionManager.getAutoSystemName(), null);
        logData.setComment("A comment");
        logData.setLogToLog(true);
        logData.setLogToScriptOutput(true);
        logData.setFormat("Some text");
        logData.setFormatType(jmri.jmrit.logixng.actions.LogData.FormatType.OnlyText);
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.LocalVariable, "MyVar"));
        maleSocket = digitalActionManager.registerAction(logData);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logData = new jmri.jmrit.logixng.actions.LogData(digitalActionManager.getAutoSystemName(), null);
        logData.setComment("A comment");
        logData.setLogToLog(true);
        logData.setLogToScriptOutput(true);
        logData.setFormat("");
        logData.setFormatType(jmri.jmrit.logixng.actions.LogData.FormatType.CommaSeparatedList);
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.Memory, "IM1"));
        maleSocket = digitalActionManager.registerAction(logData);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logData = new jmri.jmrit.logixng.actions.LogData(digitalActionManager.getAutoSystemName(), null);
        logData.setComment("A comment");
        logData.setLogToLog(true);
        logData.setLogToScriptOutput(true);
        logData.setFormat("MyVar has the value %s");
        logData.setFormatType(jmri.jmrit.logixng.actions.LogData.FormatType.StringFormat);
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.Reference, "{MyVar}"));
        maleSocket = digitalActionManager.registerAction(logData);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logData = new jmri.jmrit.logixng.actions.LogData(digitalActionManager.getAutoSystemName(), null);
        logData.setComment("A comment");
        logData.setLogToLog(true);
        logData.setLogToScriptOutput(true);
        logData.setFormat("str(10): %s, 25: %d, IM1: %s, MyVar: %s");
        logData.setFormatType(jmri.jmrit.logixng.actions.LogData.FormatType.StringFormat);
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.Formula, "str(10)"));
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.Formula, "25"));
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.Memory, "IM1"));
        logData.getDataList().add(new jmri.jmrit.logixng.actions.LogData.Data(jmri.jmrit.logixng.actions.LogData.DataType.LocalVariable, "MyVar"));
        maleSocket = digitalActionManager.registerAction(logData);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        LogLocalVariables logLocalVariables = new LogLocalVariables(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(logLocalVariables);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logLocalVariables = new LogLocalVariables(digitalActionManager.getAutoSystemName(), null);
        logLocalVariables.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(logLocalVariables);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        DigitalMany many = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(many);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        many = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        many.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(many);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        Sequence sequence =
                new Sequence(digitalActionManager.getAutoSystemName(), null);
        sequence.setRunContinuously(false);
        sequence.setStartImmediately(true);
        maleSocket = digitalActionManager.registerAction(sequence);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        sequence = new Sequence(digitalActionManager.getAutoSystemName(), null);
        sequence.setComment("A comment");
        sequence.setRunContinuously(true);
        sequence.setStartImmediately(false);
        maleSocket = digitalActionManager.registerAction(sequence);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        And andTemp = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp.setComment("Start expression");
        maleSocket = digitalExpressionManager.registerExpression(andTemp);
        maleSocket.setEnabled(false);
        sequence.getChild(0).connect(maleSocket);

        andTemp = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp.setComment("Stop expression");
        maleSocket = digitalExpressionManager.registerExpression(andTemp);
        sequence.getChild(1).connect(maleSocket);

        andTemp = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp.setComment("Reset expression");
        maleSocket = digitalExpressionManager.registerExpression(andTemp);
        sequence.getChild(2).connect(maleSocket);

        DigitalMany manyTemp = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        manyTemp.setComment("Action socket 1");
        maleSocket = digitalActionManager.registerAction(manyTemp);
        maleSocket.setEnabled(false);
        sequence.getChild(3).connect(maleSocket);

        andTemp = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp.setComment("Expression socket 1");
        maleSocket = digitalExpressionManager.registerExpression(andTemp);
        sequence.getChild(4).connect(maleSocket);

        sequence.doSocketOperation(4, FemaleSocketOperation.InsertAfter);

        manyTemp = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        manyTemp.setComment("Action socket 2");
        maleSocket = digitalActionManager.registerAction(manyTemp);
        sequence.getChild(5).connect(maleSocket);

        andTemp = new And(digitalExpressionManager.getAutoSystemName(), null);
        andTemp.setComment("Expression socket 2");
        maleSocket = digitalExpressionManager.registerExpression(andTemp);
        sequence.getChild(6).connect(maleSocket);


        ShutdownComputer shutdownComputer =
                new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.setOperation(ShutdownComputer.Operation.ShutdownComputer);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.setOperation(ShutdownComputer.Operation.RebootComputer);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.setOperation(ShutdownComputer.Operation.ShutdownJMRI);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.setOperation(ShutdownComputer.Operation.RebootJMRI);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        TableForEach tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setTableRowOrColumn(TableRowOrColumn.Column);
        maleSocket = digitalActionManager.registerAction(tableForEach);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setComment("A comment");
        tableForEach.setLocalVariableName("MyLocalVariable");
        tableForEach.setTable(csvTable);
        tableForEach.setTableRowOrColumn(TableRowOrColumn.Row);
        tableForEach.setRowOrColumnName("North yard");
        maleSocket = digitalActionManager.registerAction(tableForEach);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        maleSocket.getChild(0).connect(
                digitalActionManager.registerAction(
                        new DigitalMany(digitalActionManager.getAutoSystemName(), null)));

        tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setComment("A comment");
        tableForEach.setLocalVariableName("MyLocalVariable");
        tableForEach.setTable(csvTable);
        tableForEach.setTableRowOrColumn(TableRowOrColumn.Column);
        tableForEach.setRowOrColumnName("Second turnout");
        maleSocket = digitalActionManager.registerAction(tableForEach);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        maleSocket.getChild(0).connect(
                digitalActionManager.registerAction(
                        new DigitalMany(digitalActionManager.getAutoSystemName(), null)));


        actionThrottle = new ActionThrottle(digitalActionManager.getAutoSystemName(), null);
        actionThrottle.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(actionThrottle);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        maleSocket.getChild(0).connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));

        maleSocket.getChild(1).connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));

        maleSocket.getChild(2).connect(
                digitalExpressionManager.registerExpression(
                        new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null)));



        TriggerRoute triggerRoute =
                new TriggerRoute(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(triggerRoute);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        triggerRoute = new TriggerRoute(digitalActionManager.getAutoSystemName(), null);
        triggerRoute.setComment("A comment");
        triggerRoute.setOperationDirect(TriggerRoute.Operation.TriggerRoute);
        maleSocket = digitalActionManager.registerAction(triggerRoute);
        actionManySocket.getChild(indexAction++).connect(maleSocket);



        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.ExecuteOnChange);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        And and1 = new And(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(and1);
        maleSocket.setEnabled(false);
        ifThenElse.getChild(0).connect(maleSocket);

        And and = new And(digitalExpressionManager.getAutoSystemName(), null);
        and.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(and);
        and1.getChild(0).connect(maleSocket);


        int indexExpr = 0;


        Antecedent antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        maleSocket.setEnabled(false);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        and.getChild(indexExpr++).connect(maleSocket);

        antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        and.getChild(indexExpr++).connect(maleSocket);

        antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        antecedent.setComment("A comment");
        antecedent.setAntecedent("R1 or R2");
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        and.getChild(indexExpr++).connect(maleSocket);

        antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogErrorOnce);
        and.getChild(indexExpr++).connect(maleSocket);

        antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ShowDialogBox);
        and.getChild(indexExpr++).connect(maleSocket);

        antecedent = new Antecedent(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(antecedent);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        and.getChild(indexExpr++).connect(maleSocket);


        jmri.jmrit.logixng.expressions.DigitalCallModule expressionCallModule = new jmri.jmrit.logixng.expressions.DigitalCallModule(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionCallModule);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionCallModule = new jmri.jmrit.logixng.expressions.DigitalCallModule(digitalExpressionManager.getAutoSystemName(), null);
        expressionCallModule.setComment("A comment");
        expressionCallModule.setModule("IQM1");
        expressionCallModule.addParameter("Abc", InitialValueType.FloatingNumber, "12.32", Module.ReturnValueType.LocalVariable, "SomeVar");
        expressionCallModule.addParameter("Def", InitialValueType.Formula, "12 + 32", Module.ReturnValueType.Memory, "M1");
        expressionCallModule.addParameter("Ghi", InitialValueType.Integer, "21", Module.ReturnValueType.None, null);
        expressionCallModule.addParameter("Jkl", InitialValueType.LocalVariable, "MyVar", Module.ReturnValueType.Memory, "M34");
        expressionCallModule.addParameter("Mno", InitialValueType.Memory, "M2", Module.ReturnValueType.LocalVariable, "SomeVar");
        expressionCallModule.addParameter("Pqr", InitialValueType.None, null, Module.ReturnValueType.LocalVariable, "SomeVar");
        expressionCallModule.addParameter("Stu", InitialValueType.Reference, "{MyVar}", Module.ReturnValueType.LocalVariable, "SomeVar");
        expressionCallModule.addParameter("Vxy", InitialValueType.String, "Some string", Module.ReturnValueType.LocalVariable, "SomeVar");
        maleSocket = digitalExpressionManager.registerExpression(expressionCallModule);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionBlock expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);
// Direct / Direct / Direct :: ValueMatches
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Direct / Direct / Direct :: ValueMatches");

        expressionBlock.setAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBlock(block1);

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBeanState(ExpressionBlock.BlockState.ValueMatches);

        expressionBlock.setDataAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBlockValue("XYZ");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Direct / Direct :: Occupied
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Direct / Direct :: Occupied");

        expressionBlock.setAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBlock(block1);

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBeanState(ExpressionBlock.BlockState.Occupied);

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Direct / LocalVariable
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Direct / LocalVariable");

        expressionBlock.setAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBlock(block1);

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);

        expressionBlock.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionBlock.setStateLocalVariable("index2");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// LocalVariable / Formula
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("LocalVariable / Formula");

        expressionBlock.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionBlock.setLocalVariable("index");

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Formula);
        expressionBlock.setStateFormula("\"IT\"+index2");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Formula / Reference
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Formula / Reference");

        expressionBlock.setAddressing(NamedBeanAddressing.Formula);
        expressionBlock.setFormula("\"IT\"+index");

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Reference);
        expressionBlock.setStateReference("{IM2}");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Reference / Direct :: Allocated
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Reference / Direct :: Allocated");

        expressionBlock.setAddressing(NamedBeanAddressing.Reference);
        expressionBlock.setReference("{IM1}");

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBeanState(ExpressionBlock.BlockState.Allocated);

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionClock expressionClock = new ExpressionClock(digitalExpressionManager.getAutoSystemName(), null);
        expressionClock.setType(ExpressionClock.Type.SystemClock);
        expressionClock.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionClock);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionClock = new ExpressionClock(digitalExpressionManager.getAutoSystemName(), null);
        expressionClock.setComment("A comment");
        expressionClock.setRange(10, 20);
        expressionClock.setType(ExpressionClock.Type.FastClock);
        expressionClock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionClock);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionConditional expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.setConditional("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.False);
        expressionConditional.setAddressing(NamedBeanAddressing.Direct);
        expressionConditional.setFormula("\"IT\"+index");
        expressionConditional.setLocalVariable("index");
        expressionConditional.setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionConditional.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.setConditional("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.True);
        expressionConditional.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionConditional.setFormula("\"IT\"+index");
        expressionConditional.setLocalVariable("index");
        expressionConditional.setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionConditional.setStateAddressing(NamedBeanAddressing.Formula);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.setConditional("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.Other);
        expressionConditional.setAddressing(NamedBeanAddressing.Formula);
        expressionConditional.setFormula("\"IT\"+index");
        expressionConditional.setLocalVariable("index");
        expressionConditional.setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionConditional.setStateAddressing(NamedBeanAddressing.Reference);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.setConditional("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.False);
        expressionConditional.setAddressing(NamedBeanAddressing.Reference);
        expressionConditional.setFormula("\"IT\"+index");
        expressionConditional.setLocalVariable("index");
        expressionConditional.setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionConditional.setStateAddressing(NamedBeanAddressing.Direct);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionEntryExit expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setComment("A comment");
        expressionEntryExit.setBeanState(ExpressionEntryExit.EntryExitState.Inactive);
        expressionEntryExit.setAddressing(NamedBeanAddressing.Direct);
        expressionEntryExit.setFormula("\"IT\"+index");
        expressionEntryExit.setLocalVariable("index");
        expressionEntryExit.setReference("{IM1}");
        expressionEntryExit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionEntryExit.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionEntryExit.setStateFormula("\"IT\"+index2");
        expressionEntryExit.setStateLocalVariable("index2");
        expressionEntryExit.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setComment("A comment");
        expressionEntryExit.setBeanState(ExpressionEntryExit.EntryExitState.Inactive);
        expressionEntryExit.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionEntryExit.setFormula("\"IT\"+index");
        expressionEntryExit.setLocalVariable("index");
        expressionEntryExit.setReference("{IM1}");
        expressionEntryExit.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionEntryExit.setStateAddressing(NamedBeanAddressing.Formula);
        expressionEntryExit.setStateFormula("\"IT\"+index2");
        expressionEntryExit.setStateLocalVariable("index2");
        expressionEntryExit.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setComment("A comment");
        expressionEntryExit.setBeanState(ExpressionEntryExit.EntryExitState.Inactive);
        expressionEntryExit.setAddressing(NamedBeanAddressing.Formula);
        expressionEntryExit.setFormula("\"IT\"+index");
        expressionEntryExit.setLocalVariable("index");
        expressionEntryExit.setReference("{IM1}");
        expressionEntryExit.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionEntryExit.setStateAddressing(NamedBeanAddressing.Reference);
        expressionEntryExit.setStateFormula("\"IT\"+index2");
        expressionEntryExit.setStateLocalVariable("index2");
        expressionEntryExit.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setComment("A comment");
        expressionEntryExit.setBeanState(ExpressionEntryExit.EntryExitState.Inactive);
        expressionEntryExit.setAddressing(NamedBeanAddressing.Reference);
        expressionEntryExit.setFormula("\"IT\"+index");
        expressionEntryExit.setLocalVariable("index");
        expressionEntryExit.setReference("{IM1}");
        expressionEntryExit.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionEntryExit.setStateAddressing(NamedBeanAddressing.Direct);
        expressionEntryExit.setStateFormula("\"IT\"+index2");
        expressionEntryExit.setStateLocalVariable("index2");
        expressionEntryExit.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionLight expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.setLight(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.Off);
        expressionLight.setAddressing(NamedBeanAddressing.Direct);
        expressionLight.setFormula("\"IT\"+index");
        expressionLight.setLocalVariable("index");
        expressionLight.setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionLight.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.setLight(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.On);
        expressionLight.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionLight.setFormula("\"IT\"+index");
        expressionLight.setLocalVariable("index");
        expressionLight.setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionLight.setStateAddressing(NamedBeanAddressing.Formula);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.setLight(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.Other);
        expressionLight.setAddressing(NamedBeanAddressing.Formula);
        expressionLight.setFormula("\"IT\"+index");
        expressionLight.setLocalVariable("index");
        expressionLight.setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionLight.setStateAddressing(NamedBeanAddressing.Reference);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.setLight(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.Off);
        expressionLight.setAddressing(NamedBeanAddressing.Reference);
        expressionLight.setFormula("\"IT\"+index");
        expressionLight.setLocalVariable("index");
        expressionLight.setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionLight.setStateAddressing(NamedBeanAddressing.Direct);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionLocalVariable expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setConstantValue("10");
        expressionLocalVariable.setCaseInsensitive(true);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Value);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.GreaterThan);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.setMemory(memory2);
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Memory);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.setMemory(memory2);
        expressionLocalVariable.setOtherLocalVariable("MyOtherVar");
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.LocalVariable);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.setRegEx("/^Test$/");
        expressionLocalVariable.setMemory(memory2);
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.RegEx);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionMemory expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Memory);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.setMemory(memory1);
        expressionMemory.setConstantValue("10");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.LessThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.setMemory(memory2);
        expressionMemory.setOtherMemory(memory3);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Memory);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.setMemory(memory2);
        expressionMemory.setOtherMemory(memory3);
        expressionMemory.setLocalVariable("MyVar");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.LocalVariable);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.setMemory(memory2);
        expressionMemory.setOtherMemory(memory3);
        expressionMemory.setRegEx("/^Hello$/");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.RegEx);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionOBlock expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.setOBlock("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.Dark);
        expressionOBlock.setAddressing(NamedBeanAddressing.Direct);
        expressionOBlock.setFormula("\"IT\"+index");
        expressionOBlock.setLocalVariable("index");
        expressionOBlock.setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.setOBlock("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.Allocated);
        expressionOBlock.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionOBlock.setFormula("\"IT\"+index");
        expressionOBlock.setLocalVariable("index");
        expressionOBlock.setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.Formula);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.setOBlock("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.Occupied);
        expressionOBlock.setAddressing(NamedBeanAddressing.Formula);
        expressionOBlock.setFormula("\"IT\"+index");
        expressionOBlock.setLocalVariable("index");
        expressionOBlock.setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.Reference);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.setOBlock("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.OutOfService);
        expressionOBlock.setAddressing(NamedBeanAddressing.Reference);
        expressionOBlock.setFormula("\"IT\"+index");
        expressionOBlock.setLocalVariable("index");
        expressionOBlock.setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.Direct);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionPower expressionPower = new ExpressionPower(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionPower);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionPower = new ExpressionPower(digitalExpressionManager.getAutoSystemName(), null);
        expressionPower.setComment("A comment");
        expressionPower.setBeanState(ExpressionPower.PowerState.Off);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionPower);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionPower = new ExpressionPower(digitalExpressionManager.getAutoSystemName(), null);
        expressionPower.setComment("A comment");
        expressionPower.setBeanState(ExpressionPower.PowerState.On);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionPower);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionPower = new ExpressionPower(digitalExpressionManager.getAutoSystemName(), null);
        expressionPower.setComment("A comment");
        expressionPower.setBeanState(ExpressionPower.PowerState.Other);
        expressionPower.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionPower);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionReference expressionReference = new ExpressionReference(digitalExpressionManager.getAutoSystemName(), null);
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LogixNGTable);
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.Is);
        maleSocket = digitalExpressionManager.registerExpression(expressionReference);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionReference = new ExpressionReference(digitalExpressionManager.getAutoSystemName(), null);
        expressionReference.setComment("A comment");
        expressionReference.setReference("IL1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Light);
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        maleSocket = digitalExpressionManager.registerExpression(expressionReference);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionScript expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        expressionScript.setComment("A comment");
        expressionScript.setScript("result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )");
        expressionScript.setRegisterListenerScript("sensors.provideSensor(\"IS1\").addPropertyChangeListener(self)");
        expressionScript.setUnregisterListenerScript("sensors.provideSensor(\"IS1\").removePropertyChangeListener(self)");
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionSensor expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.setSensor(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.setAddressing(NamedBeanAddressing.Direct);
        expressionSensor.setFormula("\"IT\"+index");
        expressionSensor.setLocalVariable("index");
        expressionSensor.setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionSensor.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.setSensor(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionSensor.setFormula("\"IT\"+index");
        expressionSensor.setLocalVariable("index");
        expressionSensor.setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSensor.setStateAddressing(NamedBeanAddressing.Formula);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.setSensor(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.setAddressing(NamedBeanAddressing.Formula);
        expressionSensor.setFormula("\"IT\"+index");
        expressionSensor.setLocalVariable("index");
        expressionSensor.setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionSensor.setStateAddressing(NamedBeanAddressing.Reference);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.setSensor(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.setAddressing(NamedBeanAddressing.Reference);
        expressionSensor.setFormula("\"IT\"+index");
        expressionSensor.setLocalVariable("index");
        expressionSensor.setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSensor.setStateAddressing(NamedBeanAddressing.Direct);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionSignalHead expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setAddressing(NamedBeanAddressing.Direct);
        expressionSignalHead.setFormula("\"IT\"+index");
        expressionSignalHead.setLocalVariable("index");
        expressionSignalHead.setReference("{IM1}");
        expressionSignalHead.setQueryAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalHead.setQueryFormula("\"IT\"+index2");
        expressionSignalHead.setQueryLocalVariable("index2");
        expressionSignalHead.setQueryReference("{IM2}");
        expressionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Formula);
        expressionSignalHead.setAppearance(SignalHead.FLASHGREEN);
        expressionSignalHead.setAppearanceFormula("\"IT\"+index3");
        expressionSignalHead.setAppearanceLocalVariable("index3");
        expressionSignalHead.setAppearanceReference("{IM3}");
        expressionSignalHead.setExampleSignalHead("IH2");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalHead.setFormula("\"IT\"+index");
        expressionSignalHead.setLocalVariable("index");
        expressionSignalHead.setReference("{IM1}");
        expressionSignalHead.setQueryAddressing(NamedBeanAddressing.Formula);
        expressionSignalHead.setQueryFormula("\"IT\"+index2");
        expressionSignalHead.setQueryLocalVariable("index2");
        expressionSignalHead.setQueryReference("{IM2}");
        expressionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Reference);
        expressionSignalHead.setAppearance(SignalHead.FLASHLUNAR);
        expressionSignalHead.setAppearanceFormula("\"IT\"+index3");
        expressionSignalHead.setAppearanceLocalVariable("index3");
        expressionSignalHead.setAppearanceReference("{IM3}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setAddressing(NamedBeanAddressing.Formula);
        expressionSignalHead.setFormula("\"IT\"+index");
        expressionSignalHead.setLocalVariable("index");
        expressionSignalHead.setReference("{IM1}");
        expressionSignalHead.setQueryAddressing(NamedBeanAddressing.Reference);
        expressionSignalHead.setQueryFormula("\"IT\"+index2");
        expressionSignalHead.setQueryLocalVariable("index2");
        expressionSignalHead.setQueryReference("{IM2}");
        expressionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Direct);
        expressionSignalHead.setAppearance(SignalHead.FLASHRED);
        expressionSignalHead.setAppearanceFormula("\"IT\"+index3");
        expressionSignalHead.setAppearanceLocalVariable("index3");
        expressionSignalHead.setAppearanceReference("{IM3}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.setSignalHead("IH1");
        expressionSignalHead.setAddressing(NamedBeanAddressing.Reference);
        expressionSignalHead.setFormula("\"IT\"+index");
        expressionSignalHead.setLocalVariable("index");
        expressionSignalHead.setReference("{IM1}");
        expressionSignalHead.setQueryAddressing(NamedBeanAddressing.Direct);
        expressionSignalHead.setQueryFormula("\"IT\"+index2");
        expressionSignalHead.setQueryLocalVariable("index2");
        expressionSignalHead.setQueryReference("{IM2}");
        expressionSignalHead.setAppearanceAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalHead.setAppearance(SignalHead.FLASHYELLOW);
        expressionSignalHead.setAppearanceFormula("\"IT\"+index3");
        expressionSignalHead.setAppearanceLocalVariable("index3");
        expressionSignalHead.setAppearanceReference("{IM3}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionSignalMast expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setAddressing(NamedBeanAddressing.Direct);
        expressionSignalMast.setFormula("\"IT\"+index");
        expressionSignalMast.setLocalVariable("index");
        expressionSignalMast.setReference("{IM1}");
        expressionSignalMast.setQueryAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalMast.setQueryFormula("\"IT\"+index2");
        expressionSignalMast.setQueryLocalVariable("index2");
        expressionSignalMast.setQueryReference("{IM2}");
        expressionSignalMast.setAspectAddressing(NamedBeanAddressing.Formula);
        expressionSignalMast.setAspect("Medium Approach Slow");
        expressionSignalMast.setAspectFormula("\"IT\"+index3");
        expressionSignalMast.setAspectLocalVariable("index3");
        expressionSignalMast.setAspectReference("{IM3}");
        expressionSignalMast.setExampleSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalMast.setFormula("\"IT\"+index");
        expressionSignalMast.setLocalVariable("index");
        expressionSignalMast.setReference("{IM1}");
        expressionSignalMast.setQueryAddressing(NamedBeanAddressing.Formula);
        expressionSignalMast.setQueryFormula("\"IT\"+index2");
        expressionSignalMast.setQueryLocalVariable("index2");
        expressionSignalMast.setQueryReference("{IM2}");
        expressionSignalMast.setAspectAddressing(NamedBeanAddressing.Reference);
        expressionSignalMast.setAspect("Medium Approach");
        expressionSignalMast.setAspectFormula("\"IT\"+index3");
        expressionSignalMast.setAspectLocalVariable("index3");
        expressionSignalMast.setAspectReference("{IM3}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setAddressing(NamedBeanAddressing.Formula);
        expressionSignalMast.setFormula("\"IT\"+index");
        expressionSignalMast.setLocalVariable("index");
        expressionSignalMast.setReference("{IM1}");
        expressionSignalMast.setQueryAddressing(NamedBeanAddressing.Reference);
        expressionSignalMast.setQueryFormula("\"IT\"+index2");
        expressionSignalMast.setQueryLocalVariable("index2");
        expressionSignalMast.setQueryReference("{IM2}");
        expressionSignalMast.setAspectAddressing(NamedBeanAddressing.Direct);
        expressionSignalMast.setAspect("Approach");
        expressionSignalMast.setAspectFormula("\"IT\"+index3");
        expressionSignalMast.setAspectLocalVariable("index3");
        expressionSignalMast.setAspectReference("{IM3}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.setSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.setAddressing(NamedBeanAddressing.Reference);
        expressionSignalMast.setFormula("\"IT\"+index");
        expressionSignalMast.setLocalVariable("index");
        expressionSignalMast.setReference("{IM1}");
        expressionSignalMast.setQueryAddressing(NamedBeanAddressing.Direct);
        expressionSignalMast.setQueryFormula("\"IT\"+index2");
        expressionSignalMast.setQueryLocalVariable("index2");
        expressionSignalMast.setQueryReference("{IM2}");
        expressionSignalMast.setAspectAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalMast.setAspect("Medium Approach Slow");
        expressionSignalMast.setAspectFormula("\"IT\"+index3");
        expressionSignalMast.setAspectLocalVariable("index3");
        expressionSignalMast.setAspectReference("{IM3}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionTurnout expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.setTurnout(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Closed);
        expressionTurnout.setAddressing(NamedBeanAddressing.Direct);
        expressionTurnout.setFormula("\"IT\"+index");
        expressionTurnout.setLocalVariable("index");
        expressionTurnout.setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.setTurnout(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Thrown);
        expressionTurnout.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionTurnout.setFormula("\"IT\"+index");
        expressionTurnout.setLocalVariable("index");
        expressionTurnout.setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.Formula);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.setTurnout(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Other);
        expressionTurnout.setAddressing(NamedBeanAddressing.Formula);
        expressionTurnout.setFormula("\"IT\"+index");
        expressionTurnout.setLocalVariable("index");
        expressionTurnout.setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.Reference);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.setTurnout(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Closed);
        expressionTurnout.setAddressing(NamedBeanAddressing.Reference);
        expressionTurnout.setFormula("\"IT\"+index");
        expressionTurnout.setLocalVariable("index");
        expressionTurnout.setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.Direct);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionWarrant expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.setWarrant("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteAllocated);
        expressionWarrant.setAddressing(NamedBeanAddressing.Direct);
        expressionWarrant.setFormula("\"IT\"+index");
        expressionWarrant.setLocalVariable("index");
        expressionWarrant.setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.setWarrant("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteFree);
        expressionWarrant.setAddressing(NamedBeanAddressing.LocalVariable);
        expressionWarrant.setFormula("\"IT\"+index");
        expressionWarrant.setLocalVariable("index");
        expressionWarrant.setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.Formula);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.setWarrant("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteOccupied);
        expressionWarrant.setAddressing(NamedBeanAddressing.Formula);
        expressionWarrant.setFormula("\"IT\"+index");
        expressionWarrant.setLocalVariable("index");
        expressionWarrant.setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.Reference);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.setWarrant("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteSet);
        expressionWarrant.setAddressing(NamedBeanAddressing.Reference);
        expressionWarrant.setFormula("\"IT\"+index");
        expressionWarrant.setLocalVariable("index");
        expressionWarrant.setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.Direct);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);


        False false1 = new False(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(false1);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        false1 = new False(digitalExpressionManager.getAutoSystemName(), null);
        false1.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(false1);
        and.getChild(indexExpr++).connect(maleSocket);


        jmri.jmrit.logixng.expressions.DigitalFormula formula =
                new jmri.jmrit.logixng.expressions.DigitalFormula(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(formula);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        formula = new jmri.jmrit.logixng.expressions.DigitalFormula(digitalExpressionManager.getAutoSystemName(), null);
        formula.setComment("A comment");
        formula.setFormula("n + 1");
        maleSocket = digitalExpressionManager.registerExpression(formula);
        and.getChild(indexExpr++).connect(maleSocket);


        Hold hold = new Hold(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(hold);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        hold = new Hold(digitalExpressionManager.getAutoSystemName(), null);
        hold.setUserName("A hold expression");
        hold.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(hold);
        and.getChild(indexExpr++).connect(maleSocket);


        LastResultOfDigitalExpression lastResultOfDigitalExpression =
                new LastResultOfDigitalExpression(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(lastResultOfDigitalExpression);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        lastResultOfDigitalExpression = new LastResultOfDigitalExpression(digitalExpressionManager.getAutoSystemName(), null);
        lastResultOfDigitalExpression.setComment("A comment");
        lastResultOfDigitalExpression.setDigitalExpression("A hold expression");
        maleSocket = digitalExpressionManager.registerExpression(lastResultOfDigitalExpression);
        and.getChild(indexExpr++).connect(maleSocket);


        jmri.jmrit.logixng.expressions.LogData logDataExpr = new jmri.jmrit.logixng.expressions.LogData(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(logDataExpr);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        logDataExpr = new jmri.jmrit.logixng.expressions.LogData(digitalExpressionManager.getAutoSystemName(), null);
        logDataExpr.setComment("A comment");
        logDataExpr.setLogToLog(true);
        logDataExpr.setLogToScriptOutput(true);
        logDataExpr.setFormat("Some text");
        logDataExpr.setFormatType(jmri.jmrit.logixng.expressions.LogData.FormatType.OnlyText);
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.LocalVariable, "MyVar"));
        maleSocket = digitalExpressionManager.registerExpression(logDataExpr);
        and.getChild(indexExpr++).connect(maleSocket);

        logDataExpr = new jmri.jmrit.logixng.expressions.LogData(digitalExpressionManager.getAutoSystemName(), null);
        logDataExpr.setComment("A comment");
        logDataExpr.setLogToLog(true);
        logDataExpr.setLogToScriptOutput(true);
        logDataExpr.setFormat("");
        logDataExpr.setFormatType(jmri.jmrit.logixng.expressions.LogData.FormatType.CommaSeparatedList);
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.Memory, "IM1"));
        maleSocket = digitalExpressionManager.registerExpression(logDataExpr);
        and.getChild(indexExpr++).connect(maleSocket);

        logDataExpr = new jmri.jmrit.logixng.expressions.LogData(digitalExpressionManager.getAutoSystemName(), null);
        logDataExpr.setComment("A comment");
        logDataExpr.setLogToLog(true);
        logDataExpr.setLogToScriptOutput(true);
        logDataExpr.setFormat("MyVar has the value %s");
        logDataExpr.setFormatType(jmri.jmrit.logixng.expressions.LogData.FormatType.StringFormat);
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.Reference, "{MyVar}"));
        maleSocket = digitalExpressionManager.registerExpression(logDataExpr);
        and.getChild(indexExpr++).connect(maleSocket);

        logDataExpr = new jmri.jmrit.logixng.expressions.LogData(digitalExpressionManager.getAutoSystemName(), null);
        logDataExpr.setComment("A comment");
        logDataExpr.setLogToLog(true);
        logDataExpr.setLogToScriptOutput(true);
        logDataExpr.setFormat("str(10): %s, 25: %d, IM1: %s, MyVar: %s");
        logDataExpr.setFormatType(jmri.jmrit.logixng.expressions.LogData.FormatType.StringFormat);
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.Formula, "str(10)"));
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.Formula, "25"));
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.Memory, "IM1"));
        logDataExpr.getDataList().add(new jmri.jmrit.logixng.expressions.LogData.Data(jmri.jmrit.logixng.expressions.LogData.DataType.LocalVariable, "MyVar"));
        maleSocket = digitalExpressionManager.registerExpression(logDataExpr);
        and.getChild(indexExpr++).connect(maleSocket);


        Not not = new Not(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(not);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        not = new Not(digitalExpressionManager.getAutoSystemName(), null);
        not.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(not);
        and.getChild(indexExpr++).connect(maleSocket);


        Or or = new Or(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(or);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        or = new Or(digitalExpressionManager.getAutoSystemName(), null);
        or.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(or);
        and.getChild(indexExpr++).connect(maleSocket);


        TriggerOnce triggerOnce = new TriggerOnce(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(triggerOnce);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        triggerOnce = new TriggerOnce(digitalExpressionManager.getAutoSystemName(), null);
        triggerOnce.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(triggerOnce);
        and.getChild(indexExpr++).connect(maleSocket);


        True true1 = new True(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(true1);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        true1 = new True(digitalExpressionManager.getAutoSystemName(), null);
        true1.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(true1);
        and.getChild(indexExpr++).connect(maleSocket);




        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        AnalogExpressionConstant analogExpressionConstant = new AnalogExpressionConstant(analogExpressionManager.getAutoSystemName(), null);
        maleSocket = analogExpressionManager.registerExpression(analogExpressionConstant);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(0).connect(maleSocket);

        AnalogActionMemory analogActionMemory = new AnalogActionMemory(analogActionManager.getAutoSystemName(), null);
        maleSocket = analogActionManager.registerAction(analogActionMemory);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(1).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        analogExpressionConstant = new AnalogExpressionConstant(analogExpressionManager.getAutoSystemName(), null);
        analogExpressionConstant.setComment("A comment");
        analogExpressionConstant.setValue(12.44);
        maleSocket = analogExpressionManager.registerExpression(analogExpressionConstant);
        doAnalogAction.getChild(0).connect(maleSocket);

        analogActionMemory = new AnalogActionMemory(analogActionManager.getAutoSystemName(), null);
        analogActionMemory.setComment("A comment");
        analogActionMemory.setMemory(memory2);
        analogActionMemory.setValue(10.22);
        maleSocket = analogActionManager.registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        AnalogExpressionMemory analogExpressionMemory = new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null);
        maleSocket = analogExpressionManager.registerExpression(analogExpressionMemory);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(0).connect(maleSocket);

        AnalogMany analogMany = new AnalogMany(analogActionManager.getAutoSystemName(), null);
        maleSocket = analogActionManager.registerAction(analogMany);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(1).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        analogExpressionMemory = new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null);
        analogExpressionMemory.setComment("A comment");
        analogExpressionMemory.setMemory(memory1);
        maleSocket = analogExpressionManager.registerExpression(analogExpressionMemory);
        doAnalogAction.getChild(0).connect(maleSocket);

        analogMany = new AnalogMany(analogActionManager.getAutoSystemName(), null);
        analogMany.setComment("A comment");
        maleSocket = analogActionManager.registerAction(analogMany);
        doAnalogAction.getChild(1).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        AnalogFormula analogFormula = new AnalogFormula(analogExpressionManager.getAutoSystemName(), null);
        maleSocket = analogExpressionManager.registerExpression(analogFormula);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(0).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        analogFormula = new AnalogFormula(analogExpressionManager.getAutoSystemName(), null);
        analogFormula.setComment("A comment");
        analogFormula.setFormula("sin(a)*2 + 14");
        maleSocket = analogExpressionManager.registerExpression(analogFormula);
        doAnalogAction.getChild(0).connect(maleSocket);




        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        StringExpressionConstant stringExpressionConstant = new StringExpressionConstant(stringExpressionManager.getAutoSystemName(), null);
        maleSocket = stringExpressionManager.registerExpression(stringExpressionConstant);
        maleSocket.setEnabled(false);
        doStringAction.getChild(0).connect(maleSocket);

        StringActionMemory stringActionMemory = new StringActionMemory(stringActionManager.getAutoSystemName(), null);
        maleSocket = stringActionManager.registerAction(stringActionMemory);
        maleSocket.setEnabled(false);
        doStringAction.getChild(1).connect(maleSocket);


        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        stringExpressionConstant = new StringExpressionConstant(stringExpressionManager.getAutoSystemName(), null);
        stringExpressionConstant.setComment("A comment");
        stringExpressionConstant.setValue("Some string");
        maleSocket = stringExpressionManager.registerExpression(stringExpressionConstant);
        doStringAction.getChild(0).connect(maleSocket);

        stringActionMemory = new StringActionMemory(stringActionManager.getAutoSystemName(), null);
        stringActionMemory.setComment("A comment");
        stringActionMemory.setMemory(memory2);
        stringActionMemory.setValue("Hello");
        maleSocket = stringActionManager.registerAction(stringActionMemory);
        doStringAction.getChild(1).connect(maleSocket);


        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        StringExpressionMemory stringExpressionMemory = new StringExpressionMemory(stringExpressionManager.getAutoSystemName(), null);
        maleSocket = stringExpressionManager.registerExpression(stringExpressionMemory);
        maleSocket.setEnabled(false);
        doStringAction.getChild(0).connect(maleSocket);

        StringMany stringMany = new StringMany(stringActionManager.getAutoSystemName(), null);
        maleSocket = stringActionManager.registerAction(stringMany);
        maleSocket.setEnabled(false);
        doStringAction.getChild(1).connect(maleSocket);


        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        stringExpressionMemory = new StringExpressionMemory(stringExpressionManager.getAutoSystemName(), null);
        stringExpressionMemory.setComment("A comment");
        stringExpressionMemory.setMemory(memory1);
        maleSocket = stringExpressionManager.registerExpression(stringExpressionMemory);
        doStringAction.getChild(0).connect(maleSocket);

        stringMany = new StringMany(stringActionManager.getAutoSystemName(), null);
        stringMany.setComment("A comment");
        maleSocket = stringActionManager.registerAction(stringMany);
        doStringAction.getChild(1).connect(maleSocket);


        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        StringFormula stringFormula = new StringFormula(stringExpressionManager.getAutoSystemName(), null);
        maleSocket = stringExpressionManager.registerExpression(stringFormula);
        maleSocket.setEnabled(false);
        doStringAction.getChild(0).connect(maleSocket);


        doStringAction = new DoStringAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doStringAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        stringFormula = new StringFormula(stringExpressionManager.getAutoSystemName(), null);
        stringFormula.setComment("A comment");
        stringFormula.setFormula("sin(a)*2 + 14");
        maleSocket = stringExpressionManager.registerExpression(stringFormula);
        doStringAction.getChild(0).connect(maleSocket);




        // Check that we have actions/expressions in every managers
        Assert.assertNotEquals(0, logixNG_Manager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, analogActionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, analogExpressionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, digitalActionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, digitalExpressionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, stringActionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, stringExpressionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet().size());
        Assert.assertNotEquals(0, InstanceManager.getDefault(NamedTableManager.class).getNamedBeanSet().size());



        // Check that we can add variables to all actions/expressions and that
        // the variables are stored in the panel file
        femaleRootSocket.forEntireTree((Base b) -> {
            if (b instanceof MaleSocket) {
                addVariables((MaleSocket) b);
            }
        });


        // Check that we can rename the female sockets and that the names
        // are stored in the panel file.
        femaleRootSocket.forEntireTree((Base b) -> {
            if (b instanceof FemaleSocket) {
                ((FemaleSocket)b).setName(getRandomString(10));
            }
        });


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
            logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent, new MutableInt(0));
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

            java.util.Set<Module> moduleSet = new java.util.HashSet<>(InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet());
            for (Module aModule : moduleSet) {
                InstanceManager.getDefault(ModuleManager.class).deleteModule(aModule);
            }

            java.util.Set<NamedTable> tableSet = new java.util.HashSet<>(InstanceManager.getDefault(NamedTableManager.class).getNamedBeanSet());
            for (NamedTable aTable : tableSet) {
                InstanceManager.getDefault(NamedTableManager.class).deleteNamedTable(aTable);
            }

            while (! logixNG_InitializationManager.getList().isEmpty()) {
                logixNG_InitializationManager.delete(0);
            }

            Assert.assertEquals(0, logixNG_Manager.getNamedBeanSet().size());
            Assert.assertEquals(0, analogActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, analogExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, stringActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, stringExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet().size());
            Assert.assertEquals(0, InstanceManager.getDefault(NamedTableManager.class).getNamedBeanSet().size());
            Assert.assertEquals(0, logixNG_InitializationManager.getList().size());

            LogixNG_Thread.stopAllLogixNGThreads();
            LogixNG_Thread.assertLogixNGThreadNotRunning();

            //**********************************
            // Try to load file
            //**********************************

            results = cm.load(secondFile);
            log.debug(results ? "load was successful" : "store failed");
            if (results) {
                if (! logixNG_Manager.resolveAllTrees(new ArrayList<>())) throw new RuntimeException();
                if (! logixNG_Manager.setupAllLogixNGs(new ArrayList<>())) throw new RuntimeException();

                stringWriter = new StringWriter();
                printWriter = new PrintWriter(stringWriter);
                logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent, new MutableInt(0));

                if (!originalTree.equals(stringWriter.toString())) {
                    log.error("--------------------------------------------");
                    log.error("Old tree:");
                    log.error("XXX"+originalTree+"XXX");
                    log.error("--------------------------------------------");
                    log.error("New tree:");
                    log.error("XXX"+stringWriter.toString()+"XXX");
                    log.error("--------------------------------------------");

                    System.out.println("--------------------------------------------");
                    System.out.println("Old tree:");
                    System.out.println("XXX"+originalTree+"XXX");
                    System.out.println("--------------------------------------------");
                    System.out.println("New tree:");
                    System.out.println("XXX"+stringWriter.toString()+"XXX");
                    System.out.println("--------------------------------------------");

//                    log.error(conditionalNGManager.getBySystemName(originalTree).getChild(0).getConnectedSocket().getSystemName());

                    Assert.fail("tree has changed");
//                    throw new RuntimeException("tree has changed");
                }
            } else {
                Assert.fail("Failed to load panel");
//                throw new RuntimeException("Failed to load panel");
            }
        }


//        for (LoggingEvent evt : JUnitAppender.getBacklog()) {
//            System.out.format("Log: %s, %s%n", evt.getLevel(), evt.getMessage());
//        }


        JUnitAppender.assertErrorMessage("systemName is already registered: IH1");
        JUnitAppender.assertErrorMessage("systemName is already registered: IH2");
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
        JUnitUtil.initDebugPowerManager();

        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
//        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();

//        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static final String[] initValues = new String[]{
        "",             // None
        "32",           // Integer
        "41.429",       // FloatingNumber
        "My string",    // String
        "index",        // LocalVariable
        "IM2",          // Memory
        "{IM3}",        // Reference
        "index * 2",    // Formula
    };


    private void addVariables(MaleSocket maleSocket) {
        int i = 0;
        for (InitialValueType type : InitialValueType.values()) {
            maleSocket.addLocalVariable(String.format("A%d", i+1), type, initValues[i]);
            i++;
        }
    }


    private static final PrimitiveIterator.OfInt iterator =
            new Random(215).ints('a', 'z'+10).iterator();

    private String getRandomString(int count) {
        StringBuilder s = new StringBuilder();
        for (int i=0; i < count; i++) {
            int r = iterator.nextInt();
            if (i == 0 && r > 'z') r -= 10;     // The first char must be a character, not a digit.
            char c = (char) (r > 'z' ? r-'z'+'0' : r);
            s.append(c);
        }
        return s.toString();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndLoadTest.class);

}
