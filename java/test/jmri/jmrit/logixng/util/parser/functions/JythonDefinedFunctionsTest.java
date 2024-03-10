package jmri.jmrit.logixng.util.parser.functions;

import jmri.jmrit.logixng.actions.*;

import java.io.IOException;
import java.util.*;

import javax.script.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.*;
import jmri.script.ScriptEngineSelector;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG functions defined in Jython.
 *
 * This class tests that it's possible to define a LogixNG function in Jython
 * and use that function in a LogixNG formula.
 *
 * @author Daniel Bergqvist 2024
 */
public class JythonDefinedFunctionsTest extends AbstractDigitalActionTestBase {

    private final ScriptEngineSelector _scriptEngineSelector = new ScriptEngineSelector();

    private static final String JYTHON_FUNCTION = String.format(
            "import jmri%n" +
            "%n" +
            "class TestJythonDefinedFunction(jmri.jmrit.logixng.util.parser.Function):%n" +
            "  def getModule(self):%n" +
            "    return 'TestJythonModule'%n" +
            "%n" +
            "  def getName(self):%n" +
            "    return 'testJythonDefinedFunction'%n" +
            "%n" +
            "  def getDescription(self):%n" +
            "    return 'Test function defined in Jython.'%n" +
            "%n" +
            "  def getConstantDescriptions(self):%n" +
            "    return \"This module does not define any constants.\"%n" +
            "%n" +
            "  def calculate(self, symbolTable, parameterList):%n" +
            "    if (parameterList.size() != 2):%n" +
            "      raise jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException(\"Function requires two parameter\")%n" +
            "%n" +
            "    param1 = parameterList.get(0).calculate(symbolTable)%n" +
            "    param2 = parameterList.get(1).calculate(symbolTable)%n" +
            "    result = param1 * param2%n" +
            "    return result%n" +
            "%n" +
            "jmri.InstanceManager.getDefault(jmri.jmrit.logixng.util.parser.FunctionManager).put(\"testJythonDefinedFunction\", TestJythonDefinedFunction())");


    Memory _memory1, _memory2;
    Memory _memoryResult;
    LogixNG _logixNG;
    ConditionalNG _conditionalNG;
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
                "Digital Formula: writeMemory(\"IMResult\", readMemory(\"IM1\")) ::: Use default%n" +
                "   ?* E1%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Digital Formula: writeMemory(\"IMResult\", readMemory(\"IM1\")) ::: Use default%n" +
                "            ?* E1%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new DigitalFormula(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        DigitalFormula t = new DigitalFormula("IQDA321", null);
        Assert.assertNotNull("exists",t);
        t = new DigitalFormula("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 1", 1 == _formula.getChildCount());

        Assert.assertNotNull("getChild(0) returns a non null value",
                _formula.getChild(0));

        boolean hasThrown = false;
        try {
            _formula.getChild(1);
        } catch (IndexOutOfBoundsException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Index 1 out of bounds for length 1", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testCategory() {
        Assert.assertEquals("Category matches", Category.COMMON, _base.getCategory());
    }

    @Test
    public void testFunctionConstants() {
        FunctionManager fm = InstanceManager.getDefault(FunctionManager.class);

        Function f = fm.get("testJythonDefinedFunction");
        Assert.assertEquals("This module does not define any constants.", f.getConstantDescriptions());
    }

    @Test
    public void testExecute()
            throws IOException, SocketAlreadyConnectedException, ParserException {

        _memory1.setValue(2);
        _memory2.setValue(3);
        _memoryResult.setValue(null);
        _formula.setFormula("writeMemory(\"IMResult\", testJythonDefinedFunction(readMemory(\"IM1\"),readMemory(\"IM2\")))");
        _logixNG.execute();
        Assert.assertEquals(6, (int)_memoryResult.getValue());
        Assert.assertEquals(
                "Digital Formula: writeMemory(\"IMResult\", testJythonDefinedFunction(readMemory(\"IM1\"),readMemory(\"IM2\")))",
                _formula.getLongDescription());

        _memory1.setValue(-7);
        _memory2.setValue(11);
        _memoryResult.setValue(null);
        _formula.setFormula("writeMemory(\"IMResult\", testJythonDefinedFunction(readMemory(\"IM1\"),readMemory(\"IM2\")))");
        _logixNG.execute();
        Assert.assertEquals(-77, (int)_memoryResult.getValue());
        Assert.assertEquals(
                "Digital Formula: writeMemory(\"IMResult\", testJythonDefinedFunction(readMemory(\"IM1\"),readMemory(\"IM2\")))",
                _formula.getLongDescription());

        _memory1.setValue(-5);
        _memory2.setValue(-4);
        _memoryResult.setValue(null);
        _formula.setFormula("writeMemory(\"IMResult\", testJythonDefinedFunction(readMemory(\"IM1\"),readMemory(\"IM2\")))");
        _logixNG.execute();
        Assert.assertEquals(20, (int)_memoryResult.getValue());
        Assert.assertEquals(
                "Digital Formula: writeMemory(\"IMResult\", testJythonDefinedFunction(readMemory(\"IM1\"),readMemory(\"IM2\")))",
                _formula.getLongDescription());
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
    public void setUp() throws SocketAlreadyConnectedException, ParserException, ScriptException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _category = Category.OTHER;
        _isExternal = false;

        _memory1 = InstanceManager.getDefault(MemoryManager.class).provideMemory("IM1");  // NOI18N
        _memory1.setValue(null);

        _memory2 = InstanceManager.getDefault(MemoryManager.class).provideMemory("IM2");  // NOI18N
        _memory2.setValue(null);

        _memoryResult = InstanceManager.getDefault(MemoryManager.class).provideMemory("IMResult");  // NOI18N
        _memoryResult.setValue("");

        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _conditionalNG.setEnabled(true);
        _conditionalNG.setRunDelayed(false);
        _logixNG.addConditionalNG(_conditionalNG);

        _formula = new DigitalFormula(
                InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        _formula.setFormula("writeMemory(\"IMResult\", readMemory(\"IM1\"))");
        _maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_formula);
        _conditionalNG.getChild(0).connect(_maleSocket);
        _base = _formula;
        _baseMaleSocket = _maleSocket;

        if (! _logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();

        Bindings bindings = new SimpleBindings();
        ScriptEngineSelector.Engine engine = _scriptEngineSelector.getSelectedEngine();
        if (engine == null) throw new JmriException("Script engine is null");
        engine.getScriptEngine().eval(JYTHON_FUNCTION, bindings);

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
        _memory1 = null;
        _memory2 = null;
        _memoryResult = null;
        _logixNG = null;
        _conditionalNG = null;
        _formula = null;
        _base = null;
        _baseMaleSocket = null;
        _maleSocket = null;
    }

}
