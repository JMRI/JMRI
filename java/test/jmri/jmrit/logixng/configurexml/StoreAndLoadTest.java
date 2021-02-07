package jmri.jmrit.logixng.configurexml;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.expressions.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
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
        InstanceManager.getDefault(SignalHeadManager.class)
                .register(new VirtualSignalHead("IH2"));
        
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
        
        
        // Load table turnout_and_signals.csv
        jmri.jmrit.logixng.NamedTable csvTable =
                InstanceManager.getDefault(NamedTableManager.class)
                        .loadTableFromCSV("program:java/test/jmri/jmrit/logixng/panel_and_data_files/turnout_and_signals.csv");
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
                conditionalNGManager.createConditionalNG("An empty conditionalNG");
        logixNG.addConditionalNG(conditionalNG);
        logixNG.setEnabled(false);
        conditionalNG.setEnabled(false);
        
        
        // Create an empty ConditionalNG on the debug thread
        conditionalNG =
                conditionalNGManager.createConditionalNG(
                        "A second empty conditionalNG", LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(false);
        
        
        // Create an empty ConditionalNG on another thread
        LogixNG_Thread.createNewThread(53, "My logixng thread");
        conditionalNG =
                conditionalNGManager.createConditionalNG("A third empty conditionalNG", 53);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(false);
        
        
        // Create an empty ConditionalNG on another thread
        LogixNG_Thread.createNewThread("My other logixng thread");
        conditionalNG = conditionalNGManager.createConditionalNG(
                "A fourth empty conditionalNG", LogixNG_Thread.getThreadID("My other logixng thread"));
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(false);
        
        
        logixNG = logixNG_Manager.createLogixNG("A logixNG");
        conditionalNG =
                conditionalNGManager.createConditionalNG("Yet another conditionalNG");
        logixNG.addConditionalNG(conditionalNG);
        logixNG.setEnabled(false);
        conditionalNG.setEnabled(true);
        
        FemaleSocket femaleRootSocket = conditionalNG.getFemaleSocket();
        MaleDigitalActionSocket actionManySocket =
                digitalActionManager.registerAction(new DigitalMany(
                                        digitalActionManager.getAutoSystemName(), null));
        femaleRootSocket.connect(actionManySocket);
        femaleRootSocket.setLock(Base.Lock.HARD_LOCK);
        
        
        
        int indexAction = 0;
        
        ActionLight actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        MaleSocket maleSocket = digitalActionManager.registerAction(actionLight);
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
        actionListenOnBeans.addReference("light:IL1");
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        
        ActionLocalVariable actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setVariable("result");
        actionLocalVariable.setData("1");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        
        ActionMemory actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setOtherMemory(memory2);
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.setMemory(memory1);
        actionMemory.setData("n + 3");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        
        ActionScript actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionScript);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        actionScript.setComment("A comment");
        actionScript.setScript("import java\n");
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        
        ActionSimpleScript actionSimpleScript = new ActionSimpleScript(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSimpleScript);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        actionSimpleScript = new ActionSimpleScript(digitalActionManager.getAutoSystemName(), null);
        actionSimpleScript.setComment("A comment");
        actionSimpleScript.setScript("import java\n");
        maleSocket = digitalActionManager.registerAction(actionSimpleScript);
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
        
        
        DigitalCallModule callModule = new DigitalCallModule(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(callModule);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        callModule = new DigitalCallModule(digitalActionManager.getAutoSystemName(), null);
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
        
        
        ExecuteDelayed executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setResetIfAlreadyStarted(false);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setComment("A comment");
        executeDelayed.setDelay(100);
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
        
        
        IfThenElse ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.CONTINOUS_ACTION);
        maleSocket = digitalActionManager.registerAction(ifThenElse);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.TRIGGER_ACTION);
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
        tableForEach.setTableRowOrColumn(TableForEach.TableRowOrColumn.Column);
        maleSocket = digitalActionManager.registerAction(tableForEach);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        
        tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setComment("A comment");
        tableForEach.setLocalVariableName("MyLocalVariable");
        tableForEach.setTable(csvTable);
        tableForEach.setTableRowOrColumn(TableForEach.TableRowOrColumn.Row);
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
        tableForEach.setTableRowOrColumn(TableForEach.TableRowOrColumn.Column);
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
        
        
        
        ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.TRIGGER_ACTION);
        ifThenElse.setComment("A comment");
        ifThenElse.setType(IfThenElse.Type.TRIGGER_ACTION);
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
        expressionEntryExit.setDestinationPoints("Something");
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
        expressionEntryExit.setDestinationPoints("Something");
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
        expressionEntryExit.setDestinationPoints("Something");
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
        expressionEntryExit.setDestinationPoints("Something");
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
        expressionScript.setScript("import jmri\n");
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        and.getChild(indexExpr++).connect(maleSocket);
        JUnitAppender.assertWarnMessage("script has not initialized params._scriptClass");
        
        
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
        expressionWarrant.setWarrant("Something");
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
        expressionWarrant.setWarrant("Something");
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
        expressionWarrant.setWarrant("Something");
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
        expressionWarrant.setWarrant("Something");
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
        Assert.assertNotEquals(0, conditionalNGManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, analogActionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, analogExpressionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, digitalActionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, digitalExpressionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, stringActionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, stringExpressionManager.getNamedBeanSet().size());
        Assert.assertNotEquals(0, InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet().size());
        Assert.assertNotEquals(0, InstanceManager.getDefault(NamedTableManager.class).getNamedBeanSet().size());
        
        
        
        femaleRootSocket.forEntireTree((Base b) -> {
            if (b instanceof MaleSocket) {
                addVariables((MaleSocket) b);
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
            
            java.util.Set<Module> moduleSet = new java.util.HashSet<>(InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet());
            for (Module aModule : moduleSet) {
                InstanceManager.getDefault(ModuleManager.class).deleteModule(aModule);
            }
            
            java.util.Set<NamedTable> tableSet = new java.util.HashSet<>(InstanceManager.getDefault(NamedTableManager.class).getNamedBeanSet());
            for (NamedTable aTable : tableSet) {
                InstanceManager.getDefault(NamedTableManager.class).deleteNamedTable(aTable);
            }
            
            Assert.assertEquals(0, logixNG_Manager.getNamedBeanSet().size());
            Assert.assertEquals(0, conditionalNGManager.getNamedBeanSet().size());
            Assert.assertEquals(0, analogActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, analogExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, stringActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, stringExpressionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet().size());
            Assert.assertEquals(0, InstanceManager.getDefault(NamedTableManager.class).getNamedBeanSet().size());
            
            LogixNG_Thread.stopAllLogixNGThreads();
            LogixNG_Thread.assertLogixNGThreadNotRunning();
            
            //**********************************
            // Try to load file
            //**********************************
            
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
        
        JUnitAppender.assertWarnMessage("destinationPoints \"Something\" is not found");
        JUnitAppender.assertWarnMessage("destinationPoints \"Something\" is not found");
        JUnitAppender.assertWarnMessage("destinationPoints \"Something\" is not found");
        JUnitAppender.assertWarnMessage("destinationPoints \"Something\" is not found");
        JUnitAppender.assertWarnMessage("script has not initialized params._scriptClass");
        JUnitAppender.assertWarnMessage("warrant \"Something\" is not found");
        JUnitAppender.assertWarnMessage("warrant \"Something\" is not found");
        JUnitAppender.assertWarnMessage("warrant \"Something\" is not found");
        JUnitAppender.assertWarnMessage("warrant \"Something\" is not found");
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndLoadTest.class);

}
