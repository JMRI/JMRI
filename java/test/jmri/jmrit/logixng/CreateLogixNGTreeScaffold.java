package jmri.jmrit.logixng;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.util.*;

import jmri.*;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.BlockOrder;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.jmrit.logixng.expressions.*;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrix.loconet.*;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.script.ScriptEngineSelector;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Creates a LogixNG with all actions and expressions to test store and load.
 * <P>
 * It uses the Base.printTree(PrintWriter writer, String indent) method to
 * compare the LogixNGs before and after store and load.
 */
public class CreateLogixNGTreeScaffold {

    private static boolean setupHasBeenCalled = false;

    private static LocoNetSystemConnectionMemo _locoNetMemo;
    private static MqttSystemConnectionMemo _mqttMemo;

//    private AudioManager audioManager;

    private static NamedBeanReference getNamedBeanReference(
            Collection<NamedBeanReference> collection, String name) {
        for (NamedBeanReference ref : collection) {
            if (name.equals(ref.getName())) return ref;
        }
        return null;
    }

    public static void createLogixNGTree() throws PropertyVetoException, Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Ensure the setUp() and tearDown() methods of this class are called.
        Assert.assertTrue(setupHasBeenCalled);
/*
        audioManager = new jmri.jmrit.audio.DefaultAudioManager(
                InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class));
        audioManager.init();
        JUnitUtil.waitFor(()->{return audioManager.isInitialised();});

        audioManager.provideAudio("IAB1");
        AudioSource audioSource = (AudioSource) audioManager.provideAudio("IAS1");
        audioSource.setAssignedBuffer((AudioBuffer) audioManager.getNamedBean("IAB1"));
*/
        Block block1 = InstanceManager.getDefault(BlockManager.class).provide("IB1");
        block1.setValue("Block 1 Value");
        Block block2 = InstanceManager.getDefault(BlockManager.class).provide("IB2");
        block2.setUserName("Some block");
        block1.setValue("Block 2 Value");
        Reporter reporter1 = InstanceManager.getDefault(ReporterManager.class).provide("IR1");
        reporter1.setReport("Reporter 1 Value");
        Light light1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light1.setCommandedState(Light.OFF);
        Light light2 = InstanceManager.getDefault(LightManager.class).provide("IL2");
        light2.setUserName("Some light");
        light2.setCommandedState(Light.OFF);
        VariableLight variableLight1 = (VariableLight)InstanceManager.getDefault(LightManager.class).provide("ILVariable");
        variableLight1.setCommandedState(Light.OFF);
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
        NamedTable csvTable = InstanceManager.getDefault(NamedTableManager.class)
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

        // Create global variables
        GlobalVariable globalVariable =
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .createGlobalVariable("IQGV1", "index");
        globalVariable.setInitialValueType(InitialValueType.String);
        globalVariable.setInitialValueData("Something");

        globalVariable =
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .createGlobalVariable("IQGV2", "MyVariable");
        globalVariable.setInitialValueType(InitialValueType.Formula);
        globalVariable.setInitialValueData("\"Variable\" + str(index)");

        globalVariable =
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .createGlobalVariable("IQGV15", "AnotherGlobalVariable");
        globalVariable.setInitialValueType(InitialValueType.Array);
        globalVariable.setInitialValueData("");

        globalVariable =
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .createGlobalVariable(InstanceManager.getDefault(GlobalVariableManager.class)
                                .getAutoSystemName(), "SomeOtherGlobalVariable");
        globalVariable.setInitialValueType(InitialValueType.Map);
        globalVariable.setInitialValueData(null);




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


        ActionAudio actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        MaleSocket maleSocket = digitalActionManager.registerAction(actionAudio);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.setComment("A comment");
//        actionAudio.setAudio(audioSource);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.Play);
        actionAudio.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionAudio.getSelectNamedBean().setFormula("\"IT\"+index");
        actionAudio.getSelectNamedBean().setLocalVariable("index");
        actionAudio.getSelectNamedBean().setReference("{IM1}");
        actionAudio.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionAudio.getSelectEnum().setFormula("\"IT\"+index2");
        actionAudio.getSelectEnum().setLocalVariable("index2");
        actionAudio.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionAudio);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.setComment("A comment");
//        actionAudio.setAudio(audioSource);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.PlayToggle);
        actionAudio.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionAudio.getSelectNamedBean().setFormula("\"IT\"+index");
        actionAudio.getSelectNamedBean().setLocalVariable("index");
        actionAudio.getSelectNamedBean().setReference("{IM1}");
        actionAudio.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionAudio.getSelectEnum().setFormula("\"IT\"+index2");
        actionAudio.getSelectEnum().setLocalVariable("index2");
        actionAudio.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionAudio);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.setComment("A comment");
//        actionAudio.setAudio(audioSource);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.Pause);
        actionAudio.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionAudio.getSelectNamedBean().setFormula("\"IT\"+index");
        actionAudio.getSelectNamedBean().setLocalVariable("index");
        actionAudio.getSelectNamedBean().setReference("{IM1}");
        actionAudio.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionAudio.getSelectEnum().setFormula("\"IT\"+index2");
        actionAudio.getSelectEnum().setLocalVariable("index2");
        actionAudio.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionAudio);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.setComment("A comment");
