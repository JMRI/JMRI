package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ParsedExpression
 *
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeIdentifierTest {

    @Test
    public void testCtor() throws IdentifierNotExistsException {
        Token token = new Token(TokenType.NONE, "abc", 0);
        Map<String, Variable> variables = new HashMap<>();
        variables.put("abc", new Variable() {
            @Override
            public String getName() {
                return "abc";
            }

            @Override
            public Object getValue(SymbolTable symbolTable) {
                return "0";
            }

            @Override
            public void setValue(SymbolTable symbolTable, Object value) {
                throw new UnsupportedOperationException("Not supported");
            }
        });
        ExpressionNodeIdentifier t = new ExpressionNodeIdentifier(token, variables);
        assertNotNull( t, "not null");
    }

    private Object getConstant(String name, SymbolTable symbolTable, Map<String, Variable> variables) throws JmriException {
        Token token = new Token(TokenType.NONE, name, 0);
        ExpressionNodeIdentifier t = new ExpressionNodeIdentifier(token, variables);
        assertNotNull( t, "not null");
        return t.calculate(symbolTable);
    }

    @Test
    public void testConstants() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        assertEquals(Math.PI, (Double)getConstant("MathPI",symbolTable,variables), 0.000000001);
        assertEquals(Math.E, (Double)getConstant("MathE",symbolTable,variables), 0.000000001);
        assertEquals(NamedBean.UNKNOWN, (int)(Integer)getConstant("Unknown",symbolTable,variables));
        assertEquals(NamedBean.INCONSISTENT, (int)(Integer)getConstant("Inconsistent",symbolTable,variables));
        assertEquals(Turnout.CLOSED, (int)(Integer)getConstant("Closed",symbolTable,variables));
        assertEquals(Turnout.THROWN, (int)(Integer)getConstant("Thrown",symbolTable,variables));
        assertEquals(Sensor.INACTIVE, (int)(Integer)getConstant("Inactive",symbolTable,variables));
        assertEquals(Sensor.ACTIVE, (int)(Integer)getConstant("Active",symbolTable,variables));
        assertEquals(SignalHead.DARK, (int)(Integer)getConstant("Dark",symbolTable,variables));
        assertEquals(SignalHead.RED, (int)(Integer)getConstant("Red",symbolTable,variables));
        assertEquals(SignalHead.FLASHRED, (int)(Integer)getConstant("FlashRed",symbolTable,variables));
        assertEquals(SignalHead.YELLOW, (int)(Integer)getConstant("Yellow",symbolTable,variables));
        assertEquals(SignalHead.FLASHYELLOW, (int)(Integer)getConstant("FlashYellow",symbolTable,variables));
        assertEquals(SignalHead.GREEN, (int)(Integer)getConstant("Green",symbolTable,variables));
        assertEquals(SignalHead.FLASHGREEN, (int)(Integer)getConstant("FlashGreen",symbolTable,variables));
        assertEquals(SignalHead.LUNAR, (int)(Integer)getConstant("Lunar",symbolTable,variables));
        assertEquals(SignalHead.FLASHLUNAR, (int)(Integer)getConstant("FlashLunar",symbolTable,variables));
        assertEquals(SignalHead.HELD, (int)(Integer)getConstant("Held",symbolTable,variables));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initTimeProviderManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
