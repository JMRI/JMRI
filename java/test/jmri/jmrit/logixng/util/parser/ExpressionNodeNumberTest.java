package jmri.jmrit.logixng.util.parser;

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
public class ExpressionNodeNumberTest {

    @Test
    public void testCtor() {
        Token token = new Token(TokenType.NONE, "13.22", 0);
        ExpressionNodeFloatingNumber t = new ExpressionNodeFloatingNumber(token);
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
