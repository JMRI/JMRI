package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ForEach
 *
 * @author Daniel Bergqvist 2024
 */
public class ForEachTest extends AbstractDigitalActionTestBase {

    private Memory _memory;
    private Memory _memoryResult;
    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;
    private ForEach _forEach;
    private MaleSocket _maleSocket;
    private DigitalFormula _formula;

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
        assertNotNull( t, "exists");
        t = new TableForEach("IQDA321", null);
        assertNotNull( t, "exists");
    }

    @Test
    public void testGetChild() {
        assertEquals( 1, _forEach.getChildCount(), "getChildCount() returns 1");

        assertNotNull( _forEach.getChild(0), "getChild(0) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            _forEach.getChild(1), "Exception is thrown");
        assertEquals( "index has invalid value: 1", ex.getMessage(),
                "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.FLOW_CONTROL, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        ForEach a1 = new ForEach("IQDA321", null);
        assertEquals( "For each", a1.getShortDescription(), "strings are equal");
        ForEach a2 = new ForEach("IQDA321", null);
        assertEquals( "For each value, set variable \"\" and execute action A. Values from Sensors",
                a2.getLongDescription(), "strings are equal");
    }

    @Test
    public void testExecute()
            throws IOException, SocketAlreadyConnectedException, ParserException {

        _memory.setValue(new ArrayList<String>());
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _logixNG.execute();
        assertEquals("", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1",
                _forEach.getLongDescription());

        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        _memory.setValue(list);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _logixNG.execute();
        assertEquals("A, B, C, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1",
                _forEach.getLongDescription());

        Map<String, Integer> map = new HashMap<>();
        map.put("A", 10);
        map.put("B", 20);
        map.put("C", -3);
        _memory.setValue(map);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getKey() + \":\" + str(item.getValue()) + \", \")");
        _logixNG.execute();
        assertEquals("A:10, B:20, C:-3, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1",
                _forEach.getLongDescription());

        map = new HashMap<>();
        map.put("A", 8);
        map.put("B", -55);
        map.put("C", 32);
        _memory.setValue(map);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + \"toString:>\" + item.toString() + \"<\" + \", \")");
        _logixNG.execute();
        assertEquals("toString:>A=8<, toString:>B=-55<, toString:>C=32<, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1",
                _forEach.getLongDescription());

        map = new HashMap<>();
        map.put("A", 8);
        map.put("B", -55);
        map.put("C", 32);
        _memory.setValue(map);
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + \"getClass().getName():>\" + item.getClass().getName() + \"<\" + \", \")");
        _logixNG.execute();
        assertEquals("getClass().getName():>java.util.HashMap$Node<, getClass().getName():>java.util.HashMap$Node<, getClass().getName():>java.util.HashMap$Node<, ",
                _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1",
                _forEach.getLongDescription());

        _memory.setValue(new String[]{"A", "B", "C"});
        _memoryResult.setValue("");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item + \", \")");
        _logixNG.execute();
        assertEquals("A, B, C, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from memory IM1",
                _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Sensors);
        InstanceManager.getDefault(SensorManager.class).provideSensor("ISSomething");
        InstanceManager.getDefault(SensorManager.class).provideSensor("ISSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        assertEquals("ISCLOCKRUNNING, ISSomething, ISSomethingElse, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from Sensors",
                _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Turnouts);
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITSomething");
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        assertEquals("ITSomething, ITSomethingElse, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from Turnouts",
                _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Lights);
        InstanceManager.getDefault(LightManager.class).provideLight("ILSomething");
        InstanceManager.getDefault(LightManager.class).provideLight("ILSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        assertEquals("ILSomething, ILSomethingElse, ", _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from Lights",
                _forEach.getLongDescription());

        _memoryResult.setValue("");
        _forEach.setUseCommonSource(true);
        _forEach.setCommonManager(CommonManager.Memories);
        InstanceManager.getDefault(MemoryManager.class).provideMemory("IMSomething");
        InstanceManager.getDefault(MemoryManager.class).provideMemory("IMSomethingElse");
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IMResult\") + item.getSystemName() + \", \")");
        _logixNG.execute();
        assertEquals("IM1, IMCURRENTTIME, IMRATEFACTOR, IMResult, IMSomething, IMSomethingElse, ",
                _memoryResult.getValue());
        assertEquals("For each value, set variable \"item\" and execute action A. Values from Memories",
                _forEach.getLongDescription());
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

    @Before
    @BeforeEach
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

        assertTrue( _logixNG.setParentForAllChildren(new ArrayList<>()));
        _logixNG.activate();
        _logixNG.setEnabled(false);
    }

    @After
    @AfterEach
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
