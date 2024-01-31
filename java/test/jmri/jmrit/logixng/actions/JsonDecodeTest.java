package jmri.jmrit.logixng.actions;

import java.io.IOException;

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
 * Test JsonDecode
 *
 * @author Daniel Bergqvist 2024
 */
public class JsonDecodeTest extends AbstractDigitalActionTestBase {

    private static final String JSON_STRING = String.format(
            "{%n" +
            "  \"id\":\"tk\",%n" +
            "  \"config\":{%n" +
            "    \"signature\":\"TK\",%n" +
            "    \"name\":\"Tr\\u00e4kvista\",%n" +
            "    \"destinations\":2,%n" +
            "    \"destination\":{%n" +
            "      \"A\":{%n" +
            "        \"tracks\":1,%n" +
            "        \"type\":\"single\",%n" +
            "        \"single\":{%n" +
            "          \"id\":\"blo\",%n" +
            "          \"tracks\":1,%n" +
            "          \"exit\":\"B\",%n" +
            "          \"track\":\"left\",%n" +
            "          \"distance\":1,%n" +
            "          \"list\":[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\"],%n" +
            "          \"signature\":\"BLO\",%n" +
            "          \"name\":\"Bilbo\",%n" +
            "          \"signalin\":\"22\",%n" +
            "          \"signalout\":\"U1\",%n" +
            "          \"blockout\":\"sa1\"%n" +
            "        }" +
            "      }%n" +
            "    },%n" +
            "    \"B\":[\"Something\"],%n" +
            "    \"C\":[1,2,3]%n" +
            "  }%n" +
            "}%n");

    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;
    private JsonDecode _jsonDecode;
    private MaleSocket _maleSocket;
    private Memory _memoryResult;
    private Memory _memoryResult2;

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
                "Decode JSON in variable MyJsonVariable to variable MyResultVariable ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Many ::: Use default%n" +
                "            ::: Local variable \"MyJsonVariable\", init to String \"" + JSON_STRING + "\"%n" +
                "            ::: Local variable \"MyResultVariable\", init to None \"null\"%n" +
                "            ! A1%n" +
                "               Decode JSON in variable MyJsonVariable to variable MyResultVariable ::: Use default%n" +
                "            ! A2%n" +
                "               Set memory IM_RESULT to the value of variable MyResultVariable ::: Use default%n" +
                "            ! A3%n" +
                "               Digital Formula: MyResultVariable = str( MyResultVariable{\"config\"}{\"destination\"}{\"A\"}{\"single\"}{\"list\"}[3] ) ::: Use default%n" +
                "                  ?* E1%n" +
                "                     Socket not connected%n" +
                "            ! A4%n" +
                "               Set memory IM_RESULT_2 to the value of variable MyResultVariable ::: Use default%n" +
                "            ! A5%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new JsonDecode(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        JsonDecode t = new JsonDecode("IQDA321", null);
        Assert.assertNotNull("exists",t);
        t = new JsonDecode("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == _jsonDecode.getChildCount());

        boolean hasThrown = false;
        try {
            _jsonDecode.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }

    @Test
    public void testDescription() {
        JsonDecode a1 = new JsonDecode("IQDA321", null);
        Assert.assertEquals("strings are equal", "Decode JSON", a1.getShortDescription());
        JsonDecode a2 = new JsonDecode("IQDA321", null);
        Assert.assertEquals("strings are equal", "Decode JSON in variable null to variable null", a2.getLongDescription());
    }

    @Test
    public void testExecute()
            throws IOException, SocketAlreadyConnectedException, ParserException {

        // Execute the LogixNG
        _logixNG.setEnabled(true);

        Assert.assertEquals("com.fasterxml.jackson.databind.node.ObjectNode",
                _memoryResult.getValue().getClass().getName());
        Assert.assertEquals("{\"id\":\"tk\",\"config\":{\"signature\":\"TK\",\"name\":\"Träkvista\",\"destinations\":2,\"destination\":{\"A\":{\"tracks\":1,\"type\":\"single\",\"single\":{\"id\":\"blo\",\"tracks\":1,\"exit\":\"B\",\"track\":\"left\",\"distance\":1,\"list\":[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\"],\"signature\":\"BLO\",\"name\":\"Bilbo\",\"signalin\":\"22\",\"signalout\":\"U1\",\"blockout\":\"sa1\"}}},\"B\":[\"Something\"],\"C\":[1,2,3]}}",
                _memoryResult.getValue().toString());

        Assert.assertEquals("java.lang.String",
               _memoryResult2.getValue().getClass().getName());
        Assert.assertEquals("d",
                _memoryResult2.getValue().toString());
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

        _memoryResult = InstanceManager.getDefault(MemoryManager.class).provide("IM_RESULT");

        _memoryResult2 = InstanceManager.getDefault(MemoryManager.class).provide("IM_RESULT_2");

        _category = Category.OTHER;

        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _conditionalNG.setEnabled(true);
        _conditionalNG.setRunDelayed(false);
        _logixNG.addConditionalNG(_conditionalNG);

        DigitalMany many = new DigitalMany("IQDA101", null);
        MaleSocket m = InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        m.addLocalVariable("MyJsonVariable", SymbolTable.InitialValueType.String, JSON_STRING);
        m.addLocalVariable("MyResultVariable", SymbolTable.InitialValueType.None, null);
        _conditionalNG.getChild(0).connect(m);

        _jsonDecode = new JsonDecode("IQDA321", null);
        _jsonDecode.setJsonLocalVariable("MyJsonVariable");
        _jsonDecode.setResultLocalVariable("MyResultVariable");
        _maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_jsonDecode);
        many.getChild(0).connect(_maleSocket);
        _base = _jsonDecode;
        _baseMaleSocket = _maleSocket;

        ActionMemory memory = new ActionMemory("IQDA102", null);
        memory.getSelectNamedBean().setNamedBean("IM_RESULT");
        memory.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        memory.setOtherLocalVariable("MyResultVariable");
        many.getChild(1).connect(InstanceManager.getDefault(DigitalActionManager.class).registerAction(memory));

        DigitalFormula formula = new DigitalFormula("IQDA103", null);
        formula.setFormula("MyResultVariable = str( MyResultVariable{\"config\"}{\"destination\"}{\"A\"}{\"single\"}{\"list\"}[3] )");
        many.getChild(2).connect(InstanceManager.getDefault(DigitalActionManager.class).registerAction(formula));

        memory = new ActionMemory("IQDA104", null);
        memory.getSelectNamedBean().setNamedBean("IM_RESULT_2");
        memory.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        memory.setOtherLocalVariable("MyResultVariable");
        many.getChild(3).connect(InstanceManager.getDefault(DigitalActionManager.class).registerAction(memory));

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
        _logixNG = null;
        _conditionalNG = null;
        _jsonDecode = null;
        _base = null;
        _baseMaleSocket = null;
        _maleSocket = null;
        _memoryResult = null;
    }

}
