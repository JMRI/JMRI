package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

import javax.script.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that you can create functions in Jython to be used by formula
 *
 * @author Daniel Bergqvist 2020
 */
public class JythonFunctionsTest {

    @Test
    public void testJythonFunction() throws ScriptException, ClassNotFoundException,
            NoSuchMethodException, SecurityException, ParserException, JmriException {
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

        Map<String, Variable> _variables = new HashMap<>();
        RecursiveDescentParser t = new RecursiveDescentParser(_variables);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        FunctionNotExistsException e = assertThrows( FunctionNotExistsException.class, () ->
            t.parseExpression("jythonTest(8)"), "exception is thrown");
        assertEquals( "The function \"jythonTest\" does not exists",
                e.getMessage(), "exception message matches");

        // Load script
        scriptEngineManager.eval(myScript, scriptEngineManager.getEngineByName(jmri.script.JmriScriptEngineManager.JYTHON));

        ExpressionNode exprNode = t.parseExpression("jythonTest(8)");
        assertEquals( "Function:jythonTest(IntNumber:8)", exprNode.getDefinitionString(), "expression matches");
        assertEquals( 27.2, (Double)exprNode.calculate(symbolTable), 0.00001, "calculate is correct");

        WrongNumberOfParametersException ex = assertThrows( WrongNumberOfParametersException.class, () ->
            t.parseExpression("jythonTest()").calculate(symbolTable), "exception is thrown");
        assertEquals( "Function requires one parameter",
                ex.getMessage(), "exception message matches");

        ex = assertThrows( WrongNumberOfParametersException.class, () ->
            t.parseExpression("jythonTest(8,\"Hello\")").calculate(symbolTable), "exception is thrown");
        assertEquals( "Function requires one parameter",
                ex.getMessage(), "exception message matches");
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
