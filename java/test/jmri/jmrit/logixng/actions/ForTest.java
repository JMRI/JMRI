package jmri.jmrit.logixng.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.DigitalFormula;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test TableForEach
 * 
 * @author Daniel Bergqvist 2019
 */
public class ForTest extends AbstractDigitalActionTestBase {

    LogixNG _logixNG;
    ConditionalNG _conditionalNG;
    For _for;
    MaleSocket _maleSocket;
    
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
                "For (Init; While; AfterEach) do Do ::: Use default%n" +
                "   ! Init%n" +
                "      Many ::: Use default%n" +
                "         ! A1%n" +
                "            Socket not connected%n" +
                "   ? While%n" +
                "      Socket not connected%n" +
                "   ! AfterEach%n" +
                "      Socket not connected%n" +
                "   ! Do%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         For (Init; While; AfterEach) do Do ::: Use default%n" +
                "            ! Init%n" +
                "               Many ::: Use default%n" +
                "                  ! A1%n" +
                "                     Socket not connected%n" +
                "            ? While%n" +
                "               Socket not connected%n" +
                "            ! AfterEach%n" +
                "               Socket not connected%n" +
                "            ! Do%n" +
                "               Socket not connected%n");
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
/* DISABLE FOR NOW    
    @Test
    public void testCtorAndSetup1() {
        TableForEach action = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName("IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load digital action IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        TableForEach action = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName(null);
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup3() {
        DigitalActionManager m1 = InstanceManager.getDefault(DigitalActionManager.class);
        
        MaleSocket childSocket0 = m1.registerAction(new ActionMemory("IQDA554", null));
        
        TableForEach action = new TableForEach("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 1 female socket", 1, action.getChildCount());
        action.getChild(0).setName("ZH12");
        action.setTimerActionSocketSystemName("IQDA554");
        
        Assert.assertEquals("action female socket name is ZH12",
                "ZH12", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
        Assert.assertFalse("action female socket is not connected",
                action.getChild(0).isConnected());
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        Assert.assertTrue("action female socket is connected",
                action.getChild(0).isConnected());
        Assert.assertEquals("child is correct bean",
                childSocket0,
                action.getChild(0).getConnectedSocket());
        
        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        action.setup();
        
        Assert.assertEquals("action has 1 female sockets", 1, action.getChildCount());
    }
*/    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 4", 4 == _for.getChildCount());
        
        Assert.assertNotNull("getChild(0) returns a non null value",
                _for.getChild(0));
        
        boolean hasThrown = false;
        try {
            _for.getChild(4);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 4", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testDescription() {
        TableForEach a1 = new TableForEach("IQDA321", null);
        Assert.assertEquals("strings are equal", "Table: For each", a1.getShortDescription());
        TableForEach a2 = new TableForEach("IQDA321", null);
        Assert.assertEquals("strings are equal", "Table: For each column of row \"\" in table \"''\" set variable \"\" and execute action A1", a2.getLongDescription());
    }
    
    @Test
    public void testExecute()
            throws IOException, SocketAlreadyConnectedException, ParserException {
        
        Memory result = InstanceManager.getDefault(MemoryManager.class).provide("IM_RESULT");
//        Memory debug = InstanceManager.getDefault(MemoryManager.class).provide("IM_DEBUG");
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        
        _maleSocket.addLocalVariable("MyVariable", SymbolTable.InitialValueType.None, null);
        
        _logixNG.setEnabled(false);
        
        _for.getChild(0).disconnect();
        
        // Calculate the first 10 fibonacci numbers
        _maleSocket.addLocalVariable("n", SymbolTable.InitialValueType.None, null);         // n
        _maleSocket.addLocalVariable("fn_2", SymbolTable.InitialValueType.None, null);      // f(n-2)
        _maleSocket.addLocalVariable("fn_1", SymbolTable.InitialValueType.None, null);      // f(n-1)
        _maleSocket.addLocalVariable("fn", SymbolTable.InitialValueType.None, null);        // f(n)
        _maleSocket.addLocalVariable("N", SymbolTable.InitialValueType.Formula, "10");      // N
        _maleSocket.addLocalVariable("result", SymbolTable.InitialValueType.None, null);    // result
        
//        _maleSocket.addLocalVariable("debug", SymbolTable.InitialValueType.None, null);
        
        MaleSocket m1 = digitalActionManager.registerAction(
                new DigitalMany(digitalActionManager.getAutoSystemName(), null));
        
        ActionLocalVariable lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("n");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("2");
        m1.getChild(0).connect(digitalActionManager.registerAction(lv));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("fn_2");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("0");
        m1.getChild(1).connect(digitalActionManager.registerAction(lv));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("fn_1");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("1");
        m1.getChild(2).connect(digitalActionManager.registerAction(lv));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("result");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.SetToString);
        lv.setConstantValue("0, 1");
        m1.getChild(3).connect(digitalActionManager.registerAction(lv));
