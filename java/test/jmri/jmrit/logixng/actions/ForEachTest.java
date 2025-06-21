package jmri.jmrit.logixng.actions;

import java.io.IOException;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ForEach
 *
 * @author Daniel Bergqvist 2024
 */
public class ForEachTest extends AbstractDigitalActionTestBase {

    Memory _memory;
    Memory _memoryResult;
    LogixNG _logixNG;
    ConditionalNG _conditionalNG;
    ForEach _forEach;
    MaleSocket _maleSocket;
    DigitalFormula _formula;

    @Override
    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return _logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        DigitalMany action = new DigitalMany("IQDA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        return maleSocket;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "For each value, set variable \"item\" and execute action A. Values from memory IM1 ::: Use default%n" +
                "   ::: Local variable \"item\", init to None \"null\"%n" +
                "   ! A%n" +
                "      Digital Formula: writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \") ::: Use default%n" +
                "         ?* E1%n" +
                "            Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         For each value, set variable \"item\" and execute action A. Values from memory IM1 ::: Use default%n" +
                "            ::: Local variable \"item\", init to None \"null\"%n" +
                "            ! A%n" +
                "               Digital Formula: writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \") ::: Use default%n" +
                "                  ?* E1%n" +
                "                     Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new TableForEach(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        TableForEach t = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists",t);
        t = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == _forEach.getChildCount());

        Assert.assertNotNull("getChild(0) returns a non null value",
                _forEach.getChild(0));

        boolean hasThrown = false;
        try {
            _forEach.getChild(1);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.FLOW_CONTROL == _base.getCategory());
    }

    @Test
    public void testDescription() {
        ForEach a1 = new ForEach("IQDA321", null);
        Assert.assertEquals("strings are equal", "For each", a1.getShortDescription());
        ForEach a2 = new ForEach("IQDA321", null);
        Assert.assertEquals("strings are equal", "For each value, set variable \"\" and execute action A. Values from Sensors", a2.getLongDescription());
    }

    @Test
    public void testExecute()
            throws IOException, SocketAlreadyConnectedException, ParserException {

        _memory.setValue(new ArrayList<String>());
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _logixNG.execute();
        Assert.assertEquals("", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1", _forEach.getLongDescription());

        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        _memory.setValue(list);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _logixNG.execute();
        Assert.assertEquals("A, B, C, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1", _forEach.getLongDescription());

        Map<String, Integer> map = new HashMap<>();
        map.put("A", 10);
        map.put("B", 20);
        map.put("C", -3);
        _memory.setValue(map);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getKey() + \":\" + str(item.getValue()) + \", \")");
        _logixNG.execute();
        Assert.assertEquals("A:10, B:20, C:-3, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1", _forEach.getLongDescription());

        map = new HashMap<>();
        map.put("A", 8);
        map.put("B", -55);
        map.put("C", 32);
        _memory.setValue(map);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + \"toString:>\" + item.toString() + \"<\" + \", \")");
        _logixNG.execute();
        Assert.assertEquals("toString:>A=8<, toString:>B=-55<, toString:>C=32<, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1", _forEach.getLongDescription());

        map = new HashMap<>();
        map.put("A", 8);
        map.put("B", -55);
        map.put("C", 32);
        _memory.setValue(map);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + \"getClass().getName():>\" + item.getClass().getName() + \"<\" + \", \")");
        _logixNG.execute();
        Assert.assertEquals("getClass().getName():>java.util.HashMap$Node<, getClass().getName():>java.util.HashMap$Node<, getClass().getName():>java.util.HashMap$Node<, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1", _forEach.getLongDescription());

        _memory.setValue(new String[]{"A", "B", "C"});
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _logixNG.execute();
        Assert.assertEquals("A, B, C, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1", _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Sensors);
        InstanceManager.getDefault(SensorManager.class).provideSensor("ISSomething");
        InstanceManager.getDefault(SensorManager.class).provideSensor("ISSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        Assert.assertEquals("ISCLOCKRUNNING, ISSomething, ISSomethingElse, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from Sensors", _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Turnouts);
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITSomething");
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        Assert.assertEquals("ITSomething, ITSomethingElse, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from Turnouts", _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Lights);
        InstanceManager.getDefault(LightManager.class).provideLight("ILSomething");
        InstanceManager.getDefault(LightManager.class).provideLight("ILSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        Assert.assertEquals("ILSomething, ILSomethingElse, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from Lights", _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Memories);
        InstanceManager.getDefault(MemoryManager.class).provideMemory("IMSomething");
        InstanceManager.getDefault(MemoryManager.class).provideMemory("IMSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        Assert.assertEquals("IM1, IMCURRENTTIME, IMRATEFACTOR, IMResult, IMSomething, IMSomethingElse, ", _memoryResult.getValue());
        Assert.assertEquals("For each value, set variable \"item\" and execute action A. Values from Memories", _forEach.getLongDescription());
    }

    @Test
    @Override
    public void testIsActive() {
        _logixNG.setEnabled(true);
        super.testIsActive();
    }

    @Test
    @Override
    public void testMaleSocketIsActive() {
        _logixNG.setEnabled(true);
        super.testMaleSocketIsActive();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, ParserException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.OTHER;
        _isExternal = false;

        _memory = InstanceManager.getDefault(MemoryManager.class).provideMemory("IM1");  // NOI18N
        _memory.setValue(new ArrayList<String>());

        _memoryResult = InstanceManager.getDefault(MemoryManager.class).provideMemory("IMResult");  // NOI18N
        _memoryResult.setValue("");

        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _conditionalNG.setEnabled(true);
        _conditionalNG.setRunDelayed(false);
        _logixNG.addConditionalNG(_conditionalNG);
        _forEach = new ForEach("IQDA321", null);
        _forEach.setUserSpecifiedSource(ForEach.UserSpecifiedSource.Memory);
        _forEach.setUseCommonSource(false);
        _forEach.getSelectMemoryNamedBean().setNamedBean(_memory);
        _forEach.setLocalVariableName("item");
        _maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_forEach);
        _maleSocket.addLocalVariable("item", SymbolTable.InitialValueType.None, null);
        _conditionalNG.getChild(0).connect(_maleSocket);
        _base = _forEach;
        _baseMaleSocket = _maleSocket;

        _formula = new DigitalFormula(
                InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _forEach.getChild(0).connect(InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(_formula));

        if (! _logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        _logixNG.activate();
        _logixNG.setEnabled(false);
    }

    @After
    public void tearDown() {
        _logixNG.setEnabled(false);
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
        _category = null;
        _memory = null;
        _memoryResult = null;
        _logixNG = null;
        _conditionalNG = null;
        _forEach = null;
        _base = null;
        _baseMaleSocket = null;
        _maleSocket = null;
    }

}
