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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testParseAndCalculate() throws Exception {
        
        AtomicBoolean exceptionIsThrown = new AtomicBoolean();
        Map<String, Variable> variables = new HashMap<>();
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        variables.put("abc", new MyVariable("abc", "ABC"));
        variables.put("x", new MyVariable("x", 12));
        variables.put("someString", new MyVariable("someString", "A simple string"));
        variables.put("myTestField", new MyVariable("myTestField", new TestField()));
        
        Variable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);
        
        MyVariable mySecondVar = new MyVariable("mySecondVar", "");
        variables.put(mySecondVar.getName(), mySecondVar);
        
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
        Assert.assertEquals("expression matches", "String:\"a little string\"", exprNode.getDefinitionString());
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
        Assert.assertEquals("expression matches", "Function:sin(IntNumber:180,String:\"deg\")", exprNode.getDefinitionString());
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
        
        
        
        ExpressionNode expressionNode = t.parseExpression("\"A simple string\".toString()");
        Assert.assertEquals("expression matches", "String:\"A simple string\"->Method:toString()", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "A simple string", expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("\"A simple string\".length()");
        Assert.assertEquals("expression matches", "String:\"A simple string\"->Method:length()", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 15, (int)expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("\"A simple string\".substring(2,8)");
        Assert.assertEquals("expression matches", "String:\"A simple string\"->Method:substring(IntNumber:2,IntNumber:8)", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "simple", expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("\"A simple string\".indexOf(\"i\",8)");
        Assert.assertEquals("expression matches", "String:\"A simple string\"->Method:indexOf(String:\"i\",IntNumber:8)", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (int)expressionNode.calculate(symbolTable));
        
        expressionNode = t.parseExpression("someString.toString()");
        Assert.assertEquals("expression matches", "Identifier:someString->Method:toString()", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "A simple string", expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("someString.length()");
        Assert.assertEquals("expression matches", "Identifier:someString->Method:length()", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 15, (int)expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("someString.substring(2,8)");
        Assert.assertEquals("expression matches", "Identifier:someString->Method:substring(IntNumber:2,IntNumber:8)", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "simple", expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("someString.indexOf(\"i\",8)");
        Assert.assertEquals("expression matches", "Identifier:someString->Method:indexOf(String:\"i\",IntNumber:8)", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (int)expressionNode.calculate(symbolTable));
        
        
        
        TestField testField = (TestField) variables.get("myTestField").getValue(symbolTable);
        
        expressionNode = t.parseExpression("myTestField.myString");
        Assert.assertEquals("expression matches", "Identifier:myTestField->InstanceVariable:myString", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "Hello", expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("myTestField.myInt");
        Assert.assertEquals("expression matches", "Identifier:myTestField->InstanceVariable:myInt", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 32, (int)expressionNode.calculate(symbolTable));
        expressionNode = t.parseExpression("myTestField.myFloat");
        Assert.assertEquals("expression matches", "Identifier:myTestField->InstanceVariable:myFloat", expressionNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 31.32, (float)expressionNode.calculate(symbolTable), 0.000001);
        
        
        
        ExpressionNodeInstanceVariable instanceVariable = new ExpressionNodeInstanceVariable("myString", variables);
        Assert.assertEquals("Hello", testField.myString);
        instanceVariable.assignValue(testField, symbolTable, "Something else");
        Assert.assertEquals("Something else", testField.myString);
        
        instanceVariable = new ExpressionNodeInstanceVariable("myInt", variables);
        Assert.assertEquals(32, testField.myInt);
        instanceVariable.assignValue(testField, symbolTable, (long)23103);
        Assert.assertEquals(23103, testField.myInt);
        
        instanceVariable = new ExpressionNodeInstanceVariable("myFloat", variables);
        Assert.assertEquals(31.32, testField.myFloat, 0.000001);
        instanceVariable.assignValue(testField, symbolTable, 112.12);
        Assert.assertEquals((float)112.12, testField.myFloat, 0.000001);
        
        
        
        exprNode = t.parseExpression("myVar = 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar)=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", 12, (long)(Long)myVar.getValue(symbolTable));
        
        myVar.setValue(symbolTable, 10);
        exprNode = t.parseExpression("myVar += 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar)+=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar is correct", 10, (long)(Integer)myVar.getValue(symbolTable));
        Assert.assertEquals("calculate is correct", 22, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", 22, (long)(Long)myVar.getValue(symbolTable));
        
        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar -= 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar)-=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar is correct", 10, (long)(Long)myVar.getValue(symbolTable));
        Assert.assertEquals("calculate is correct", -2, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", -2, (long)(Long)myVar.getValue(symbolTable));
        
        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar *= 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar)*=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar is correct", 10, (long)(Long)myVar.getValue(symbolTable));
        Assert.assertEquals("calculate is correct", 120, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", 120, (long)(Long)myVar.getValue(symbolTable));
        
        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar /= 2");
        Assert.assertEquals("expression matches", "(Identifier:myVar)/=(IntNumber:2)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar is correct", 10, (long)(Long)myVar.getValue(symbolTable));
        Assert.assertEquals("calculate is correct", 5, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", 5, (long)(Long)myVar.getValue(symbolTable));
        
        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar %= 3");
        Assert.assertEquals("expression matches", "(Identifier:myVar)%=(IntNumber:3)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar is correct", 10, (long)(Long)myVar.getValue(symbolTable));
        Assert.assertEquals("calculate is correct", 1, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", 1, (long)(Long)myVar.getValue(symbolTable));
        
        
        
        List<Object> myList = new ArrayList<>();
        myList.add(0, "Test");
        myList.add(1, "Something");
        myList.add(2, "Else");
        myVar.setValue(symbolTable, myList);
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] = 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1])=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1] is correct", 12, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] += 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1])+=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar[1] is correct", 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        Assert.assertEquals("calculate is correct", 22, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1] is correct", 22, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] -= 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1])-=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar[1] is correct", 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        Assert.assertEquals("calculate is correct", -2, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1] is correct", -2, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] *= 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1])*=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar[1] is correct", 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        Assert.assertEquals("calculate is correct", 120, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1] is correct", 120, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] /= 2");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1])/=(IntNumber:2)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar[1] is correct", 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        Assert.assertEquals("calculate is correct", 5, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1] is correct", 5, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] %= 3");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1])%=(IntNumber:3)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar[1] is correct", 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        Assert.assertEquals("calculate is correct", 1, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1] is correct", 1, (long)((List<Long>)myVar.getValue(symbolTable)).get(1));
        
        mySecondVar._myValue = "Something";
        myList.set(1, mySecondVar);
        exprNode = t.parseExpression("myVar[1]._myValue = \"Something else\"");
        Assert.assertEquals("expression matches", "(Identifier:myVar->[IntNumber:1]->InstanceVariable:_myValue)=(String:\"Something else\")", exprNode.getDefinitionString());
        Assert.assertEquals("myVar[1]._myValue is correct", "Something", mySecondVar._myValue);
        Assert.assertEquals("calculate is correct", "Something else", exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar[1]._myValue is correct", "Something else", mySecondVar._myValue);
        
        exprNode = t.parseExpression("myVar[1].myFunc(\"Hello\")");
        Assert.assertEquals("expression matches", "Identifier:myVar->[IntNumber:1]->Method:myFunc(String:\"Hello\")", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "++Hello++", exprNode.calculate(symbolTable));
        
        
        
        
        Map<Object, Object> myMap = new HashMap<>();
        myMap.put("Red", "Test");
        myMap.put("Green", "Something");
        myMap.put("Yellow", "Else");
        myVar.setValue(symbolTable, myMap);
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} = 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Green\"})=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Green\"} is correct", 12, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} += 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Green\"})+=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar{\"Green\"} is correct", 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        Assert.assertEquals("calculate is correct", 22, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Green\"} is correct", 22, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} -= 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Green\"})-=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar{\"Green\"} is correct", 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        Assert.assertEquals("calculate is correct", -2, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Green\"} is correct", -2, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} *= 12");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Green\"})*=(IntNumber:12)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar{\"Green\"} is correct", 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        Assert.assertEquals("calculate is correct", 120, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Green\"} is correct", 120, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} /= 2");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Green\"})/=(IntNumber:2)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar{\"Green\"} is correct", 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        Assert.assertEquals("calculate is correct", 5, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Green\"} is correct", 5, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} %= 3");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Green\"})%=(IntNumber:3)", exprNode.getDefinitionString());
        Assert.assertEquals("myVar{\"Green\"} is correct", 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        Assert.assertEquals("calculate is correct", 1, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Green\"} is correct", 1, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"));
        
        mySecondVar._myValue = "Something";
        myMap.put("Hello", mySecondVar);
        exprNode = t.parseExpression("myVar{\"Hello\"}._myValue = \"Something else\"");
        Assert.assertEquals("expression matches", "(Identifier:myVar->{String:\"Hello\"}->InstanceVariable:_myValue)=(String:\"Something else\")", exprNode.getDefinitionString());
        Assert.assertEquals("myVar{\"Hello\"}._myValue is correct", "Something", mySecondVar._myValue);
        Assert.assertEquals("calculate is correct", "Something else", exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar{\"Hello\"}._myValue is correct", "Something else", mySecondVar._myValue);
        
        exprNode = t.parseExpression("myVar{\"Hello\"}.myFunc(\"Hello\")");
        Assert.assertEquals("expression matches", "Identifier:myVar->{String:\"Hello\"}->Method:myFunc(String:\"Hello\")", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", "++Hello++", exprNode.calculate(symbolTable));
        
        
        
        
        Map<String, Object> myMap1 = new HashMap<>();
        Map<String, Object> myMap2 = new HashMap<>();
        Map<String, Object> myMap3 = new HashMap<>();
        Map<String, Object> myMap4 = new HashMap<>();
        Map<String, Object> myMap5 = new HashMap<>();
        
        List<Object> myOtherList = new ArrayList<>();
        myOtherList.add(0, "Test!!");
        myOtherList.add(1, "Something!!");
        myOtherList.add(2, "Else!!");
        
        List<Object> myThirdList = new ArrayList<>();
        myThirdList.add(0, "Test!!");
        myThirdList.add(1, "Something!!");
        myThirdList.add(2, "Else!!");
        
        variables.put("myIndex1", new MyVariable("myIndex1", 1));
        variables.put("myIndex2", new MyVariable("myIndex2", 2));
        variables.put("theThirdKey", new MyVariable("theThirdKey", "A third key"));
        
        myMap1.put("A key", myMap2);
        myMap2.put("A second key", myList);
        myList.set(1, myOtherList);
        myOtherList.set(2, myMap3);
//        myMap2.put("A second key", myMap3);
        myMap3.put("A third key", myThirdList);
        myThirdList.set(1, myMap4);
        myMap4.put("A fourth key", myMap5);
        myMap5.put("A fifth key", (long)10);
        myVar.setValue(symbolTable, myMap1);
        exprNode = t.parseExpression(
                "myVar"                     // The local variable
                + "{\"A key\"}"             // "A key" in map1
                + "{\"A second key\"}"      // "A second key" in map2
                + "[myIndex1]"              // Index myIndex1 in list1
                + "[myIndex2]"              // Index myIndex2 in list2
                + "{theThirdKey}"           // The local variable "theThirdKey" with the value "A third key" in map3
                + "[1]"                     // Index 1 in list3
                + "{\"A fourth key\"}"      // "A fourth key" in map4
                + "{\"A fifth key\"}"       // "A fifth key" in map6
                + " = 12");                 // set to the value 12
        exprNode.calculate(symbolTable);
        Assert.assertEquals("expression matches",
                "(Identifier:myVar->{String:\"A key\"}->{String:\"A second key\"}->[Identifier:myIndex1]->[Identifier:myIndex2]->{Identifier:theThirdKey}->[IntNumber:1]->{String:\"A fourth key\"}->{String:\"A fifth key\"})=(IntNumber:12)",
                exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 12, (long)(Long)exprNode.calculate(symbolTable));
        Assert.assertEquals("myVar is correct", 12, (long)(Long)myMap5.get("A fifth key"));
        
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
    
    
    public static class MyVariable implements Variable {
        // If this class is private instead of public, we will get the error:
        // The method myFunc(String) from the type RecursiveDescentParserTest.MyVariable is never used locally
        // But the myFunc(String) is used by formula.
        
        private final String _name;
        private Object _value;
        
        public Object _myValue;     // Must be public for the test
        
        
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
            _value = value;
        }

        public String myFunc(String param) {
            return "++" + param + "++";
        }
    }
    
    
    private static class TestField {
        public String myString = "Hello";
        public int myInt = 32;
        public float myFloat = (float) 31.32;
    }
    
}
