package jmri.jmrit.logixng.util.parser;

//import jmri.jmrit.logixng.util.parser.RecursiveDescentParser.Function;
//import jmri.jmrit.logixng.util.parser.RecursiveDescentParser.OperatorInfo;
import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.ScriptException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
    
    // This test is only to check how Jython handles different types.
    // This test must be removed later.
//    @Ignore("Test used only to check how jython handles things.")
    @Test
    public void testDaniel() throws ScriptException {
        
        jmri.script.JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();
/*        
        String myScript = ""
                + "a = 15\n"
                + "print a\n"
                + "print\n"
                + "print \"add\"\n"
                + "print 15 + 30\n"
//                + "print \"15\" + 30\n"
                + "print \"15\" + \"30\"\n"
                + "print\n"
                + "print \"boolean\"\n"
                + "print False\n"
                + "print True\n"
                + "print False + True\n"
                + "print False + True + True\n"
                + "print False - True\n"
                + "print False + True + 10\n"
                + "print False * 10\n"
                + "print True * 10\n"
//                + "print True + \"aa\"\n"
//                + "print False + True + \"aa\"\n"
                + "print\n"
                + "print \"compare\"\n"
                + "print 4 < 10\n"
                + "print True < False\n"
                + "print True < 0\n"
                + "print \"Hej\" < \"Kalle\"\n"
                + "print \"Hej\" < \"Kalle\"\n"
                + "print False < \"Kalle\"\n"
                + "print True < \"Kalle\"\n"
                + "print \"Hej\" < 10\n"
                + "print 10 < \"Hej\"\n"
                + "print \"0\" < 10\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n";
*/        
        String myScript = ""
                + "print \"jmri.jmrit.logixng.util.parser.RecursiveDescentParserTest\"\n"
                + "print False == 0\n"
                + "print True == 0\n"
                + "print False < 0\n"
                + "print True < 0\n"
                + "print True > 0\n"
                + "print True == 1\n"
                + "print False < 2\n"
                + "print True < 2\n"
                + "print False < \"\"\n"
                + "print True < \"\"\n"
                + "print 999 < \"\"\n"
                + "print 999 < \"\"\n"
                + "print\n"
                + "print\n"
                + "print False and False\n"
                + "print False and True\n"
                + "print True and True\n"
                + "print not False\n"
                + "print not True\n"
                + "print\n"
                + "print\n"
                + "print False or False\n"
                + "print False or True\n"
                + "print True or True\n"
                + "print not False\n"
                + "print not True\n"
                + "print\n"
                + "print \"Numbers:\"\n"
                + "print False and 0\n"
                + "print False and 1\n"
                + "print 1 and 1\n"
                + "print 1 and 2\n"
                + "print 8 and 16\n"
                + "print 8 or 16\n"
                + "print not 0\n"
                + "print not 1\n"
                + "print\n"
                + "print \"Strings:\"\n"
                + "print False and \"\"\n"
                + "print False and \"\"\n"
                + "print True and \"\"\n"
                + "print not \"\"\n"
                + "print not \"\"\n"
                + "print \"\"\n";
        
        scriptEngineManager.eval(myScript, scriptEngineManager.getEngineByName(jmri.script.JmriScriptEngineManager.PYTHON));
    }
    
    
    @Test
    public void testParseAndCalculate() throws Exception {
        
        AtomicBoolean exceptionIsThrown = new AtomicBoolean();
        Map<String, Variable> _variables = new HashMap<>();
        
        _variables.put("abc", new MyVariable("abc", "ABC"));
        _variables.put("x", new MyVariable("x", 12));
        
        RecursiveDescentParser t = new RecursiveDescentParser(_variables);
        ExpressionNode exprNode = t.parseExpression("");
        Assert.assertTrue("expression node is null", null == exprNode);
        exprNode = t.parseExpression("134");
        Assert.assertTrue("expression matches", "IntNumber:134".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)134L).equals(exprNode.calculate()));
        exprNode = t.parseExpression("abc");
        Assert.assertTrue("expression matches", "Identifier:abc".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", "ABC".equals(exprNode.calculate()));
        exprNode = t.parseExpression("\"a little string\"");
        Assert.assertTrue("expression matches", "String:a little string".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
//        System.err.format("expected: '%s', %s%n", 134d, ((Object)134d).getClass().getName());
        Assert.assertTrue("calculate is correct", "a little string".equals(exprNode.calculate()));
        exprNode = t.parseExpression("123*1233");
        Assert.assertTrue("expression matches", "(IntNumber:123)*(IntNumber:1233)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)151659L).equals(exprNode.calculate()));
        exprNode = t.parseExpression("123+2123");
        Assert.assertTrue("expression matches", "(IntNumber:123)+(IntNumber:2123)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)2246L).equals(exprNode.calculate()));
        exprNode = t.parseExpression("123*3.2331");
        Assert.assertTrue("expression matches", "(IntNumber:123)*(FloatNumber:3.2331)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Double)397.6713).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12+45*12.2");
        Assert.assertTrue("expression matches", "(IntNumber:12)+((IntNumber:45)*(FloatNumber:12.2))".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Double)561.0).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12*45+34");
        Assert.assertTrue("expression matches", "((IntNumber:12)*(IntNumber:45))+(IntNumber:34)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)574L).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12-57/43");
        Assert.assertTrue("expression matches", "(IntNumber:12)-((IntNumber:57)/(IntNumber:43))".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Long)11L).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12/23.2-43");
        Assert.assertTrue("expression matches", "((IntNumber:12)/(FloatNumber:23.2))-(IntNumber:43)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Double)(-42.482758620689655172413793103448)).equals(exprNode.calculate()));
        
        exprNode = t.parseExpression("12 < 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)<(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12 <= 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)<=(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12 > 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)>(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12 >= 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)>=(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12 == 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)==(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate()));
        exprNode = t.parseExpression("12 != 2");
        Assert.assertTrue("expression matches", "(IntNumber:12)!=(IntNumber:2)".equals(exprNode.getDefinitionString()));
//        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate()));
/*        
        exprNode = t.parseExpression("not 12 < 2");
        System.err.format("getDefinitionString: '%s'%n", exprNode.getDefinitionString());
        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("expression matches", "(not (IntNumber:12))<(IntNumber:2)".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Boolean)false).equals(exprNode.calculate()));
        exprNode = t.parseExpression("not (12 < 2)");
        System.err.format("getDefinitionString: '%s'%n", exprNode.getDefinitionString());
        System.err.format("calculate: '%s', %s%n", exprNode.calculate(), exprNode.calculate().getClass().getName());
        Assert.assertTrue("expression matches", "not ((IntNumber:12)<(IntNumber:2))".equals(exprNode.getDefinitionString()));
        Assert.assertTrue("calculate is correct", ((Boolean)true).equals(exprNode.calculate()));
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
        Object result = exprNode.calculate();
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertTrue("calculate is probably correct", (result instanceof Double) && (((Double)result) >= 0.0) && (((Double)result) <= 1.0));
        exprNode = t.parseExpression("int(23.56)");
        Assert.assertTrue("expression matches", "Function:int(FloatNumber:23.56)".equals(exprNode.getDefinitionString()));
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertTrue("calculate is correct", ((Integer)23).equals(exprNode.calculate()));
        exprNode = t.parseExpression("sin(180,\"deg\")");
        Assert.assertTrue("expression matches", "Function:sin(IntNumber:180,String:deg)".equals(exprNode.getDefinitionString()));
//        System.err.format("Result: %s, %s%n", result, result.getClass().getName());
        Assert.assertEquals("calculate is correct", 0, (Double)exprNode.calculate(), 1e-15);
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
        
        exceptionIsThrown.set(false);
        try {
            t.parseExpression("abcde");
        } catch (IdentifierNotExistsException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The identifier \"abcde\" does not exists".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
        
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
            exprNode.calculate();
        } catch (WrongNumberOfParametersException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "Function \"sin\" has wrong number of parameters".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
        
        exceptionIsThrown.set(false);
        try {
            exprNode = t.parseExpression("123+\"abc\"");
            exprNode.calculate();
        } catch (CalculateException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The two operands \"123\" and \"abc\" have different types".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
    }
    
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
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
        public Object getValue() {
            return _value;
        }
    }
}
