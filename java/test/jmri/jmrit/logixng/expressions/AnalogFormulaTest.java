package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.actions.DoAnalogAction;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test And
 * 
 * @author Daniel Bergqvist 2018
 */
public class AnalogFormulaTest extends AbstractAnalogExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AnalogFormula expressionFormula;
    private AnalogActionMemory analogActionMemory;
    private Memory analogMemory;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    private static int beanID = 901;
    
    @Override
    public MaleSocket getConnectableChild() {
        AnalogExpressionBean childExpression = new AnalogExpressionConstant("IQAE"+Integer.toString(beanID++), null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Analog Formula: E1 ::: Log error%n" +
                "   ?* E1%n" +
                "      Get analog constant 1 ::: Log error%n" +
                "   ?* E2%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Log error%n" +
                "            ?~ E%n" +
                "               Analog Formula: E1 ::: Log error%n" +
                "                  ?* E1%n" +
                "                     Get analog constant 1 ::: Log error%n" +
                "                  ?* E2%n" +
                "                     Socket not connected%n" +
                "            !~ A%n" +
                "               Set memory IM2 ::: Log error%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) throws Exception {
        AnalogFormula a = new AnalogFormula(systemName, null);
//        a.setFormula("R1");
        return a;
    }
    
    @Override
    public boolean addNewSocket() throws SocketAlreadyConnectedException {
        int count = _base.getChildCount();
        for (int i=0; i < count; i++) {
            if (!_base.getChild(i).isConnected()) {
                _base.getChild(i).connect(getConnectableChild());
            }
        }
        return true;
    }
    
    @Test
    public void testCtor() throws Exception {
        AnalogFormula expression2;
        
        expression2 = new AnalogFormula("IQAE321", null);
//        expression2.setFormula("R1");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1", expression2.getLongDescription());
        
        expression2 = new AnalogFormula("IQAE321", "My expression");
//        expression2.setFormula("R1");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1", expression2.getLongDescription());
        
        expression2 = new AnalogFormula("IQAE321", null);
//        expression2.setFormula("R1 and R2");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1 and R2", expression2.getLongDescription());
        
        expression2 = new AnalogFormula("IQAE321", "My expression");
//        expression2.setFormula("R1 or R2");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
//        Assert.assertEquals("String matches", "Formula: R1 or R2", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new AnalogFormula("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new AnalogFormula("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        AnalogExpressionManager m = InstanceManager.getDefault(AnalogExpressionManager.class);
        String managerName = m.getClass().getName();
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE52", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE554", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE3", null)));
        
        List<AnalogFormula.SocketData> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new AnalogFormula.SocketData("XYZ123", "IQAE52", managerName));
        actionSystemNames.add(new AnalogFormula.SocketData("ZH12", null, managerName));   // This is null by purpose
        actionSystemNames.add(new AnalogFormula.SocketData("Hello", "IQAE554", managerName));
        // IQAE61232 doesn't exist by purpose
        actionSystemNames.add(new AnalogFormula.SocketData("SomethingElse", "IQAE61232", managerName));
        actionSystemNames.add(new AnalogFormula.SocketData("Yes123", "IQAE3", managerName));
        
        AnalogFormula expression = new AnalogFormula("IQAE321", null, actionSystemNames);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 5 female sockets", 5, expression.getChildCount());
        
        for (int i=0; i < 5; i++) {
            AnalogFormula.SocketData socketData = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+socketData._socketName,
                    socketData._socketName, expression.getChild(i).getName());
            Assert.assertEquals("expression female socket is of correct class",
                    "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$GenericSocket",
                    expression.getChild(i).getClass().getName());
            Assert.assertFalse("expression female socket is not connected",
                    expression.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load analog expression IQAE61232");
        
        for (int i=0; i < 5; i++) {
            AnalogFormula.SocketData socketName = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+socketName._socketName,
                    socketName._socketName, expression.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("expression female socket is connected",
                        expression.getChild(i).isConnected());
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("expression female socket is not connected",
                        expression.getChild(i).isConnected());
            }
        }
        
        Assert.assertEquals("expression has 5 female sockets", 5, expression.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        AnalogExpressionManager m = InstanceManager.getDefault(AnalogExpressionManager.class);
        String managerName = m.getClass().getName();
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE52", null)));
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE99", null)));
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE554", null)));
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE61232", null)));
        maleSockets.add(m.registerExpression(new AnalogExpressionMemory("IQAE3", null)));
        
        List<AnalogFormula.SocketData> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new AnalogFormula.SocketData("XYZ123", "IQAE52", managerName));
        actionSystemNames.add(new AnalogFormula.SocketData("ZH12", "IQAE99", managerName));
        actionSystemNames.add(new AnalogFormula.SocketData("Hello", "IQAE554", managerName));
        actionSystemNames.add(new AnalogFormula.SocketData("SomethingElse", "IQAE61232", managerName));
        actionSystemNames.add(new AnalogFormula.SocketData("Yes123", "IQAE3", managerName));
        
        AnalogFormula expression = new AnalogFormula("IQAE321", null, actionSystemNames);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 5 female sockets", 5, expression.getChildCount());
        
        for (int i=0; i < 5; i++) {
            AnalogFormula.SocketData socketData = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+socketData._socketName,
                    socketData._socketName, expression.getChild(i).getName());
            Assert.assertEquals("expression female socket is of correct class",
                    "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$GenericSocket",
                    expression.getChild(i).getClass().getName());
            Assert.assertFalse("expression female socket is not connected",
                    expression.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        expression.setup();
        
        for (int i=0; i < 5; i++) {
            AnalogFormula.SocketData socketData = actionSystemNames.get(i);
            Assert.assertEquals("expression female socket name is "+socketData._socketName,
                    socketData._socketName, expression.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("expression female socket is connected",
                        expression.getChild(i).isConnected());
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        expression.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("expression female socket is not connected",
                        expression.getChild(i).isConnected());
            }
        }
        
        // Since all the sockets are connected, a new socket must have been created.
        Assert.assertEquals("expression has 6 female sockets", 6, expression.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        expression.setup();
        
        Assert.assertEquals("expression has 6 female sockets", 6, expression.getChildCount());
    }
    
    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        List<AnalogFormula.SocketData> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new AnalogFormula.SocketData("XYZ123", "IQAE52", ""));
        
        AnalogFormula expression = new AnalogFormula("IQAE321", null, actionSystemNames);
        
        java.lang.reflect.Method method =
                expression.getClass().getDeclaredMethod("setExpressionSystemNames", new Class<?>[]{List.class});
        method.setAccessible(true);
        
        boolean hasThrown = false;
        try {
            method.invoke(expression, new Object[]{null});
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                hasThrown = true;
                Assert.assertEquals("Exception message is correct",
                        "expression system names cannot be set more than once",
                        e.getCause().getMessage());
            }
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testSetChildCount() throws SocketAlreadyConnectedException {
        AnalogFormula a = (AnalogFormula)_base;
        AtomicBoolean ab = new AtomicBoolean(false);
        
        _base.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            ab.set(true);
        });
        
        a.setChildCount(1);
        Assert.assertEquals("numChilds are correct", 1, a.getChildCount());
        
        // Test increase num children
        ab.set(false);
        a.setChildCount(a.getChildCount()+1);
        Assert.assertEquals("numChilds are correct", 2, a.getChildCount());
        Assert.assertTrue("PropertyChangeEvent fired", ab.get());
        
        // Test decrease num children
        ab.set(false);
        Assert.assertTrue("We have least two children", a.getChildCount() > 1);
        a.setChildCount(1);
        Assert.assertEquals("numChilds are correct", 1, a.getChildCount());
        Assert.assertTrue("PropertyChangeEvent fired", ab.get());
        
        // Test decrease num children when all children are connected
        ab.set(false);
        a.getChild(0).disconnect();
        a.getChild(0).connect(getConnectableChild());
        a.getChild(1).disconnect();
        a.getChild(1).connect(getConnectableChild());
        a.getChild(2).disconnect();
        a.getChild(2).connect(getConnectableChild());
        Assert.assertEquals("numChilds are correct", 4, a.getChildCount());
        a.setChildCount(2);
        Assert.assertEquals("numChilds are correct", 2, a.getChildCount());
        Assert.assertTrue("PropertyChangeEvent fired", ab.get());
        
        // Test setChildCount to same number of children as before
        ab.set(false);
        a.setChildCount(2);
        Assert.assertEquals("numChilds are correct", 2, a.getChildCount());
        Assert.assertFalse("PropertyChangeEvent not fired", ab.get());
    }
    
    @Test
    public void testFormula() throws ParserException, SocketAlreadyConnectedException {
        AnalogFormula a = (AnalogFormula)_base;
        
        a.getChild(0).disconnect();
        a.getChild(0).connect(getConnectableChild());
        a.getChild(0).setName("Abc");
        a.getChild(1).disconnect();
        a.getChild(1).connect(getConnectableChild());
        a.getChild(1).setName("Xyz");
        
        a.setFormula("Xyz + Abc");
        Assert.assertEquals("Formula is correct", "Xyz + Abc", a.getFormula());
        
        a.setFormula("Abc - Xyz");
        Assert.assertEquals("Formula is correct", "Abc - Xyz", a.getFormula());
    }
    
    @Test
    public void testGetChild() throws Exception {
        AnalogFormula expression2 = new AnalogFormula("IQAE321", null);
//        expression2.setFormula("R1");
        
        for (int i=0; i < 3; i++) {
            Assert.assertTrue("getChildCount() returns "+i, i+1 == expression2.getChildCount());
            
            Assert.assertNotNull("getChild(0) returns a non null value",
                    expression2.getChild(0));
            
            assertIndexOutOfBoundsException(expression2::getChild, i+1, i+1);
            
            // Connect a new child expression
            AnalogExpressionConstant expr = new AnalogExpressionConstant("IQAE"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expr);
            expression2.getChild(i).connect(maleSocket);
        }
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    // Test the methods connected(FemaleSocket) and getExpressionSystemName(int)
    @Test
    public void testConnected_getExpressionSystemName() throws SocketAlreadyConnectedException {
        AnalogFormula expression = new AnalogFormula("IQAE121", null);
        
        AnalogExpressionMemory analogExpressionMemory = new AnalogExpressionMemory("IQAE122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpressionMemory);
        
        Assert.assertEquals("Num children is correct", 1, expression.getChildCount());
        
        // Test connect and disconnect
        expression.getChild(0).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertEquals("getExpressionSystemName(0) is correct", "IQAE122", expression.getExpressionSystemName(0));
        Assert.assertNull("getExpressionSystemName(1) is null", expression.getExpressionSystemName(1));
        expression.getChild(0).disconnect();
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertNull("getExpressionSystemName(1) is null", expression.getExpressionSystemName(1));
        
        expression.getChild(1).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertEquals("getExpressionSystemName(1) is correct", "IQAE122", expression.getExpressionSystemName(1));
        expression.getChild(0).disconnect();    // Test removing child with the wrong index.
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertEquals("getExpressionSystemName(1) is correct", "IQAE122", expression.getExpressionSystemName(1));
        expression.getChild(1).disconnect();
        Assert.assertEquals("Num children is correct", 2, expression.getChildCount());
        Assert.assertNull("getExpressionSystemName(0) is null", expression.getExpressionSystemName(0));
        Assert.assertNull("getExpressionSystemName(1) is null", expression.getExpressionSystemName(1));
    }
    
    @Test
    public void testDescription() {
        AnalogFormula expression = new AnalogFormula("IQAE321", null);
        Assert.assertEquals("strings matches", "Analog Formula", expression.getShortDescription());
        Assert.assertEquals("strings matches", "Analog Formula: empty", expression.getLongDescription());
    }
    
    @Test
    public void testEvaluateEmptyFormula() throws ParserException, JmriException {
        AnalogFormula expression = new AnalogFormula("IQAE321", null);
        expression.setFormula("");
        Assert.assertTrue("Empty formula returns 0.0", 0.0 == expression.evaluate());
    }
/*    
    private void testValidate(boolean expectedResult, String formula, List<AnalogExpressionBean> conditionalVariablesList) throws Exception {
        Formula ix1 = new Formula("IQDE321", "IXIC 1");
        ix1.setFormula("R1");
        
        int count = 0;
        List<ExpressionEntry> expressionEntryList = new ArrayList<>();
        for (AnalogExpressionBean expressionFormula : conditionalVariablesList) {
            String socketName = "E"+Integer.toString(count++);
            FemaleAnalogExpressionSocket socket =
                    InstanceManager.getDefault(AnalogExpressionManager.class)
                            .createFemaleSocket(null, this, socketName);
            socket.connect((MaleSocket) expressionFormula);
            expressionEntryList.add(new ExpressionEntry(socket, socketName));
        }
        
        if (expectedResult) {
            Assert.assertTrue("validateFormula() returns null for '"+formula+"'",
                    ix1.validateFormula(formula, expressionEntryList) == null);
        } else {
            Assert.assertTrue("validateFormula() returns error message for '"+formula+"'",
                    ix1.validateFormula(formula, expressionEntryList) != null);
        }
    }
    
    private void testCalculate(int expectedResult, String formula,
            List<AnalogExpressionBean> conditionalVariablesList, String errorMessage)
            throws Exception {
        
        Formula ix1 = new Formula("IQDE321", "IXIC 1");
        ix1.setFormula(formula);
        
//        for (int i=0; i < ix1.getChildCount(); i++) {
//            ix1.getChild(i).disconnect();
//        }
        
        ix1.setChildCount(conditionalVariablesList.size());
        
        for (int i=0; i < conditionalVariablesList.size(); i++) {
            ix1.getChild(i).connect((MaleSocket)conditionalVariablesList.get(i));
        }
        
        switch (expectedResult) {
            case Formula.FALSE:
                Assert.assertFalse("validateFormula() returns FALSE for '"+formula+"'",
                        ix1.evaluate());
                break;
                
            case Formula.TRUE:
//                System.err.format("formula: %s%n", formula);
//                System.err.format("variable: %b%n", conditionalVariablesList.get(0).evaluate(isCompleted));
                Assert.assertTrue("validateFormula() returns TRUE for '"+formula+"'",
                        ix1.evaluate());
                break;
                
            default:
                throw new RuntimeException(String.format("Unknown expected result: %d", expectedResult));
        }
        
        if (! errorMessage.isEmpty()) {
            jmri.util.JUnitAppender.assertErrorMessageStartsWith(errorMessage);
        }
    }
    
    @Test
    public void testValidate() throws Exception {
        AnalogExpressionBean[] conditionalVariables_Empty = { };
        List<AnalogExpressionBean> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        AnalogExpressionBean trueExpression =
                InstanceManager.getDefault(
                        AnalogExpressionManager.class).registerExpression(
                                new True(InstanceManager.getDefault(AnalogExpressionManager.class).getAutoSystemName(), null));
//        AnalogExpressionBean falseExpression = InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(new False(conditionalNG));
        
        AnalogExpressionBean[] conditionalVariables_True
                = { trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        AnalogExpressionBean[] conditionalVariables_TrueTrueTrue
                = { trueExpression
                        , trueExpression
                        , trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        // Test empty formula string
        testValidate(EXPECT_FAILURE, "", conditionalVariablesList_Empty);
        
        testValidate(EXPECT_SUCCESS, "R1", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2", conditionalVariablesList_True);
        
        // Test parentheses
        testValidate(EXPECT_SUCCESS, "([{R1)}]", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "(R2", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2)", conditionalVariablesList_True);
        
        // Test several items
        testValidate(EXPECT_FAILURE, "R1 and R2 and R3", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R1", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test uppercase and lowercase
        testValidate(EXPECT_SUCCESS, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test several items and parenthese
        testValidate(EXPECT_SUCCESS, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        
        // Test invalid combinations
        testValidate(EXPECT_FAILURE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue);
    }
    
    @Test
    @SuppressWarnings("unused") // test building in progress
    public void testCalculate() throws Exception {
        AnalogExpressionBean[] conditionalVariables_Empty = { };
        List<AnalogExpressionBean> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        AnalogExpressionBean trueExpression =
                InstanceManager.getDefault(
                        AnalogExpressionManager.class).registerExpression(
                                new True(InstanceManager.getDefault(AnalogExpressionManager.class).getAutoSystemName(), null));
        AnalogExpressionBean falseExpression =
                InstanceManager.getDefault(
                        AnalogExpressionManager.class).registerExpression(
                                new False(InstanceManager.getDefault(AnalogExpressionManager.class).getAutoSystemName(), null));
        
        AnalogExpressionBean[] conditionalVariables_True
                = { trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        AnalogExpressionBean[] conditionalVariables_False
                = { falseExpression };
        List<AnalogExpressionBean> conditionalVariablesList_False = Arrays.asList(conditionalVariables_False);
        
        AnalogExpressionBean[] conditionalVariables_TrueTrueTrue
                = { trueExpression
                        , trueExpression
                        , trueExpression };
        List<AnalogExpressionBean> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        // Test with two digit variable numbers
        AnalogExpressionBean[] conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse
                = {trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression
                        , trueExpression
                        , trueExpression
                        , falseExpression };
        List<AnalogExpressionBean> conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse =
                Arrays.asList(conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse);
        
        
        // Test empty formula string
        testCalculate(Formula.FALSE, "", conditionalVariablesList_Empty, "");
//        testCalculate(Formula.FALSE, "", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= , ex= java.lang.StringIndexOutOfBoundsException");
        testCalculate(Formula.FALSE, "", conditionalVariablesList_True, "");
        
        // Test illegal number
        testCalculate(Formula.FALSE, "R#", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R#, ex= java.lang.NumberFormatException");
        testCalculate(Formula.FALSE, "R-", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R-, ex= java.lang.NumberFormatException");
        testCalculate(Formula.FALSE, "Ra", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= Ra, ex= java.lang.NumberFormatException");
        
        // Test single condition
        testCalculate(Formula.TRUE, "R1", conditionalVariablesList_True, "");
        testCalculate(Formula.FALSE, "R1", conditionalVariablesList_False, "");
        testCalculate(Formula.FALSE, "not R1", conditionalVariablesList_True, "");
        testCalculate(Formula.TRUE, "not R1", conditionalVariablesList_False, "");
        
        // Test single item but wrong item (R2 instead of R1)
//        testCalculate(Formula.FALSE, "R2)", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= R2), ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test two digit variable numbers
        testCalculate(Formula.TRUE, "R3 and R12 or R5 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "R3 and (R12 or R5) and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "R12 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.TRUE, "R12 or R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Formula.FALSE, "not (R12 or R10)",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        
        // Test parentheses
        testCalculate(Formula.TRUE, "([{R1)}]", conditionalVariablesList_True, "");
//        testCalculate(Formula.FALSE, "(R2", conditionalVariablesList_True,
//                "IXIC 1 parseCalculation error formula= (R2, ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test several items
        testCalculate(Formula.FALSE, "R1 and R2 and R3", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error formula= R1 and R2 and R3, ex= java.lang.IndexOutOfBoundsException");
        testCalculate(Formula.TRUE, "R1", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue, "");
        
        // Test invalid combinations of and, or, not
        testCalculate(Formula.FALSE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 and or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or and R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 not R3 and R2, ex= jmri.JmriException: Could not find expected operator < NOTR3ANDR2 >");
        testCalculate(Formula.FALSE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= and R1 not R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR1NOTR3ANDR2 >");
        testCalculate(Formula.FALSE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 or R3 and R2 or, ex= java.lang.StringIndexOutOfBoundsException");
        
        // Test several items and parenthese
        testCalculate(Formula.TRUE, "(R1 and R3) and R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= (R1 and) R3 and not R2, ex= jmri.JmriException: Unexpected operator or characters < )R3ANDNOTR2 >");
        testCalculate(Formula.FALSE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1( and R3) and not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3)ANDNOTR2 >");
        testCalculate(Formula.FALSE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error formula= R1 (and R3 and) not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3AND)NOTR2 >");
        testCalculate(Formula.FALSE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.TRUE, "(R1 and (R3) and R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Formula.FALSE, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
    }
*/    
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doAnalogAction);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionFormula = new AnalogFormula("IQAE321", null);
//        expressionFormula.setFormula("1");
//        expressionFormula.setFormula("true");
        expressionFormula.setFormula("E1");
        MaleSocket maleSocketExpressionFormula =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expressionFormula);
        doAnalogAction.getChild(0).connect(maleSocketExpressionFormula);
        
        AnalogExpressionConstant childExpression = new AnalogExpressionConstant("IQAE322", null);
        childExpression.setValue(1.0);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        maleSocketExpressionFormula.getChild(0).connect(maleSocketChild);
        
        _base = expressionFormula;
        _baseMaleSocket = maleSocketExpressionFormula;
        
        analogMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM2");
        analogActionMemory = new AnalogActionMemory("IQAA1", null);
        analogActionMemory.setMemory(analogMemory);
        MaleSocket socketAnalogActionMemory = InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);
        doAnalogAction.getChild(1).connect(socketAnalogActionMemory);
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