//        actionAudio.setAudio(audioSource);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.PauseToggle);
        actionAudio.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionAudio.getSelectNamedBean().setFormula("\"IT\"+index");
        actionAudio.getSelectNamedBean().setLocalVariable("index");
        actionAudio.getSelectNamedBean().setReference("{IM1}");
        actionAudio.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionAudio.getSelectEnum().setFormula("\"IT\"+index2");
        actionAudio.getSelectEnum().setLocalVariable("index2");
        actionAudio.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionAudio);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.Stop);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.FadeIn);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.FadeOut);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.Rewind);

        actionAudio = new ActionAudio(digitalActionManager.getAutoSystemName(), null);
        actionAudio.getSelectEnum().setEnum(ActionAudio.Operation.ResetPosition);


        ActionBlock actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setEnabled(false);
        maleSocket.setLocked(true);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        maleSocket.setLocked(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
//        maleSocket.setSystem(true);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct / Direct :: SetValue
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Direct / Direct / Direct :: SetValue");
        maleSocket.setLocked(false);

        actionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionBlock.getSelectNamedBean().setNamedBean(block1);

        actionBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionBlock.getSelectEnum().setEnum(ActionBlock.DirectOperation.SetValue);

        actionBlock.setDataAddressing(NamedBeanAddressing.Direct);
        actionBlock.setBlockValue("ABC");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct :: SetOccupied
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Direct / Direct :: SetOccupied");

        actionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionBlock.getSelectNamedBean().setNamedBean(block1);

        actionBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionBlock.getSelectEnum().setEnum(ActionBlock.DirectOperation.SetOccupied);

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / LocalVariable
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Direct / LocalVariable");

        actionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionBlock.getSelectNamedBean().setNamedBean(block1);

        actionBlock.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionBlock.getSelectEnum().setLocalVariable("index2");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// LocalVariable / Formula
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("LocalVariable / Formula");

        actionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionBlock.getSelectNamedBean().setLocalVariable("index");

        actionBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionBlock.getSelectEnum().setFormula("\"IT\"+index2");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Formula / Reference
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Formula / Reference");

        actionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionBlock.getSelectNamedBean().setFormula("\"IT\"+index");

        actionBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionBlock.getSelectEnum().setReference("{IM2}");

        maleSocket = digitalActionManager.registerAction(actionBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Reference / Direct :: SetNullValue
        actionBlock = new ActionBlock(digitalActionManager.getAutoSystemName(), null);
        actionBlock.setComment("Reference / Direct :: SetAltColorOn");

        actionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionBlock.getSelectNamedBean().setReference("{IM1}");

        actionBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionBlock.getSelectEnum().setEnum(ActionBlock.DirectOperation.SetNullValue);

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
        actionClock.getSelectEnum().setEnum(ActionClock.ClockState.StartClock);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// StopClock
        actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        actionClock.setComment("StopClock");
        actionClock.getSelectEnum().setEnum(ActionClock.ClockState.StopClock);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// SetClock
        actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        actionClock.setComment("SetClock");
        actionClock.getSelectEnum().setEnum(ActionClock.ClockState.SetClock);
        actionClock.getSelectTime().setValue(720);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// SetClock
        actionClock = new ActionClock(digitalActionManager.getAutoSystemName(), null);
        actionClock.setComment("SetClock");
        actionClock.getSelectEnum().setAddressing(NamedBeanAddressing.Memory);
        actionClock.getSelectEnum().setMemory(memory2);
        actionClock.getSelectTime().setAddressing(NamedBeanAddressing.Memory);
        actionClock.getSelectTime().setMemory(memory1);

        maleSocket = digitalActionManager.registerAction(actionClock);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionClockRate actionClockRate = new ActionClockRate(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionClockRate);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// StartClock
        actionClockRate = new ActionClockRate(digitalActionManager.getAutoSystemName(), null);
        actionClockRate.setComment("StartClock");
        actionClockRate.getSelectEnum().setEnum(ActionClockRate.ClockState.SetClockRate);
        actionClockRate.getSelectSpeed().setValue(4.234);

        maleSocket = digitalActionManager.registerAction(actionClockRate);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// StartClock
        actionClockRate = new ActionClockRate(digitalActionManager.getAutoSystemName(), null);
        actionClockRate.setComment("StartClock");
        actionClockRate.getSelectEnum().setAddressing(NamedBeanAddressing.Memory);
        actionClockRate.getSelectEnum().setMemory(memory2);
        actionClockRate.getSelectSpeed().setAddressing(NamedBeanAddressing.Memory);
        actionClockRate.getSelectSpeed().setMemory(memory1);
        actionClockRate.getSelectSpeed().setListenToMemory(true);

        maleSocket = digitalActionManager.registerAction(actionClockRate);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// StopClock
        actionClockRate = new ActionClockRate(digitalActionManager.getAutoSystemName(), null);
        actionClockRate.setComment("StopClock");
        actionClockRate.getSelectEnum().setEnum(ActionClockRate.ClockState.IncreaseClockRate);
        actionClockRate.getSelectSpeed().setValue(0.5);

        maleSocket = digitalActionManager.registerAction(actionClockRate);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// SetClock
        actionClockRate = new ActionClockRate(digitalActionManager.getAutoSystemName(), null);
        actionClockRate.setComment("SetClock");
        actionClockRate.getSelectEnum().setEnum(ActionClockRate.ClockState.DecreaseClockRate);
        actionClockRate.getSelectSpeed().setValue(1.22);

        maleSocket = digitalActionManager.registerAction(actionClockRate);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionDispatcher actionDispatcher = new ActionDispatcher(digitalActionManager.getAutoSystemName(), null);
        actionDispatcher.getSelectEnum().setEnum(ActionDispatcher.DirectOperation.TrainPriority);
        maleSocket = digitalActionManager.registerAction(actionDispatcher);
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
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Off);
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLight.getSelectNamedBean().setLocalVariable("index");
        actionLight.getSelectNamedBean().setReference("{IM1}");
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionLight.getSelectEnum().setFormula("\"IT\"+index2");
        actionLight.getSelectEnum().setLocalVariable("index2");
        actionLight.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.On);
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLight.getSelectNamedBean().setLocalVariable("index");
        actionLight.getSelectNamedBean().setReference("{IM1}");
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionLight.getSelectEnum().setFormula("\"IT\"+index2");
        actionLight.getSelectEnum().setLocalVariable("index2");
        actionLight.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Toggle);
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLight.getSelectNamedBean().setLocalVariable("index");
        actionLight.getSelectNamedBean().setReference("{IM1}");
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionLight.getSelectEnum().setFormula("\"IT\"+index2");
        actionLight.getSelectEnum().setLocalVariable("index2");
        actionLight.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogErrorOnce);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.setComment("A comment");
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Intensity);
        actionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLight.getSelectNamedBean().setLocalVariable("index");
        actionLight.getSelectNamedBean().setReference("{IM1}");
        actionLight.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionLight.getSelectEnum().setFormula("\"IT\"+index2");
        actionLight.getSelectEnum().setLocalVariable("index2");
        actionLight.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ShowDialogBox);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Interval);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Unknown);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        actionLight.getSelectNamedBean().setNamedBean(light1);
        actionLight.getSelectEnum().setEnum(ActionLight.LightState.Inconsistent);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        actionLight = new ActionLight(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLight);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionLightIntensity actionLightIntensity = new ActionLightIntensity(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLightIntensity);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLightIntensity = new ActionLightIntensity(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionLightIntensity);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.Default);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLightIntensity = new ActionLightIntensity(digitalActionManager.getAutoSystemName(), null);
        actionLightIntensity.setComment("A comment");
        actionLightIntensity.getSelectNamedBean().setNamedBean(variableLight1);
        actionLightIntensity.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionLightIntensity.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLightIntensity.getSelectNamedBean().setLocalVariable("index");
        actionLightIntensity.getSelectNamedBean().setReference("{IM1}");
        maleSocket = digitalActionManager.registerAction(actionLightIntensity);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLightIntensity = new ActionLightIntensity(digitalActionManager.getAutoSystemName(), null);
        actionLightIntensity.setComment("A comment");
        actionLightIntensity.getSelectNamedBean().setNamedBean(variableLight1);
        actionLightIntensity.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionLightIntensity.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLightIntensity.getSelectNamedBean().setLocalVariable("index");
        actionLightIntensity.getSelectNamedBean().setReference("{IM1}");
        maleSocket = digitalActionManager.registerAction(actionLightIntensity);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLightIntensity = new ActionLightIntensity(digitalActionManager.getAutoSystemName(), null);
        actionLightIntensity.setComment("A comment");
        actionLightIntensity.getSelectNamedBean().setNamedBean(variableLight1);
        actionLightIntensity.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionLightIntensity.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLightIntensity.getSelectNamedBean().setLocalVariable("index");
        actionLightIntensity.getSelectNamedBean().setReference("{IM1}");
        maleSocket = digitalActionManager.registerAction(actionLightIntensity);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogErrorOnce);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLightIntensity = new ActionLightIntensity(digitalActionManager.getAutoSystemName(), null);
        actionLightIntensity.setComment("A comment");
        actionLightIntensity.getSelectNamedBean().setNamedBean(variableLight1);
        actionLightIntensity.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionLightIntensity.getSelectNamedBean().setFormula("\"IT\"+index");
        actionLightIntensity.getSelectNamedBean().setLocalVariable("index");
        actionLightIntensity.getSelectNamedBean().setReference("{IM1}");
        maleSocket = digitalActionManager.registerAction(actionLightIntensity);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ShowDialogBox);
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
        NamedBeanReference ref = getNamedBeanReference(actionListenOnBeans.getReferences(), light1.getSystemName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(light1.getSystemName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Light, ref.getType());
        Assert.assertFalse(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Light:"+light2.getUserName());
        actionListenOnBeans.setLocalVariableNamedBean("localVariableNamedBean");
        actionListenOnBeans.setLocalVariableEvent("localVariableEvent");
        actionListenOnBeans.setLocalVariableNewValue("localVariableNewValue");
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), light2.getUserName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(light2.getUserName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Light, ref.getType());
        Assert.assertFalse(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Memory:"+memory1.getSystemName()+":no");
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), memory1.getSystemName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(memory1.getSystemName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Memory, ref.getType());
        Assert.assertFalse(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Memory:"+memory2.getUserName()+":yes");
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), memory2.getUserName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(memory2.getUserName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Memory, ref.getType());
        Assert.assertTrue(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Sensor:"+sensor1.getSystemName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), sensor1.getSystemName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(sensor1.getSystemName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Sensor, ref.getType());
        Assert.assertFalse(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Sensor:"+sensor2.getUserName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), sensor2.getUserName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(sensor2.getUserName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Sensor, ref.getType());
        Assert.assertFalse(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Turnout:"+turnout1.getSystemName());
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), turnout1.getSystemName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(turnout1.getSystemName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Turnout, ref.getType());
        Assert.assertFalse(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference("Turnout:"+turnout2.getUserName()+":yes");
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        ref = getNamedBeanReference(actionListenOnBeans.getReferences(), turnout2.getUserName());
        Assert.assertNotNull(ref);
        Assert.assertEquals(turnout2.getUserName(), ref.getName());
        Assert.assertEquals(NamedBeanType.Turnout, ref.getType());
        Assert.assertTrue(ref.getListenOnAllProperties());

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.setComment("A comment");
        actionListenOnBeans.addReference(new NamedBeanReference("MyGlobalVariable", NamedBeanType.GlobalVariable, false));
        maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        for (NamedBeanType namedBeanType : NamedBeanType.values()) {
            actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
            actionListenOnBeans.setComment("A comment");
            actionListenOnBeans.addReference(new NamedBeanReference("MyBean"+namedBeanType.name(), namedBeanType, false));
            maleSocket = digitalActionManager.registerAction(actionListenOnBeans);
            actionManySocket.getChild(indexAction++).connect(maleSocket);
        }


        ActionListenOnBeansTable actionListenOnBeansTable = new ActionListenOnBeansTable(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeansTable);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeansTable = new ActionListenOnBeansTable(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeansTable.setComment("A comment");
        actionListenOnBeansTable.getSelectNamedBean().setNamedBean(csvTable);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeansTable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeansTable = new ActionListenOnBeansTable(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeansTable.setComment("A comment");
        actionListenOnBeansTable.getSelectNamedBean().setNamedBean(csvTable);
        actionListenOnBeansTable.setRowOrColumnName("Signal before");
        actionListenOnBeansTable.setTableRowOrColumn(TableRowOrColumn.Row);
        actionListenOnBeansTable.setIncludeCellsWithoutHeader(false);
        actionListenOnBeansTable.setListenOnAllProperties(false);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeansTable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeansTable = new ActionListenOnBeansTable(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeansTable.setComment("A comment");
        actionListenOnBeansTable.getSelectNamedBean().setNamedBean(csvTable);
        actionListenOnBeansTable.setRowOrColumnName("2");
        actionListenOnBeansTable.setTableRowOrColumn(TableRowOrColumn.Column);
        actionListenOnBeansTable.setIncludeCellsWithoutHeader(false);
        actionListenOnBeansTable.setListenOnAllProperties(true);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeansTable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeansTable = new ActionListenOnBeansTable(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeansTable.setComment("A comment");
        actionListenOnBeansTable.getSelectNamedBean().setNamedBean(csvTable);
        actionListenOnBeansTable.setIncludeCellsWithoutHeader(true);
        actionListenOnBeansTable.setListenOnAllProperties(false);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeansTable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionListenOnBeansTable = new ActionListenOnBeansTable(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeansTable.setComment("A comment");
        actionListenOnBeansTable.getSelectNamedBean().setNamedBean(csvTable);
        actionListenOnBeansTable.setIncludeCellsWithoutHeader(true);
        actionListenOnBeansTable.setListenOnAllProperties(true);
        maleSocket = digitalActionManager.registerAction(actionListenOnBeansTable);
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
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyMemoryToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.getSelectBlockNamedBean().setNamedBean(block1);
        actionLocalVariable.getSelectReporterNamedBean().setNamedBean(reporter1);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyBlockToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.getSelectBlockNamedBean().setNamedBean(block1);
        actionLocalVariable.getSelectReporterNamedBean().setNamedBean(reporter1);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyReporterToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.getSelectBlockNamedBean().setNamedBean(block1);
        actionLocalVariable.getSelectReporterNamedBean().setNamedBean(reporter1);
        actionLocalVariable.setFormula("a+b");
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyVariableToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.setFormula("a+b");
        set_LogixNG_SelectTable_Data(csvTable, actionLocalVariable.getSelectTable(), NamedBeanAddressing.Reference);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.CopyTableCellToVariable);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.getSelectBlockNamedBean().setNamedBean(block1);
        actionLocalVariable.getSelectReporterNamedBean().setNamedBean(reporter1);
        actionLocalVariable.setFormula("a+b");
        set_LogixNG_SelectTable_Data(csvTable, actionLocalVariable.getSelectTable(), NamedBeanAddressing.Direct);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.SetToNull);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.setFormula("a+b");
        set_LogixNG_SelectTable_Data(csvTable, actionLocalVariable.getSelectTable(), NamedBeanAddressing.Formula);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionLocalVariable = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        actionLocalVariable.setComment("A comment");
        actionLocalVariable.setLocalVariable("result");
        actionLocalVariable.setVariableOperation(ActionLocalVariable.VariableOperation.SetToString);
        actionLocalVariable.setConstantValue("1");
        actionLocalVariable.setOtherLocalVariable("SomeVar");
        actionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory3);
        actionLocalVariable.setFormula("a+b");
        set_LogixNG_SelectTable_Data(csvTable, actionLocalVariable.getSelectTable(), NamedBeanAddressing.LocalVariable);
        maleSocket = digitalActionManager.registerAction(actionLocalVariable);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionMemory actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.getSelectNamedBean().setNamedBean(memory1);
        actionMemory.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionMemory.getSelectNamedBean().setFormula("\"IT\"+index");
        actionMemory.getSelectNamedBean().setLocalVariable("index");
        actionMemory.getSelectNamedBean().setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.getSelectNamedBean().setNamedBean(memory1);
        actionMemory.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionMemory.getSelectNamedBean().setFormula("\"IT\"+index");
        actionMemory.getSelectNamedBean().setLocalVariable("index");
        actionMemory.getSelectNamedBean().setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.getSelectNamedBean().setNamedBean(memory1);
        actionMemory.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionMemory.getSelectNamedBean().setFormula("\"IT\"+index");
        actionMemory.getSelectNamedBean().setLocalVariable("index");
        actionMemory.getSelectNamedBean().setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        set_LogixNG_SelectTable_Data(csvTable, actionMemory.getSelectTable(), NamedBeanAddressing.Reference);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.getSelectNamedBean().setNamedBean(memory1);
        actionMemory.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionMemory.getSelectNamedBean().setFormula("\"IT\"+index");
        actionMemory.getSelectNamedBean().setLocalVariable("index");
        actionMemory.getSelectNamedBean().setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.CopyTableCellToMemory);
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        set_LogixNG_SelectTable_Data(csvTable, actionMemory.getSelectTable(), NamedBeanAddressing.Direct);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.getSelectNamedBean().setNamedBean(memory1);
        actionMemory.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionMemory.getSelectNamedBean().setFormula("\"IT\"+index");
        actionMemory.getSelectNamedBean().setLocalVariable("index");
        actionMemory.getSelectNamedBean().setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        set_LogixNG_SelectTable_Data(csvTable, actionMemory.getSelectTable(), NamedBeanAddressing.Formula);
        maleSocket = digitalActionManager.registerAction(actionMemory);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionMemory = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        actionMemory.setComment("A comment");
        actionMemory.getSelectNamedBean().setNamedBean(memory1);
        actionMemory.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionMemory.getSelectNamedBean().setFormula("\"IT\"+index");
        actionMemory.getSelectNamedBean().setLocalVariable("index");
        actionMemory.getSelectNamedBean().setReference("{IM1}");
        actionMemory.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
        actionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        actionMemory.setOtherConstantValue("Some string");
        actionMemory.setOtherFormula("n + 3");
        actionMemory.setOtherLocalVariable("Somevar");
        set_LogixNG_SelectTable_Data(csvTable, actionMemory.getSelectTable(), NamedBeanAddressing.LocalVariable);
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

        actionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.getSelectNamedBean().setNamedBean("OB99");

        actionOBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.getSelectEnum().setEnum(ActionOBlock.DirectOperation.SetValue);

        actionOBlock.setDataAddressing(NamedBeanAddressing.Direct);
        actionOBlock.setOBlockValue("ABC");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct :: ClearError
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Direct / Direct :: ClearError");

        actionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.getSelectNamedBean().setNamedBean("OB99");

        actionOBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.getSelectEnum().setEnum(ActionOBlock.DirectOperation.ClearError);

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / LocalVariable
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Direct / LocalVariable");

        actionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.getSelectNamedBean().setNamedBean("OB99");

        actionOBlock.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionOBlock.getSelectEnum().setLocalVariable("index2");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// LocalVariable / Formula
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("LocalVariable / Formula");

        actionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionOBlock.getSelectNamedBean().setLocalVariable("index");

        actionOBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionOBlock.getSelectEnum().setFormula("\"IT\"+index2");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Formula / Reference
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Formula / Reference");

        actionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionOBlock.getSelectNamedBean().setFormula("\"IT\"+index");

        actionOBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionOBlock.getSelectEnum().setReference("{IM2}");

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogErrorOnce);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Reference / Direct :: SetOutOfService
        actionOBlock = new ActionOBlock(digitalActionManager.getAutoSystemName(), null);
        actionOBlock.setComment("Reference / Direct :: SetOutOfService");

        actionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionOBlock.getSelectNamedBean().setReference("{IM1}");

        actionOBlock.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionOBlock.getSelectEnum().setEnum(ActionOBlock.DirectOperation.SetOutOfService);

        maleSocket = digitalActionManager.registerAction(actionOBlock);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ShowDialogBox);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionPower actionPower = new ActionPower(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionPower);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionPower = new ActionPower(digitalActionManager.getAutoSystemName(), null);
        actionPower.setComment("A comment");
        actionPower.getSelectEnum().setEnum(ActionPower.PowerState.Off);
        maleSocket = digitalActionManager.registerAction(actionPower);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionPower = new ActionPower(digitalActionManager.getAutoSystemName(), null);
        actionPower.setComment("A comment");
        actionPower.getSelectEnum().setEnum(ActionPower.PowerState.On);
        maleSocket = digitalActionManager.registerAction(actionPower);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionReporter actionReporter = new ActionReporter(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionReporter);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionScript actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionScript);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        actionScript.setComment("A comment");
        actionScript.setScript("import java\n");
        actionScript.setOperationAddressing(NamedBeanAddressing.Direct);
        actionScript.setOperationFormula("a+b");
        actionScript.setOperationLocalVariable("myVar");
        actionScript.setOperationReference("{M1}");
        actionScript.setScriptAddressing(NamedBeanAddressing.Formula);
        actionScript.setScriptFormula("c+d");
        actionScript.setScriptLocalVariable("myOtherVar");
        actionScript.setScriptReference("{M2}");
        actionScript.getScriptEngineSelector().setSelectedEngine(ScriptEngineSelector.JYTHON);
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        actionScript.setComment("A comment");
        actionScript.setScript("myFile.py");
        actionScript.setOperationAddressing(NamedBeanAddressing.Formula);
        actionScript.setOperationFormula("a+b");
        actionScript.setOperationLocalVariable("myVar");
        actionScript.setOperationReference("{M1}");
        actionScript.setScriptAddressing(NamedBeanAddressing.LocalVariable);
        actionScript.setScriptFormula("c+d");
        actionScript.setScriptLocalVariable("myOtherVar");
        actionScript.setScriptReference("{M2}");
        actionScript.getScriptEngineSelector().setSelectedEngine(ScriptEngineSelector.ECMA_SCRIPT);
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        actionScript.setComment("A comment");
        actionScript.setScript("import java\n");
        actionScript.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionScript.setOperationFormula("a+b");
        actionScript.setOperationLocalVariable("myVar");
        actionScript.setOperationReference("{M1}");
        actionScript.setScriptAddressing(NamedBeanAddressing.Reference);
        actionScript.setScriptFormula("c+d");
        actionScript.setScriptLocalVariable("myOtherVar");
        actionScript.setScriptReference("{M2}");
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionScript = new ActionScript(digitalActionManager.getAutoSystemName(), null);
        actionScript.setComment("A comment");
        actionScript.setScript("import java\n");
        actionScript.setOperationAddressing(NamedBeanAddressing.Reference);
        actionScript.setOperationFormula("a+b");
        actionScript.setOperationLocalVariable("myVar");
        actionScript.setOperationReference("{M1}");
        actionScript.setScriptAddressing(NamedBeanAddressing.Direct);
        actionScript.setScriptFormula("c+d");
        actionScript.setScriptLocalVariable("myOtherVar");
        actionScript.setScriptReference("{M2}");
        maleSocket = digitalActionManager.registerAction(actionScript);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionSensor actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSensor);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.getSelectNamedBean().setNamedBean(sensor1);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Inactive);
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSensor.getSelectNamedBean().setLocalVariable("index");
        actionSensor.getSelectNamedBean().setReference("{IM1}");
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionSensor.getSelectEnum().setFormula("\"IT\"+index2");
        actionSensor.getSelectEnum().setLocalVariable("index2");
        actionSensor.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.getSelectNamedBean().setNamedBean(sensor1);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSensor.getSelectNamedBean().setLocalVariable("index");
        actionSensor.getSelectNamedBean().setReference("{IM1}");
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionSensor.getSelectEnum().setFormula("\"IT\"+index2");
        actionSensor.getSelectEnum().setLocalVariable("index2");
        actionSensor.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.getSelectNamedBean().setNamedBean(sensor1);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Toggle);
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSensor.getSelectNamedBean().setLocalVariable("index");
        actionSensor.getSelectNamedBean().setReference("{IM1}");
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionSensor.getSelectEnum().setFormula("\"IT\"+index2");
        actionSensor.getSelectEnum().setLocalVariable("index2");
        actionSensor.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.setComment("A comment");
        actionSensor.getSelectNamedBean().setNamedBean(sensor1);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Unknown);
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSensor.getSelectNamedBean().setLocalVariable("index");
        actionSensor.getSelectNamedBean().setReference("{IM1}");
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionSensor.getSelectEnum().setFormula("\"IT\"+index2");
        actionSensor.getSelectEnum().setLocalVariable("index2");
        actionSensor.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(actionSensor);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.getSelectNamedBean().setNamedBean(sensor1);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Inconsistent);
        maleSocket = digitalActionManager.registerAction(actionSensor);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionSignalHead actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        actionSignalHead.setComment("A comment");
        actionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        actionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalHead.getSelectNamedBean().setLocalVariable("index");
        actionSignalHead.getSelectNamedBean().setReference("{IM1}");
        actionSignalHead.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalHead.setOperationFormula("\"IT\"+index2");
        actionSignalHead.setOperationLocalVariable("index2");
        actionSignalHead.setOperationReference("{IM2}");
        actionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Formula);
        actionSignalHead.setAppearance(SignalHead.FLASHGREEN);
        actionSignalHead.setAppearanceFormula("\"IT\"+index3");
        actionSignalHead.setAppearanceLocalVariable("index3");
        actionSignalHead.setAppearanceReference("{IM3}");
        actionSignalHead.getSelectExampleNamedBean().setNamedBean("IH2");
        maleSocket = digitalActionManager.registerAction(actionSignalHead);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalHead = new ActionSignalHead(digitalActionManager.getAutoSystemName(), null);
        actionSignalHead.setComment("A comment");
        actionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        actionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalHead.getSelectNamedBean().setLocalVariable("index");
        actionSignalHead.getSelectNamedBean().setReference("{IM1}");
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
        actionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        actionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalHead.getSelectNamedBean().setLocalVariable("index");
        actionSignalHead.getSelectNamedBean().setReference("{IM1}");
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
        actionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        actionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalHead.getSelectNamedBean().setLocalVariable("index");
        actionSignalHead.getSelectNamedBean().setReference("{IM1}");
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
        actionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalMast.getSelectNamedBean().setLocalVariable("index");
        actionSignalMast.getSelectNamedBean().setReference("{IM1}");
        actionSignalMast.setOperationAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalMast.setOperationFormula("\"IT\"+index2");
        actionSignalMast.setOperationLocalVariable("index2");
        actionSignalMast.setOperationReference("{IM2}");
        actionSignalMast.setAspectAddressing(NamedBeanAddressing.Formula);
        actionSignalMast.setAspect("Medium Approach Slow");
        actionSignalMast.setAspectFormula("\"IT\"+index3");
        actionSignalMast.setAspectLocalVariable("index3");
        actionSignalMast.setAspectReference("{IM3}");
        actionSignalMast.getSelectExampleNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        maleSocket = digitalActionManager.registerAction(actionSignalMast);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionSignalMast = new ActionSignalMast(digitalActionManager.getAutoSystemName(), null);
        actionSignalMast.setComment("A comment");
        actionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalMast.getSelectNamedBean().setLocalVariable("index");
        actionSignalMast.getSelectNamedBean().setReference("{IM1}");
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
        actionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalMast.getSelectNamedBean().setLocalVariable("index");
        actionSignalMast.getSelectNamedBean().setReference("{IM1}");
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
        actionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        actionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        actionSignalMast.getSelectNamedBean().setLocalVariable("index");
        actionSignalMast.getSelectNamedBean().setReference("{IM1}");
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


        ActionSound simpleSound = new ActionSound(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(simpleSound);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleSound = new ActionSound(digitalActionManager.getAutoSystemName(), null);
        simpleSound.setComment("A comment");
        simpleSound.setSound("bell.wav\n");
        simpleSound.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        simpleSound.getSelectEnum().setFormula("a+b");
        simpleSound.getSelectEnum().setLocalVariable("myVar");
        simpleSound.getSelectEnum().setReference("{M1}");
        simpleSound.setSoundAddressing(NamedBeanAddressing.Formula);
        simpleSound.setSoundFormula("c+d");
        simpleSound.setSoundLocalVariable("myOtherVar");
        simpleSound.setSoundReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleSound);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleSound = new ActionSound(digitalActionManager.getAutoSystemName(), null);
        simpleSound.setComment("A comment");
        simpleSound.setSound("bell.wav\n");
        simpleSound.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        simpleSound.getSelectEnum().setFormula("a+b");
        simpleSound.getSelectEnum().setLocalVariable("myVar");
        simpleSound.getSelectEnum().setReference("{M1}");
        simpleSound.setSoundAddressing(NamedBeanAddressing.LocalVariable);
        simpleSound.setSoundFormula("c+d");
        simpleSound.setSoundLocalVariable("myOtherVar");
        simpleSound.setSoundReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleSound);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleSound = new ActionSound(digitalActionManager.getAutoSystemName(), null);
        simpleSound.setComment("A comment");
        simpleSound.setSound("bell.wav\n");
        simpleSound.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        simpleSound.getSelectEnum().setFormula("a+b");
        simpleSound.getSelectEnum().setLocalVariable("myVar");
        simpleSound.getSelectEnum().setReference("{M1}");
        simpleSound.setSoundAddressing(NamedBeanAddressing.Reference);
        simpleSound.setSoundFormula("c+d");
        simpleSound.setSoundLocalVariable("myOtherVar");
        simpleSound.setSoundReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleSound);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        simpleSound = new ActionSound(digitalActionManager.getAutoSystemName(), null);
        simpleSound.setComment("A comment");
        simpleSound.setSound("bell.wav\n");
        simpleSound.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        simpleSound.getSelectEnum().setFormula("a+b");
        simpleSound.getSelectEnum().setLocalVariable("myVar");
        simpleSound.getSelectEnum().setReference("{M1}");
        simpleSound.setSoundAddressing(NamedBeanAddressing.Direct);
        simpleSound.setSoundFormula("c+d");
        simpleSound.setSoundLocalVariable("myOtherVar");
        simpleSound.setSoundReference("{M2}");
        maleSocket = digitalActionManager.registerAction(simpleSound);
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
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Closed);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Direct);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Formula);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Formula);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.LocalVariable);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.LocalVariable);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Reference);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Memory);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setMemory(memory3);
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.LocalVariable);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Memory);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setMemory(memory2);
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Reference);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Unknown);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Reference);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Table);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Inconsistent);
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Direct);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        // Test an action there the turnout is given by the system name.
        // The system name should be stored and loaded from the panel file.
        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout2.getSystemName());
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Direct);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Inconsistent);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        // Test an action there the turnout is given by the user name.
        // The user name should be stored and loaded from the panel file.
        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout2.getUserName());
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Direct);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Inconsistent);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Closed);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Direct);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Formula);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Formula);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.LocalVariable);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.LocalVariable);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Reference);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.setComment("A comment");
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Unknown);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        actionTurnout.getSelectNamedBean().setLocalVariable("index");
        actionTurnout.getSelectNamedBean().setReference("{IM1}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Reference);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setFormula("\"IT\"+index2");
        actionTurnout.getSelectEnum().setLocalVariable("index2");
        actionTurnout.getSelectEnum().setReference("{IM2}");
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Table);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectNamedBean().getSelectTable(),
                NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setAddressing(NamedBeanAddressing.Table);
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Inconsistent);
        set_LogixNG_SelectTable_Data(csvTable, actionTurnout.getSelectEnum().getSelectTable(),
                NamedBeanAddressing.Direct);
        maleSocket = digitalActionManager.registerAction(actionTurnout);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionTurnoutLock actionTurnoutLock = new ActionTurnoutLock(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionTurnoutLock);
        maleSocket.setEnabled(false);
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

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectNamedBean().setNamedBean("IW99");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectEnum().setEnum(ActionWarrant.DirectOperation.SetTrainName);

        actionWarrant.setDataAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setTrainIdName("ABC");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct / Direct :: ControlAutoTrain - Resume
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / Direct / Direct :: ControlAutoTrain - Resume");

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectNamedBean().setNamedBean("IW99");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectEnum().setEnum(ActionWarrant.DirectOperation.ControlAutoTrain);

        actionWarrant.setDataAddressing(NamedBeanAddressing.Direct);
        actionWarrant.setControlAutoTrain(ActionWarrant.ControlAutoTrain.Resume);

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / Direct :: AllocateWarrantRoute
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / Direct :: AllocateWarrantRoute");

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectNamedBean().setNamedBean("IW99");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectEnum().setEnum(ActionWarrant.DirectOperation.AllocateWarrantRoute);

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Direct / LocalVariable
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Direct / LocalVariable");

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectNamedBean().setNamedBean("IW99");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        actionWarrant.getSelectEnum().setLocalVariable("index2");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// LocalVariable / Formula
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("LocalVariable / Formula");

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionWarrant.getSelectNamedBean().setLocalVariable("index");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionWarrant.getSelectEnum().setFormula("\"IT\"+index2");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Formula / Reference
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Formula / Reference");

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        actionWarrant.getSelectNamedBean().setFormula("\"IT\"+index");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionWarrant.getSelectEnum().setReference("{IM2}");

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

