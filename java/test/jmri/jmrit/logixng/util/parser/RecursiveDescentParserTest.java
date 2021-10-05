package jmri.jmrit.logixng.util.parser;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test ExpressionParser
 * 
 * @author Daniel Bergqvist 2019
 */
public class RecursiveDescentParserTest {

    @Test
    public void testCtor() {
        RecursiveDescentParser t = new RecursiveDescentParser(null);
        Assert.assertNotNull("not null", t);
    }
    
    @Test
    public void testParseAndCalculate() throws Exception {
        
        AtomicBoolean exceptionIsThrown = new AtomicBoolean();
        Map<String, Variable> variables = new HashMap<>();
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        variables.put("abc", new MyVariable("abc", "ABC"));
        variables.put("x", new MyVariable("x", 12));
        variables.put("someString", new MyVariable("someString", "A simple string"));
        variables.put("myTestField", new MyVariable("myTestField", new TestField()));
        
        RecursiveDescentParser t = new RecursiveDescentParser(variables);
        ExpressionNode exprNode = t.parseExpression("");
        Assert.assertTrue("expression node is null", null == exprNode);
        exprNode = t.parseExpression("134");
        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)134L).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("abc");
        Assert.assertTrue("expression matches", "Identifier:abc".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", "ABC".equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("\"a little string\"");
        Assert.assertTrue("expression matches", "String:a little string".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
//        System.err.format("expected: '%s', %s%n", 134d, ((Object)134d).getClass().getName());
        Assert.assertTrue("calculate is correct", "a little string".equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("123*1233");
        Assert.assertTrue("expression matches", "(IntNumber:123)*(IntNumber:1233)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)151659L).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("123+2123");
        Assert.assertTrue("expression matches", "(IntNumber:123)+(IntNumber:2123)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)2246L).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("123*3.2331");
        Assert.assertTrue("expression matches", "(IntNumber:123)*(FloatNumber:3.2331)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Double)397.6713).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12+45*12.2");
        Assert.assertTrue("expression matches", "(IntNumber:12)+((IntNumber:45)*(FloatNumber:12.2))".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Double)561.0).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12*45+34");
        Assert.assertTrue("expression matches", "((IntNumber:12)*(IntNumber:45))+(IntNumber:34)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)574L).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12-57/43");
        Assert.assertTrue("expression matches", "(IntNumber:12)-((IntNumber:57)/(IntNumber:43))".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)11L).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12/23.2-43");
        Assert.assertTrue("expression matches", "((IntNumber:12)/(FloatNumber:23.2))-(IntNumber:43)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Double)(-42.482758620689655172413793103448)).equals(exprNode.calculate(symbolTable)));
        
        exprNode = t.parseExpression("12 < 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)<(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12 <= 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)<=(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12 > 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)>(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12 >= 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)>=(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12 == 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)==(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("12 != 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)!=(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate(symbolTable)));
/*        
        exprNode = t.parseExpression("not 12 < 2");
        System.err.format("getDefinitionString: '%s'%n", exprNode.getDefinitionString());
        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("expression matches", "(not (IntNumber:12))<(IntNumber:2)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("not (12 < 2)");
        System.err.format("getDefinitionString: '%s'%n", exprNode.getDefinitionString());
        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        Assert.assertTrue("expression matches", "not ((IntNumber:12)<(IntNumber:2))".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate(symbolTable)));
*/        
        exprNode = t.parseExpression("2 <= 3");
        Assert.assertTrue("expression matches", "(IntNumber:2)<=(IntNumber:3)".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= 3 and 3");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= 3 and 3 > 4");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("4 and 2");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("4 or 2");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= 3 and 3 > 4 or 2");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= 3 and 3 > 4 or 2 < 3");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("(2 <= 3) and 3 > 4 or 2 < 3");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= (3 and 3) > 4 or 2 < 3");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= 3 and (3 > 4) or 2 < 3");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("2 <= 3 and (3 > 4) or (2 < 3)");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("(2 <= 3) and (3 > 4) or 2 < 3");
//        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
        
        exceptionIsThrown.set(false);
        try {
            t.parseExpression("12+31*(23-1)+((((9*2+3)-2)/23");
        } catch (InvalidSyntaxException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "Invalid syntax error".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());

        exprNode = t.parseExpression("12+31*(23-1)+21*((((9*2+3)-2)/23+3)/3+4)");
        Assert.assertTrue(
                "expression matches",
                "((IntNumber:12)+((IntNumber:31)*((IntNumber:23)-(IntNumber:1))))+((IntNumber:21)*((((((((IntNumber:9)*(IntNumber:2))+(IntNumber:3))-(IntNumber:2))/(IntNumber:23))+(IntNumber:3))/(IntNumber:3))+(IntNumber:4)))"
                        .equals(exprNode.getDefinitionString()));
        
        exprNode = t.parseExpression("random()");
        Assert.assertTrue("expression matches", "Function:random()".equals(exprNode.getDefinitionString()));
        Object result = exprNode.calculate(symbolTable);
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertTrue("calculate is probably correct", (result instanceof Double) && (((Double)result) >= 0.0) && (((Double)result) <= 1.0));
        exprNode = t.parseExpression("int(23.56)");
        Assert.assertTrue("expression matches", "Function:int(FloatNumber:23.56)".equals(exprNode.getDefinitionString()));
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertTrue("calculate is correct", ((Integer)23).equals(exprNode.calculate(symbolTable)));
        exprNode = t.parseExpression("sin(180,\"deg\")");
        Assert.assertTrue("expression matches", "Function:sin(IntNumber:180,String:deg)".equals(exprNode.getDefinitionString()));
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertEquals("calculate is correct", 0, (Double)exprNode.calculate(symbolTable), 1e-15);
        exprNode = t.parseExpression("int(x*2+5)");
        Assert.assertTrue("expression matches", "Function:int(((Identifier:x)*(IntNumber:2))+(IntNumber:5))".equals(exprNode.getDefinitionString()));
        exprNode = t.parseExpression("int((x))");
        Assert.assertTrue("expression matches", "Function:int(Identifier:x)".equals(exprNode.getDefinitionString()));
