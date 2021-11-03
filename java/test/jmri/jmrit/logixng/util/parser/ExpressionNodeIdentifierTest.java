package jmri.jmrit.logixng.util.parser;

import java.util.HashMap;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("not null", t);
    }
    
    private Object getConstant(String name, SymbolTable symbolTable, Map<String, Variable> variables) throws JmriException {
        Token token = new Token(TokenType.NONE, name, 0);
        ExpressionNodeIdentifier t = new ExpressionNodeIdentifier(token, variables);
        Assert.assertNotNull("not null", t);
        return t.calculate(symbolTable);
    }
    
    @Test
    public void testConstants() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        Map<String, Variable> variables = new HashMap<>();
        Assert.assertEquals(Math.PI, (double)(Double)getConstant("MathPI",symbolTable,variables), 0.000000001);
        Assert.assertEquals(Math.E, (double)(Double)getConstant("MathE",symbolTable,variables), 0.000000001);
        Assert.assertEquals(NamedBean.UNKNOWN, (int)(Integer)getConstant("Unknown",symbolTable,variables));
        Assert.assertEquals(NamedBean.INCONSISTENT, (int)(Integer)getConstant("Inconsistent",symbolTable,variables));
        Assert.assertEquals(Turnout.CLOSED, (int)(Integer)getConstant("Closed",symbolTable,variables));
        Assert.assertEquals(Turnout.THROWN, (int)(Integer)getConstant("Thrown",symbolTable,variables));
        Assert.assertEquals(Sensor.INACTIVE, (int)(Integer)getConstant("Inactive",symbolTable,variables));
        Assert.assertEquals(Sensor.ACTIVE, (int)(Integer)getConstant("Active",symbolTable,variables));
        Assert.assertEquals(SignalHead.DARK, (int)(Integer)getConstant("Dark",symbolTable,variables));
        Assert.assertEquals(SignalHead.RED, (int)(Integer)getConstant("Red",symbolTable,variables));
        Assert.assertEquals(SignalHead.FLASHRED, (int)(Integer)getConstant("FlashRed",symbolTable,variables));
        Assert.assertEquals(SignalHead.YELLOW, (int)(Integer)getConstant("Yellow",symbolTable,variables));
        Assert.assertEquals(SignalHead.FLASHYELLOW, (int)(Integer)getConstant("FlashYellow",symbolTable,variables));
        Assert.assertEquals(SignalHead.GREEN, (int)(Integer)getConstant("Green",symbolTable,variables));
        Assert.assertEquals(SignalHead.FLASHGREEN, (int)(Integer)getConstant("FlashGreen",symbolTable,variables));
        Assert.assertEquals(SignalHead.LUNAR, (int)(Integer)getConstant("Lunar",symbolTable,variables));
        Assert.assertEquals(SignalHead.FLASHLUNAR, (int)(Integer)getConstant("FlashLunar",symbolTable,variables));
        Assert.assertEquals(SignalHead.HELD, (int)(Integer)getConstant("Held",symbolTable,variables));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