// Reference / Direct :: DeallocateWarrant
        actionWarrant = new ActionWarrant(digitalActionManager.getAutoSystemName(), null);
        actionWarrant.setComment("Reference / Direct :: DeallocateWarrant");

        actionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        actionWarrant.getSelectNamedBean().setReference("{IM1}");

        actionWarrant.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        actionWarrant.getSelectEnum().setEnum(ActionWarrant.DirectOperation.DeallocateWarrant);

        maleSocket = digitalActionManager.registerAction(actionWarrant);
        maleSocket.setErrorHandlingType(MaleSocket.ErrorHandlingType.AbortExecution);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        WebBrowser webBrowser = new WebBrowser(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(webBrowser);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrit.display.logixng.ActionPositionable actionPositionable =
                new jmri.jmrit.display.logixng.ActionPositionable(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionPositionable);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrix.loconet.logixng.ActionClearSlots actionClearSlots =
                new jmri.jmrix.loconet.logixng.ActionClearSlots(digitalActionManager.getAutoSystemName(), null, _locoNetMemo);
        maleSocket = digitalActionManager.registerAction(actionClearSlots);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrix.loconet.logixng.ActionUpdateSlots actionUpdateSlots =
                new jmri.jmrix.loconet.logixng.ActionUpdateSlots(digitalActionManager.getAutoSystemName(), null, _locoNetMemo);
        maleSocket = digitalActionManager.registerAction(actionUpdateSlots);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrix.mqtt.logixng.Publish publish =
                new jmri.jmrix.mqtt.logixng.Publish(digitalActionManager.getAutoSystemName(), null, _mqttMemo);
        maleSocket = digitalActionManager.registerAction(publish);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrix.mqtt.logixng.Subscribe subscribe =
                new jmri.jmrix.mqtt.logixng.Subscribe(digitalActionManager.getAutoSystemName(), null, _mqttMemo);
        maleSocket = digitalActionManager.registerAction(subscribe);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        jmri.jmrit.logixng.actions.DigitalCallModule callModule = new jmri.jmrit.logixng.actions.DigitalCallModule(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(callModule);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        callModule = new jmri.jmrit.logixng.actions.DigitalCallModule(digitalActionManager.getAutoSystemName(), null);
        callModule.setComment("A comment");
        callModule.getSelectNamedBean().setNamedBean("IQM1");
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


        jmri.jmrit.logixng.actions.DigitalFormula actionFormula =
                new jmri.jmrit.logixng.actions.DigitalFormula(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(actionFormula);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        actionFormula = new jmri.jmrit.logixng.actions.DigitalFormula(digitalActionManager.getAutoSystemName(), null);
        actionFormula.setComment("A comment");
        actionFormula.setFormula("n + 1");
        maleSocket = digitalActionManager.registerAction(actionFormula);
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
        enableLogix.getSelectNamedBean().setNamedBean(logixIX1);
        enableLogix.getSelectEnum().setEnum(EnableLogix.Operation.Enable);
        enableLogix.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        enableLogix.getSelectNamedBean().setFormula("\"IT\"+index");
        enableLogix.getSelectNamedBean().setLocalVariable("index");
        enableLogix.getSelectNamedBean().setReference("{IM1}");
        enableLogix.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        enableLogix.getSelectEnum().setFormula("\"IT\"+index2");
        enableLogix.getSelectEnum().setLocalVariable("index2");
        enableLogix.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.getSelectNamedBean().setNamedBean(logixIX1);
        enableLogix.getSelectEnum().setEnum(EnableLogix.Operation.Disable);
        enableLogix.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        enableLogix.getSelectNamedBean().setFormula("\"IT\"+index");
        enableLogix.getSelectNamedBean().setLocalVariable("index");
        enableLogix.getSelectNamedBean().setReference("{IM1}");
        enableLogix.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        enableLogix.getSelectEnum().setFormula("\"IT\"+index2");
        enableLogix.getSelectEnum().setLocalVariable("index2");
        enableLogix.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.getSelectNamedBean().setNamedBean(logixIX1);
        enableLogix.getSelectEnum().setEnum(EnableLogix.Operation.Enable);
        enableLogix.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        enableLogix.getSelectNamedBean().setFormula("\"IT\"+index");
        enableLogix.getSelectNamedBean().setLocalVariable("index");
        enableLogix.getSelectNamedBean().setReference("{IM1}");
        enableLogix.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        enableLogix.getSelectEnum().setFormula("\"IT\"+index2");
        enableLogix.getSelectEnum().setLocalVariable("index2");
        enableLogix.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        enableLogix = new EnableLogix(digitalActionManager.getAutoSystemName(), null);
        enableLogix.setComment("A comment");
        enableLogix.getSelectNamedBean().setNamedBean(logixIX1);
        enableLogix.getSelectEnum().setEnum(EnableLogix.Operation.Enable);
        enableLogix.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        enableLogix.getSelectNamedBean().setFormula("\"IT\"+index");
        enableLogix.getSelectNamedBean().setLocalVariable("index");
        enableLogix.getSelectNamedBean().setReference("{IM1}");
        enableLogix.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        enableLogix.getSelectEnum().setFormula("\"IT\"+index2");
        enableLogix.getSelectEnum().setLocalVariable("index2");
        enableLogix.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(enableLogix);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ActionEntryExit entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(entryExit);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairDisabled);
        entryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        entryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        entryExit.getSelectNamedBean().setLocalVariable("index");
        entryExit.getSelectNamedBean().setReference("{IM1}");
        entryExit.getSelectEnum().setAddressing(NamedBeanAddressing.LocalVariable);
        entryExit.getSelectEnum().setFormula("\"IT\"+index2");
        entryExit.getSelectEnum().setLocalVariable("index2");
        entryExit.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairEnabled);
        entryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        entryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        entryExit.getSelectNamedBean().setLocalVariable("index");
        entryExit.getSelectNamedBean().setReference("{IM1}");
        entryExit.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        entryExit.getSelectEnum().setFormula("\"IT\"+index2");
        entryExit.getSelectEnum().setLocalVariable("index2");
        entryExit.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairSegment);
        entryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        entryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        entryExit.getSelectNamedBean().setLocalVariable("index");
        entryExit.getSelectNamedBean().setReference("{IM1}");
        entryExit.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        entryExit.getSelectEnum().setFormula("\"IT\"+index2");
        entryExit.getSelectEnum().setLocalVariable("index2");
        entryExit.getSelectEnum().setReference("{IM2}");
        maleSocket = digitalActionManager.registerAction(entryExit);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        entryExit = new ActionEntryExit(digitalActionManager.getAutoSystemName(), null);
        entryExit.setComment("A comment");
        entryExit.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairDisabled);
        entryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        entryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        entryExit.getSelectNamedBean().setLocalVariable("index");
        entryExit.getSelectNamedBean().setReference("{IM1}");
        entryExit.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        entryExit.getSelectEnum().setFormula("\"IT\"+index2");
        entryExit.getSelectEnum().setLocalVariable("index2");
        entryExit.getSelectEnum().setReference("{IM2}");
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
        executeDelayed.setUseIndividualTimers(false);
        maleSocket = digitalActionManager.registerAction(executeDelayed);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        executeDelayed = new ExecuteDelayed(digitalActionManager.getAutoSystemName(), null);
        executeDelayed.setComment("A comment");
        executeDelayed.setDelayAddressing(NamedBeanAddressing.LocalVariable);
        executeDelayed.setDelayLocalVariable("MyVar");
        executeDelayed.setResetIfAlreadyStarted(true);
        executeDelayed.setUseIndividualTimers(true);
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
        logLocalVariables.setIncludeGlobalVariables(true);
        logLocalVariables.setExpandArraysAndMaps(false);
        maleSocket = digitalActionManager.registerAction(logLocalVariables);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        logLocalVariables = new LogLocalVariables(digitalActionManager.getAutoSystemName(), null);
        logLocalVariables.setComment("A comment");
        logLocalVariables.setIncludeGlobalVariables(false);
        logLocalVariables.setExpandArraysAndMaps(true);
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


        ShowDialog showDialog = new ShowDialog(digitalActionManager.getAutoSystemName(), null);
        showDialog.getEnabledButtons().add(ShowDialog.Button.Ok);
        maleSocket = digitalActionManager.registerAction(showDialog);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        showDialog = new ShowDialog(digitalActionManager.getAutoSystemName(), null);
        showDialog.setComment("A comment");
        showDialog.getEnabledButtons().add(ShowDialog.Button.Ok);
        showDialog.setLocalVariableForSelectedButton("myVar");
        showDialog.setModal(true);
        showDialog.setMultiLine(true);
        showDialog.setFormat("Some text");
        showDialog.setFormatType(ShowDialog.FormatType.OnlyText);
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.LocalVariable, "MyVar"));
        maleSocket = digitalActionManager.registerAction(showDialog);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        Or orTemp = new Or(digitalExpressionManager.getAutoSystemName(), null);
        MaleSocket maleSocketOr = digitalExpressionManager.registerExpression(orTemp);
        showDialog.getValidateSocket().connect(maleSocketOr);

        LogLocalVariables logLocalVariablesTemp = new LogLocalVariables(digitalActionManager.getAutoSystemName(), null);
        MaleSocket maleSocketLogLocalVariables = digitalActionManager.registerAction(logLocalVariablesTemp);
        showDialog.getExecuteSocket().connect(maleSocketLogLocalVariables);

        showDialog = new ShowDialog(digitalActionManager.getAutoSystemName(), null);
        showDialog.setComment("A comment");
        showDialog.getEnabledButtons().add(ShowDialog.Button.Cancel);
        showDialog.getEnabledButtons().add(ShowDialog.Button.Yes);
        showDialog.getEnabledButtons().add(ShowDialog.Button.No);
        showDialog.setModal(true);
        showDialog.setMultiLine(true);
        showDialog.setFormat("");
        showDialog.setFormatType(ShowDialog.FormatType.CommaSeparatedList);
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.Memory, "IM1"));
        maleSocket = digitalActionManager.registerAction(showDialog);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        showDialog = new ShowDialog(digitalActionManager.getAutoSystemName(), null);
        showDialog.setComment("A comment");
        showDialog.getEnabledButtons().add(ShowDialog.Button.No);
        showDialog.setModal(true);
        showDialog.setMultiLine(true);
        showDialog.setFormat("MyVar has the value %s");
        showDialog.setFormatType(ShowDialog.FormatType.StringFormat);
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.Reference, "{MyVar}"));
        maleSocket = digitalActionManager.registerAction(showDialog);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        showDialog = new ShowDialog(digitalActionManager.getAutoSystemName(), null);
        showDialog.setComment("A comment");
        showDialog.getEnabledButtons().add(ShowDialog.Button.No);
        showDialog.setModal(true);
        showDialog.setMultiLine(true);
        showDialog.setFormat("str(10): %s, 25: %d, IM1: %s, MyVar: %s");
        showDialog.setFormatType(ShowDialog.FormatType.StringFormat);
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.Formula, "str(10)"));
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.Formula, "25"));
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.Memory, "IM1"));
        showDialog.getDataList().add(new ShowDialog.Data(ShowDialog.DataType.LocalVariable, "MyVar"));
        maleSocket = digitalActionManager.registerAction(showDialog);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        ShutdownComputer shutdownComputer =
                new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.getSelectEnum().setEnum(ShutdownComputer.Operation.ShutdownComputer);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.getSelectEnum().setEnum(ShutdownComputer.Operation.RebootComputer);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.getSelectEnum().setEnum(ShutdownComputer.Operation.ShutdownJMRI);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        shutdownComputer = new ShutdownComputer(digitalActionManager.getAutoSystemName(), null);
        shutdownComputer.setComment("A comment");
        shutdownComputer.getSelectEnum().setEnum(ShutdownComputer.Operation.RebootJMRI);
        maleSocket = digitalActionManager.registerAction(shutdownComputer);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        TableForEach tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setRowOrColumn(TableRowOrColumn.Column);
        maleSocket = digitalActionManager.registerAction(tableForEach);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setComment("A comment");
        tableForEach.setLocalVariableName("MyLocalVariable");
        tableForEach.getSelectNamedBean().setNamedBean(csvTable);
        tableForEach.setRowOrColumn(TableRowOrColumn.Row);
        tableForEach.setRowOrColumnName("North yard");
        maleSocket = digitalActionManager.registerAction(tableForEach);
        actionManySocket.getChild(indexAction++).connect(maleSocket);
        maleSocket.getChild(0).connect(
                digitalActionManager.registerAction(
                        new DigitalMany(digitalActionManager.getAutoSystemName(), null)));

        tableForEach = new TableForEach(digitalActionManager.getAutoSystemName(), null);
        tableForEach.setComment("A comment");
        tableForEach.setLocalVariableName("MyLocalVariable");
        tableForEach.setRowOrColumn(TableRowOrColumn.Column);
        tableForEach.getSelectNamedBean().setNamedBean(csvTable);
        tableForEach.getSelectNamedBean().setReference("{MyTableRef}");
        tableForEach.getSelectNamedBean().setLocalVariable("MyTableVar");
        tableForEach.getSelectNamedBean().setFormula("MyTableFormula");
        tableForEach.setRowOrColumnName("Second turnout");
        tableForEach.setRowOrColumnReference("{MyRowOrColumnRef}");
        tableForEach.setRowOrColumnLocalVariable("MyRowOrColumnVar");
        tableForEach.setRowOrColumnFormula("MyRowOrColumnFormula");
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

        actionThrottle.getLocoAddressSocket().connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));

        actionThrottle.getLocoSpeedSocket().connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));

        actionThrottle.getLocoDirectionSocket().connect(
                digitalExpressionManager.registerExpression(
                        new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null)));

        actionThrottle.getLocoFunctionSocket().connect(
                analogExpressionManager.registerExpression(
                        new AnalogExpressionMemory(analogExpressionManager.getAutoSystemName(), null)));

        actionThrottle.getLocoFunctionOnOffSocket().connect(
                digitalExpressionManager.registerExpression(
                        new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null)));



        Timeout timeout = new Timeout(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(timeout);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        timeout = new Timeout(digitalActionManager.getAutoSystemName(), null);
        timeout.setComment("A comment");
        timeout.getSelectDelay().setAddressing(NamedBeanAddressing.Direct);
        timeout.getSelectDelay().setValue(100);
        maleSocket = digitalActionManager.registerAction(timeout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        timeout = new Timeout(digitalActionManager.getAutoSystemName(), null);
        timeout.setComment("A comment");
        timeout.getSelectDelay().setAddressing(NamedBeanAddressing.Memory);
        timeout.getSelectDelay().setMemory(memory3);
        maleSocket = digitalActionManager.registerAction(timeout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        timeout = new Timeout(digitalActionManager.getAutoSystemName(), null);
        timeout.setComment("A comment");
        timeout.getSelectDelay().setAddressing(NamedBeanAddressing.LocalVariable);
        timeout.getSelectDelay().setLocalVariable("MyVar");
        maleSocket = digitalActionManager.registerAction(timeout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        timeout = new Timeout(digitalActionManager.getAutoSystemName(), null);
        timeout.setComment("A comment");
        timeout.getSelectDelay().setAddressing(NamedBeanAddressing.Reference);
        timeout.getSelectDelay().setReference("{MyMemory}");
        maleSocket = digitalActionManager.registerAction(timeout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        timeout = new Timeout(digitalActionManager.getAutoSystemName(), null);
        timeout.setComment("A comment");
        timeout.getSelectDelay().setAddressing(NamedBeanAddressing.Formula);
        timeout.getSelectDelay().setFormula("MyVar + 10");
        maleSocket = digitalActionManager.registerAction(timeout);
        actionManySocket.getChild(indexAction++).connect(maleSocket);


        TriggerRoute triggerRoute =
                new TriggerRoute(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(triggerRoute);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        triggerRoute = new TriggerRoute(digitalActionManager.getAutoSystemName(), null);
        triggerRoute.setComment("A comment");
        triggerRoute.getSelectEnum().setEnum(TriggerRoute.Operation.TriggerRoute);
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
        expressionCallModule.getSelectNamedBean().setNamedBean("IQM1");
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

        expressionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionBlock.getSelectNamedBean().setNamedBean(block1);

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

        expressionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionBlock.getSelectNamedBean().setNamedBean(block1);

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Direct);
        expressionBlock.setBeanState(ExpressionBlock.BlockState.Occupied);

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Direct / LocalVariable
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Direct / LocalVariable");

        expressionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionBlock.getSelectNamedBean().setNamedBean(block1);

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);

        expressionBlock.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionBlock.setStateLocalVariable("index2");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// LocalVariable / Formula
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("LocalVariable / Formula");

        expressionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionBlock.getSelectNamedBean().setLocalVariable("index");

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Formula);
        expressionBlock.setStateFormula("\"IT\"+index2");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Formula / Reference
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Formula / Reference");

        expressionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionBlock.getSelectNamedBean().setFormula("\"IT\"+index");

        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);

        expressionBlock.setStateAddressing(NamedBeanAddressing.Reference);
        expressionBlock.setStateReference("{IM2}");

        maleSocket = digitalExpressionManager.registerExpression(expressionBlock);
        and.getChild(indexExpr++).connect(maleSocket);

// Reference / Direct :: Allocated
        expressionBlock = new ExpressionBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionBlock.setComment("Reference / Direct :: Allocated");

        expressionBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionBlock.getSelectNamedBean().setReference("{IM1}");

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
        expressionConditional.getSelectNamedBean().setNamedBean("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.False);
        expressionConditional.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionConditional.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionConditional.getSelectNamedBean().setLocalVariable("index");
        expressionConditional.getSelectNamedBean().setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionConditional.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.getSelectNamedBean().setNamedBean("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.True);
        expressionConditional.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionConditional.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionConditional.getSelectNamedBean().setLocalVariable("index");
        expressionConditional.getSelectNamedBean().setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionConditional.setStateAddressing(NamedBeanAddressing.Formula);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.getSelectNamedBean().setNamedBean("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.Other);
        expressionConditional.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionConditional.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionConditional.getSelectNamedBean().setLocalVariable("index");
        expressionConditional.getSelectNamedBean().setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionConditional.setStateAddressing(NamedBeanAddressing.Reference);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionConditional = new ExpressionConditional(digitalExpressionManager.getAutoSystemName(), null);
        expressionConditional.setComment("A comment");
        expressionConditional.getSelectNamedBean().setNamedBean("IX1C1");
        expressionConditional.setConditionalState(ExpressionConditional.ConditionalState.False);
        expressionConditional.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionConditional.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionConditional.getSelectNamedBean().setLocalVariable("index");
        expressionConditional.getSelectNamedBean().setReference("{IM1}");
        expressionConditional.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionConditional.setStateAddressing(NamedBeanAddressing.Direct);
        expressionConditional.setStateFormula("\"IT\"+index2");
        expressionConditional.setStateLocalVariable("index2");
        expressionConditional.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionConditional);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionDispatcher expressionDispatcher = new ExpressionDispatcher(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionDispatcher);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionEntryExit expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionEntryExit);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionEntryExit = new ExpressionEntryExit(digitalExpressionManager.getAutoSystemName(), null);
        expressionEntryExit.setComment("A comment");
        expressionEntryExit.setBeanState(ExpressionEntryExit.EntryExitState.Inactive);
        expressionEntryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionEntryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionEntryExit.getSelectNamedBean().setLocalVariable("index");
        expressionEntryExit.getSelectNamedBean().setReference("{IM1}");
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
        expressionEntryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionEntryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionEntryExit.getSelectNamedBean().setLocalVariable("index");
        expressionEntryExit.getSelectNamedBean().setReference("{IM1}");
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
        expressionEntryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionEntryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionEntryExit.getSelectNamedBean().setLocalVariable("index");
        expressionEntryExit.getSelectNamedBean().setReference("{IM1}");
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
        expressionEntryExit.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionEntryExit.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionEntryExit.getSelectNamedBean().setLocalVariable("index");
        expressionEntryExit.getSelectNamedBean().setReference("{IM1}");
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
        expressionLight.getSelectNamedBean().setNamedBean(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.Off);
        expressionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionLight.getSelectNamedBean().setLocalVariable("index");
        expressionLight.getSelectNamedBean().setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionLight.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.getSelectNamedBean().setNamedBean(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.On);
        expressionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionLight.getSelectNamedBean().setLocalVariable("index");
        expressionLight.getSelectNamedBean().setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionLight.setStateAddressing(NamedBeanAddressing.Formula);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.getSelectNamedBean().setNamedBean(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.Other);
        expressionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionLight.getSelectNamedBean().setLocalVariable("index");
        expressionLight.getSelectNamedBean().setReference("{IM1}");
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionLight.setStateAddressing(NamedBeanAddressing.Reference);
        expressionLight.setStateFormula("\"IT\"+index2");
        expressionLight.setStateLocalVariable("index2");
        expressionLight.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionLight);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLight = new ExpressionLight(digitalExpressionManager.getAutoSystemName(), null);
        expressionLight.setComment("A comment");
        expressionLight.getSelectNamedBean().setNamedBean(light1);
        expressionLight.setBeanState(ExpressionLight.LightState.Off);
        expressionLight.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionLight.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionLight.getSelectNamedBean().setLocalVariable("index");
        expressionLight.getSelectNamedBean().setReference("{IM1}");
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
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Memory);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.LocalVariable);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setOtherLocalVariable("MyOtherVar");
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.LocalVariable);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.Reference);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setOtherLocalVariable("MyOtherVar");
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Table);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.Direct);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setOtherLocalVariable("MyOtherVar");
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Table);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.Formula);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setOtherLocalVariable("MyOtherVar");
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Table);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.LocalVariable);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setOtherLocalVariable("MyOtherVar");
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.Table);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.Reference);
        maleSocket = digitalExpressionManager.registerExpression(expressionLocalVariable);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionLocalVariable = new ExpressionLocalVariable(digitalExpressionManager.getAutoSystemName(), null);
        expressionLocalVariable.setComment("A comment");
        expressionLocalVariable.setLocalVariable("MyVar");
        expressionLocalVariable.setRegEx("/^Test$/");
        expressionLocalVariable.getSelectMemoryNamedBean().setNamedBean(memory2);
        expressionLocalVariable.setCaseInsensitive(false);
        expressionLocalVariable.setCompareTo(ExpressionLocalVariable.CompareTo.RegEx);
        expressionLocalVariable.setVariableOperation(ExpressionLocalVariable.VariableOperation.LessThan);
        set_LogixNG_SelectTable_Data(csvTable, expressionLocalVariable.getSelectTable(), NamedBeanAddressing.Formula);
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
        expressionMemory.getSelectNamedBean().setNamedBean(memory1);
        expressionMemory.setConstantValue("10");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.LessThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Value);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.getSelectNamedBean().setNamedBean(memory2);
        expressionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Memory);
        set_LogixNG_SelectTable_Data(csvTable, expressionMemory.getSelectTable(), NamedBeanAddressing.Reference);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.getSelectNamedBean().setNamedBean(memory2);
        expressionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.Table);
        set_LogixNG_SelectTable_Data(csvTable, expressionMemory.getSelectTable(), NamedBeanAddressing.Direct);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.getSelectNamedBean().setNamedBean(memory2);
        expressionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        expressionMemory.setLocalVariable("MyVar");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.LocalVariable);
        set_LogixNG_SelectTable_Data(csvTable, expressionMemory.getSelectTable(), NamedBeanAddressing.Formula);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionMemory = new ExpressionMemory(digitalExpressionManager.getAutoSystemName(), null);
        expressionMemory.setComment("A comment");
        expressionMemory.getSelectNamedBean().setNamedBean(memory2);
        expressionMemory.getSelectOtherMemoryNamedBean().setNamedBean(memory3);
        expressionMemory.setRegEx("/^Hello$/");
        expressionMemory.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
        expressionMemory.setCompareTo(ExpressionMemory.CompareTo.RegEx);
        set_LogixNG_SelectTable_Data(csvTable, expressionMemory.getSelectTable(), NamedBeanAddressing.LocalVariable);
        maleSocket = digitalExpressionManager.registerExpression(expressionMemory);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionOBlock expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.getSelectNamedBean().setNamedBean("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.Dark);
        expressionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionOBlock.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionOBlock.getSelectNamedBean().setLocalVariable("index");
        expressionOBlock.getSelectNamedBean().setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.getSelectNamedBean().setNamedBean("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.Allocated);
        expressionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionOBlock.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionOBlock.getSelectNamedBean().setLocalVariable("index");
        expressionOBlock.getSelectNamedBean().setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.Formula);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.getSelectNamedBean().setNamedBean("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.Occupied);
        expressionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionOBlock.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionOBlock.getSelectNamedBean().setLocalVariable("index");
        expressionOBlock.getSelectNamedBean().setReference("{IM1}");
        expressionOBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionOBlock.setStateAddressing(NamedBeanAddressing.Reference);
        expressionOBlock.setStateFormula("\"IT\"+index2");
        expressionOBlock.setStateLocalVariable("index2");
        expressionOBlock.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionOBlock);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionOBlock = new ExpressionOBlock(digitalExpressionManager.getAutoSystemName(), null);
        expressionOBlock.setComment("A comment");
        expressionOBlock.getSelectNamedBean().setNamedBean("OB99");
        expressionOBlock.setBeanState(OBlock.OBlockStatus.OutOfService);
        expressionOBlock.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionOBlock.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionOBlock.getSelectNamedBean().setLocalVariable("index");
        expressionOBlock.getSelectNamedBean().setReference("{IM1}");
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


        ExpressionReporter expressionReporter = new ExpressionReporter(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionReporter);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionScript expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        expressionScript.setComment("A comment");
        expressionScript.setScript("myFile.py");
        expressionScript.setOperationType(ExpressionScript.OperationType.RunScript);
        expressionScript.setRegisterListenerScript("sensors.provideSensor(\"IS1\").addPropertyChangeListener(self)");
        expressionScript.setUnregisterListenerScript("sensors.provideSensor(\"IS1\").removePropertyChangeListener(self)");
        expressionScript.getScriptEngineSelector().setSelectedEngine(ScriptEngineSelector.ECMA_SCRIPT);
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionScript = new ExpressionScript(digitalExpressionManager.getAutoSystemName(), null);
        expressionScript.setComment("A comment");
        expressionScript.setScript("result.setValue( sensors.provideSensor(\"IS1\").getState() == ACTIVE )");
        expressionScript.setOperationType(ExpressionScript.OperationType.SingleLineCommand);
        expressionScript.setRegisterListenerScript("sensors.provideSensor(\"IS1\").addPropertyChangeListener(self)");
        expressionScript.setUnregisterListenerScript("sensors.provideSensor(\"IS1\").removePropertyChangeListener(self)");
        expressionScript.getScriptEngineSelector().setSelectedEngine(ScriptEngineSelector.ECMA_SCRIPT);
        maleSocket = digitalExpressionManager.registerExpression(expressionScript);
        and.getChild(indexExpr++).connect(maleSocket);


        ExpressionSensor expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.getSelectNamedBean().setNamedBean(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSensor.getSelectNamedBean().setLocalVariable("index");
        expressionSensor.getSelectNamedBean().setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionSensor.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.getSelectNamedBean().setNamedBean(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSensor.getSelectNamedBean().setLocalVariable("index");
        expressionSensor.getSelectNamedBean().setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSensor.setStateAddressing(NamedBeanAddressing.Formula);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.getSelectNamedBean().setNamedBean(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSensor.getSelectNamedBean().setLocalVariable("index");
        expressionSensor.getSelectNamedBean().setReference("{IM1}");
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionSensor.setStateAddressing(NamedBeanAddressing.Reference);
        expressionSensor.setStateFormula("\"IT\"+index2");
        expressionSensor.setStateLocalVariable("index2");
        expressionSensor.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionSensor);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSensor = new ExpressionSensor(digitalExpressionManager.getAutoSystemName(), null);
        expressionSensor.setComment("A comment");
        expressionSensor.getSelectNamedBean().setNamedBean(sensor1);
        expressionSensor.setBeanState(ExpressionSensor.SensorState.Inactive);
        expressionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionSensor.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSensor.getSelectNamedBean().setLocalVariable("index");
        expressionSensor.getSelectNamedBean().setReference("{IM1}");
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
        expressionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        expressionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalHead.getSelectNamedBean().setLocalVariable("index");
        expressionSignalHead.getSelectNamedBean().setReference("{IM1}");
        expressionSignalHead.setQueryAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalHead.setQueryFormula("\"IT\"+index2");
        expressionSignalHead.setQueryLocalVariable("index2");
        expressionSignalHead.setQueryReference("{IM2}");
        expressionSignalHead.setAppearanceAddressing(NamedBeanAddressing.Formula);
        expressionSignalHead.setAppearance(SignalHead.FLASHGREEN);
        expressionSignalHead.setAppearanceFormula("\"IT\"+index3");
        expressionSignalHead.setAppearanceLocalVariable("index3");
        expressionSignalHead.setAppearanceReference("{IM3}");
        expressionSignalHead.getSelectExampleNamedBean().setNamedBean("IH2");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalHead);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalHead = new ExpressionSignalHead(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalHead.setComment("A comment");
        expressionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        expressionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalHead.getSelectNamedBean().setLocalVariable("index");
        expressionSignalHead.getSelectNamedBean().setReference("{IM1}");
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
        expressionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        expressionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalHead.getSelectNamedBean().setLocalVariable("index");
        expressionSignalHead.getSelectNamedBean().setReference("{IM1}");
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
        expressionSignalHead.getSelectNamedBean().setNamedBean("IH1");
        expressionSignalHead.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionSignalHead.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalHead.getSelectNamedBean().setLocalVariable("index");
        expressionSignalHead.getSelectNamedBean().setReference("{IM1}");
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
        expressionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalMast.getSelectNamedBean().setLocalVariable("index");
        expressionSignalMast.getSelectNamedBean().setReference("{IM1}");
        expressionSignalMast.setQueryAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalMast.setQueryFormula("\"IT\"+index2");
        expressionSignalMast.setQueryLocalVariable("index2");
        expressionSignalMast.setQueryReference("{IM2}");
        expressionSignalMast.setAspectAddressing(NamedBeanAddressing.Formula);
        expressionSignalMast.setAspect("Medium Approach Slow");
        expressionSignalMast.setAspectFormula("\"IT\"+index3");
        expressionSignalMast.setAspectLocalVariable("index3");
        expressionSignalMast.setAspectReference("{IM3}");
        expressionSignalMast.getSelectExampleNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        maleSocket = digitalExpressionManager.registerExpression(expressionSignalMast);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionSignalMast = new ExpressionSignalMast(digitalExpressionManager.getAutoSystemName(), null);
        expressionSignalMast.setComment("A comment");
        expressionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalMast.getSelectNamedBean().setLocalVariable("index");
        expressionSignalMast.getSelectNamedBean().setReference("{IM1}");
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
        expressionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalMast.getSelectNamedBean().setLocalVariable("index");
        expressionSignalMast.getSelectNamedBean().setReference("{IM1}");
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
        expressionSignalMast.getSelectNamedBean().setNamedBean("IF$shsm:AAR-1946:CPL(IH1)");
        expressionSignalMast.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionSignalMast.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionSignalMast.getSelectNamedBean().setLocalVariable("index");
        expressionSignalMast.getSelectNamedBean().setReference("{IM1}");
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
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Closed);
        expressionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionTurnout.getSelectNamedBean().setLocalVariable("index");
        expressionTurnout.getSelectNamedBean().setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Thrown);
        expressionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionTurnout.getSelectNamedBean().setLocalVariable("index");
        expressionTurnout.getSelectNamedBean().setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.Formula);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Other);
        expressionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionTurnout.getSelectNamedBean().setLocalVariable("index");
        expressionTurnout.getSelectNamedBean().setReference("{IM1}");
        expressionTurnout.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionTurnout.setStateAddressing(NamedBeanAddressing.Reference);
        expressionTurnout.setStateFormula("\"IT\"+index2");
        expressionTurnout.setStateLocalVariable("index2");
        expressionTurnout.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionTurnout);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionTurnout = new ExpressionTurnout(digitalExpressionManager.getAutoSystemName(), null);
        expressionTurnout.setComment("A comment");
        expressionTurnout.getSelectNamedBean().setNamedBean(turnout1);
        expressionTurnout.setBeanState(ExpressionTurnout.TurnoutState.Closed);
        expressionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionTurnout.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionTurnout.getSelectNamedBean().setLocalVariable("index");
        expressionTurnout.getSelectNamedBean().setReference("{IM1}");
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
        expressionWarrant.getSelectNamedBean().setNamedBean("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteAllocated);
        expressionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        expressionWarrant.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionWarrant.getSelectNamedBean().setLocalVariable("index");
        expressionWarrant.getSelectNamedBean().setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.LocalVariable);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.getSelectNamedBean().setNamedBean("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteFree);
        expressionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        expressionWarrant.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionWarrant.getSelectNamedBean().setLocalVariable("index");
        expressionWarrant.getSelectNamedBean().setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.Formula);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.getSelectNamedBean().setNamedBean("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteOccupied);
        expressionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        expressionWarrant.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionWarrant.getSelectNamedBean().setLocalVariable("index");
        expressionWarrant.getSelectNamedBean().setReference("{IM1}");
        expressionWarrant.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        expressionWarrant.setStateAddressing(NamedBeanAddressing.Reference);
        expressionWarrant.setStateFormula("\"IT\"+index2");
        expressionWarrant.setStateLocalVariable("index2");
        expressionWarrant.setStateReference("{IM2}");
        maleSocket = digitalExpressionManager.registerExpression(expressionWarrant);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionWarrant = new ExpressionWarrant(digitalExpressionManager.getAutoSystemName(), null);
        expressionWarrant.setComment("A comment");
        expressionWarrant.getSelectNamedBean().setNamedBean("IW99");
        expressionWarrant.setBeanState(ExpressionWarrant.WarrantState.RouteSet);
        expressionWarrant.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        expressionWarrant.getSelectNamedBean().setFormula("\"IT\"+index");
        expressionWarrant.getSelectNamedBean().setLocalVariable("index");
        expressionWarrant.getSelectNamedBean().setReference("{IM1}");
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


        jmri.jmrit.logixng.expressions.DigitalFormula expressionFormula =
                new jmri.jmrit.logixng.expressions.DigitalFormula(digitalExpressionManager.getAutoSystemName(), null);
        maleSocket = digitalExpressionManager.registerExpression(expressionFormula);
        maleSocket.setEnabled(false);
        and.getChild(indexExpr++).connect(maleSocket);

        expressionFormula = new jmri.jmrit.logixng.expressions.DigitalFormula(digitalExpressionManager.getAutoSystemName(), null);
        expressionFormula.setComment("A comment");
        expressionFormula.setFormula("n + 1");
        maleSocket = digitalExpressionManager.registerExpression(expressionFormula);
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
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean("A hold expression");
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


        jmri.jmrix.loconet.logixng.ExpressionSlotUsage expressionSlotUsage =
                new jmri.jmrix.loconet.logixng.ExpressionSlotUsage(digitalExpressionManager.getAutoSystemName(), null, _locoNetMemo);
        maleSocket = digitalExpressionManager.registerExpression(expressionSlotUsage);
        maleSocket.setEnabled(false);
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
        analogActionMemory.getSelectNamedBean().setNamedBean(memory2);
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
        analogExpressionMemory.getSelectNamedBean().setNamedBean(memory1);
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

        AnalogActionLightIntensity analogActionLightIntensity = new AnalogActionLightIntensity(analogActionManager.getAutoSystemName(), null);
        maleSocket = analogActionManager.registerAction(analogActionLightIntensity);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(1).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        analogFormula = new AnalogFormula(analogExpressionManager.getAutoSystemName(), null);
        analogFormula.setComment("A comment");
        analogFormula.setFormula("sin(a)*2 + 14");
        maleSocket = analogExpressionManager.registerExpression(analogFormula);
        doAnalogAction.getChild(0).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        AnalogExpressionAnalogIO analogExpressionAnalogIO = new AnalogExpressionAnalogIO(analogExpressionManager.getAutoSystemName(), null);
        maleSocket = analogExpressionManager.registerExpression(analogExpressionAnalogIO);
        maleSocket.setEnabled(false);
        doAnalogAction.getChild(0).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        TimeSinceMidnight timeSinceMidnight = new TimeSinceMidnight(analogExpressionManager.getAutoSystemName(), null);
        maleSocket = analogExpressionManager.registerExpression(timeSinceMidnight);
        maleSocket.setEnabled(false);
        timeSinceMidnight.setType(TimeSinceMidnight.Type.SystemClock);
        doAnalogAction.getChild(0).connect(maleSocket);


        doAnalogAction = new DoAnalogAction(digitalActionManager.getAutoSystemName(), null);
        maleSocket = digitalActionManager.registerAction(doAnalogAction);
        maleSocket.setEnabled(false);
        actionManySocket.getChild(indexAction++).connect(maleSocket);

        timeSinceMidnight = new TimeSinceMidnight(analogExpressionManager.getAutoSystemName(), null);
        timeSinceMidnight.setComment("A comment");
        timeSinceMidnight.setType(TimeSinceMidnight.Type.FastClock);
        maleSocket = analogExpressionManager.registerExpression(timeSinceMidnight);
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
        stringActionMemory.getSelectNamedBean().setNamedBean(memory2);
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
        stringExpressionMemory.getSelectNamedBean().setNamedBean(memory1);
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

        StringActionStringIO stringActionStringIO = new StringActionStringIO(stringActionManager.getAutoSystemName(), null);
        maleSocket = stringActionManager.registerAction(stringActionStringIO);
        maleSocket.setEnabled(false);
        doStringAction.getChild(1).connect(maleSocket);


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
        Assert.assertNotEquals(0, InstanceManager.getDefault(GlobalVariableManager.class).getNamedBeanSet().size());



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


        // Verify that we have all the actions and expressions in the tree, even
        // actions and expressions defined outside of the jmri.jmrit.logixng tree.

        Set<Class<? extends Base>> testedClasses = new HashSet<>();
        Map<Class<? extends FemaleSocket>, FemaleSocket> femaleSocketMap = new HashMap<>();
        List<Class<? extends Base>> missingClasses = new ArrayList<>();

        femaleRootSocket.forEntireTree((Base b) -> {
//            if (!(b instanceof FemaleSocket) && !(b instanceof MaleSocket)) {
            if (b instanceof MaleSocket) {
                Base o = ((MaleSocket) b).getObject();
                while (o instanceof MaleSocket) {
                    o = ((MaleSocket) o).getObject();
                }
//                System.out.format("Class: %s%n", o.getClass().getName());
                testedClasses.add(o.getClass());
                for (int i=0; i < o.getChildCount(); i++) {
                    FemaleSocket fs = o.getChild(i);
                    femaleSocketMap.put(fs.getClass(), fs);
                }
            }
        });

        for (var femaleSocket : femaleSocketMap.values()) {
            var connectableClasses = femaleSocket.getConnectableClasses();

            for (var list : connectableClasses.values()) {
                for (var clazz : list) {
                    if (!testedClasses.contains(clazz)) {
//                        System.out.format("Class is not tested: %s%n", clazz.getName());
                        missingClasses.add(clazz);
                    }
                }
            }
        }

        Collections.sort(missingClasses, (o1,o2) -> {
            return o1.getName().compareTo(o2.getName());
        });

        for (var clazz : missingClasses) {
            log.error("Class {} is not added by CreateLogixNGTreeScaffold.createLogixNGTree()", clazz.getName());
        }
        Assert.assertTrue(missingClasses.isEmpty());

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
    }


    private static void set_LogixNG_SelectTable_Data(
            NamedTable csvTable,
            LogixNG_SelectTable selectTable,
            NamedBeanAddressing nameAddressing)
            throws ParserException {

        int next1 = nameAddressing.ordinal() + 1;
        if ((next1 < NamedBeanAddressing.values().length)
                && (NamedBeanAddressing.values()[next1] == NamedBeanAddressing.Table)) {
            next1++;
        }
        if (next1 >= NamedBeanAddressing.values().length) next1 = 0;
        NamedBeanAddressing rowAddressing = NamedBeanAddressing.values()[next1];

        int next2 = next1 + 1;
        if ((next2 < NamedBeanAddressing.values().length)
                && (NamedBeanAddressing.values()[next2] == NamedBeanAddressing.Table)) {
            next2++;
        }
        if (next2 >= NamedBeanAddressing.values().length) next2 = 0;
        NamedBeanAddressing colAddressing = NamedBeanAddressing.values()[next2];

        selectTable.setTableNameAddressing(nameAddressing);
        selectTable.setTable(csvTable);
        selectTable.setTableNameReference("{tableRef}");
        selectTable.setTableNameLocalVariable("tableVariable");
        selectTable.setTableNameFormula("\"IT\"+str(index)");
        selectTable.setTableRowAddressing(rowAddressing);
        selectTable.setTableRowName("The row");
        selectTable.setTableRowReference("{rowRef}");
        selectTable.setTableRowLocalVariable("rowVariable");
        selectTable.setTableRowFormula("\"Row \"+str(index)");
        selectTable.setTableColumnAddressing(colAddressing);
        selectTable.setTableColumnName("The column");
        selectTable.setTableColumnReference("{columnRef}");
        selectTable.setTableColumnLocalVariable("columnVariable");
        selectTable.setTableColumnFormula("\"Column \"+str(index)");
    }


    private static final String[] initValues = new String[]{
        "",             // None
        "32",           // Integer
        "41.429",       // FloatingNumber
        "My string",    // String
        "",             // Array
        "",             // Map
        "index",        // LocalVariable
        "IM2",          // Memory
        "{IM3}",        // Reference
        "index * 2",    // Formula
        "sensors.provide(\"mySensor)\"",    // Script expression
        "scripts:InitLogixNGVariable",      // Script file
        "MyTable",      // LogixNG Table
    };


    private static void addVariables(MaleSocket maleSocket) {
        int i = 0;
        for (InitialValueType type : InitialValueType.values()) {
            maleSocket.addLocalVariable(String.format("A%d", i+1), type, initValues[i]);
            i++;
        }
    }


    private static final PrimitiveIterator.OfInt iterator =
            new Random(215).ints('a', 'z'+10).iterator();

    private static String getRandomString(int count) {
        StringBuilder s = new StringBuilder();
        for (int i=0; i < count; i++) {
            int r = iterator.nextInt();
            if (i == 0 && r > 'z') r -= 10;     // The first char must be a character, not a digit.
            char c = (char) (r > 'z' ? r-'z'+'0' : r);
            s.append(c);
        }
        return s.toString();
    }


    /**
     * Delete all the LogixNGs, ConditionalNGs, and so on.
     */
    public static void cleanup() {

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

        java.util.Set<GlobalVariable> globalVariableSet = new java.util.HashSet<>(InstanceManager.getDefault(GlobalVariableManager.class).getNamedBeanSet());
        for (GlobalVariable globalVariable : globalVariableSet) {
            InstanceManager.getDefault(GlobalVariableManager.class).deleteGlobalVariable(globalVariable);
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
        Assert.assertEquals(0, InstanceManager.getDefault(GlobalVariableManager.class).getNamedBeanSet().size());
        Assert.assertEquals(0, logixNG_InitializationManager.getList().size());
    }


    public static void setUp() {
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

        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager sm = new SlotManager(lnis);
        _locoNetMemo = new LocoNetSystemConnectionMemo(lnis, sm);
        sm.setSystemConnectionMemo(_locoNetMemo);
        InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, _locoNetMemo);

        _mqttMemo = new MqttSystemConnectionMemo();
        InstanceManager.setDefault(MqttSystemConnectionMemo.class, _mqttMemo);

//        JUnitUtil.initLogixNGManager();

        setupHasBeenCalled = true;
    }

    public static void tearDown() {
        setupHasBeenCalled = false;     // Reset for the next test

        _locoNetMemo = null;
        _mqttMemo = null;

//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();

        // Delete all the LogixNGs, ConditionalNGs, and so on.
        cleanup();

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateLogixNGTreeScaffold.class);

}