/*        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setVariable("debug");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.SetToString);
        lv.setData("");
        m1.getChild(4).connect(digitalActionManager.registerAction(lv));
*/        
        _for.getChild(0).connect(m1);
        
        
        DigitalFormula formula = new DigitalFormula(digitalExpressionManager.getAutoSystemName(), null);
        formula.setFormula("n < N");
        _for.getChild(1).connect(digitalExpressionManager.registerExpression(formula));
        
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("n");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("n + 1");
        _for.getChild(2).connect(digitalActionManager.registerAction(lv));
        
        
        m1 = digitalActionManager.registerAction(
                new DigitalMany(digitalActionManager.getAutoSystemName(), null));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("fn");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("fn_1 + fn_2");
        m1.getChild(0).connect(digitalActionManager.registerAction(lv));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("fn_2");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("fn_1");
        m1.getChild(1).connect(digitalActionManager.registerAction(lv));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("fn_1");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("fn");
        m1.getChild(2).connect(digitalActionManager.registerAction(lv));
        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setLocalVariable("result");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("result + \", \" + str(fn)");
        m1.getChild(3).connect(digitalActionManager.registerAction(lv));
        
        ActionMemory lm = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        lm.setMemory("IM_RESULT");
        lm.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        lm.setOtherLocalVariable("result");
        m1.getChild(4).connect(digitalActionManager.registerAction(lm));
/*        
        lv = new ActionLocalVariable(digitalActionManager.getAutoSystemName(), null);
        lv.setVariable("debug");
        lv.setVariableOperation(ActionLocalVariable.VariableOperation.CalculateFormula);
        lv.setFormula("debug + format(\"N: %d, n: %d, fn_2: %d, fn_1: %d, fn: %d, result: %s%n\", N, n, fn_2, fn_1, fn, result)");
        m1.getChild(5).connect(digitalActionManager.registerAction(lv));
        
        lm = new ActionMemory(digitalActionManager.getAutoSystemName(), null);
        lm.setMemory("IM_DEBUG");
        lm.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        lm.setData("debug");
        m1.getChild(6).connect(digitalActionManager.registerAction(lm));
*/        
        _for.getChild(3).connect(m1);
        
        
        _logixNG.setEnabled(true);
        
//        SymbolTable symbolTable =
//                InstanceManager.getDefault(LogixNG_Manager.class).getSymbolTable();
        
//        System.out.format("N: %s%n", symbolTable.getValue("N"));
//        System.out.format("n: %s%n", symbolTable.getValue("n"));
//        System.out.format("fn_1: %s%n", symbolTable.getValue("fn_1"));
//        System.out.format("fn: %s%n", symbolTable.getValue("fn"));
//        System.out.format("result: %s%n", symbolTable.getValue("result"));
//        System.out.format("result: %s%n", result.getValue());
        
//        System.out.format("%n%n%ndebug: %s%n", debug.getValue());
        
        // The memory "result" should have a list of the first 10 fibonacci numbers
        Assert.assertEquals("0, 1, 1, 2, 3, 5, 8, 13, 21, 34",
                result.getValue());
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
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.OTHER;
        _isExternal = false;
        
        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _conditionalNG.setEnabled(true);
        _conditionalNG.setRunDelayed(false);
        _logixNG.addConditionalNG(_conditionalNG);
        _for = new For("IQDA321", null);
        _maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(_for);
        _conditionalNG.getChild(0).connect(_maleSocket);
        _base = _for;
        _baseMaleSocket = _maleSocket;
        
        _for.getChild(0).connect(InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(new DigitalMany(
                        InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null)));
        
        if (! _logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        _logixNG.setEnabled(false);
    }

    @After
    public void tearDown() {
        _logixNG.setEnabled(false);
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
        _category = null;
        _logixNG = null;
        _conditionalNG = null;
        _for = null;
        _base = null;
        _baseMaleSocket = null;
        _maleSocket = null;
    }
    
}
