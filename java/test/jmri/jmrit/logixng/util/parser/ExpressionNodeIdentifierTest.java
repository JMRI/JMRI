package jmri.jmrit.logixng.util.parser;

import java.util.HashMap;
import java.util.Map;

import jmri.jmrit.logixng.SymbolTable;
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
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
