package jmri.jmrit.logixng.util.parser;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;

import javax.script.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that you can create functions in Jython to be used by formula
 *
 * @author Daniel Bergqvist 2020
 */
public class JythonFunctionsTest {

    @Test
    public void testJythonFunction() throws ScriptException, ClassNotFoundException, NoSuchMethodException, SecurityException, ParserException, JmriException {
        jmri.script.JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

        String myScript = ""
                + "import jmri\n"
                + "\n"
                + "class MyFunction(jmri.jmrit.logixng.util.parser.Function):\n"
                + "  def getModule(self):\n"
                + "    return \"Jython example\"\n"
                + "  \n"
                + "  def getName(self):\n"
                + "    return \"jythonTest\"\n"
                + "  \n"
                + "  def calculate(self, symbolTable, parameterList):\n"
                + "    if (parameterList.size() != 1):"
                + "      raise jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException(\"Function requires one parameter\")\n"
                + "    return parameterList.get(0).calculate(symbolTable) * 3.4\n"
                + "  \n"
                + "  def getDescription(self):\n"
                + "    return \"Example of function defined in Jython\"\n"
                + "\n"
                + "\n"
                + "jmri.InstanceManager.getDefault(jmri.jmrit.logixng.util.parser.FunctionManager).put(\"jythonTest\", MyFunction())\n";

//        System.out.format("%s%n", myScript);

        AtomicBoolean exceptionIsThrown = new AtomicBoolean(false);
        Map<String, Variable> _variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(_variables);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        try {
            t.parseExpression("jythonTest(8)");
        } catch (FunctionNotExistsException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "The function \"jythonTest\" does not exists".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());

        // Load script
        scriptEngineManager.eval(myScript, scriptEngineManager.getEngineByName(jmri.script.JmriScriptEngineManager.JYTHON));

        ExpressionNode exprNode = t.parseExpression("jythonTest(8)");
        Assert.assertEquals("expression matches", "Function:jythonTest(IntNumber:8)", exprNode.getDefinitionString());
        Assert.assertEquals("calculate is correct", 27.2, (Double)exprNode.calculate(symbolTable), 0.00001);

        exceptionIsThrown.set(false);
        try {
            t.parseExpression("jythonTest()").calculate(symbolTable);
        } catch (WrongNumberOfParametersException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "Function requires one parameter".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());

        exceptionIsThrown.set(false);
        try {
            t.parseExpression("jythonTest(8,\"Hello\")").calculate(symbolTable);
        } catch (WrongNumberOfParametersException e) {
//            System.err.format("Error message: %s%n", e.getMessage());
            Assert.assertTrue("exception message matches", "Function requires one parameter".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
