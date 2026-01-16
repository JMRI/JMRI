package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionParser
 *
 * @author Daniel Bergqvist 2019
 */
public class RecursiveDescentParserTest {

    @Test
    public void testCtor() {
        RecursiveDescentParser t = new RecursiveDescentParser(null);
        assertNotNull( t, "not null");
    }

    @Test
    public void testParseAndCalculate() throws JmriException {

        Map<String, Variable> variables = new HashMap<>();

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        variables.put("abc", new MyVariable("abc", "ABC"));
        variables.put("x", new MyVariable("x", 12));

        Variable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        MyVariable mySecondVar = new MyVariable("mySecondVar", "");
        variables.put(mySecondVar.getName(), mySecondVar);

        RecursiveDescentParser t = new RecursiveDescentParser(variables);
        ExpressionNode exprNode = t.parseExpression("");
        assertNull( exprNode, "expression node is null");
        exprNode = t.parseExpression("134");
        assertEquals( "IntNumber:134", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 134L, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("abc");
        assertEquals( "Identifier:abc", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "ABC", exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("\"a little string\"");
        assertEquals( "String:\"a little string\"", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "a little string", exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("123*1233");
        assertEquals( "(IntNumber:123)*(IntNumber:1233)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 151659L, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("123+2123");
        assertEquals( "(IntNumber:123)+(IntNumber:2123)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2246L,exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("123*3.2331");
        assertEquals( "(IntNumber:123)*(FloatNumber:3.2331)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 397.6713, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12+45*12.2");
        assertEquals( "(IntNumber:12)+((IntNumber:45)*(FloatNumber:12.2))", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 561.0, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12*45+34");
        assertEquals( "((IntNumber:12)*(IntNumber:45))+(IntNumber:34)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 574L, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12-57/43");
        assertEquals( "(IntNumber:12)-((IntNumber:57)/(IntNumber:43))", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 11L, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12/23.2-43");
        assertEquals( "((IntNumber:12)/(FloatNumber:23.2))-(IntNumber:43)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -42.482758620689655172413793103448, exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("12 < 2");
        assertEquals( "(IntNumber:12)<(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12 <= 2");
        assertEquals( "(IntNumber:12)<=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12 > 2");
        assertEquals( "(IntNumber:12)>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12 >= 2");
        assertEquals( "(IntNumber:12)>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12 == 2");
        assertEquals( "(IntNumber:12)==(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("12 != 2");
        assertEquals( "(IntNumber:12)!=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(symbolTable), exprNode.calculate(symbolTable).getClass().getName());
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");
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
        assertEquals( "(IntNumber:2)<=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
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

        InvalidSyntaxException ex = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("12+31*(23-1)+((((9*2+3)-2)/23"), "exception is thrown");
        assertEquals( "Invalid syntax error", ex.getMessage(), "exception message matches");

        exprNode = t.parseExpression("12+31*(23-1)+21*((((9*2+3)-2)/23+3)/3+4)");
        assertEquals(
            "((IntNumber:12)+((IntNumber:31)*((IntNumber:23)-(IntNumber:1))))+((IntNumber:21)*((((((((IntNumber:9)*(IntNumber:2))+(IntNumber:3))-(IntNumber:2))/(IntNumber:23))+(IntNumber:3))/(IntNumber:3))+(IntNumber:4)))",
                    exprNode.getDefinitionString(),
                    "expression matches");

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
        CalculateException e = assertThrows( CalculateException.class, () -> {
            var eNode = t.parseExpression("123+\"abc\"");
            eNode.calculate(symbolTable);
        }, "exception is thrown");
        assertEquals( "The two operands \"123\" and \"abc\" have different types",
                e.getMessage(), "exception message matches");



        exprNode = t.parseExpression("myVar = 12");
        assertEquals( "(Identifier:myVar)=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 12, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        myVar.setValue(symbolTable, 10);
        exprNode = t.parseExpression("myVar += 12");
        assertEquals( "(Identifier:myVar)+=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Integer)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 22, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 22, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar -= 12");
        assertEquals( "(Identifier:myVar)-=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( -2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -2, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar *= 12");
        assertEquals( "(Identifier:myVar)*=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 120, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 120, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar /= 2");
        assertEquals( "(Identifier:myVar)/=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 5, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 5, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar %= 3");
        assertEquals( "(Identifier:myVar)%=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 1, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 1, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");


        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("++myVar");
        assertNotNull(exprNode);
        assertEquals( "++(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 11, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 11, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        int i = 10; // The purpuse of these tests is to ensure LogixNG formula ++ follows Java ++
        myVar.setValue(symbolTable, (long)10);
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( ++i, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");


        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar++");
        assertEquals( "(Identifier:myVar)++", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 11, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        i = 10;     // The purpuse of these tests is to ensure LogixNG formula ++ follows Java ++
        myVar.setValue(symbolTable, (long)10);
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( i++, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");


        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("--myVar");
        assertEquals( "--(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 9, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 9, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        i = 10;     // The purpuse of these tests is to ensure LogixNG formula -- follows Java --
        myVar.setValue(symbolTable, (long)10);
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( --i, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");


        myVar.setValue(symbolTable, (long)10);
        exprNode = t.parseExpression("myVar--");
        assertEquals( "(Identifier:myVar)--", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 9, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");

        i = 10;     // The purpuse of these tests is to ensure LogixNG formula -- follows Java --
        myVar.setValue(symbolTable, (long)10);
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
        assertEquals( i--, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( i, (long)(Long)myVar.getValue(symbolTable), "myVar is correct");
    }


    //             <rule2> = <rule1> ||
    //             <rule2> += <rule1> ||
    //             <rule2> -= <rule1> ||
    //             <rule2> *= <rule1> ||
    //             <rule2> /= <rule1> ||
    //             <rule2> %= <rule1> ||
    //             <rule2> &= <rule1> ||
    //             <rule2> ^= <rule1> ||
    //             <rule2> |= <rule1> ||
    //             <rule2> <<= <rule1> ||
    //             <rule2> >>= <rule1> ||
    //             <rule2> >>>= <rule1>
    @Test
    public void testRule1() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        MyVariable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        myVar._value = 10;
        ExpressionNode exprNode = t.parseExpression("myVar = 2");
        assertEquals( "(Identifier:myVar)=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar += 2");
        assertEquals( "(Identifier:myVar)+=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar -= 2");
        assertEquals( "(Identifier:myVar)-=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 8, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar *= 2");
        assertEquals( "(Identifier:myVar)*=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 20, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar /= 3");
        assertEquals( "(Identifier:myVar)/=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 3, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar %= 3");
        assertEquals( "(Identifier:myVar)%=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 1, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar &= 3");
        assertEquals( "(Identifier:myVar)&=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar ^= 3");
        assertEquals( "(Identifier:myVar)^=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 9, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar |= 3");
        assertEquals( "(Identifier:myVar)|=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 11, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 1000;
        exprNode = t.parseExpression("myVar <<= 2");
        assertEquals( "(Identifier:myVar)<<=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 1000 << 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = 1000;
        assertEquals( 4000, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = -1000;
        exprNode = t.parseExpression("myVar <<= 2");
        assertEquals( "(Identifier:myVar)<<=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -1000 << 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = 1000;
        assertEquals( 4000, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 1000;
        exprNode = t.parseExpression("myVar >>= 2");
        assertEquals( "(Identifier:myVar)>>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 1000 >> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = 1000;
        assertEquals( 250, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = -1000;
        exprNode = t.parseExpression("myVar >>= 2");
        assertEquals( "(Identifier:myVar)>>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -1000 >> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = -1000;
        assertEquals( -250, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 1000;
        exprNode = t.parseExpression("myVar >>>= 2");
        assertEquals( "(Identifier:myVar)>>>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 1000 >>> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = 1000;
        assertEquals( 250, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = -1000;
        exprNode = t.parseExpression("myVar >>>= 2");
        assertEquals( "(Identifier:myVar)>>>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -1000L >>> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = -1000;
        assertEquals( 4611686018427387654L, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Rule2 is ternary. <rule3a> | <rule3a> ? <rule2> : <rule2>
    @Test
    public void testRule2() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        MyVariable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        myVar._value = 2;
        ExpressionNode exprNode = t.parseExpression("myVar < 4 ? \"Hello\" : \"World\"");
        assertEquals( "((Identifier:myVar)<(IntNumber:4))?(String:\"Hello\"):(String:\"World\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "Hello", exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = 10;
        assertEquals( "World", exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 2;
        exprNode = t.parseExpression("str(myVar == 2 ? 4 : 2)");
        assertEquals( "Function:str(((Identifier:myVar)==(IntNumber:2))?(IntNumber:4):(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "4", exprNode.calculate(symbolTable), "calculate is correct");
        myVar._value = 10;
        assertEquals( "2", exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("str(sensorBlink.getState() == 2 ? 4 : 2)");
        assertEquals( "Function:str(((Identifier:sensorBlink->Method:getState())==(IntNumber:2))?(IntNumber:4):(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");

        exprNode = t.parseExpression("sensorBlink.setState(myVar == 2 ? 4 : 2)");
        assertEquals( "Identifier:sensorBlink->Method:setState(((Identifier:myVar)==(IntNumber:2))?(IntNumber:4):(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");

        exprNode = t.parseExpression("sensorBlink.setState(sensorBlink.getState() == 2 ? 4 : 2)");
        assertEquals( "Identifier:sensorBlink->Method:setState(((Identifier:sensorBlink->Method:getState())==(IntNumber:2))?(IntNumber:4):(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
    }


    // Logical OR
    // <rule3a> ::= <rule3b> | <rule3b> || <rule3b>
    @Test
    public void testRule3a() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("(1 < 2) || (5 > 2)");
        assertEquals( "((IntNumber:1)<(IntNumber:2))||((IntNumber:5)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(1 < 2) || (1 > 2)");
        assertEquals( "((IntNumber:1)<(IntNumber:2))||((IntNumber:1)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(2 < 2) || (1 > 2)");
        assertEquals( "((IntNumber:2)<(IntNumber:2))||((IntNumber:1)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Logical XOR
    // <rule3b> ::= <rule4> | <rule4> || <rule4>
    @Test
    public void testRule3b() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("(1 < 2) ^^ (5 > 2)");
        assertEquals( "((IntNumber:1)<(IntNumber:2))^^((IntNumber:5)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(1 < 2) ^^ (1 > 2)");
        assertEquals( "((IntNumber:1)<(IntNumber:2))^^((IntNumber:1)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(2 < 2) ^^ (1 > 2)");
        assertEquals( "((IntNumber:2)<(IntNumber:2))^^((IntNumber:1)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Logical AND
    // <rule4> ::= <rule5> | <rule5> && <rule5>
    @Test
    public void testRule4() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("(1 < 2) && (5 > 2)");
        assertEquals( "((IntNumber:1)<(IntNumber:2))&&((IntNumber:5)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(1 < 2) && (1 > 2)");
        assertEquals( "((IntNumber:1)<(IntNumber:2))&&((IntNumber:1)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(2 < 2) && (1 > 2)");
        assertEquals( "((IntNumber:2)<(IntNumber:2))&&((IntNumber:1)>(IntNumber:2))", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Bitwise OR
    // <rule5> ::= <rule6> | <rule6> | <rule6>
    @Test
    public void testRule5() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("2 | 10");
        assertEquals( "(IntNumber:2)|(IntNumber:10)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 | 5");
        assertEquals( "(IntNumber:2)|(IntNumber:5)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 7, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Bitwise XOR
    // <rule6> ::= <rule7> | <rule7> ^ <rule7>
    @Test
    public void testRule6() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("2 ^ 10");
        assertEquals( "(IntNumber:2)^(IntNumber:10)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 8, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 ^ 5");
        assertEquals( "(IntNumber:2)^(IntNumber:5)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 7, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Bitwise AND
    // <rule7> ::= <rule8> | <rule8> & <rule8>
    @Test
    public void testRule7() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("2 & 10");
        assertEquals( "(IntNumber:2)&(IntNumber:10)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 & 5");
        assertEquals( "(IntNumber:2)&(IntNumber:5)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 0, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Equality
    // <rule8> ::= <rule9> | <rule9> == <rule9> | <rule9> != <rule9>
    @Test
    public void testRule8() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("1 == 2");
        assertEquals( "(IntNumber:1)==(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 == 2");
        assertEquals( "(IntNumber:2)==(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("1 != 2");
        assertEquals( "(IntNumber:1)!=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 != 2");
        assertEquals( "(IntNumber:2)!=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Relational
    // <rule9> ::= <rule10> | <rule10> < <rule10> | <rule10> <= <rule10> | <rule10> > <rule10> | <rule10> >= <rule10>
    @Test
    public void testRule9() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("1 < 2");
        assertEquals( "(IntNumber:1)<(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 < 2");
        assertEquals( "(IntNumber:2)<(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("3 < 2");
        assertEquals( "(IntNumber:3)<(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("1 <= 2");
        assertEquals( "(IntNumber:1)<=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 <= 2");
        assertEquals( "(IntNumber:2)<=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("3 <= 2");
        assertEquals( "(IntNumber:3)<=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("1 > 2");
        assertEquals( "(IntNumber:1)>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 > 2");
        assertEquals( "(IntNumber:2)>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("3 > 2");
        assertEquals( "(IntNumber:3)>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("1 >= 2");
        assertEquals( "(IntNumber:1)>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("2 >= 2");
        assertEquals( "(IntNumber:2)>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("3 >= 2");
        assertEquals( "(IntNumber:3)>=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Shift
    // <rule10> ::= <rule11> | <rule11> << <rule11> | <rule11> >> <rule11> | <rule11> >>> <rule11>
    @Test
    public void testRule10() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("10 << 2");
        assertEquals( "(IntNumber:10)<<(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 40, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 10 << 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("-10 << 2");
        assertEquals( "(-(IntNumber:10))<<(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -40, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -10 << 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("1000 >> 2");
        assertEquals( "(IntNumber:1000)>>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 250, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 1000 >> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("-1000 >> 2");
        assertEquals( "(-(IntNumber:1000))>>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -250, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -1000 >> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("1000 >>> 2");
        assertEquals( "(IntNumber:1000)>>>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 250, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 1000 >>> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("-1000 >>> 2");
        assertEquals( "(-(IntNumber:1000))>>>(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 4611686018427387654L, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -1000L >>> 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Additive
    // <rule11> ::= <rule12> | <rule12> + <rule12> | <rule12> - <rule12>
    @Test
    public void testRule11() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("10 + 2");
        assertEquals( "(IntNumber:10)+(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("10 - 2");
        assertEquals( "(IntNumber:10)-(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 8, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Multiplicative
    // <rule12> ::= <rule13> | <rule13> * <rule13> | <rule13> / <rule13> | <rule13> % <rule13>
    @Test
    public void testRule12() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("10 * 2");
        assertEquals( "(IntNumber:10)*(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 20, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("10 / 3");
        assertEquals( "(IntNumber:10)/(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 3, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("10 % 3");
        assertEquals( "(IntNumber:10)%(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 1 , (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Rule13 in Java is cast object and object creation. Not relevant here.


    // Unary pre-increment, unary pre-decrement, unary plus, unary minus, unary logical NOT, unary bitwise NOT
    // <rule14> ::= <rule16> | ++ <rule16> | -- <rule16> | + <rule16> | - <rule16> | ! <rule16> | ~ <rule16>
    @Test
    public void testRule14() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        MyVariable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        myVar._value = 10;
        ExpressionNode exprNode = t.parseExpression("++myVar");
        assertEquals( "++(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 11, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 11, (long)myVar._value, "myVar is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("--myVar");
        assertEquals( "--(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 9, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 9, (long)myVar._value, "myVar is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("+myVar");
        assertEquals( "+(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("-myVar");
        assertEquals( "-(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("!1");
        assertEquals( "!(IntNumber:1)", exprNode.getDefinitionString(), "expression matches");
        assertFalse( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("!0");
        assertEquals( "!(IntNumber:0)", exprNode.getDefinitionString(), "expression matches");
        assertTrue( (Boolean)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("~myVar");
        assertEquals( "~(Identifier:myVar)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( -11, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Rule15 in Java is unary post-increment, unary post-decrement, ++ and --.
    @Test
    public void testRule15() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        MyVariable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        myVar._value = 10;
        ExpressionNode exprNode = t.parseExpression("myVar++");
        assertEquals( "(Identifier:myVar)++", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 11, (long)myVar._value, "myVar is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("myVar--");
        assertEquals( "(Identifier:myVar)--", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 9, (long)myVar._value, "myVar is correct");

    }


    // Parentheses
    // <rule16> ::= <rule20> | ( <firstRule> )
    @Test
    public void testRule16() throws JmriException {

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        MyVariable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        myVar._value = (long)10;
        ExpressionNode exprNode = t.parseExpression("(myVar)");
        assertEquals( "Identifier:myVar", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("(2)");
        assertEquals( "IntNumber:2", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("(myVar = 2)");
        assertEquals( "(Identifier:myVar)=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myVar._value = 10;
        exprNode = t.parseExpression("(((myVar = 2)))");
        assertEquals( "(Identifier:myVar)=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

    }


    // Identifiers and constants
    // <rule20> ::= <identifier>
    //              | <identifier> ( <rule21> )
    //              | <rule20> [ <rule21> ]
    //              | <rule20> { <rule21> }
    //              | <rule20> . <rule20>
    //              | <rule20> . <identifier> ( <rule21> )

    // <rule20> ::= <identifier>
    //              | <identifier> ( <rule21> )
    //              | <identifier> [ <rule21> ]
    //              | <identifier> { <rule21> }
    //              | <identifier> . <identifier>
    //              | <identifier> . <identifier> ( <rule21> )
    //              | <identifier> . <identifier> [ <rule21> ]
    //              | <identifier> . <identifier> { <rule21> }
    //              | <identifier> . <identifier>
    //              | <identifier> . <identifier> . <identifier> ( <rule21> )
    //              | <identifier> . <identifier> . <identifier> [ <rule21> ]
    //              | <identifier> . <identifier> . <identifier> { <rule21> }
    //              | <identifier> . <identifier> ( <rule21> ) . <identifier> ( <rule21> )
    //              | <identifier> . <identifier> ( <rule21> ) . <identifier> [ <rule21> ]
    //              | <identifier> . <identifier> ( <rule21> ) . <identifier> { <rule21> }
    //              | <integer number>
    //              | <floating number>
    //              | <string>
    @Test
    @SuppressWarnings("unchecked")
    public void testRule20() throws JmriException {


        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        variables.put("someString", new MyVariable("someString", "A simple string"));
        variables.put("myTestField", new MyVariable("myTestField", new TestField()));

        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        MyVariable myVar = new MyVariable("myVar", "");
        variables.put(myVar.getName(), myVar);

        MyVariable mySecondVar = new MyVariable("mySecondVar", "");
        variables.put(mySecondVar.getName(), mySecondVar);

        myVar._value = 10;
        ExpressionNode exprNode = t.parseExpression("myVar = 2");
        assertEquals( "(Identifier:myVar)=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        exprNode = t.parseExpression("random()");
        assertEquals( "Function:random()", exprNode.getDefinitionString(), "expression matches");
        Object result = exprNode.calculate(symbolTable);
        Double dResult = assertInstanceOf( Double.class, result);
        assertTrue( (dResult >= 0.0) && (dResult <= 1.0), "calculate is probably correct, " + dResult);
        exprNode = t.parseExpression("int(23.56)");
        assertEquals( "Function:int(FloatNumber:23.56)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 23L, exprNode.calculate(symbolTable), "calculate is correct");
        exprNode = t.parseExpression("sin(180,\"deg\")");
        assertEquals( "Function:sin(IntNumber:180,String:\"deg\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 0, (Double)exprNode.calculate(symbolTable), 1e-15, "calculate is correct");
        exprNode = t.parseExpression("int(x*2+5)");
        assertTrue( "Function:int(((Identifier:x)*(IntNumber:2))+(IntNumber:5))".equals(exprNode.getDefinitionString()), "expression matches");
        exprNode = t.parseExpression("int((x))");
        assertTrue( "Function:int(Identifier:x)".equals(exprNode.getDefinitionString()), "expression matches");

        FunctionNotExistsException e = assertThrows( FunctionNotExistsException.class, () ->
            t.parseExpression("abc(123)"), "exception is thrown");
        assertEquals( "The function \"abc\" does not exists", e.getMessage(), "exception message matches");


        e = assertThrows( FunctionNotExistsException.class, () ->
            t.parseExpression("abc(123)"), "exception is thrown");
        assertEquals( "The function \"abc\" does not exists", e.getMessage(), "exception message matches");

        WrongNumberOfParametersException ex = assertThrows( WrongNumberOfParametersException.class, () -> {
            var exceptionExprNode = t.parseExpression("sin(1,2,3)");
            exceptionExprNode.calculate(symbolTable);
        }, "exception is thrown");
        assertEquals( "Function \"sin\" has wrong number of parameters", ex.getMessage(), "exception message matches");



        jmri.Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRun(false);
        fastClock.setTime(new Date(0,0,0,11,05));   // 11:05

        int minSinceMidnight = (11 * 60) + 5;
        exprNode = t.parseExpression("fastClock()");
        assertEquals( "Function:fastClock()", exprNode.getDefinitionString(), "expression matches");
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        assertEquals( minSinceMidnight, (int)exprNode.calculate(symbolTable), "calculate is correct");



        ExpressionNode expressionNode = t.parseExpression("\"A simple string\".toString()");
        assertEquals( "String:\"A simple string\"->Method:toString()", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( "A simple string", expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("\"A simple string\".length()");
        assertEquals( "String:\"A simple string\"->Method:length()", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( 15, (int)expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("\"A simple string\".substring(2,8)");
        assertEquals( "String:\"A simple string\"->Method:substring(IntNumber:2,IntNumber:8)", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( "simple", expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("\"A simple string\".indexOf(\"i\",8)");
        assertEquals( "String:\"A simple string\"->Method:indexOf(String:\"i\",IntNumber:8)", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (int)expressionNode.calculate(symbolTable), "calculate is correct");

        expressionNode = t.parseExpression("someString.toString()");
        assertEquals( "Identifier:someString->Method:toString()", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( "A simple string", expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("someString.length()");
        assertEquals( "Identifier:someString->Method:length()", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( 15, (int)expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("someString.substring(2,8)");
        assertEquals( "Identifier:someString->Method:substring(IntNumber:2,IntNumber:8)", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( "simple", expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("someString.indexOf(\"i\",8)");
        assertEquals( "Identifier:someString->Method:indexOf(String:\"i\",IntNumber:8)", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (int)expressionNode.calculate(symbolTable), "calculate is correct");



        TestField testField = (TestField) variables.get("myTestField").getValue(symbolTable);

        expressionNode = t.parseExpression("myTestField.myString");
        assertEquals( "Identifier:myTestField->InstanceVariable:myString", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( "Hello", expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("myTestField.myInt");
        assertEquals( "Identifier:myTestField->InstanceVariable:myInt", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( 32, (int)expressionNode.calculate(symbolTable), "calculate is correct");
        expressionNode = t.parseExpression("myTestField.myFloat");
        assertEquals( "Identifier:myTestField->InstanceVariable:myFloat", expressionNode.getDefinitionString(), "expression matches");
        assertEquals( 31.32, (float)expressionNode.calculate(symbolTable), 0.000001, "calculate is correct");



        ExpressionNodeInstanceVariable instanceVariable = new ExpressionNodeInstanceVariable("myString", variables);
        assertEquals("Hello", testField.myString);
        instanceVariable.assignValue(testField, symbolTable, "Something else");
        assertEquals("Something else", testField.myString);

        instanceVariable = new ExpressionNodeInstanceVariable("myInt", variables);
        assertEquals(32, testField.myInt);
        instanceVariable.assignValue(testField, symbolTable, (long)23103);
        assertEquals(23103, testField.myInt);

        instanceVariable = new ExpressionNodeInstanceVariable("myFloat", variables);
        assertEquals(31.32, testField.myFloat, 0.000001);
        instanceVariable.assignValue(testField, symbolTable, 112.12);
        assertEquals((float)112.12, testField.myFloat, 0.000001);



        List<Object> myList = new ArrayList<>();
        myList.add(0, "Test");
        myList.add(1, "Something");
        myList.add(2, "Else");
        myVar.setValue(symbolTable, myList);
        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] = 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 12, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");

        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] += 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])+=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");
        assertEquals( 22, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 22, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");

        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] -= 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])-=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");
        assertEquals( -2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -2, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");

        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] *= 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])*=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");
        assertEquals( 120, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 120, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");

        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] /= 2");
        assertEquals( "(Identifier:myVar->[IntNumber:1])/=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");
        assertEquals( 5, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 5, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");

        myList.set(1, (long)10);
        exprNode = t.parseExpression("myVar[1] %= 3");
        assertEquals( "(Identifier:myVar->[IntNumber:1])%=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");
        assertEquals( 1, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 1, (long)((List<Long>)myVar.getValue(symbolTable)).get(1), "myVar[1] is correct");

        mySecondVar._myValue = "Something";
        myList.set(1, mySecondVar);
        exprNode = t.parseExpression("myVar[1]._myValue = \"Something else\"");
        assertEquals( "(Identifier:myVar->[IntNumber:1]->InstanceVariable:_myValue)=(String:\"Something else\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "Something", mySecondVar._myValue, "myVar[1]._myValue is correct");
        assertEquals( "Something else", exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( "Something else", mySecondVar._myValue, "myVar[1]._myValue is correct");

        exprNode = t.parseExpression("myVar[1].myFunc(\"Hello\")");
        assertEquals( "Identifier:myVar->[IntNumber:1]->Method:myFunc(String:\"Hello\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "++Hello++", exprNode.calculate(symbolTable), "calculate is correct");




        Object[] myArray = new Object[]{"Hello", 10, "Something"};
        myVar.setValue(symbolTable, myArray);
        myArray[1] = 10L;
        exprNode = t.parseExpression("myVar[1] = 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 12, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");

        myArray[1] = 10L;
        exprNode = t.parseExpression("myVar[1] += 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])+=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");
        assertEquals( 22, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");

        myArray[1] = 10L;
        exprNode = t.parseExpression("myVar[1] -= 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])-=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");
        assertEquals( -2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -2, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");

        myArray[1] = 10L;
        exprNode = t.parseExpression("myVar[1] *= 12");
        assertEquals( "(Identifier:myVar->[IntNumber:1])*=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");
        assertEquals( 120, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 120, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");

        myArray[1] = 10L;
        exprNode = t.parseExpression("myVar[1] /= 2");
        assertEquals( "(Identifier:myVar->[IntNumber:1])/=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");
        assertEquals( 5, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 5, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");

        myArray[1] = 10L;
        exprNode = t.parseExpression("myVar[1] %= 3");
        assertEquals( "(Identifier:myVar->[IntNumber:1])%=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");
        assertEquals( 1, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 1, (long)((Object[])myVar.getValue(symbolTable))[1], "myVar[1] is correct");

        mySecondVar._myValue = "Something";
        myArray[1] = mySecondVar;
        exprNode = t.parseExpression("myVar[1]._myValue = \"Something else\"");
        assertEquals( "(Identifier:myVar->[IntNumber:1]->InstanceVariable:_myValue)=(String:\"Something else\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "Something", mySecondVar._myValue, "myVar[1]._myValue is correct");
        assertEquals( "Something else", exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( "Something else", mySecondVar._myValue, "myVar[1]._myValue is correct");

        exprNode = t.parseExpression("myVar[1].myFunc(\"Hello\")");
        assertEquals( "Identifier:myVar->[IntNumber:1]->Method:myFunc(String:\"Hello\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "++Hello++", exprNode.calculate(symbolTable), "calculate is correct");




        Map<Object, Object> myMap = new HashMap<>();
        myMap.put("Red", "Test");
        myMap.put("Green", "Something");
        myMap.put("Yellow", "Else");
        myVar.setValue(symbolTable, myMap);
        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} = 12");
        assertEquals( "(Identifier:myVar->{String:\"Green\"})=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 12, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");

        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} += 12");
        assertEquals( "(Identifier:myVar->{String:\"Green\"})+=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");
        assertEquals( 22, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 22, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");

        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} -= 12");
        assertEquals( "(Identifier:myVar->{String:\"Green\"})-=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");
        assertEquals( -2, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( -2, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");

        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} *= 12");
        assertEquals( "(Identifier:myVar->{String:\"Green\"})*=(IntNumber:12)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");
        assertEquals( 120, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 120, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");

        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} /= 2");
        assertEquals( "(Identifier:myVar->{String:\"Green\"})/=(IntNumber:2)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");
        assertEquals( 5, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 5, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");

        myMap.put("Green", (long)10);
        exprNode = t.parseExpression("myVar{\"Green\"} %= 3");
        assertEquals( "(Identifier:myVar->{String:\"Green\"})%=(IntNumber:3)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 10, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");
        assertEquals( 1, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 1, (long)((Map<Object, Object>)myVar.getValue(symbolTable)).get("Green"), "myVar{\"Green\"} is correct");

        mySecondVar._myValue = "Something";
        myMap.put("Hello", mySecondVar);
        exprNode = t.parseExpression("myVar{\"Hello\"}._myValue = \"Something else\"");
        assertEquals( "(Identifier:myVar->{String:\"Hello\"}->InstanceVariable:_myValue)=(String:\"Something else\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "Something", mySecondVar._myValue, "myVar{\"Hello\"}._myValue is correct");
        assertEquals( "Something else", exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( "Something else", mySecondVar._myValue, "myVar{\"Hello\"}._myValue is correct");

        exprNode = t.parseExpression("myVar{\"Hello\"}.myFunc(\"Hello\")");
        assertEquals( "Identifier:myVar->{String:\"Hello\"}->Method:myFunc(String:\"Hello\")", exprNode.getDefinitionString(), "expression matches");
        assertEquals( "++Hello++", exprNode.calculate(symbolTable), "calculate is correct");



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
        assertEquals(
                "(Identifier:myVar->{String:\"A key\"}->{String:\"A second key\"}->[Identifier:myIndex1]->[Identifier:myIndex2]->{Identifier:theThirdKey}->[IntNumber:1]->{String:\"A fourth key\"}->{String:\"A fifth key\"})=(IntNumber:12)",
                exprNode.getDefinitionString(), "expression matches");
        assertEquals( 12, (long)(Long)exprNode.calculate(symbolTable), "calculate is correct");
        assertEquals( 12, (long)(Long)myMap5.get("A fifth key"), "myVar is correct");
    }

    @Test
    public void testOther() throws JmriException {

        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(variables);

        ExpressionNode exprNode = t.parseExpression("");
        assertNull( exprNode, "expression is null");

        exprNode = t.parseExpression("     ");
        assertNull( exprNode, "expression is null");

        exprNode = t.parseExpression("       \t\t     ");
        assertNull( exprNode, "expression is null");

        exprNode = t.parseExpression("  \t\t  \n \n \t   ");
        assertNull( exprNode, "expression is null");

        InvalidSyntaxException e = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("[LongString]"), "exception is thrown");
        assertEquals( "Invalid syntax error", e.getMessage(), "exception message matches");

        e = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("LongString.[substring]"), "exception is thrown");
        assertEquals( "Invalid syntax error", e.getMessage(), "exception message matches");

        e = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("LongString.substring({Start},{End})"), "exception is thrown");
        assertEquals( "Invalid syntax error", e.getMessage(), "exception message matches");

        e = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("LongString.substring(Start,{End})"), "exception is thrown");
        assertEquals( "Invalid syntax error", e.getMessage(), "exception message matches");

        e = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("LongString.substring(Start,[End])"), "exception is thrown");
        assertEquals( "Invalid syntax error", e.getMessage(), "exception message matches");

        e = assertThrows( InvalidSyntaxException.class, () ->
            t.parseExpression("LongString.substring([Start],[End])"), "exception is thrown");
        assertEquals( "Invalid syntax error", e.getMessage(), "exception message matches");

    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }


    private static class MyVariable implements Variable {

        private final String _name;
        Object _value;

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

        @SuppressWarnings("unused") // myFunc(String) is used by formula.
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