//        exprNode = t.parseExpression("abc(x*(2+3),23,\"Abc\",2)");
//        Assert.assertTrue(
//                "expression matches",
//                "Function:abc((Identifier:x)*((IntNumber:2)+(IntNumber:3)),IntNumber:23,String:Abc,IntNumber:2)"
//                        .equals(exprNode.getDefinitionString()));
        
        exceptionIsThrown.set(false);
        try {
            t.parseExpression("abc(123)");
        } catch (FunctionNotExistsException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The function \"abc\" does not exists".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
/*        
        exceptionIsThrown.set(false);
        try {
            t.parseExpression("abcde");
        } catch (IdentifierNotExistsException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The identifier \"abcde\" does not exists".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
*/        
        exceptionIsThrown.set(false);
        try {
            t.parseExpression("abc(123)");
        } catch (FunctionNotExistsException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The function \"abc\" does not exists".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
        
        exceptionIsThrown.set(false);
        try {
            exprNode = t.parseExpression("sin(1,2,3)");
            exprNode.calculate(symbolTable);
        } catch (WrongNumberOfParametersException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "Function \"sin\" has wrong number of parameters".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
        
        exceptionIsThrown.set(false);
        try {
            exprNode = t.parseExpression("123+\"abc\"");
            exprNode.calculate(symbolTable);
        } catch (CalculateException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The two operands \"123\" and \"abc\" have different types".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
        
        
        
        jmri.Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRun(false);
        fastClock.setTime(new Date(0,0,0,11,05));   // 11:05
        
        int minSinceMidnight = (11 * 60) + 5;
        exprNode = t.parseExpression("fastClock()");
        Assert.assertEquals("expression matches", "Function:fastClock()", exprNode.getDefinitionString());
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertEquals("calculate is correct", minSinceMidnight, (int)exprNode.calculate(symbolTable));
        
        
        
        exprNode = t.parseExpression("someString.toString()");
        Assert.assertEquals("expression matches", "Method:someString.toString()", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "A simple string", exprNode.calculate(symbolTable));
        exprNode = t.parseExpression("someString.length()");
        Assert.assertEquals("expression matches", "Method:someString.length()", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 15, (int)exprNode.calculate(symbolTable));
        exprNode = t.parseExpression("someString.substring(2,8)");
        Assert.assertEquals("expression matches", "Method:someString.substring(IntNumber:2,IntNumber:8)", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "simple", exprNode.calculate(symbolTable));
        exprNode = t.parseExpression("someString.indexOf(\"i\",8)");
        Assert.assertEquals("expression matches", "Method:someString.indexOf(String:i,IntNumber:8)", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (int)exprNode.calculate(symbolTable));
        
        
        
        exprNode = t.parseExpression("myTestField.myString");
        Assert.assertEquals("expression matches", "InstanceVariable:myTestField.myString", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "Hello", exprNode.calculate(symbolTable));
        exprNode = t.parseExpression("myTestField.myInt");
        Assert.assertEquals("expression matches", "InstanceVariable:myTestField.myInt", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 32, (int)exprNode.calculate(symbolTable));
        exprNode = t.parseExpression("myTestField.myFloat");
        Assert.assertEquals("expression matches", "InstanceVariable:myTestField.myFloat", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 31.32, (float)exprNode.calculate(symbolTable), 0.000001);
        
        
        
        TestField testField = (TestField) variables.get("myTestField").getValue(symbolTable);
        
        ExpressionNodeInstanceVariable instanceVariable = new ExpressionNodeInstanceVariable("myTestField", "myString", variables);
        Assert.assertEquals("Hello", testField.myString);
        instanceVariable.assignValue(symbolTable, "Something else");
        Assert.assertEquals("Something else", testField.myString);
        
        instanceVariable = new ExpressionNodeInstanceVariable("myTestField", "myInt", variables);
        Assert.assertEquals(32, testField.myInt);
        instanceVariable.assignValue(symbolTable, (long)23103);
        Assert.assertEquals(23103, testField.myInt);
        
        instanceVariable = new ExpressionNodeInstanceVariable("myTestField", "myFloat", variables);
        Assert.assertEquals(31.32, testField.myFloat, 0.000001);
        instanceVariable.assignValue(symbolTable, (double)112.12);
        Assert.assertEquals((float)112.12, testField.myFloat, 0.000001);
    }
    
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    private static class MyVariable implements Variable {
        
        private final String _name;
        private final Object _value;
        
        private MyVariable(String name, Object value) {
            _name = name;
            _value = value;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Object getValue(SymbolTable symbolTable) {
            return _value;
        }

        @Override
        public void setValue(SymbolTable symbolTable, Object value) {
            throw new UnsupportedOperationException("Not supported");
        }
    }
    
    
    private static class TestField {
        public String myString = "Hello";
        public int myInt = 32;
        public float myFloat = (float) 31.32;
    }
    
}
